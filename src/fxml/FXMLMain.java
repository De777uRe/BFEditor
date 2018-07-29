package fxml;

import java.util.Optional;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class FXMLMain extends Application {
    
    private FXMLController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.load(getClass().getResource("../TestGUI.fxml").openStream());
        Parent p = FXMLLoader.load(getClass().getResource("../TestGUI.fxml"));
        controller =  (FXMLController) fxmlLoader.getController();
        
        Scene scene = new Scene(p, 500, 500);
        primaryStage.setTitle("BFEditor");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        final Stage keyDialogStage = new Stage();
        keyDialogStage.initModality(Modality.APPLICATION_MODAL);
        keyDialogStage.initOwner(scene.getWindow());
        
        TextInputDialog keyDialog = new TextInputDialog();
        keyDialog.setTitle("Journal Key Input");
        keyDialog.setHeaderText("The Key Entered Here Will Be Used for Encryption");
        keyDialog.setContentText("Desired Key: ");
        
        Optional<String> keyResult = keyDialog.showAndWait();
        
        if (keyResult.isPresent()) {
            controller.setKey(keyResult.get());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

}
