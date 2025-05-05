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
import java.util.UUID;

public class TransactionDatabaseManager {
    private SP_Shop plugin;
    public DatabaseManager dbManager;

    public TransactionDatabaseManager(SP_Shop plugin) {
        this.plugin = plugin;
        this.dbManager = new DatabaseManager(plugin);
        // La création des tables est maintenant gérée par DatabaseSetup
    }

    /*
     * Enregistre une transaction dans la base de données
     * @param item L'item vendu
     * @param seller Le vendeur
     * @param buyer L'acheteur
     * @param price Le prix de la transaction
     * @param quantity La quantité d'items
     */
    public void recordTransaction(Material item, Player seller, Player buyer, double price, int quantity) {
        String query = "INSERT INTO transactions (item_name, seller_name, buyer_name, price, quantity) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, item.name());
            pstmt.setString(2, seller.getName());
            pstmt.setString(3, buyer.getName());
            pstmt.setDouble(4, price);
            pstmt.setInt(5, quantity);

            pstmt.executeUpdate();

            // Mettre à jour les statistiques quotidiennes
            updateDailyStats(item.name(), price, quantity);

            // Mettre à jour les limites quotidiennes du joueur
            updatePlayerDailyLimits(buyer.getUniqueId().toString(), item.name(), quantity, true);

        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement de la transaction : " + e.getMessage());
        }
    }

    /**
     * Met à jour les statistiques quotidiennes du shop
     * @param itemName Nom de l'item
     * @param price Prix de la transaction
     * @param quantity Quantité d'items
     */
    private void updateDailyStats(String itemName, double price, int quantity) {
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String query = "INSERT INTO shop_stats (stat_date, total_transactions, total_sales, most_bought_item) " +
                "VALUES (?, 1, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "total_transactions = total_transactions + 1, " +
                "total_sales = total_sales + ?, " +
                "most_bought_item = IF((SELECT COUNT(*) FROM transactions WHERE item_name = ? AND DATE(transaction_date) = ?) > " +
                "(SELECT COUNT(*) FROM transactions WHERE item_name = most_bought_item AND DATE(transaction_date) = ?), ?, most_bought_item)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            double totalPrice = price * quantity;

            pstmt.setString(1, date);
            pstmt.setDouble(2, totalPrice);
            pstmt.setString(3, itemName);
            pstmt.setDouble(4, totalPrice);
            pstmt.setString(5, itemName);
            pstmt.setString(6, date);
            pstmt.setString(7, date);
            pstmt.setString(8, itemName);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la mise à jour des statistiques quotidiennes : " + e.getMessage());
        }
    }

    /**
     * Met à jour les limites quotidiennes du joueur
     * @param playerUuid UUID du joueur
     * @param itemName Nom de l'item
     * @param quantity Quantité d'items
     * @param isBuy True si c'est un achat, False si c'est une vente
     */
    private void updatePlayerDailyLimits(String playerUuid, String itemName, int quantity, boolean isBuy) {
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String query = "INSERT INTO player_daily_transactions (player_uuid, item_name, buy_count, sell_count, date) " +
                "VALUES (?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "buy_count = buy_count + ?, " +
                "sell_count = sell_count + ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, playerUuid);
            pstmt.setString(2, itemName);
            pstmt.setInt(3, isBuy ? quantity : 0);
            pstmt.setInt(4, isBuy ? 0 : quantity);
            pstmt.setString(5, date);
            pstmt.setInt(6, isBuy ? quantity : 0);
            pstmt.setInt(7, isBuy ? 0 : quantity);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la mise à jour des limites quotidiennes : " + e.getMessage());
        }
    }

    /**
     * Enregistre une vente du joueur au serveur
     * @param item L'item vendu
     * @param seller Le vendeur
     * @param price Le prix total de la transaction
     * @param quantity La quantité d'items
     */
    public void recordSaleToServer(Material item, Player seller, double price, int quantity) {
        String query = "INSERT INTO transactions (item_name, seller_name, buyer_name, price, quantity) VALUES (?, ?, 'SERVER', ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, item.name());
            pstmt.setString(2, seller.getName());
            pstmt.setDouble(3, price);
            pstmt.setInt(4, quantity);

            pstmt.executeUpdate();

            // Mettre à jour les statistiques
            String date = new java.sql.Date(System.currentTimeMillis()).toString();
            String statsQuery = "INSERT INTO shop_stats (stat_date, total_transactions, total_sales, most_sold_item) " +
                    "VALUES (?, 1, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "total_transactions = total_transactions + 1, " +
                    "total_sales = total_sales + ?, " +
                    "most_sold_item = IF((SELECT COUNT(*) FROM transactions WHERE item_name = ? AND seller_name != 'SERVER' AND DATE(transaction_date) = ?) > " +
                    "(SELECT COUNT(*) FROM transactions WHERE item_name = most_sold_item AND seller_name != 'SERVER' AND DATE(transaction_date) = ?), ?, most_sold_item)";

            try (PreparedStatement statsPstmt = conn.prepareStatement(statsQuery)) {
                statsPstmt.setString(1, date);
                statsPstmt.setDouble(2, price);
                statsPstmt.setString(3, item.name());
                statsPstmt.setDouble(4, price);
                statsPstmt.setString(5, item.name());
                statsPstmt.setString(6, date);
                statsPstmt.setString(7, date);
                statsPstmt.setString(8, item.name());

                statsPstmt.executeUpdate();
            }

            // Mettre à jour les limites quotidiennes du joueur
            updatePlayerDailyLimits(seller.getUniqueId().toString(), item.name(), quantity, false);

        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'enregistrement de la vente au serveur : " + e.getMessage());
        }
    }

    /**
     * Vérifie si un joueur a atteint sa limite quotidienne d'achat/vente pour un item
     * @param player Le joueur
     * @param item L'item
     * @param isBuy True pour vérifier la limite d'achat, False pour la limite de vente
     * @return True si la limite est atteinte, False sinon
     */
    public boolean isLimitReached(Player player, Material item, boolean isBuy) {
        String date = new java.sql.Date(System.currentTimeMillis()).toString();
        String query = "SELECT " + (isBuy ? "buy_count" : "sell_count") + " AS count, " +
                "(SELECT " + (isBuy ? "daily_buy_limit" : "daily_sell_limit") + " FROM item_limits WHERE item_name = ?) AS limit " +
                "FROM player_daily_transactions " +
                "WHERE player_uuid = ? AND item_name = ? AND date = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setString(1, item.name());
            pstmt.setString(2, player.getUniqueId().toString());
            pstmt.setString(3, item.name());
            pstmt.setString(4, date);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("count");
                    int limit = rs.getInt("limit");

                    // -1 signifie pas de limite
                    return limit != -1 && count >= limit;
                }
                return false;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la vérification des limites quotidiennes : " + e.getMessage());
            return false;
        }
    }

    /*
     * Récupère les transactions récentes
     * @param limit Nombre maximal de transactions à récupérer
     * @return Liste des transactions
     */
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions ORDER BY transaction_date DESC LIMIT ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
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

    /*
     * Récupère les transactions d'un joueur spécifique
     * @param playerName Nom du joueur
     * @param limit Nombre maximal de transactions à récupérer
     * @return Liste des transactions
     */
    public List<Transaction> getPlayerTransactions(String playerName, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE seller_name = ? OR buyer_name = ? ORDER BY transaction_date DESC LIMIT ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, playerName);
            pstmt.setString(2, playerName);
            pstmt.setInt(3, limit);

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
            plugin.getLogger().severe("Erreur lors de la récupération des transactions du joueur : " + e.getMessage());
        }

        return transactions;
    }

    /**
     * Récupère les statistiques des ventes d'un item spécifique
     * @param itemName Nom de l'item
     * @param days Nombre de jours à inclure
     * @return Statistiques de l'item
     */
    public ItemStatistics getItemStatistics(String itemName, int days) {
        String query = "SELECT " +
                "COUNT(*) as total_transactions, " +
                "SUM(quantity) as total_quantity, " +
                "AVG(price / quantity) as average_price, " +
                "MAX(price / quantity) as max_price, " +
                "MIN(price / quantity) as min_price " +
                "FROM transactions " +
                "WHERE item_name = ? AND transaction_date >= DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, itemName);
            pstmt.setInt(2, days);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new ItemStatistics(
                            itemName,
                            rs.getInt("total_transactions"),
                            rs.getInt("total_quantity"),
                            rs.getDouble("average_price"),
                            rs.getDouble("max_price"),
                            rs.getDouble("min_price")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des statistiques de l'item : " + e.getMessage());
        }

        return new ItemStatistics(itemName, 0, 0, 0, 0, 0);
    }

    /**
     * Met à jour les prix des items dans la base de données depuis le fichier prices.yml
     */
    public void syncPricesFromConfig() {
        String query = "INSERT INTO item_prices (item_name, buy_price, sell_price) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE buy_price = VALUES(buy_price), sell_price = VALUES(sell_price)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            for (String itemKey : plugin.getPricesConfig().getConfigurationSection("items").getKeys(false)) {
                double buyPrice = plugin.getPricesConfig().getDouble("items." + itemKey + ".buy", 0);
                double sellPrice = plugin.getPricesConfig().getDouble("items." + itemKey + ".sell", 0);

                pstmt.setString(1, itemKey);
                pstmt.setDouble(2, buyPrice);
                pstmt.setDouble(3, sellPrice);
                pstmt.addBatch();
            }

            pstmt.executeBatch();
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la synchronisation des prix : " + e.getMessage());
        }
    }

    /**
     * Récupère toutes les catégories du shop
     * @return Liste des catégories
     */
    public List<ShopCategory> getAllCategories() {
        List<ShopCategory> categories = new ArrayList<>();
        String query = "SELECT * FROM shop_categories ORDER BY display_order ASC";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int categoryId = rs.getInt("id");
                ShopCategory category = new ShopCategory(
                        categoryId,
                        rs.getString("category_name"),
                        Material.valueOf(rs.getString("display_item")),
                        rs.getInt("display_order")
                );

                // Récupérer les items de cette catégorie
                String itemsQuery = "SELECT i.item_name FROM category_items i WHERE i.category_id = ?";
                try (PreparedStatement itemsPstmt = conn.prepareStatement(itemsQuery)) {
                    itemsPstmt.setInt(1, categoryId);
                    try (ResultSet itemsRs = itemsPstmt.executeQuery()) {
                        while (itemsRs.next()) {
                            category.addItem(Material.valueOf(itemsRs.getString("item_name")));
                        }
                    }
                }

                categories.add(category);
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de la récupération des catégories : " + e.getMessage());
        }

        return categories;
    }

    /**
     * Ajoute une nouvelle catégorie
     * @param name Nom de la catégorie
     * @param displayItem Item d'affichage
     * @param order Ordre d'affichage
     * @return ID de la catégorie créée, -1 en cas d'erreur
     */
    public int addCategory(String name, Material displayItem, int order) {
        String query = "INSERT INTO shop_categories (category_name, display_item, display_order) VALUES (?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, name);
            pstmt.setString(2, displayItem.name());
            pstmt.setInt(3, order);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'ajout d'une catégorie : " + e.getMessage());
        }

        return -1;
    }

    /**
     * Ajoute un item à une catégorie
     * @param categoryId ID de la catégorie
     * @param item Item à ajouter
     * @return true si l'ajout a réussi, false sinon
     */
    public boolean addItemToCategory(int categoryId, Material item) {
        String query = "INSERT INTO category_items (category_id, item_name) VALUES (?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, categoryId);
            pstmt.setString(2, item.name());

            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().severe("Erreur lors de l'ajout d'un item à une catégorie : " + e.getMessage());
            return false;
        }
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
     * Classe représentant les statistiques d'un item
     */
    public static class ItemStatistics {
        private String itemName;
        private int totalTransactions;
        private int totalQuantity;
        private double averagePrice;
        private double maxPrice;
        private double minPrice;

        public ItemStatistics(String itemName, int totalTransactions, int totalQuantity,
                              double averagePrice, double maxPrice, double minPrice) {
            this.itemName = itemName;
            this.totalTransactions = totalTransactions;
            this.totalQuantity = totalQuantity;
            this.averagePrice = averagePrice;
            this.maxPrice = maxPrice;
            this.minPrice = minPrice;
        }

        // Getters
        public String getItemName() { return itemName; }
        public int getTotalTransactions() { return totalTransactions; }
        public int getTotalQuantity() { return totalQuantity; }
        public double getAveragePrice() { return averagePrice; }
        public double getMaxPrice() { return maxPrice; }
        public double getMinPrice() { return minPrice; }
    }

    /**
     * Classe représentant une catégorie du shop
     */
    public static class ShopCategory {
        private int id;
        private String name;
        private Material displayItem;
        private int order;
        private List<Material> items;

        public ShopCategory(int id, String name, Material displayItem, int order) {
            this.id = id;
            this.name = name;
            this.displayItem = displayItem;
            this.order = order;
            this.items = new ArrayList<>();
        }

        public void addItem(Material item) {
            items.add(item);
        }

        // Getters
        public int getId() { return id; }
        public String getName() { return name; }
        public Material getDisplayItem() { return displayItem; }
        public int getOrder() { return order; }
        public List<Material> getItems() { return items; }
    }

    // Ferme la connexion à la base de données
    public void closeConnection() {
        dbManager.closeConnection();
    }
    /*
     * Exécute une requête SELECT et retourne le ResultSet
     * @param query La requête SQL à exécuter
     * @return Le ResultSet résultant
     * @throws SQLException En cas d'erreur SQL
     */
    public ResultSet executeQuery(String query) throws SQLException {
        Connection conn = dbManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        return stmt.executeQuery();
    }

    /*
     * Exécute une requête UPDATE, INSERT ou DELETE
     * @param query La requête SQL à exécuter
     * @return Le nombre de lignes affectées
     * @throws SQLException En cas d'erreur SQL
     */
    public int executeUpdate(String query) throws SQLException {
        Connection conn = dbManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(query);
        int result = stmt.executeUpdate();
        stmt.close();
        conn.close();
        return result;
    }
}