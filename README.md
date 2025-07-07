## Project Description

The **Secure File Sharing System** is an advanced peer-to-peer application developed in Java, designed to enable **encrypted and reliable file exchange** across a network. Leveraging SSL/TLS, it ensures all file transfers are secure and protected from unauthorized access or interception.

The system allows users to seamlessly share and download files by specifying peer IP addresses and ports. Its intuitive command-line interface makes it easy to manage file sharing tasks securely. Ideal for collaborative environments and distributed networks, this project demonstrates practical networking, security, and file management skills, offering a robust solution for decentralized and secure file sharing.

## Features

- **Secure Peer Discovery:** Discover peers in the network securely.
- **Encrypted File Sharing:** Share files with other peers using SSL/TLS encryption.
- **Encrypted File Downloading:** Download files from other peers securely.
- **User Interface:** Simple command-line interface for easy interaction.

## Requirements

- Java Development Kit (JDK) 8 or higher
- Java Keystore (JKS) configured for SSL certificates

## How to Run

1. **Clone the repository:**
   ```sh
   git clone https://github.com/JafarLone/secure-file-sharing-system.git
   cd secure-file-sharing-system
2. **Compile the Java files:**
   ```sh
   javac FileSharingSystem.java
   ## How to Run

3. **Run the File Sharing System:**
   ```sh
   java FileSharingSystem
## Usage

Upon running the program, the following options are available:

- **List Shared Files:** Display a list of files currently shared by the user.
- **Share a File:** Enter the path of the file you want to share.
- **Download a File:** Enter the name of the file to download, along with the IP address and port of the peer sharing the file.
- **Exit:** Terminate the program.
