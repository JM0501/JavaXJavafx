/*
 * @author MrT. Mohlamonyane
 * @version Computer Science P05
 * @since 2024
 */

//package.
package application.file;

//imports.
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/*
 * @class LeecherHandler: Handles connection to Seeder, asking what files are present on the Seeder and provides 
 * functionality to choose available files, recieve and save files from Seeder.
 */
public class LeecherHandler {

	// Client datagram socket
    private DatagramSocket socket;
    private InetAddress seederAddress;
    private int seederPort;

    // Necessary variables
    private TextField txtHost;
    private TextField txtPort;
    private ListView<String> listViewFiles;
    private TextArea txtLogs;

    public LeecherHandler(Stage primaryStage) {
    	createLayout(primaryStage);
    }

    // Function to set GUI
    private void createLayout(Stage primaryStage) {
    	// Seeder host field
    	Label lblHost = new Label("Seeder Host:");
        txtHost = new TextField();
        txtHost.setPromptText("Enter Seeder Host");

        // Seeder port field
        Label lblPort = new Label("Seeder Port:");
        txtPort = new TextField();
        txtPort.setPromptText("Enter Seeder Port");

        Button btnConnect = new Button("Connect");
        btnConnect.setOnAction(e -> connectToSeeder());
        
        Button btnReqFiles = new Button("Request File List");
        btnReqFiles.setOnAction(e -> requestFileList());
        
        listViewFiles = new ListView<>();
        Button btnRetrieve = new Button("Retrieve File");
        btnRetrieve.setOnAction(e -> retrieveSelectedFile());

        txtLogs = new TextArea();
        txtLogs.setEditable(false);
        
     // HBox for host and port fields along with the connect button
        HBox connectionLayout = new HBox(10, lblHost, txtHost, lblPort, txtPort, btnConnect);

        // VBox to organize everything vertically
        VBox layout = new VBox(15, connectionLayout, listViewFiles, btnReqFiles,btnRetrieve, txtLogs);
        layout.setPadding(new javafx.geometry.Insets(20));
       
        Scene scene = new Scene(layout, 400, 400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Leecher Mode");
        primaryStage.show();
    }

    // Connect to the Seeder using the provided host and port
    private void connectToSeeder() {
        try {
            String host = txtHost.getText().trim();
            seederPort = Integer.parseInt(txtPort.getText().trim());
            seederAddress = InetAddress.getByName(host);

            socket = new DatagramSocket();

            txtLogs.appendText("Connected to Seeder at " + host + ":" + seederPort + "\n");
        } catch (Exception e) {
            txtLogs.appendText("Failed to connect to Seeder.\n");
        }
    }

    // Request the list of available files from the Seeder
    private void requestFileList() {
        try {
            byte[] buffer = "LIST".getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, seederAddress, seederPort);
            socket.send(packet);

            // Wait for the response with the file list
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket responsePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
            socket.receive(responsePacket);

            String fileList = new String(responsePacket.getData(), 0, responsePacket.getLength());
            listViewFiles.getItems().clear();
            for (String file : fileList.split("\n")) {
                listViewFiles.getItems().add(file);
            }

            txtLogs.appendText("Received file list from Seeder.\n");
            System.out.println("Files Recieved...");
        } catch (IOException e) {
            txtLogs.appendText("Failed to request file list.\n");
        }
    }

    // Retrieve the selected file from the Seeder
    private void retrieveSelectedFile() {
        String selectedFile = listViewFiles.getSelectionModel().getSelectedItem();
        if (selectedFile == null) {
            txtLogs.appendText("No file selected.\n");
            return;
        }

        String fileName = selectedFile.substring(selectedFile.indexOf(". ") + 2).trim();
        try {
            byte[] buffer = ("FILE " + fileName).getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, seederAddress, seederPort);
            socket.send(packet);

            txtLogs.appendText("Requested file: " + fileName + "\n");
            receiveFile(fileName);
        } catch (IOException e) {
            txtLogs.appendText("Failed to request file: " + fileName + "\n");
        }
    }

    // Receive the file from the Seeder and save it locally
    private void receiveFile(String fileName) {
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Choose Directory to Save File");
            File saveDirectory = directoryChooser.showDialog(null);

            if (saveDirectory == null) {
                txtLogs.appendText("No directory chosen.\n");
                return;
            }

            try (FileOutputStream fos = new FileOutputStream(saveDirectory + "/" + fileName)) {
                byte[] receiveBuffer = new byte[1024];
                DatagramPacket filePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                while (true) {
                    socket.receive(filePacket);
                    fos.write(filePacket.getData(), 0, filePacket.getLength());

                    // Check if it's the last packet (you might want to add an end-of-file signal)
                    if (filePacket.getLength() < receiveBuffer.length) {
                        break;
                    }
                }
            }

            txtLogs.appendText("File received and saved: " + fileName + "\n");
        } catch (IOException e) {
            txtLogs.appendText("Failed to receive file: " + fileName + "\n");
        }
    }
}
