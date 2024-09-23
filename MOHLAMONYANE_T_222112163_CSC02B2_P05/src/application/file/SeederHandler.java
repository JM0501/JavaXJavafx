/*
 * @author MrT. Mohlamonyane
 * @version Computer Science P05
 * @since 2024
 */

//package.
package application.file;

//imports.
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/*
 * @class SeederHandler: Handles the sending of a list of availabe files, 
 * adds a file to the list of files and send a chosen file to Leecher.
 */
public class SeederHandler {

    // Client DatagramSocket and port to connect to.
    private static DatagramSocket clientSocket = null;
    private int portToConnectTo = 1548;

    // Necessary variables
    private static File selectedFile = null;
    // List to hold available files
    private static List<File> availableFiles = new ArrayList<>();
    private static TextArea txtView = new TextArea();
    private static Label lblChosen = new Label("No chosen file");

    public SeederHandler(Stage primaryStage) {
        try {
            // Create UDP connection
            clientSocket = new DatagramSocket(portToConnectTo);
            // Create layout
            createLayout(primaryStage);
            // Start listening for requests
            listenForRequests();
        } catch (SocketException e) {
            System.err.println("Could not create UDP connection...");
        }
    }

    // Function to create layout in Seeder mode
    private static void createLayout(Stage primaryStage) {
        // Button to add a file
        Button btnAdd = new Button("Add File");
        btnAdd.setOnAction(e -> addFile(primaryStage));

        // Layout for adding a file with a label showing the chosen file name
        HBox addFileLayout = new HBox(10, btnAdd, lblChosen);

        // Vertical layout to arrange elements
        VBox layout = new VBox(10, txtView, addFileLayout);
        layout.setPadding(new javafx.geometry.Insets(20));

        // Create and set the scene
        Scene scene = new Scene(layout, 400, 300);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Seeder Mode");
        primaryStage.show();
    }

    // Function to add a file to the list of available files
    private static void addFile(Stage primaryStage) {
        FileChooser fileChooser = new FileChooser();
        selectedFile = fileChooser.showOpenDialog(primaryStage);
        if (selectedFile != null) {
            availableFiles.add(selectedFile);
            lblChosen.setText("Chosen file: " + selectedFile.getName());
            txtView.appendText("Added: " + selectedFile.getName() + "\n");
        } else {
            lblChosen.setText("No chosen file");
        }
    }

    // Function to listen for incoming requests from the Leecher
    private void listenForRequests() {
    	System.out.println("Listening for requests....");
        new Thread(() -> {
            while (true) {
                try {
                    byte[] buffer = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    clientSocket.receive(receivePacket);

                    String receivedCommand = new String(receivePacket.getData(), 0, receivePacket.getLength()).trim();
                    InetAddress peerAddress = receivePacket.getAddress();
                    int peerPort = receivePacket.getPort();

                    if (receivedCommand.equals("LIST")) {
                        sendFileList(peerAddress, peerPort);
                    } else if (receivedCommand.startsWith("FILE")) {
                        String fileName = receivedCommand.substring(5).trim();
                        sendRequestedFile(fileName, peerAddress, peerPort);
                    }
                } catch (IOException e) {
                    System.err.println("Error while listening for requests...");
                }
            }
        }).start();
    }

    // Function to send the list of available files to the Leecher
    private void sendFileList(InetAddress peerAddress, int peerPort) {
        StringBuilder fileList = new StringBuilder();
        for (int i = 0; i < availableFiles.size(); i++) {
            fileList.append(i + 1).append(". ").append(availableFiles.get(i).getName()).append("\n");
        }

        byte[] fileListBytes = fileList.toString().getBytes();
        DatagramPacket sendPacket = new DatagramPacket(fileListBytes, fileListBytes.length, peerAddress, peerPort);

        try {
            clientSocket.send(sendPacket);
            System.out.println("File list sent to peer.");
        } catch (IOException e) {
            System.err.println("Could not send file list to peer...");
        }
    }

    // Function to send the requested file to the Leecher
    private void sendRequestedFile(String fileName, InetAddress peerAddress, int peerPort) {
        File fileToSend = availableFiles.stream()
                .filter(file -> file.getName().equals(fileName))
                .findFirst()
                .orElse(null);

        if (fileToSend != null) {
            try (FileInputStream fis = new FileInputStream(fileToSend)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    DatagramPacket filePacket = new DatagramPacket(buffer, bytesRead, peerAddress, peerPort);
                    clientSocket.send(filePacket);
                }
                System.out.println("File sent: " + fileToSend.getName());
                txtView.appendText("File sent: " + fileToSend.getName() + "\n");
            } catch (IOException e) {
                System.err.println("Error sending file: " + fileToSend.getName());
            }
        } else {
            System.err.println("Requested file not found: " + fileName);
        }
    }
}
