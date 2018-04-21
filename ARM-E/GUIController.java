import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.stage.Window;

public class GUIController{
	@FXML
	private TextField Val1;
	@FXML
	private TextField Val2;
	@FXML
	private TextField Val3;
	@FXML
	private TextField Val4;
	@FXML
	private TextField Val5;
	@FXML
	private TextField Val6;
	@FXML
	private TextField Val7;
	@FXML
	private TextField Val8;
	@FXML
	private TextField Val9;
	@FXML
	private TextField Val10;
	@FXML
	private TextField Val11;
	@FXML
	private TextField Val12;
	@FXML
	private TextField Val13;
	@FXML
	private TextField CPSR_Value;
	@FXML
	private TextField Op_Code_Register_Value;
	@FXML
	private TableView<String> Input_Table;
	@FXML
	private Button Run;

	@FXML
	protected void handleRunButtonAction(ActionEvent event){
		Window owner = Run.getScene().getWindow();
		System.out.println("In GUI Controller");
	}
}
