package application;
	
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;


public class Main extends Application {
    private LocalDate date = LocalDate.now();
    private final TextArea textSpace = new TextArea();
    private static Map<LocalDate, String> entryMap = new HashMap<LocalDate, String>();
    
    @Override
	public void start(Stage primaryStage) {
		try {
		    primaryStage.setTitle("BFEditor");
		    
		    listenerSetup();
		    
			BorderPane root = new BorderPane();
			
			// MAIN TOP
			BorderPane bpTop = new BorderPane();
			
//			HBox toolPane = new HBox();
//			toolPane.setPadding(new Insets(15, 12, 15, 12));
//			toolPane.setSpacing(10);
//            toolPane.setStyle("-fx-background-color: #669999;");
            MenuBar menuBar = new MenuBar();
            menuBar.setStyle("-fx-background-color: #669999;");
            Menu menuFile = new Menu("File");
            MenuItem menuSave = new MenuItem("Save...");
            menuSave.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    System.out.println("Saving File");
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Save Encrypted Journal");
                    fileChooser.showOpenDialog(primaryStage);
                }
            });
            menuFile.getItems().add(menuSave);
            menuBar.getMenus().add(menuFile);
            
            
            HBox datePane = new HBox();
            datePane.setPadding(new Insets(15, 12, 15, 12));
            datePane.setSpacing(10);
            datePane.setStyle("-fx-background-color: #00ccff;");
//            TextArea dateSpace = new TextArea();
//            dateSpace.setPrefHeight(datePane.getHeight());
//            dateSpace.setPrefWidth(1000);
//            datePane.getChildren().add(dateSpace);
            final DatePicker datePicker = new DatePicker();
            datePicker.setOnAction(new EventHandler<ActionEvent>() {
                public void handle(ActionEvent t) {
                    date = datePicker.getValue();
                    System.out.println("Selected Date: " + date);
                    textSpace.setText(entryMap.get(date));
                }
            });
            datePicker.setValue(LocalDate.now());
            datePicker.setEditable(false);
            datePane.getChildren().add(datePicker);
            
//		    bpTop.setTop(toolPane);
            bpTop.setTop(menuBar);
		    bpTop.setBottom(datePane);

		    
		    
			HBox rootBot = new HBox();
			
//			registerStageListeners(primaryStage, textSpace);
			
			root.setTop(bpTop);
			root.setCenter(textSpace);
			root.setBottom(rootBot);
			
			Scene scene = new Scene(root, 400, 400);
			scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
			
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
	
	private void listenerSetup() {
        textSpace.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                System.out.println("Entry changed from: \n" + oldValue + "\nto: \n" + newValue);
                entryMap.put(date, newValue);
            }
        });
	}
	
//	private void registerStageListeners(Stage primaryStage, TextArea textSpace) {
//        primaryStage.widthProperty().addListener(new ChangeListener<Number>() {
//            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneWidth, Number newSceneWidth) {
//                double newWidth = newSceneWidth.doubleValue();
//                System.out.println("Width: " + newSceneWidth);
////                textSpace.setPrefWidth(newWidth - (newWidth / 8));
//            }
//        });
//        primaryStage.heightProperty().addListener(new ChangeListener<Number>() {
//            @Override public void changed(ObservableValue<? extends Number> observableValue, Number oldSceneHeight, Number newSceneHeight) {
//                double newHeight = newSceneHeight.doubleValue();
//                System.out.println("Height: " + newSceneHeight);
////                textSpace.setPrefHeight(newHeight - (newHeight / 8));
//            }
//        });
//	}
}
