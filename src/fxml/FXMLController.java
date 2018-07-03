package fxml;

import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class FXMLController {

    public FXMLController() {
        // TODO Auto-generated constructor stub
    }
    
    @FXML
    private MenuItem aboutItem;
    
    @FXML
    private void invokeAboutItem() {
        System.out.println("THIS IS A TEST CALL");
    }
}
