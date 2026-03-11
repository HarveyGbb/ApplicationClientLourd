package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class AuthService {
    
    private static final String BASE_URL = "http://127.0.0.1:8001/api";
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public String login(String identifiant, String password) throws Exception {
        String json = "{\"identifiant\":\"" + identifiant + "\", \"password\":\"" + password + "\"}";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/login"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
                
        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() == 200) {
            JsonObject res = JsonParser.parseString(response.body()).getAsJsonObject();
            return res.get("user").getAsJsonObject().get("name").getAsString();
        }
        return null; // Retourne null si le mot de passe est faux
    }
}
