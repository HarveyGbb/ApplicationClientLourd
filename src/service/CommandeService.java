package service;
import com.google.gson.Gson;
import java.util.Map;
import com.google.gson.reflect.TypeToken;
import model.Commandes; // Vérifie bien que cet import correspond à ton package model

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class CommandeService {

    private static final String BASE_URL = "http://127.0.0.1:8001/api";
    private static final Gson GSON = new Gson();
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public List<Commandes> getCommandesEnCours() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/commandes/en-cours"))
                .GET()
                .build();
                
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return GSON.fromJson(response.body(), new TypeToken<List<Commandes>>(){}.getType());
    }

    public void mettreAJourStatut(int id, String statut) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/commandes/" + id + "/statut"))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString("{\"statut\":\"" + statut + "\"}"))
                .build();
                
        CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public List<Commandes> getHistoriqueJour() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/commandes/export-jour"))
                .GET()
                .build();
                
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        return GSON.fromJson(response.body(), new TypeToken<List<Commandes>>(){}.getType());
    }
    public Map<String, Integer> getStatsPlats() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/commandes/stats-plats")) // L'URL Laravel
                .GET()
                .build();
                
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        // On transforme le JSON {"Nems": 15, "Riz": 10} en Map<String, Integer>
        java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, Integer>>(){}.getType();
        return GSON.fromJson(response.body(), type);
    }
}
