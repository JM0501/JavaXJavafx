/*
 * @author MrT. Mohlamonyane
 * @version Computer Science P05
 * @since 2024
 */

//package.
package application;

//imports.
import application.file.LeecherHandler;
import application.file.SeederHandler;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/*
 * @class Client: Handles user mode preference, whether they want to run on Seeder Mode or Leecher Mode.
 */
public class Client extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Create GUI elements
        Label modeLabel = new Label("Select the mode you want to use:");

        // Radio buttons for mode selection
        RadioButton seederMode = new RadioButton("Seeder");
        RadioButton leecherMode = new RadioButton("Leecher");

        // ToggleGroup to ensure only one radio button is selected at a time
        ToggleGroup modeGroup = new ToggleGroup();
        seederMode.setToggleGroup(modeGroup);
        leecherMode.setToggleGroup(modeGroup);

        // Default selection
        seederMode.setSelected(true);

        // Button to confirm the selection
        Button confirmButton = new Button("Confirm");
        confirmButton.setOnAction(e -> {
            try {
                RadioButton selectedMode = (RadioButton) modeGroup.getSelectedToggle();
                String mode = selectedMode.getText();

                if (mode.equals("Seeder")) {
                    // Transition to Seeder mode window
                    openSeederWindow(primaryStage);
                    System.out.println("Switched to Seeder Mode...");
                } else if (mode.equals("Leecher")) {
                    // Transition to Leecher mode window
                    openLeecherWindow(primaryStage);
                    System.out.println("Switched to Leecher Mode...");
                }
            } catch (Exception ex) {
                System.err.println("Could not switch to chosen mode...");
            }
        });

        // Arrange elements in a VBox layout
        VBox layout = new VBox(10, modeLabel, seederMode, leecherMode, confirmButton);
        layout.setPadding(new javafx.geometry.Insets(20));

        // Create and set the scene
        Scene scene = new Scene(layout, 300, 200);
        
        //reference .css file.
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Client Mode Selection");
        primaryStage.show();
    }

    // Method to open the Seeder window
    private void openSeederWindow(Stage primaryStage) {
        // Set window title
        primaryStage.setTitle("Seeder Mode");
        
        // Create a SeederHandler instance to manage the Seeder functionality
        SeederHandler seederHandler = new SeederHandler(primaryStage);
    }

    // Method to open the Leecher window
    private void openLeecherWindow(Stage primaryStage) {
        // Set window title
        primaryStage.setTitle("Leecher Mode");

        // Create a LeecherHandler instance to manage the Leecher functionality
        LeecherHandler leecherHandler = new LeecherHandler(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
