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
            // On demande à Java de charger le fichier FXML qu'on vient de créer
            Parent root = FXMLLoader.load(getClass().getResource("login_view.fxml"));
            
            // On crée la fenêtre avec les dimensions
            Scene scene = new Scene(root, 500, 700);
            
            primaryStage.setTitle("ÉCRAN CUISINE - DRAGON D'OR");
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


