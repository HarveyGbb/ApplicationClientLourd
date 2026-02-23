package application.model;

public class Plat {
    public int id;
    public String nom;
    public String description;
    public String prix; // decimal(10,2) en SQL arrive souvent en String via JSON
    public String categorie;
    public int categorie_id;
    public String image_url;
    public boolean disponible; // tinyint(1) correspond à boolean en Java
    public int stock;
    public String created_at;
    public String updated_at;
    
    // Garde bien cet objet Pivot, c'est lui qui contient la quantité de la commande !
    public Pivot pivot;

    public class Pivot {
        public int quantite;
        public int commande_id;
        public int plat_id;
    }
}
