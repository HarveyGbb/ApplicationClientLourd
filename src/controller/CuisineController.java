package controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Stage;

import model.Commandes;
import model.Plat;
import service.CommandeService; // Import de ta couche de service

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CuisineController {

    @FXML private VBox containerCommandes;
    @FXML private Label lblStatut;
    @FXML private Label lblClock;

    private int dernierId = 0;
    private Timeline timeline;
    
    // 1. Déclaration du Service
    private CommandeService commandeService = new CommandeService();

    @FXML
    public void initialize() {
        chargerDonnees();
        timeline = new Timeline(new KeyFrame(Duration.seconds(8), event -> chargerDonnees()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void chargerDonnees() {
        new Thread(() -> {
            try {
                // 2. Appel au Service 
                List<Commandes> list = commandeService.getCommandesEnCours();

                Platform.runLater(() -> {
                    if (lblClock != null) {
                        lblClock.setText("Mise a jour : " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
                    }
                    lblStatut.setText("SERVICE ACTIF");
                    containerCommandes.getChildren().clear();
                    
                    if (list != null && !list.isEmpty()) {
                        for (Commandes c : list) {
                            if (c.id > dernierId && dernierId != 0) java.awt.Toolkit.getDefaultToolkit().beep();
                            containerCommandes.getChildren().add(creerCarteElite(c));
                        }
                        dernierId = list.get(list.size()-1).id;
                    } else {
                        Label vide = new Label("Aucune commande en attente.");
                        vide.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 16px;");
                        containerCommandes.getChildren().add(vide);
                    }
                });
            } catch (Exception e) { 
                Platform.runLater(() -> lblStatut.setText("SERVEUR HORS-LIGNE")); 
            }
        }).start();
    }

    private VBox creerCarteElite(Commandes c) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 8);");

        // Ligne 1 : ID + Badge Statut
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);
        
        Label idBadge = new Label("#" + c.id);
        idBadge.setStyle("-fx-background-color: #34495e; -fx-text-fill: white; -fx-padding: 4 10; -fx-font-weight: bold; -fx-background-radius: 6;");
        
        Label statusBadge = new Label();
        if ("en_cuisine".equals(c.statut)) {
            statusBadge.setText("EN PREPARATION");
            statusBadge.setStyle("-fx-text-fill: #e67e22; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #e67e22; -fx-border-radius: 4; -fx-padding: 3 6;");
        } else {
            statusBadge.setText("EN ATTENTE");
            statusBadge.setStyle("-fx-text-fill: #3498db; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #3498db; -fx-border-radius: 4; -fx-padding: 3 6;");
        }
        
        HBox spacer = new HBox(); HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().addAll(idBadge, new Label("  "), statusBadge, spacer);

        // Ligne 2 : Nom du client
        Label client = new Label(c.prenom + " " + c.nom.toUpperCase());
        client.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Ligne 3 : Liste des plats
        VBox platsList = new VBox(8);
        for (Plat p : c.plats) {
            HBox pLine = new HBox(10);
            Label qte = new Label((p.pivot != null ? p.pivot.quantite : "1") + "x");
            qte.setStyle("-fx-text-fill: #d35400; -fx-font-weight: bold; -fx-font-size: 15px;");
            Label nP = new Label(p.nom);
            nP.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 15px;");
            pLine.getChildren().addAll(qte, nP);
            platsList.getChildren().add(pLine);
        }

        // Ligne 4 : Boutons
        HBox btns = new HBox(15);
        Button b1 = new Button("CUISINER");
        styleBtn(b1, "#e67e22");
        b1.setOnAction(e -> majStatut(c.id, "en_cuisine"));

        Button b2 = new Button("TERMINER");
        styleBtn(b2, "#27ae60");
        b2.setOnAction(e -> majStatut(c.id, "prete"));

        btns.getChildren().addAll(b1, b2);
        HBox.setHgrow(b1, Priority.ALWAYS); HBox.setHgrow(b2, Priority.ALWAYS);
        b1.setMaxWidth(Double.MAX_VALUE); b2.setMaxWidth(Double.MAX_VALUE);

        // Effet au survol de la carte
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-border-color: #3498db; -fx-border-width: 1;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-border-color: #3498db; -fx-border-width: 1;", "")));

        card.getChildren().addAll(topRow, client, new Separator(), platsList, btns);
        return card;
    }

    private void styleBtn(Button b, String color) {
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-padding: 10; -fx-cursor: hand;");
    }

    private void majStatut(int id, String s) {
        new Thread(() -> {
            try {
                // 3. Appel au Service pour la mise à jour 
                commandeService.mettreAJourStatut(id, s);
                Platform.runLater(this::chargerDonnees);
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            if (timeline != null) timeline.stop();
            // 4. Nouveau chemin de chargement
            Parent r = FXMLLoader.load(getClass().getResource("/view/login_view.fxml"));
            Stage st = (Stage) ((Node) event.getSource()).getScene().getWindow();
            st.setScene(new Scene(r));
            st.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}


