import javax.net.ssl.*;
import java.io.*;
import java.net.*;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;

class Peer {
    private String ipAddress;
    private int port;

    public Peer(String ipAddress, int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }
}

public class SecureFileSharingSystem {
    private static final int PORT = 9000;
    private static List<Peer> knownPeers = new ArrayList<>();
    private static List<String> sharedFiles = new ArrayList<>();

    public static void main(String[] args) {
        try {
            // Load keystore for SSL
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("mykeystore.jks"), "your-keystore-password".toCharArray());

            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
            kmf.init(keyStore, "your-key-password".toCharArray());

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            SSLServerSocket sslServerSocket = (SSLServerSocket) ssf.createServerSocket(PORT);

            // Start peer discovery
            new Thread(() -> discoverPeers()).start();

            // Start user interface
            new Thread(() -> startUserInterface()).start();

            System.out.println("Secure File Sharing Server started on port " + PORT);

            while (true) {
                SSLSocket clientSocket = (SSLSocket) sslServerSocket.accept();
                new Thread(() -> handleClient(clientSocket)).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void discoverPeers() {
        knownPeers.add(new Peer("172.20.10.11", PORT));
        knownPeers.add(new Peer("172.20.10.9", PORT));
    }

    private static void startUserInterface() {
        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.println("1. List Shared Files");
                System.out.println("2. Share a File");
                System.out.println("3. Download a File");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");

                int choice = Integer.parseInt(userInput.readLine());

                switch (choice) {
                    case 1:
                        listSharedFiles();
                        break;
                    case 2:
                        shareFile(userInput);
                        break;
                    case 3:
                        downloadFile(userInput);
                        break;
                    case 4:
                        System.exit(0);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void listSharedFiles() {
        System.out.println("Shared Files:");
        for (String fileName : sharedFiles) {
            System.out.println(fileName);
        }
    }

    private static void shareFile(BufferedReader userInput) throws IOException {
        System.out.print("Enter the path of the file to share: ");
        String filePath = userInput.readLine();
        File file = new File(filePath);

        if (file.exists()) {
            sharedFiles.add(file.getName());
            System.out.println("File shared successfully: " + file.getName());
        } else {
            System.out.println("File not found.");
        }
    }

    private static void downloadFile(BufferedReader userInput) throws IOException {
        System.out.print("Enter the name of the file to download: ");
        String fileName = userInput.readLine();

        System.out.print("Enter the IP address of the peer: ");
        String peerIP = userInput.readLine();

        System.out.print("Enter the port of the peer: ");
        int peerPort = Integer.parseInt(userInput.readLine());

        try {
            SSLContext sslContext = SSLContext.getDefault();
            SSLSocketFactory ssf = sslContext.getSocketFactory();
            SSLSocket sslSocket = (SSLSocket) ssf.createSocket(peerIP, peerPort);

            sslSocket.startHandshake();

            PrintWriter out = new PrintWriter(sslSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));

            out.println("DOWNLOAD");
            out.println(fileName);

            String response = in.readLine();
            if (response.equals("ACK")) {
                receiveFile(sslSocket, fileName);
                System.out.println("File downloaded successfully: " + fileName);
            } else {
                System.out.println("File not found on the peer.");
            }

            sslSocket.close();
        } catch (IOException e) {
            System.out.println("Error connecting to the peer. Make sure the information is correct.");
        }
    }

    private static void receiveFile(SSLSocket sslSocket, String fileName) {
        try {
            InputStream inputStream = sslSocket.getInputStream();
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);

            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                fileOutputStream.write(buffer, 0, bytesRead);
            }

            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleClient(SSLSocket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message = in.readLine();

            if (message.equals("HELLO")) {
                out.println("HELLO");
            } else if (message.equals("SHARE")) {
                out.println("ACK");
                String fileName = in.readLine();
                sharedFiles.add(fileName);
            } else if (message.equals("LIST")) {
                out.println(String.join(",", sharedFiles));
            } else if (message.equals("DOWNLOAD")) {
                out.println("ACK");
                String fileName = in.readLine();
                sendFile(clientSocket, fileName);
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(SSLSocket clientSocket, String fileName) {
        try {
            File file = new File(fileName);
            byte[] fileBytes = new byte[(int) file.length()];

            FileInputStream fileInputStream = new FileInputStream(file);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            bufferedInputStream.read(fileBytes, 0, fileBytes.length);

            OutputStream outputStream = clientSocket.getOutputStream();
            outputStream.write(fileBytes, 0, fileBytes.length);
            outputStream.flush();

            bufferedInputStream.close();
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
