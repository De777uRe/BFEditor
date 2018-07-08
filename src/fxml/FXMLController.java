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

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
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
    	
    	ColorPicker colorChooser = new ColorPicker();
    	
//    	SwingNode colorChooserNode = new SwingNode();
//    	colorChooserNode.setContent(colorChooser);
    	
    	Alert dialog = new Alert(Alert.AlertType.NONE);
    	dialog.initOwner((Stage) anchorPane.getScene().getWindow());
    	dialog.setTitle("Color for Menu Bar Color");
    	dialog.setResizable(true);
    	dialog.getDialogPane().setContent(colorChooser);
    	dialog.getDialogPane().getButtonTypes().setAll(ButtonType.OK);
    	dialog.getDialogPane().setMinSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
    	dialog.getDialogPane().setPrefHeight(90);
    	dialog.getDialogPane().setPrefWidth(320);
    	menuHBox.setStyle("-fx-background-color: #336699;");
    	
    	Optional<ButtonType> response = dialog.showAndWait();
    	if (response.filter(r -> r == ButtonType.OK).isPresent()) {
    		Color pickedColor = colorChooser.getValue();
    		System.out.println("Picked color: " + pickedColor);
//    		menuHBox.setStyle("-fx-background-color: " + String.format("#%02X%02X%02X", (int) (pickedColor.getRed()*255), (int)(pickedColor.getGreen() * 255), (int)(pickedColor.getBlue()*255)) + ";");
//    		menuHBox.setBackground(new Background(new BackgroundFill(Color.valueOf(pickedColor.toString()), CornerRadii.EMPTY, Insets.EMPTY)));
//    		menuHBox.backgroundProperty().setValue(new Background(new BackgroundFill(Color.valueOf(pickedColor.toString()), CornerRadii.EMPTY, Insets.EMPTY)));
    		menuHBox.setStyle("-fx-background-color: #FFFFFF;");
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
}
