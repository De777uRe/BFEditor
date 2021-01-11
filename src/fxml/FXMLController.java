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
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.DateCell;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

public class FXMLController {
    public enum SaveType {
        VERSION,
        ENTRY,
        MENU_COLOR,
        DATE_HBOX_COLOR,
        DATE_PICKER_COLOR,
        ENTRY_TEXT_AREA_COLOR
    }

	private LocalDate date = LocalDate.now();

	// TODO Add these maps to a map of maps
    // TODO These are not actually a color map
    private static Map<LocalDate, String> dateHBoxColorMap = new HashMap<>();
    private static Map<LocalDate, String> datePickerColorMap = new HashMap<>();
	private static Map<LocalDate, String> entryMap = new HashMap<>();
    private static Map<LocalDate, String> entryTextAreaColorMap = new HashMap<>();
    private static Map<LocalDate, String> menuColorMap = new HashMap<>();

	// TODO Reformat save file to indicate a version number. On load, if version number
    // not found, this indicates the loading of an old, poorly formatted save file.
    // In the case of loading the old save file format, reformat the save file
    // to whatever better format chosen.

    private static Map<SaveType, Map<LocalDate, String>> stateMaps = new HashMap<SaveType, Map<LocalDate, String>>() {{
        put(SaveType.DATE_HBOX_COLOR, dateHBoxColorMap);
        put(SaveType.DATE_PICKER_COLOR, datePickerColorMap);
        put(SaveType.ENTRY, entryMap);
        put(SaveType.ENTRY_TEXT_AREA_COLOR, entryTextAreaColorMap);
        put(SaveType.MENU_COLOR, menuColorMap);
    }};

    private static String version = "1.0.3";

	private static String key;
	// TODO Was this to store the key in something better than a String?
	private Key aesKey;
	private final byte salt[] = { 3, 25, (byte) 2017, 8, 19, (byte) 1996, 7, 7 };

	private final Background markedBackground = new Background(new BackgroundFill(Color.rgb(0x00, 0x00, 0x00), CornerRadii.EMPTY, Insets.EMPTY));
	private final Background currentDayBackground = new Background(new BackgroundFill(Color.rgb(75, 90, 90), CornerRadii.EMPTY, Insets.EMPTY));
	private String currentDayStyle = "-fx-text-fill: " + "#FFFFFF";
	private Background defaultBackground;
	private String defaultStyle;
	private File defaultSavePath;

	private EventHandler<MouseEvent> onMouseExitedHandler;

    @FXML
    private AnchorPane anchorPane;

    @FXML
    private HBox menuHBox;
    @FXML
    private HBox dateHBox;

    @FXML
    private Label logLabel;
    @FXML
    private Label lastUpdatedLabel;

    @FXML
    private MenuBar bfMenu;

    @FXML
    private Menu fileMenu;
    @FXML
    private Menu editMenu;
    @FXML
    private Menu helpMenu;

    @FXML
    private MenuItem saveItem;
    @FXML
    private MenuItem saveAsItem;
    @FXML
    private MenuItem loadItem;
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
    private HTMLEditor entryTextArea;

    @FXML
    public void initialize() {
        onMouseExitedHandler = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                entryMap.put(date, entryTextArea.getHtmlText());
            }
        };

        defaultBackground = datePicker.getBackground();
        defaultStyle = datePicker.getStyle();

        final Callback<DatePicker, DateCell> dayCellFactory =
                new Callback<DatePicker, DateCell>() {
            @Override
            public DateCell call(final DatePicker datePicker) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate item, boolean empty) {
                        super.updateItem(item, empty);

//                        System.out.println("CALLING UPDATE ITEM");

                        if (entryMap.containsKey(item) && !entryMap.get(getItem()).contains("<body contenteditable=\"true\"></body>")) {
                          setBackground(markedBackground);
                          setStyle("-fx-text-fill: white");
                        }
                        else {
                            setBackground(defaultBackground);
                            setStyle(defaultStyle);
                        }

                        if (item.equals(datePicker.getValue())) {
                            setBackground(currentDayBackground);
                            setStyle(currentDayStyle);
                        }
                    }
                };
            }
        };

        datePicker.setDayCellFactory(dayCellFactory);

        for (Node node : entryTextArea.lookupAll("ToolBar")) {
            node.setOnMouseExited(onMouseExitedHandler);
        }

       anchorPane.setOnMouseExited(onMouseExitedHandler);
       menuHBox.setOnMouseExited(onMouseExitedHandler);
       dateHBox.setOnMouseExited(onMouseExitedHandler);

       datePicker.setValue(date);
       logLabel.setText("");

        WebView webview = (WebView) entryTextArea.lookup("WebView");
        GridPane.setVgrow(webview, Priority.ALWAYS);
    }

    @FXML
    private void onKeyReleased(KeyEvent ke) {
        entryMap.put(date, entryTextArea.getHtmlText());
        DateTimeFormatter df = DateTimeFormatter.ofPattern("LLLL-dd-yyyy");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:s a");
        logLabel.setText(LocalDate.now().format(df).toString() + " " + LocalTime.now().format(dtf).toString());
        if(logLabel.getText() != "")
        {
            lastUpdatedLabel.setText("Last Updated:");
        }
    }

    @FXML
    private void invokeFileMenu() {
    	System.out.println("Invoked File Menu");
    }

    @FXML
    private void invokeSaveItem() {
    	System.out.println("Invoked Save Item");

        System.out.println("Overwriting Save File");

        if (defaultSavePath != null) {
            try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(defaultSavePath))) {
                saveToFile(os);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("IOException caught in invokeSaveItem()\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }

        System.out.println("Overwriting File: " + defaultSavePath);
    }

    @FXML
    private void invokeSaveAsItem() {
        System.out.println("Invoked Save As Item");

        System.out.println("Saving File");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Encrypted Journal");
        File savedFilePath = fileChooser.showSaveDialog((Stage) anchorPane.getScene().getWindow());

        if (savedFilePath != null) {
            try (ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(savedFilePath))) {
                saveToFile(os);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("IOException caught in invokeSaveAsItem()\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }

        System.out.println("Saving to File: " + savedFilePath);
    }

    private void saveToFile(ObjectOutputStream os) {
        try {
            os.writeObject(version);
            os.writeChars("\n");

            Map<SaveType, Object> saveMap = new HashMap<>();

            // TODO Remove redundancy
            for (SaveType saveType : SaveType.values()) {
                switch(saveType){
                    case DATE_HBOX_COLOR:
                        stateMaps.replace(saveType, dateHBoxColorMap);
                        break;
                    case DATE_PICKER_COLOR:
                        stateMaps.replace(saveType, datePickerColorMap);
                        break;
                    case ENTRY:
                        stateMaps.replace(saveType, entryMap);
                        break;
                    case ENTRY_TEXT_AREA_COLOR:
                        stateMaps.replace(saveType, entryTextAreaColorMap);
                        break;
                    case MENU_COLOR:
                        stateMaps.replace(saveType, menuColorMap);
                        break;
                    default:
                        System.out.println("Save To File hit default");
                }

                if(saveType != SaveType.VERSION) {
                    saveMap.put(saveType, stateMaps.get(saveType));
                }
            }

            os.writeObject(saveMap);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("IOException caught in invokeSaveItem() entryMap save\n" + e.getStackTrace());
            e.printStackTrace();
        }


    }

    private Map<LocalDate, Entry<byte[], byte[]>> encryptEntries(Map<LocalDate, String> entryMap) {
        Map<LocalDate, Entry<byte[], byte[]>> encryptionMap = new HashMap<LocalDate, Entry<byte[], byte[]>>();
        Map<LocalDate, String> copyMap = new HashMap<LocalDate, String>(entryMap);

        Iterator<Entry<LocalDate, String>> it = copyMap.entrySet().iterator();

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
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("NoSuchAlgorithmException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("InvalidKeySpecException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("NoSuchPaddingException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("InvalidKeyException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (InvalidParameterSpecException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("InvalidParameterSpecException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("IllegalBlockSizeException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (BadPaddingException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("BadPaddingException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("UnsupportedEncodingException caught in encryptString() from: " + entry + "\n" + e.getStackTrace());
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
                entryTextArea.setHtmlText(entryMap.get(date));
                if (menuColorMap.get(date) != null)
                    bfMenu.setStyle("-fx-background-color: #" + menuColorMap.get(date));
                else
                    bfMenu.setStyle(null);

                if (dateHBoxColorMap.get(date) != null)
                    dateHBox.setStyle("-fx-background-color: #" + dateHBoxColorMap.get(date));
                else
                    dateHBox.setStyle(null);

                if (datePickerColorMap.get(date) != null) {
                    System.out.println("Loading a style from file");
                    datePicker.setStyle("-fx-control-inner-background: #" + datePickerColorMap.get(date));
//                    datePicker.getDayCellFactory().call(datePicker);
                }
                else {
                    datePicker.setStyle(null);
                    datePicker.setBackground(defaultBackground);
                    datePicker.setStyle(defaultStyle);
                }

                if (entryTextAreaColorMap.get(date) != null)
                    entryTextArea.setStyle("-fx-control-inner-background: #" + entryTextAreaColorMap.get(date));
                else
                    entryTextArea.setStyle(null);
                defaultSavePath = loadedFilePath;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("FileNotFoundException caught in invokeLoadItem()" + "\n" + e.getStackTrace());
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("IOException caught in invokeLoadItem()" + "\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }
    }

    private void populateSavedChanges(ObjectInputStream is) {
        Object firstObj = null;
        Object secondObj = null;
        SecretKey secret = null;
        SecretKey tmp = null;
        SecretKeyFactory factory = null;
        KeySpec spec = null;

        try {
            factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            spec = new PBEKeySpec(key.toCharArray(), salt, 65536, 256);
            tmp = factory.generateSecret(spec);
            secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("NoSuchAlgorithmException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("InvalidKeySpecException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
            e.printStackTrace();
        }

        try {
            firstObj = is.readObject();
            version = (String) firstObj;
            is.skip(8);
            System.out.println(version);

            secondObj = is.readObject();
            Map<SaveType, Object> saveMap = (HashMap<SaveType, Object>) secondObj;

            System.out.println("Loading Save File (New Version)");

            for (Map.Entry<SaveType, Object> entry : saveMap.entrySet()) {
                System.out.println(entry.getKey().toString());
                System.out.println(entry.getValue().toString());
                if (entry.getValue() instanceof Map) {
                    SaveType saveType = entry.getKey();
                    HashMap<LocalDate, String>  readMap = (HashMap<LocalDate, String>) entry.getValue();
                   switch(saveType) {
                       case DATE_HBOX_COLOR:
                           dateHBoxColorMap = readMap;
                           break;
                       case DATE_PICKER_COLOR:
                           datePickerColorMap = readMap;
                           break;
                       case ENTRY:
                           entryMap = readMap;
                           break;
                       case ENTRY_TEXT_AREA_COLOR:
                           entryTextAreaColorMap = readMap;
                           break;
                       case MENU_COLOR:
                           menuColorMap = readMap;
                           break;
                       default:
                           System.out.println("Save To File hit default");
                   }
                }
            }

        } catch(ClassCastException cce){
            System.out.println("JOURNAL VERSION NOT FOUND AT TOP OF SAVE");
            System.out.println("Loading Save File (Old Version)");

            try {
                menuColorMap = (Map<LocalDate, String>) firstObj;
                is.skip(8);
                // TODO Skipping bytes seems sloppy
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

                        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
                        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(pair.getValue().getKey()));
                        String plaintext = new String(cipher.doFinal(pair.getValue().getValue()), "UTF-8");
                        entryMap.put(pair.getKey(), plaintext);
                        System.out.println("Plaintext: " + plaintext);
                    } catch (NoSuchAlgorithmException e) {
                        // TODO Auto-generated catch block
                        entryTextArea.setHtmlText("NoSuchAlgorithmException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                        e.printStackTrace();
                    } catch (NoSuchPaddingException e) {
                        // TODO Auto-generated catch block
                        entryTextArea.setHtmlText("NoSuchPaddingException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                        e.printStackTrace();
                    } catch (InvalidKeyException e) {
                        // TODO Auto-generated catch block
                        entryTextArea.setHtmlText("InvalidKeyException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                        e.printStackTrace();
                    } catch (InvalidAlgorithmParameterException e) {
                        // TODO Auto-generated catch block
                        entryTextArea.setHtmlText("InvalidAlgorithmParameterException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                        e.printStackTrace();
                    } catch (IllegalBlockSizeException e) {
                        // TODO Auto-generated catch block
                        entryTextArea.setHtmlText("IllegalBlockSizeException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                        e.printStackTrace();
                    } catch (BadPaddingException e) {
                        // TODO Auto-generated catch block
                        entryTextArea.setHtmlText("BadPaddingException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                        e.printStackTrace();
                    }

                    it.remove();
                }
            } catch(IOException e){
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("IOException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                e.printStackTrace();
            } catch(ClassNotFoundException e){
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("ClassNotFoundException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
                e.printStackTrace();
            }
        } catch(IOException e){
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("IOException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
            e.printStackTrace();
        } catch(ClassNotFoundException e){
            // TODO Auto-generated catch block
            entryTextArea.setHtmlText("ClassNotFoundException caught in populateSavedChangesFromOld()\n" + e.getStackTrace());
            e.printStackTrace();
        }
    }


    @FXML
    private void invokeLoadItemOld() {
        System.out.println("Invoked Load Item");

        System.out.println("Loading File");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load Encrypted Journal");
        File loadedFilePath = fileChooser.showOpenDialog((Stage) anchorPane.getScene().getWindow());

        if (loadedFilePath != null) {
            try (ObjectInputStream is = new ObjectInputStream(new FileInputStream(loadedFilePath))) {
//                populateSavedChangesFromOld(is);

                // TODO Trigger Event
                System.out.println("LOADING DATE: " + date);
                datePicker.setValue(LocalDate.now());
                entryTextArea.setHtmlText(entryMap.get(date));
                if (menuColorMap.get(date) != null)
                    bfMenu.setStyle("-fx-background-color: #" + menuColorMap.get(date));
                else
                    bfMenu.setStyle(null);

                if (dateHBoxColorMap.get(date) != null)
                    dateHBox.setStyle("-fx-background-color: #" + dateHBoxColorMap.get(date));
                else
                    dateHBox.setStyle(null);

                if (datePickerColorMap.get(date) != null) {
                    System.out.println("Loading a style from file");
                    datePicker.setStyle("-fx-control-inner-background: #" + datePickerColorMap.get(date));
//                    datePicker.getDayCellFactory().call(datePicker);
                }
                else {
                    datePicker.setStyle(null);
                    datePicker.setBackground(defaultBackground);
                    datePicker.setStyle(defaultStyle);
                }

                if (entryTextAreaColorMap.get(date) != null)
                    entryTextArea.setStyle("-fx-control-inner-background: #" + entryTextAreaColorMap.get(date));
                else
                    entryTextArea.setStyle(null);
                defaultSavePath = loadedFilePath;
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("FileNotFoundException caught in invokeLoadItem()" + "\n" + e.getStackTrace());
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                entryTextArea.setHtmlText("IOException caught in invokeLoadItem()" + "\n" + e.getStackTrace());
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void invokeMenuBarItem() {
    	System.out.println("Invoked Edit -> Menu Bar Item");
    	
    	String newColor = genericColorPrompt("Color for Menu Bar", "-fx-background-color: #", bfMenu);
    	menuColorMap.put(date, newColor);

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
        dialog.getDialogPane().setPrefHeight(110);
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
        
        Alert alert = new Alert(AlertType.INFORMATION, "BFJournal Version 1.0.2", ButtonType.OK);
        alert.setHeaderText("Your Personal Encrypted Journal");
        alert.setTitle("About BFJournal");
        alert.showAndWait();
    }
    
    @FXML
    private void invokeHistoryItem() {
        Alert alert = new Alert(AlertType.INFORMATION, "Changelog", ButtonType.OK);
        alert.setHeaderText("Version History");
        alert.setTitle("Changelog");
        alert.setContentText("Version 1.0.2 \n" +
                             "Added Custom Icon \n" +
                             "Added Password Dialog (Hidden Password Entry) \n" +
                             "Added Changelog \n" +
                             "***************************************************\n\n" +
                             "Version 1.0.1 \n" +
                             "Conversion from TextArea to HTMLEditor for Formatting Options \n" +
                             "Fixed Bug that Caused Saved Entries to be Deleted in Current Session \n" +
                             "Added About Section \n" +
                             "Fixed Custom Color Window Size for Mac \n" +
                             "***************************************************\n\n" +
                             "Version 1.0.0 \n" +
                             "Created Initial GUI \n" +
                             "Ported to SceneBuilder \n" +
                             "File Menu and Date Bar Colors Are Editable \n" +
                             "Added Ability to Save Colors \n" +
                             "Implemented Encryption/Decryption \n");
        alert.showAndWait();
    }
    
    @FXML
    private void invokeDatePicker() {
    	System.out.println("Invoked Date Picker");
    	
    	date = datePicker.getValue();
        System.out.println("Selected Date: " + date);
        entryTextArea.setHtmlText("");
        
        entryTextArea.setHtmlText(entryMap.get(date));
        
        if (menuColorMap.get(date) != null)
            bfMenu.setStyle("-fx-background-color: #" + menuColorMap.get(date));
        else
            bfMenu.setStyle(null);
        
        if (dateHBoxColorMap.get(date) != null)
            dateHBox.setStyle("-fx-background-color: #" + dateHBoxColorMap.get(date));
        else
            dateHBox.setStyle(null);
        
        if (datePickerColorMap.get(date) != null) {
            System.out.println("Loading a style from file");
            datePicker.setStyle("-fx-control-inner-background: #" + datePickerColorMap.get(date));
            datePicker.getDayCellFactory().call(datePicker);
        }
        else {
            datePicker.setStyle(null);
            datePicker.setBackground(defaultBackground);
            datePicker.setStyle(defaultStyle);
        }
        
//        if (datePickerColorMap.get(date) != null) {
//            System.out.println("Setting style from datepicker");
//            datePicker.setStyle("-fx-control-inner-background: #" + datePickerColorMap.get(date));
//        }
//        else {
//            datePicker.setStyle(null);
//        }
        
        if (entryTextAreaColorMap.get(date) != null)
            entryTextArea.setStyle("-fx-control-inner-background: #" + entryTextAreaColorMap.get(date));
        else
            entryTextArea.setStyle(null);
    }
    
    public void setKey(String key) {
        try {
            byte[] keyBytes;
            keyBytes = key.getBytes("UTF-8");
            String encodedKey = new String(keyBytes, "UTF-8");
            
            this.key = encodedKey;
            System.out.println("KEY SET: " + encodedKey);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
