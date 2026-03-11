package service;

import model.Commandes;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

public class ExportService {

    /*
     * Génère le fichier CSV sur le Bureau 
     */
    public String exporterBilanCSV(List<Commandes> historique) throws Exception {
        String desktopPath = System.getProperty("user.home") + "/Desktop/Bilan_Dragon_Or.csv";
        File file = new File(desktopPath);
        
        double cumul = 0;
        
        // Utilisation du try-with-resources pour fermer le fichier automatiquement
        try (FileWriter writer = new FileWriter(file)) {
            writer.append("ID;Client;Total;Statut\n");
            
            for (Commandes c : historique) {
                cumul += c.total;
                writer.append(c.id + ";" + c.prenom + " " + c.nom + ";" + c.total + " €;" + c.statut + "\n");
            }
            
            writer.append("\n;;TOTAL JOURNEE :;" + String.format("%.2f", cumul) + " €\n");
        }
        
        return file.getAbsolutePath(); // On retourne le chemin pour que le contrôleur puisse l'afficher
    }
}
