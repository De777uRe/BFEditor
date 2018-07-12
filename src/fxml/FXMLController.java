package fxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLController {
	private LocalDate date = LocalDate.now();
	private static Map<LocalDate, String> entryMap = new HashMap<LocalDate, String>();
	
    
    @FXML
    private AnchorPane anchorPane;
    
    @FXML
    private HBox menuHBox;
    @FXML
    private HBox dateHBox;
    
    @FXML
    private MenuBar bfMenu;
    
    @FXML
    private Menu fileMenu;
    @FXML
    private Menu editMenu;
    @FXML
    private Menu helpMenu;
    
    @FXML
    private MenuItem menuBarItem;
    @FXML
    private MenuItem dateBarItem;
    @FXML
    private MenuItem datePickerItem;
    @FXML
    private MenuItem textAreaItem;
    @FXML
    private MenuItem aboutItem;
    
    @FXML DatePicker datePicker;
    
    @FXML
    private TextArea entryTextArea;
    
    @FXML
    public void initialize() {
        entryTextArea.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                System.out.println("Entry changed from: \n" + oldValue + "\nto: \n" + newValue);
                entryMap.put(date, newValue);
            }
        });
        
        datePicker.setValue(date);
    }
    
    @FXML
    private void invokeFileMenu() {
    	System.out.println("Invoked File Menu");
    }
    
    @FXML
    private void invokeSaveItem() {
    	System.out.println("Invoked Save Item");
    	
        System.out.println("Saving File");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Encrypted Journal");
        File savedFilePath = fileChooser.showSaveDialog((Stage) anchorPane.getScene().getWindow());
        
        if (savedFilePath != null) {
            try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(savedFilePath))) {
                os.writeObject(entryMap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        System.out.println("Saving to File: " + savedFilePath);
    }
    
    @FXML
    private void invokeLoadItem() {
    	System.out.println("Invoked Load Item");
    	
    	System.out.println("Loading File");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Encrypted Journal");
        File loadedFilePath = fileChooser.showOpenDialog((Stage) anchorPane.getScene().getWindow());
        
        if (loadedFilePath != null) {
            try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(loadedFilePath))) {
                entryMap = (Map<LocalDate, String>) is.readObject();
                // TODO Trigger Event
                entryTextArea.setText(entryMap.get(date));
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    @FXML
    private void invokeMenuBarItem() {
    	System.out.println("Invoked Edit -> Menu Bar Item");
    	
    	genericColorPrompt("Color for Menu Bar", "-fx-background-color: #", bfMenu);
    	
//    	ColorPicker colorChooser = new ColorPicker();
//    	
//    	Alert dialog = new Alert(Alert.AlertType.NONE);
//    	dialog.initOwner((Stage) anchorPane.getScene().getWindow());
//    	dialog.setTitle("Color for Menu Bar");
//    	dialog.setResizable(true);
//    	dialog.getDialogPane().setContent(colorChooser);
//    	dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
//    	dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//    	dialog.getDialogPane().setPrefHeight(90);
//    	dialog.getDialogPane().setPrefWidth(320);
////    	bfMenu.setStyle("-fx-background-color: #2f4f4f;");
////    	dateHBox.setStyle("-fx-background-color: #2f4f4f;");
//    	
//    	// USING CSS
////    	anchorPane.getScene().getStylesheets().add(getClass().getResource("../application/application.css").toExternalForm());
////    	menuHBox.getStyleClass().add("hbox");
//    	
//    	Optional<ButtonType> response = dialog.showAndWait();
//    	if (response.filter(r -> r == ButtonType.OK).isPresent()) {
//    		Color pickedColor = colorChooser.getValue();
//    		System.out.println("Picked color: " + colorToHex(pickedColor));
//    		bfMenu.setStyle("-fx-background-color: #" + colorToHex(pickedColor) + ";");
//    	}
    }
    
    @FXML
    private void invokeDateBarItem() {
        System.out.println("Invoked Edit -> Date Bar Item");
        
        genericColorPrompt("Color for Date Bar", "-fx-background-color: #", dateHBox);
        
//        ColorPicker colorChooser = new ColorPicker();
//        
//        Alert dialog = new Alert(Alert.AlertType.NONE);
//        dialog.initOwner((Stage) anchorPane.getScene().getWindow());
//        dialog.setTitle("Color for Date Bar");
//        dialog.setResizable(true);
//        dialog.getDialogPane().setContent(colorChooser);
//        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
//        dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//        dialog.getDialogPane().setPrefHeight(90);
//        dialog.getDialogPane().setPrefWidth(320);
//        
//        Optional<ButtonType> response = dialog.showAndWait();
//        if (response.filter(r -> r == ButtonType.OK).isPresent()) {
//            Color pickedColor = colorChooser.getValue();
//            System.out.println("Picked color: " + colorToHex(pickedColor));
//            dateHBox.setStyle("-fx-background-color: #" + colorToHex(pickedColor) + ";");
//        }
    }
    
    @FXML
    private void invokeDatePickerItem() {
        // TODO ONLY FILLS BORDER
        System.out.println("Invoked Edit -> Date Picker Item");
        
        genericColorPrompt("Color for Date Picker", "-fx-control-inner-background: #", datePicker);
        
//        ColorPicker colorChooser = new ColorPicker();
//        
//        Alert dialog = new Alert(Alert.AlertType.NONE);
//        dialog.initOwner((Stage) anchorPane.getScene().getWindow());
//        dialog.setTitle("Color for Date Picker");
//        dialog.setResizable(true);
//        dialog.getDialogPane().setContent(colorChooser);
//        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
//        dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//        dialog.getDialogPane().setPrefHeight(90);
//        dialog.getDialogPane().setPrefWidth(320);
//        
//        Optional<ButtonType> response = dialog.showAndWait();
//        if (response.filter(r -> r == ButtonType.OK).isPresent()) {
//            Color pickedColor = colorChooser.getValue();
//            System.out.println("Picked color: " + colorToHex(pickedColor));
//            datePicker.setStyle("-fx-control-inner-background: #" + colorToHex(pickedColor) + ";");
//        }
    }
    
    @FXML
    private void invokeTextAreaItem() {
        // TODO ONLY FILLS BORDER
        System.out.println("Invoked Edit -> Text Area Item");
        
        genericColorPrompt("Color for Text Area", "-fx-control-inner-background: #", entryTextArea);
        
//        ColorPicker colorChooser = new ColorPicker();
//        
//        Alert dialog = new Alert(Alert.AlertType.NONE);
//        dialog.initOwner((Stage) anchorPane.getScene().getWindow());
//        dialog.setTitle("Color for Text Area");
//        dialog.setResizable(true);
//        dialog.getDialogPane().setContent(colorChooser);
//        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
//        dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
//        dialog.getDialogPane().setPrefHeight(90);
//        dialog.getDialogPane().setPrefWidth(320);
//        
//        Optional<ButtonType> response = dialog.showAndWait();
//        if (response.filter(r -> r == ButtonType.OK).isPresent()) {
//            Color pickedColor = colorChooser.getValue();
//            System.out.println("Picked color: " + colorToHex(pickedColor));
//            entryTextArea.setStyle("-fx-control-inner-background: #" + colorToHex(pickedColor) + ";");
//        }
    }
    
    private void genericColorPrompt(String title, String cssString, Node target) {
        ColorPicker colorChooser = new ColorPicker();
        
        Alert dialog = new Alert(Alert.AlertType.NONE);
        dialog.initOwner((Stage) anchorPane.getScene().getWindow());
        dialog.setTitle(title);
        dialog.setResizable(true);
        dialog.getDialogPane().setContent(colorChooser);
        dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
        dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        dialog.getDialogPane().setPrefHeight(90);
        dialog.getDialogPane().setPrefWidth(320);
        
        Optional<ButtonType> response = dialog.showAndWait();
        if (response.filter(r -> r == ButtonType.OK).isPresent()) {
            Color pickedColor = colorChooser.getValue();
            System.out.println("Picked color: " + colorToHex(pickedColor));
            target.setStyle(cssString + colorToHex(pickedColor) + ";");
        }
    }
    
    @FXML
    private void invokeAboutItem() {
        System.out.println("Invoked About Item");
    }
    
    @FXML
    private void invokeDatePicker() {
    	System.out.println("Invoked Date Picker");
    	
    	date = datePicker.getValue();
        System.out.println("Selected Date: " + date);
        entryTextArea.setText(entryMap.get(date));
    }
    
    private String colorToHex(Color color) {
        String hex1;
        String hex2;

        hex1 = Integer.toHexString(color.hashCode()).toUpperCase();

        switch (hex1.length()) {
        case 2:
            hex2 = "000000";
            break;
        case 3:
            hex2 = String.format("00000%s", hex1.substring(0,1));
            break;
        case 4:
            hex2 = String.format("0000%s", hex1.substring(0,2));
            break;
        case 5:
            hex2 = String.format("000%s", hex1.substring(0,3));
            break;
        case 6:
            hex2 = String.format("00%s", hex1.substring(0,4));
            break;
        case 7:
            hex2 = String.format("0%s", hex1.substring(0,5));
            break;
        default:
            hex2 = hex1.substring(0, 6);
        }
        return hex2;
    }
}
