/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.database;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransactionDatabaseManager {
    private Connection connection;
    private SP_Shop plugin;

    public TransactionDatabaseManager(SP_Shop plugin) {
        this.plugin = plugin;
        initializeDatabase();
    }

    private void initializeDatabase() {
        try {
            // Créer le dossier du plugin s'il n'existe pas
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }

            // Établir la connexion à la base de données SQLite
            connection = DriverManager.getConnection("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/transactions.db");

            // Créer la table des transactions si elle n'existe pas
            try (Statement statement = connection.createStatement()) {
                statement.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "item_name TEXT NOT NULL, " +
                        "seller_name TEXT NOT NULL, " +
                        "buyer_name TEXT NOT NULL, " +
                        "price REAL NOT NULL, " +
                        "quantity INTEGER NOT NULL, " +
                        "transaction_date DATETIME DEFAULT CURRENT_TIMESTAMP" +
                        ")");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'initialisation de la base de données : " + e.getMessage());
        }
    }

    /**
     * Enregistre une transaction dans la base de données
     * @param item L'item vendu
     * @param seller Le vendeur
     * @param buyer L'acheteur
     * @param price Le prix de la transaction
     * @param quantity La quantité d'items
     */
    public void recordTransaction(Material item, Player seller, Player buyer, double price, int quantity) {
        String query = "INSERT INTO transactions (item_name, seller_name, buyer_name, price, quantity) VALUES (?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, item.name());
            pstmt.setString(2, seller.getName());
            pstmt.setString(3, buyer.getName());
            pstmt.setDouble(4, price);
            pstmt.setInt(5, quantity);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement de la transaction : " + e.getMessage());
        }
    }

    /**
     * Récupère les transactions récentes
     * @param limit Nombre maximal de transactions à récupérer
     * @return Liste des transactions
     */
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT ?";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setInt(1, limit);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction transaction = new Transaction(
                            rs.getInt("id"),
                            Material.valueOf(rs.getString("item_name")),
                            rs.getString("seller_name"),
                            rs.getString("buyer_name"),
                            rs.getDouble("price"),
                            rs.getInt("quantity"),
                            rs.getTimestamp("transaction_date").toLocalDateTime()
                    );
                    transactions.add(transaction);
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des transactions : " + e.getMessage());
        }

        return transactions;
    }

    /**
     * Classe représentant une transaction
     */
    public static class Transaction {
        private int id;
        private Material item;
        private String sellerName;
        private String buyerName;
        private double price;
        private int quantity;
        private LocalDateTime transactionDate;

        public Transaction(int id, Material item, String sellerName, String buyerName,
                           double price, int quantity, LocalDateTime transactionDate) {
            this.id = id;
            this.item = item;
            this.sellerName = sellerName;
            this.buyerName = buyerName;
            this.price = price;
            this.quantity = quantity;
            this.transactionDate = transactionDate;
        }

        // Getters
        public int getId() { return id; }
        public Material getItem() { return item; }
        public String getSellerName() { return sellerName; }
        public String getBuyerName() { return buyerName; }
        public double getPrice() { return price; }
        public int getQuantity() { return quantity; }
        public LocalDateTime getTransactionDate() { return transactionDate; }
    }

    /**
     * Ferme la connexion à la base de données
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la fermeture de la connexion à la base de données : " + e.getMessage());
        }
    }
}