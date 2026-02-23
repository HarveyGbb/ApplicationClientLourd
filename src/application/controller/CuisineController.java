package application.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import application.model.Commandes;
import application.model.Plat;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

public class CuisineController {

    @FXML private VBox containerCommandes;
    @FXML private Label lblStatut;

    private int dernierIdCommandeConnu = 0;
    private Timeline timeline;

    @FXML
    public void initialize() {
        chargerDonnees();
        timeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> chargerDonnees()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void chargerDonnees() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:8001/api/commandes/en-cours"))
                        .build();
                
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                List<Commandes> list = new Gson().fromJson(response.body(), new TypeToken<List<Commandes>>(){}.getType());

                Platform.runLater(() -> {
                    lblStatut.setText("🟢 Connecté");
                    containerCommandes.getChildren().clear();
                    int maxId = 0;
                    if (list != null) {
                        for (Commandes c : list) {
                            if (c.id > maxId) maxId = c.id;
                            containerCommandes.getChildren().add(creerCarte(c));
                        }
                    }
                    if (maxId > dernierIdCommandeConnu && dernierIdCommandeConnu != 0) java.awt.Toolkit.getDefaultToolkit().beep();
                    dernierIdCommandeConnu = maxId;
                });
            } catch (Exception e) { Platform.runLater(() -> lblStatut.setText("🔴 Serveur injoignable")); }
        }).start();
    }

    private VBox creerCarte(Commandes c) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-padding: 15; -fx-border-radius: 5;");
        card.getChildren().add(new Label("COMMANDE N°" + c.id + " | " + c.prenom + " " + c.nom));
        for (Plat p : c.plats) {
            card.getChildren().add(new Label(" • " + (p.pivot != null ? p.pivot.quantite : "1") + "x " + p.nom));
        }
        
        HBox hb = new HBox(10);
        Button b1 = new Button("👨‍🍳 En Cuisine"); b1.setOnAction(e -> majStatut(c.id, "en_cuisine"));
        Button b2 = new Button("✅ Prête"); b2.setOnAction(e -> majStatut(c.id, "prete"));
        hb.getChildren().addAll(b1, b2);
        card.getChildren().add(hb);
        return card;
    }

    private void majStatut(int id, String s) {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder()
                        .uri(URI.create("http://127.0.0.1:8001/api/commandes/" + id + "/statut"))
                        .header("Content-Type", "application/json")
                        .PUT(HttpRequest.BodyPublishers.ofString("{\"statut\":\"" + s + "\"}"))
                        .build();
                client.send(req, HttpResponse.BodyHandlers.ofString());
                Platform.runLater(this::chargerDonnees);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    public void handleExport() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Accès Gérant");
        dialog.setHeaderText("Extraction du bilan journalier");
        dialog.setContentText("Code secret :");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get().equals("8888")) {
            executerExport();
        } else {
            lblStatut.setText("❌ Code incorrect");
        }
    }

    private void executerExport() {
        new Thread(() -> {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest req = HttpRequest.newBuilder().uri(URI.create("http://127.0.0.1:8001/api/commandes/export-jour")).build();
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                List<Commandes> histo = new Gson().fromJson(res.body(), new TypeToken<List<Commandes>>(){}.getType());

                File f = new File(System.getProperty("user.home") + "/Desktop/Bilan_Dragon_Or.csv");
                double cumulCalcul = 0;
                
                try (FileWriter w = new FileWriter(f)) {
                    w.append("ID;Client;Plats;Total;Statut\n");
                    for (Commandes c : histo) {
                        StringBuilder sb = new StringBuilder();
                        for(Plat p : c.plats) sb.append(p.nom).append(" | ");
                        cumulCalcul += c.total;
                        w.append(c.id + ";" + c.prenom + " " + c.nom + ";" + sb.toString() + ";" + c.total + " €;" + c.statut + "\n");
                    }
                    w.append("\n;;TOTAL CUMULÉ DU JOUR :;" + String.format("%.2f", cumulCalcul) + " €\n");
                    
                    final String totalAffiche = String.format("%.2f", cumulCalcul);
                    Platform.runLater(() -> lblStatut.setText("✅ Exporté ! Cumul : " + totalAffiche + "€"));
                }
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    public void handleLogout() {
        try {
            if (timeline != null) timeline.stop();
            Parent r = FXMLLoader.load(getClass().getResource("/application/login_view.fxml"));
            Stage s = new Stage(); s.setScene(new Scene(r, 350, 400)); s.show();
            ((Stage) containerCommandes.getScene().getWindow()).close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}