package model;

public class Plat {
    public int id;
    public String nom;
    public String description;
    public String prix; 
    public String categorie;
    public int categorie_id;
    public String image_url;
    public boolean disponible; 
    public int stock;
    public String created_at;
    public String updated_at;
    

    public Pivot pivot;

    public class Pivot {
        public int quantite;
        public int commande_id;
        public int plat_id;
    }
}
