import java.io.*;
import java.net.*;
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

class FileSharingSystem {
    private static final int PORT = 9000;
    private static List<Peer> knownPeers = new ArrayList<>();
    private static List<String> sharedFiles = new ArrayList<>();

    public static void main(String[] args) {
        
        try {
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                
                // Start peer discovery thread
                new Thread(() -> discoverPeers()).start();

                // Start user interface thread
                new Thread(() -> startUserInterface()).start();

                while (true) {
                    Socket clientSocket = serverSocket.accept();
                    new Thread(() -> handleClient(clientSocket)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void discoverPeers() {
        // In a real-world scenario, you would implement a more sophisticated peer discovery mechanism.
        // For simplicity, we'll just list some peers initially.
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
            Socket socket = new Socket(peerIP, peerPort);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Request the file
            out.println("DOWNLOAD");
            out.println(fileName);

            // Receive the file
            String response = in.readLine();
            if (response.equals("ACK")) {
                receiveFile(socket, fileName);
                System.out.println("File downloaded successfully: " + fileName);
            } else {
                System.out.println("File not found on the peer.");
            }

            socket.close();
        } catch (IOException e) {
            System.out.println("Error connecting to the peer. Make sure the information is correct.");
        }
    }

    private static void receiveFile(Socket socket, String fileName) {
        try {
            InputStream inputStream = socket.getInputStream();
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

    private static void handleClient(Socket clientSocket) {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            String message = in.readLine();

            if (message.equals("HELLO")) {
                // Peer is saying hello, exchange information
                out.println("HELLO");
            } else if (message.equals("SHARE")) {
                // Peer wants to share files, exchange file information
                out.println("ACK");
                String fileName = in.readLine();
                sharedFiles.add(fileName);
            } else if (message.equals("LIST")) {
                // Peer wants to list available files
                out.println(String.join(",", sharedFiles));
            } else if (message.equals("DOWNLOAD")) {
                // Peer wants to download a file
                out.println("ACK");
                String fileName = in.readLine();
                sendFile(clientSocket, fileName);
            }

            clientSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendFile(Socket clientSocket, String fileName) {
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
