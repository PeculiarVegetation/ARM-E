/**
 * Graphical User Interface main for ARM-E
 * @author(Sydney)
 * @version(04/04/2018)
 */

//JavaFX library dependencies
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class GUI extends Application{
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("ARM-E_Frontend.fxml"));
        primaryStage.setTitle("ARM-E: ARM Assembly Emulator");
        primaryStage.setScene(new Scene(root, 766, 571));
        primaryStage.show();
    }

    public static void main(String[] args){

        launch(args);
    }
}
