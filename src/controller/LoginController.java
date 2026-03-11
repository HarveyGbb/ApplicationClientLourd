package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import service.AuthService; // Import de ta couche de service

public class LoginController {

    @FXML private TextField txtIdentifiant;
    @FXML private PasswordField txtMotDePasse;
    @FXML private Label lblErreur;
    
    // 1. Déclaration  service d'authentification
    private AuthService authService = new AuthService();

    @FXML
    public void seConnecter(ActionEvent event) {
        String idSaisi = txtIdentifiant.getText();
        String mdpSaisi = txtMotDePasse.getText();

        System.out.println("Tentative de connexion pour : " + idSaisi);

        new Thread(() -> {
            try {
                // 2. Appel au Service pour  gèrer le JSON et la requête web 
                String roleName = authService.login(idSaisi, mdpSaisi);
                
                Platform.runLater(() -> {
                    if (roleName != null) {
                        System.out.println("Connexion validée par Laravel. Utilisateur : " + roleName);
                        
                        // 3. Nouveaux chemins vers le dossier "view"
                        String fxml = roleName.equalsIgnoreCase("Chef") ? "/view/admin_view.fxml" : "/view/cuisine_view.fxml";
                        changerDePage(event, fxml);
                    } else {
                        // Si le service retourne null, c'est que le mot de passe ou l'ID est faux (Code 401)
                        lblErreur.setText("Identifiants incorrects.");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> lblErreur.setText("Erreur : Serveur Laravel éteint."));
                e.printStackTrace();
            }
        }).start();
    }

    private void changerDePage(ActionEvent event, String fxmlPath) {
        try {
            System.out.println("Chargement de la vue : " + fxmlPath);
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
            System.out.println("Page changée avec succès !");
        } catch (Exception e) {
            System.err.println("ERREUR lors du changement de page : " + e.getMessage());
            e.printStackTrace();
            lblErreur.setText("Erreur de chargement de l'interface.");
        }
    }
}
