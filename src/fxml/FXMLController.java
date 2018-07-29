package fxml;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.time.LocalDate;
import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

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
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class FXMLController {
	private LocalDate date = LocalDate.now();
	
	// TODO Add these maps to a map of maps
	private static Map<LocalDate, String> entryMap = new HashMap<LocalDate, String>();
	private static Map<LocalDate, String> bfMenuColorMap = new HashMap<LocalDate, String>();
	private static Map<LocalDate, String> dateHBoxColorMap = new HashMap<LocalDate, String>();
	private static Map<LocalDate, String> datePickerColorMap = new HashMap<LocalDate, String>();
	private static Map<LocalDate, String> entryTextAreaColorMap = new HashMap<LocalDate, String>();
	
	private static String key;
	private Key aesKey;
	private final byte salt[] = { 3, 25, (byte) 2017, 8, 19, (byte) 1996, 7, 7 };
	
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
//                Text containerText = new Text(entryTextArea.getText());
//                containerText.setFill(Color.BLUE);
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
                saveToFile(os, entryMap, bfMenuColorMap, dateHBoxColorMap, datePickerColorMap, entryTextAreaColorMap);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        System.out.println("Saving to File: " + savedFilePath);
    }
    
    private void saveToFile(ObjectOutputStream os, Map<LocalDate, String> entryMap, Map<?, ?> ...maps) {
        for (Map<?, ?> map : maps) {
            try {
                os.writeObject(map);
                os.writeChars("\n");
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        try {
            os.writeObject(encryptEntries(entryMap));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private Map<LocalDate, Entry<byte[], byte[]>> encryptEntries(Map<LocalDate, String> entryMap) {
        Map<LocalDate, Entry<byte[], byte[]>> encryptionMap = new HashMap<LocalDate, Entry<byte[], byte[]>>();
        
        Iterator<Entry<LocalDate, String>> it = entryMap.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<LocalDate, String> pair = (Map.Entry<LocalDate, String>)it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            Entry<byte[], byte[]> ivCipherEntry = encryptString(pair.getValue());
            encryptionMap.put(pair.getKey(), ivCipherEntry);
            it.remove();
        }
        
        return encryptionMap;
    }
    
    private Entry<byte[], byte[]> encryptString(String entry) {
        Entry<byte[], byte[]> ivCipherEntry = null;
        
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            SecretKey tmp = factory.generateSecret(spec);
            SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
            
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secret);
            AlgorithmParameters params = cipher.getParameters();
            byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
            byte[] cipherText = cipher.doFinal(entry.getBytes("UTF-8"));
            
            ivCipherEntry = new SimpleEntry<byte[], byte[]>(iv, cipherText);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e2) {
            // TODO Auto-generated catch block
            e2.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return ivCipherEntry;
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
                populateSavedChanges(is);
                
                // TODO Trigger Event
                System.out.println("LOADING DATE: " + date);
                datePicker.setValue(LocalDate.now());
                entryTextArea.setText(entryMap.get(date));
                if (bfMenuColorMap.get(date) != null)
                    bfMenu.setStyle("-fx-background-color: #" + bfMenuColorMap.get(date));
                else
                    bfMenu.setStyle(null);
                
                if (dateHBoxColorMap.get(date) != null)
                    dateHBox.setStyle("-fx-background-color: #" + dateHBoxColorMap.get(date));
                else
                    dateHBox.setStyle(null);
                
                if (datePickerColorMap.get(date) != null)
                    datePicker.setStyle("-fx-control-inner-background: #" + datePickerColorMap.get(date));
                else
                    datePicker.setStyle(null);
                
                if (entryTextAreaColorMap.get(date) != null)
                    entryTextArea.setStyle("-fx-control-inner-background: #" + entryTextAreaColorMap.get(date));
                else
                    entryTextArea.setStyle(null);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
    
    private void populateSavedChanges(ObjectInputStream is) {
        try {
            // TODO Skipping bytes seems sloppy
            bfMenuColorMap = (Map<LocalDate, String>) is.readObject();
            is.skip(8);
            dateHBoxColorMap = (Map<LocalDate, String>) is.readObject();
            is.skip(8);
            datePickerColorMap = (Map<LocalDate, String>) is.readObject();
            is.skip(8);
            entryTextAreaColorMap = (Map<LocalDate, String>) is.readObject();
            is.skip(8);
            
            Map<LocalDate, Entry<byte[], byte[]>> loadedEntryMap = (Map<LocalDate, Entry<byte[], byte[]>>) is.readObject();
            
            Iterator<Entry<LocalDate, Entry<byte[], byte[]>>> it = loadedEntryMap.entrySet().iterator();
            
            while (it.hasNext()) {
                Map.Entry<LocalDate, Entry<byte[], byte[]>> pair = it.next();
                
                try {
                    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
                    KeySpec spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
                    SecretKey tmp = factory.generateSecret(spec);
                    SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
                    
                    Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                    cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(pair.getValue().getKey()));
                    String plaintext = new String(cipher.doFinal(pair.getValue().getValue()), "UTF-8");
                    entryMap.put(pair.getKey(), plaintext);
                    System.out.println("Plaintext: " + plaintext);
                } catch (NoSuchAlgorithmException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvalidKeySpecException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
                it.remove();
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    @FXML
    private void invokeMenuBarItem() {
    	System.out.println("Invoked Edit -> Menu Bar Item");
    	
    	String newColor = genericColorPrompt("Color for Menu Bar", "-fx-background-color: #", bfMenu);
    	bfMenuColorMap.put(date, newColor);

    	// USING CSS
//    	anchorPane.getScene().getStylesheets().add(getClass().getResource("../application/application.css").toExternalForm());
//    	menuHBox.getStyleClass().add("hbox");
    }
    
    @FXML
    private void invokeDateBarItem() {
        System.out.println("Invoked Edit -> Date Bar Item");
        
        String newColor = genericColorPrompt("Color for Date Bar", "-fx-background-color: #", dateHBox);
        dateHBoxColorMap.put(date, newColor);
    }
    
    @FXML
    private void invokeDatePickerItem() {
        System.out.println("Invoked Edit -> Date Picker Item");
        
        String newColor = genericColorPrompt("Color for Date Picker", "-fx-control-inner-background: #", datePicker);
        datePickerColorMap.put(date, newColor);
        
//        datePicker.getStyleClass().add("date-picker");
        //menuHBox.getStyleClass().add("hbox");
    }
    
    @FXML
    private void invokeTextAreaItem() {
        System.out.println("Invoked Edit -> Text Area Item");
        
        String newColor = genericColorPrompt("Color for Text Area", "-fx-control-inner-background: #", entryTextArea);
        entryTextAreaColorMap.put(date, newColor);
    }
    
    private String genericColorPrompt(String title, String cssString, Node target) {
        String newColorString = null;
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
            Color newColor = colorChooser.getValue();
            newColorString = colorToHex(newColor);
            System.out.println("Picked color: " + newColorString);
            target.setStyle(cssString + newColorString + ";");
        }
        
        return newColorString;
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
        
        if (bfMenuColorMap.get(date) != null)
            bfMenu.setStyle("-fx-background-color: #" + bfMenuColorMap.get(date));
        else
            bfMenu.setStyle(null);
        
        if (dateHBoxColorMap.get(date) != null)
            dateHBox.setStyle("-fx-background-color: #" + dateHBoxColorMap.get(date));
        else
            dateHBox.setStyle(null);
        
        if (datePickerColorMap.get(date) != null)
            datePicker.setStyle("-fx-control-inner-background: #" + datePickerColorMap.get(date));
        else
            datePicker.setStyle(null);
        
        if (entryTextAreaColorMap.get(date) != null)
            entryTextArea.setStyle("-fx-control-inner-background: #" + entryTextAreaColorMap.get(date));
        else
            entryTextArea.setStyle(null);
    }
    
    public void setKey(String key) {
        this.key = key;
        System.out.println("KEY SET: " + key);
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
