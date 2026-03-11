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
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.stage.Stage;

import model.Commandes;
import model.Plat;
import service.CommandeService;
import service.ExportService;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AdminController {

    @FXML private VBox mainContent;
    @FXML private Label lblTitrePage;
    @FXML private Label lblClock;
    @FXML private Label lblStatut;

    private int dernierIdConnu = 0;
    private Timeline refreshTimeline; // Timeline pour les commandes
    
    private CommandeService commandeService = new CommandeService();
    private ExportService exportService = new ExportService();

    @FXML
    public void initialize() {
        System.out.println("AdminController : Dashboard lance.");
        
        // Horloge en haut à droite 
        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (lblClock != null) {
                lblClock.setText(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
            }
        }));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // On affiche l'onglet des commandes 
        afficherCommandesView();
    }

   
    // VUE 1 : ONGLET COMMANDES
    
    @FXML
    public void afficherCommandesView() {
        lblTitrePage.setText("Commandes en cours");
        mainContent.getChildren().clear();
        mainContent.setAlignment(Pos.TOP_LEFT); //  aligne en haut

        //  crée un conteneur spécifique pour les cartes de commandes
        VBox containerCartes = new VBox(15);
        mainContent.getChildren().add(containerCartes);

        //  charge les données une première fois
        chargerDonneesCommandes(containerCartes);

        //  rafraîchissement automatique toutes les 10 secondes
        if (refreshTimeline != null) refreshTimeline.stop();
        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(10), event -> chargerDonneesCommandes(containerCartes)));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE);
        refreshTimeline.play();
    }

    private void chargerDonneesCommandes(VBox containerCartes) {
        new Thread(() -> {
            try {
                List<Commandes> list = commandeService.getCommandesEnCours();

                Platform.runLater(() -> {
                    lblStatut.setText("MODE GERANT - CONNECTE");
                    containerCartes.getChildren().clear();
                    
                    int maxId = 0;
                    if (list != null && !list.isEmpty()) {
                        for (Commandes c : list) {
                            if (c.id > maxId) maxId = c.id;
                            containerCartes.getChildren().add(creerCarteDesign(c));
                        }
                    } else {
                        Label vide = new Label("Aucune commande en cours.");
                        vide.setStyle("-fx-text-fill: #95a5a6; -fx-font-style: italic; -fx-font-size: 16px;");
                        containerCartes.getChildren().add(vide);
                    }
                    
                    if (maxId > dernierIdConnu && dernierIdConnu != 0) {
                        java.awt.Toolkit.getDefaultToolkit().beep();
                    }
                    dernierIdConnu = maxId;
                });
            } catch (Exception e) { 
                Platform.runLater(() -> lblStatut.setText("ERREUR SERVEUR API"));
                e.printStackTrace();
            }
        }).start();
    }

    private VBox creerCarteDesign(Commandes c) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                     "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 5); " +
                     "-fx-border-color: #dcdde1; -fx-border-width: 1; -fx-border-radius: 12;");

        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        Label idBadge = new Label("#" + c.id);
        idBadge.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-weight: bold; -fx-background-radius: 5;");
        Label nomClient = new Label("  " + c.prenom + " " + c.nom.toUpperCase());
        nomClient.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2f3640;");
        header.getChildren().addAll(idBadge, nomClient);

        VBox platsBox = new VBox(5);
        for (Plat p : c.plats) {
            Label pLabel = new Label(" - " + (p.pivot != null ? p.pivot.quantite : "1") + "x " + p.nom);
            pLabel.setStyle("-fx-text-fill: #353b48;");
            platsBox.getChildren().add(pLabel);
        }

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER);
        
        Button bCuisine = new Button("CUISINER");
        styleBtn(bCuisine, "#e67e22");
        bCuisine.setOnAction(e -> majStatut(c.id, "en_cuisine"));

        Button bPret = new Button("TERMINE");
        styleBtn(bPret, "#27ae60");
        bPret.setOnAction(e -> majStatut(c.id, "prete"));

        actions.getChildren().addAll(bCuisine, bPret);
        HBox.setHgrow(bCuisine, Priority.ALWAYS); 
        HBox.setHgrow(bPret, Priority.ALWAYS);
        bCuisine.setMaxWidth(Double.MAX_VALUE); 
        bPret.setMaxWidth(Double.MAX_VALUE);

        if ("en_cuisine".equals(c.statut)) {
            card.setStyle(card.getStyle() + "-fx-border-color: #e67e22; -fx-border-width: 2;");
        }

        card.getChildren().addAll(header, new Separator(), platsBox, actions);
        return card;
    }

    private void styleBtn(Button b, String color) {
        b.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 6; -fx-cursor: hand;");
    }

    private void majStatut(int id, String s) {
        new Thread(() -> {
            try {
                commandeService.mettreAJourStatut(id, s);
                Platform.runLater(this::afficherCommandesView); // Recharge la vue des commandes
            } catch (Exception e) { e.printStackTrace(); }
        }).start();
    }

    
    // VUE 2 : ONGLET STATISTIQUES (BARCHART)
    
    @FXML
    public void afficherStatsView() {
        lblTitrePage.setText("Statistiques des Ventes");
        
        if (refreshTimeline != null) refreshTimeline.stop();
        
        mainContent.getChildren().clear();
        mainContent.setAlignment(Pos.CENTER); 

        new Thread(() -> {
            try {
                Map<String, Integer> stats = commandeService.getStatsPlats();

                Platform.runLater(() -> {
                    CategoryAxis xAxis = new CategoryAxis();
                    xAxis.setLabel("Plats");
                    xAxis.setStyle("-fx-tick-label-fill: #2f3640; -fx-font-weight: bold; -fx-font-size: 13px;");

                    NumberAxis yAxis = new NumberAxis();
                    yAxis.setLabel("Quantites vendues");
                    yAxis.setStyle("-fx-tick-label-fill: #2f3640; -fx-font-weight: bold; -fx-font-size: 13px;");

                    BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
                    barChart.setTitle("Recapitulatif des plats commandes aujourd'hui");
                    barChart.setLegendVisible(false);
                    barChart.setPrefHeight(600);
                    barChart.setPrefWidth(800);

                    XYChart.Series<String, Number> series = new XYChart.Series<>();
                    for (Map.Entry<String, Integer> entry : stats.entrySet()) {
                        series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
                    }

                    barChart.getData().add(series);
                    mainContent.getChildren().add(barChart);
                    
                   
                    
                    barChart.applyCss();
                    barChart.layout();

                    // 2 couleur du Titre principal
                    javafx.scene.Node title = barChart.lookup(".chart-title");
                    if (title != null) {
                        title.setStyle("-fx-text-fill: #2f3640; -fx-font-weight: bold; -fx-font-size: 18px;");
                    }

                
                    javafx.scene.Node xLabel = xAxis.lookup(".axis-label");
                    if (xLabel != null) {
                        xLabel.setStyle("-fx-text-fill: #2f3640; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }

                    // 4 couleur Quantites vendues
                    javafx.scene.Node yLabel = yAxis.lookup(".axis-label");
                    if (yLabel != null) {
                        yLabel.setStyle("-fx-text-fill: #2f3640; -fx-font-weight: bold; -fx-font-size: 14px;");
                    }

                    // 5 barres en bleu 
                    for (javafx.scene.Node n : barChart.lookupAll(".default-color0.chart-bar")) {
                        n.setStyle("-fx-bar-fill: #3498db;"); 
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Label erreur = new Label("Erreur de chargement des statistiques. L'API est-elle prete ?");
                    erreur.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                    mainContent.getChildren().add(erreur);
                });
                e.printStackTrace();
            }
        }).start();
    }

    
    // ACTIONS GLOBALES (EXPORT & LOGOUT)
    
    @FXML
    public void handleExport(ActionEvent event) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Securite");
        dialog.setHeaderText("Confirmation de l'exportation CSV");
        dialog.setContentText("Code secret :");
        Optional<String> result = dialog.showAndWait();

        if (result.isPresent() && result.get().equals("8888")) {
            executerExport();
        } else {
            lblStatut.setText("CODE INCORRECT");
        }
    }

    private void executerExport() {
        new Thread(() -> {
            try {
                List<Commandes> histo = commandeService.getHistoriqueJour();
                String cheminFichier = exportService.exporterBilanCSV(histo);
                
                Platform.runLater(() -> {
                    lblStatut.setText("EXPORT REUSSI SUR LE BUREAU");
                    System.out.println("Fichier disponible ici : " + cheminFichier);
                });
            } catch (Exception e) { 
                Platform.runLater(() -> lblStatut.setText("ERREUR D'ECRITURE DU FICHIER"));
                e.printStackTrace(); 
            }
        }).start();
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        try {
            if (refreshTimeline != null) refreshTimeline.stop();
            Parent r = FXMLLoader.load(getClass().getResource("/view/login_view.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(r));
            stage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }
}
