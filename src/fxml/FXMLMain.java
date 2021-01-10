package fxml;

import java.util.Optional;

import application.PasswordDialog;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class FXMLMain extends Application {
    
    private FXMLController controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.load(getClass().getClassLoader().getResourceAsStream("TestGUI.fxml"));
//        Parent p = fxmlLoader.load(getClass().getResourceAsStream("../TestGUI.fxml"));
        Parent p = FXMLLoader.load(getClass().getClassLoader().getResource("TestGUI.fxml"));
        controller =  (FXMLController) fxmlLoader.getController();
        
        primaryStage.getIcons().add(new Image(getClass().getClassLoader().getResourceAsStream("BFIcon.png")));
        
        Scene scene = new Scene(p, 750, 500);
        primaryStage.setTitle("BFJournal");
        primaryStage.setScene(scene);
        primaryStage.show();

        PasswordDialog pd = new PasswordDialog();
        Optional<String> result = pd.showAndWait();
        result.ifPresent(password -> controller.setKey(password));
    }

    public static void main(String[] args) {
        launch(args);
    }

}
