package application;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class PasswordDialog extends Dialog<String> {
  private final PasswordField passwordField = new PasswordField();
  private final TextField textField = new TextField();
  
  CheckBox previewCheckBox = new CheckBox("Show Password");

  public PasswordDialog() {
    setTitle("Password");
    setHeaderText("Please enter your password.");

    getDialogPane().getButtonTypes().addAll(ButtonType.OK);

    textField.setManaged(false);
    textField.setVisible(false);
    
    passwordField.setPromptText("Password");
    
    textField.managedProperty().bind(previewCheckBox.selectedProperty());
    textField.visibleProperty().bind(previewCheckBox.selectedProperty());
    
    passwordField.managedProperty().bind(previewCheckBox.selectedProperty().not());
    passwordField.visibleProperty().bind(previewCheckBox.selectedProperty().not());
    
    textField.textProperty().bindBidirectional(passwordField.textProperty());

    HBox hBox = new HBox();
    hBox.getChildren().addAll(passwordField, textField);
    hBox.setPadding(new Insets(10));
    
    HBox hBoxCB = new HBox();
    hBoxCB.getChildren().addAll(previewCheckBox);
    hBoxCB.setPadding(new Insets(10));
    
    HBox.setHgrow(passwordField, Priority.ALWAYS);
    HBox.setHgrow(textField, Priority.ALWAYS);
    
    VBox vBox = new VBox();
//    vBox.setPadding(new Insets(20));
    VBox.setVgrow(previewCheckBox, Priority.ALWAYS);
    
    vBox.getChildren().addAll(hBox, hBoxCB);

    getDialogPane().setContent(vBox);

    Platform.runLater(() -> passwordField.requestFocus());

    setResultConverter(dialogButton -> {
      if (dialogButton == ButtonType.OK) {
        return passwordField.getText();
      }
      return null;
    });
  }
  
  public PasswordField getPasswordField() {
    return passwordField;
  }
}