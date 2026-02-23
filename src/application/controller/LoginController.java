package application.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class LoginController {

    @FXML
    private TextField txtIdentifiant;

    @FXML
    private PasswordField txtMotDePasse;

    @FXML
    private Label lblErreur;

    @FXML
    public void btnConnexionClique() {
        String idSaisi = txtIdentifiant.getText();
        String mdpSaisi = txtMotDePasse.getText();

        lblErreur.setStyle("-fx-text-fill: #f39c12;"); 
        lblErreur.setText("⏳ Vérification sur le serveur...");

        // Debug console
        System.out.println("Tentative : " + idSaisi + " / " + mdpSaisi);

        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                
                // On crée le JSON avec les bonnes clés : 'identifiant' et 'password'
                String jsonBody = "{\"identifiant\":\"" + idSaisi + "\", \"password\":\"" + mdpSaisi + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:8001/api/login"))
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Debug réponse
                System.out.println("Réponse Serveur : " + response.statusCode() + " -> " + response.body());

                Platform.runLater(() -> {
                    if (response.statusCode() == 200) {
                        lblErreur.setStyle("-fx-text-fill: #2ecc71;");
                        lblErreur.setText("✅ Connexion réussie !");
                        ouvrirEcranCuisine();
                    } else {
                        lblErreur.setStyle("-fx-text-fill: #e74c3c;");
                        lblErreur.setText("❌ Identifiant ou code incorrect.");
                    }
                });

            } catch (Exception e) {
                Platform.runLater(() -> {
                    lblErreur.setStyle("-fx-text-fill: #e74c3c;");
                    lblErreur.setText("🔴 Erreur : Serveur injoignable.");
                });
                e.printStackTrace();
            }
        }).start();
    }

    private void ouvrirEcranCuisine() {
        try {
            // Vérifie que le fichier est bien à la racine du package 'application'
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/application/cuisine_view.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Écran Cuisine - Dragon d'Or");
            stage.setScene(new Scene(root, 500, 700));
            stage.show();

            // Fermer la fenêtre de login
            Stage currentStage = (Stage) txtIdentifiant.getScene().getWindow();
            currentStage.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Erreur chargement FXML Cuisine : " + e.getMessage());
        }
    }
}