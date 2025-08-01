package db;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static DatabaseManager instance;
    private Connection connection;

    private final String DB_URL = "jdbc:mysql://localhost:3306/stockdb?useSSL=false&serverTimezone=UTC";
    private final String DB_USER = "root";
    private final String DB_PASSWORD = ""; // Mets ton mot de passe ici si nécessaire

    private DatabaseManager() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    public void initializeDatabase() {
        try {
            createTables();
            insertSampleData();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTables() throws SQLException {
        String[] createTableQueries = {
            "CREATE TABLE IF NOT EXISTS produits (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "nom VARCHAR(100) NOT NULL, " +
            "description VARCHAR(255), " +
            "prix DECIMAL(10,2) NOT NULL, " +
            "categorie VARCHAR(50), " +
            "date_creation DATETIME DEFAULT CURRENT_TIMESTAMP" +
            ")",

            "CREATE TABLE IF NOT EXISTS stock (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "produit_id INT NOT NULL, " +
            "quantite INT NOT NULL DEFAULT 0, " +
            "min_quantite INT NOT NULL DEFAULT 10, " +
            "max_quantite INT NOT NULL DEFAULT 100, " +
            "localisation VARCHAR(50), " +
            "derniere_update DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "FOREIGN KEY (produit_id) REFERENCES produits(id)" +
            ")",

            "CREATE TABLE IF NOT EXISTS fournisseurs (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "nom VARCHAR(100) NOT NULL, " +
            "contact_email VARCHAR(100), " +
            "contact_phone VARCHAR(20), " +
            "adresse VARCHAR(255), " +
            "fiabilite_score DECIMAL(3,2) DEFAULT 5.00" +
            ")",

            "CREATE TABLE IF NOT EXISTS commandes (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "client_nom VARCHAR(100) NOT NULL, " +
            "commande_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "status VARCHAR(20) DEFAULT 'En attente', " +
            "montant_total DECIMAL(10,2), " +
            "adresse_livraison VARCHAR(255)" +
            ")",

            "CREATE TABLE IF NOT EXISTS commande_items (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "commande_id INT NOT NULL, " +
            "produit_id INT NOT NULL, " +
            "quantite INT NOT NULL, " +
            "prix_unitaire DECIMAL(10,2) NOT NULL, " +
            "FOREIGN KEY (commande_id) REFERENCES commandes(id), " +
            "FOREIGN KEY (produit_id) REFERENCES produits(id)" +
            ")",

            "CREATE TABLE IF NOT EXISTS livraisons (" +
            "id INT PRIMARY KEY AUTO_INCREMENT, " +
            "commande_id INT NOT NULL, " +
            "livraison_date DATETIME DEFAULT CURRENT_TIMESTAMP, " +
            "status VARCHAR(20) DEFAULT 'En attente', " +
            "livreur VARCHAR(50), " +
            "tracking_number VARCHAR(100), " +
            "estimation_livraison DATETIME, " +
            "FOREIGN KEY (commande_id) REFERENCES commandes(id)" +
            ")"
        };

        try (Statement statement = connection.createStatement()) {
            for (String query : createTableQueries) {
                statement.executeUpdate(query);
            }
        }

        System.out.println("✅ Tables MySQL créées avec succès");
    }

    private void insertSampleData() throws SQLException {
        System.out.println("Insertion des données est commencée!");

        try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM produits")) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) return;
        }

        // Insertion produits
        String[] produits = {
            "INSERT INTO produits (nom, description, prix, categorie) VALUES ('Ordinateur Portable', 'HP EliteBook 15 pouces', 7899.99, 'Informatique')",
            "INSERT INTO produits (nom, description, prix, categorie) VALUES ('Souris Wireless', 'Logitech MX Master 3', 379.99, 'Accessoires')",
            "INSERT INTO produits (nom, description, prix, categorie) VALUES ('Clavier Mécanique', 'Corsair K95 RGB', 249.99, 'Accessoires')",
            "INSERT INTO produits (nom, description, prix, categorie) VALUES ('Écran 24 pouces', 'Dell UltraSharp U2419H', 1299.99, 'Moniteurs')",
            "INSERT INTO produits (nom, description, prix, categorie) VALUES ('Webcam HD', 'Logitech C920', 669.99, 'Accessoires')"
        };

        for (String query : produits) {
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.execute();
            }
        }

        // Insertion stock
        String[] stocks = {
            "INSERT INTO stock (produit_id, quantite, min_quantite, max_quantite, localisation) VALUES (1, 25, 5, 50, 'Entrepôt A')",
            "INSERT INTO stock (produit_id, quantite, min_quantite, max_quantite, localisation) VALUES (2, 100, 20, 200, 'Entrepôt B')",
            "INSERT INTO stock (produit_id, quantite, min_quantite, max_quantite, localisation) VALUES (3, 45, 10, 80, 'Entrepôt B')",
            "INSERT INTO stock (produit_id, quantite, min_quantite, max_quantite, localisation) VALUES (4, 30, 8, 60, 'Entrepôt A')",
            "INSERT INTO stock (produit_id, quantite, min_quantite, max_quantite, localisation) VALUES (5, 75, 15, 120, 'Entrepôt C')"
        };

        for (String query : stocks) {
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.execute();
            }
        }

        // Insertion fournisseurs
        String[] fournisseurs = {
            "INSERT INTO fournisseurs (nom, contact_email, contact_phone, adresse, fiabilite_score) VALUES ('TechDistrib', 'contact@techdistrib.com', '+21267004051', '123 Rue de Milan, Casa', 4.5)",
            "INSERT INTO fournisseurs (nom, contact_email, contact_phone, adresse, fiabilite_score) VALUES ('ComputerWorld', 'sales@computerworld.com', '+21224006651', '456 Avenue des oliviers, Marrakech', 4.2)",
            "INSERT INTO fournisseurs (nom, contact_email, contact_phone, adresse, fiabilite_score) VALUES ('AccessoiresInformatiques', 'info@accessoirepro.fr', '+21225045078', '789 Boulevard Hassan II, Ouarzazate', 4.8)"
        };

        for (String query : fournisseurs) {
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.execute();
            }
        }

        System.out.println("✅ Données de test MySQL insérées avec succès");
    }

    // Méthodes utilitaires
    public List<Product> getAllproduits() {
        List<Product> produits = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT p.*, s.quantite, s.min_quantite FROM produits p LEFT JOIN stock s ON p.id = s.produit_id")) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Product product = new Product(
                        rs.getInt("id"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getDouble("prix"),
                        rs.getString("categorie"),
                        rs.getInt("quantite"),
                        rs.getInt("min_quantite")
                );
                produits.add(product);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return produits;
    }

    public boolean updateStock(int productId, int new_quantite) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE stock SET quantite = ?, derniere_update = CURRENT_DATE WHERE produit_id = ?")) {
            stmt.setInt(1, new_quantite);
            stmt.setInt(2, productId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int createOrder(String client_nom, String adresse_livraison , double montant_total) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO commandes (client_nom, adresse_livraison , montant_total) VALUES (?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, client_nom);
            stmt.setString(2, adresse_livraison);
            stmt.setDouble(3, montant_total);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    public int createOrderItems(int orderId , int productId , int quantity , double unitPrice) {
        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO commande_items(commande_id , produit_id , quantite , prix_unitaire) VALUES (?, ?, ? , ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, orderId);
            stmt.setInt(2, productId);
            stmt.setInt(3, quantity);
            stmt.setDouble(4, unitPrice);
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                return keys.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }
    
    
 

    public int getProductIdBynom(String produit_nom) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT id FROM produits WHERE nom = ?")) {
            stmt.setString(1, produit_nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        return -1;
    }
    
    
    public double getUnitPriceByName(String produit_nom) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT prix FROM produits WHERE nom = ?")) {
            stmt.setString(1, produit_nom);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("prix");
            }
        }
        return -1;
    }
    

    // Classe interne Product
    public static class Product {
        public int id;
        public String nom;
        public String description;
        public double prix;
        public String categorie;
        public int quantite;
        public int min_quantite;

        public Product(int id, String nom, String description, double prix,
                       String categorie, int quantite, int min_quantite) {
            this.id = id;
            this.nom = nom;
            this.description = description;
            this.prix = prix;
            this.categorie = categorie;
            this.quantite = quantite;
            this.min_quantite = min_quantite;
        }

        @Override
        public String toString() {
            return String.format("%s - Stock: %d (Min: %d) - %.2f€",
                    nom, quantite, min_quantite, prix);
        }
    }


    public void handleDeliveryCreate(int orderId, String status, String carrier, String trackingNumber, Date estimatedDelivery) throws SQLException {
        String insertRequest = "INSERT INTO livraisons (commande_id, livraison_date, status, livreur, tracking_number, estimation_livraison) "
        		+ "VALUES (?, CURRENT_TIMESTAMP, ?, ?, ?, ?)";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertRequest)) {
            insertStmt.setInt(1, orderId);
            insertStmt.setString(2, status);
            insertStmt.setString(3, carrier);
            insertStmt.setString(4, trackingNumber);
            
            // Conversion java.util.Date -> java.sql.Date (ou Timestamp si ta colonne le nécessite)
            java.sql.Timestamp  sqlEstimatedDelivery = new java.sql.Timestamp (estimatedDelivery.getTime());
            insertStmt.setTimestamp (5, sqlEstimatedDelivery);
            
            insertStmt.executeUpdate();
            
            // La mise à jour de status dans la table commandes
            String updateRequestOrder = "UPDATE commandes SET status = ? WHERE id = ?";
            PreparedStatement updateStmtOrder = connection.prepareStatement(updateRequestOrder);
            updateStmtOrder.setString(1, status);
            updateStmtOrder.setInt(2, orderId);
            updateStmtOrder.executeUpdate();
        }
    }

	
	public void handleDeliveryUpdate(int orderId, String status) {
		
	    try (PreparedStatement stmt = connection.prepareStatement("SELECT * FROM livraisons WHERE commande_id = ?")) {
	    	stmt.setInt(1, orderId);
            ResultSet rs = stmt.executeQuery();
            
	        if (rs.next()) {      // Déjà existante : mise à jour du champ status
	            
	            String updateRequest = "UPDATE livraisons SET status = ? WHERE commande_id = ?";
	            PreparedStatement updateStmt = connection.prepareStatement(updateRequest);
	            updateStmt.setString(1, status);
	            updateStmt.setInt(2, orderId);
	            updateStmt.executeUpdate();
	            
	            // La mise à jour de status dans la table commandes
	            String updateRequestOrder = "UPDATE commandes SET status = ? WHERE id = ?";
	            PreparedStatement updateStmtOrder = connection.prepareStatement(updateRequestOrder);
	            updateStmtOrder.setString(1, status);
	            updateStmtOrder.setInt(2, orderId);
	            updateStmtOrder.executeUpdate();
	            
	        } 

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    

	}

}
