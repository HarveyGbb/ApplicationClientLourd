package model;

import java.util.List;

public class Commandes {
    public int id;
    public String nom;
    public String prenom;
    public String telephone;
    public String heure_retrait;
    public String statut; 
    
 
    public double total;  
    
    public String date_commande;
    public String created_at;
    public String updated_at;
    
 
    public List<Plat> plats;
}