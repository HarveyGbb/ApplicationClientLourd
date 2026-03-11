package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/view/login_view.fxml"));
            
            //  fenêtre avec les dimensions
            Scene scene = new Scene(root, 500, 700);
            
            //  titre de la fenêtre
            primaryStage.setTitle("DRAGON D'OR - Connexion");
            primaryStage.setScene(scene);
            primaryStage.show();
            
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}



