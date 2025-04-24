/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.database;

import fr.silvercore.sP_Shop.SP_Shop;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseSetup {
    private final SP_Shop plugin;
    private final DatabaseManager dbManager;

    public DatabaseSetup(SP_Shop plugin, DatabaseManager dbManager) {
        this.plugin = plugin;
        this.dbManager = dbManager;
    }

    /**
     * Initialise toutes les tables nécessaires pour le plugin SP_Shop
     */
    public void initializeDatabase() {
        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            plugin.getLogger().log(Level.INFO, "[SP_Shop] Initialisation des tables de la base de données...");

            // Table des transactions
            stmt.execute("CREATE TABLE IF NOT EXISTS transactions (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "item_name VARCHAR(255) NOT NULL, " +
                    "seller_name VARCHAR(255) NOT NULL, " +
                    "buyer_name VARCHAR(255) NOT NULL, " +
                    "price DECIMAL(10,2) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            // Table des prix des items
            stmt.execute("CREATE TABLE IF NOT EXISTS item_prices (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "item_name VARCHAR(255) NOT NULL UNIQUE, " +
                    "buy_price DECIMAL(10,2) NOT NULL, " +
                    "sell_price DECIMAL(10,2) NOT NULL, " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            // Table des soldes des joueurs
            stmt.execute("CREATE TABLE IF NOT EXISTS player_balances (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "player_uuid VARCHAR(36) NOT NULL UNIQUE, " +
                    "player_name VARCHAR(255) NOT NULL, " +
                    "balance DECIMAL(10,2) NOT NULL DEFAULT 0.0, " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            // Table des offres spéciales
            stmt.execute("CREATE TABLE IF NOT EXISTS special_offers (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "item_name VARCHAR(255) NOT NULL, " +
                    "discount_percentage DECIMAL(5,2) NOT NULL, " +
                    "start_date TIMESTAMP NOT NULL, " +
                    "end_date TIMESTAMP NOT NULL, " +
                    "is_active BOOLEAN NOT NULL DEFAULT TRUE" +
                    ")");

            // Table des limites d'achat/vente
            stmt.execute("CREATE TABLE IF NOT EXISTS item_limits (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "item_name VARCHAR(255) NOT NULL, " +
                    "daily_buy_limit INT NOT NULL DEFAULT -1, " +
                    "daily_sell_limit INT NOT NULL DEFAULT -1, " +
                    "reset_time TIME DEFAULT '00:00:00'" +
                    ")");

            // Table pour suivre les achats/ventes quotidiens par joueur
            stmt.execute("CREATE TABLE IF NOT EXISTS player_daily_transactions (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "player_uuid VARCHAR(36) NOT NULL, " +
                    "item_name VARCHAR(255) NOT NULL, " +
                    "buy_count INT NOT NULL DEFAULT 0, " +
                    "sell_count INT NOT NULL DEFAULT 0, " +
                    "date DATE NOT NULL, " +
                    "UNIQUE KEY unique_player_item_date (player_uuid, item_name, date)" +
                    ")");

            // Table de configuration du plugin
            stmt.execute("CREATE TABLE IF NOT EXISTS plugin_config (" +
                    "config_key VARCHAR(255) PRIMARY KEY, " +
                    "config_value TEXT NOT NULL, " +
                    "last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                    ")");

            // Table des statistiques du shop
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_stats (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "stat_date DATE NOT NULL, " +
                    "total_transactions INT NOT NULL DEFAULT 0, " +
                    "total_sales DECIMAL(15,2) NOT NULL DEFAULT 0.0, " +
                    "most_sold_item VARCHAR(255), " +
                    "most_bought_item VARCHAR(255), " +
                    "UNIQUE KEY unique_stat_date (stat_date)" +
                    ")");

            // Table des catégories d'items pour le shop
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_categories (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "category_name VARCHAR(255) NOT NULL UNIQUE, " +
                    "display_item VARCHAR(255) NOT NULL, " +
                    "display_order INT NOT NULL DEFAULT 0" +
                    ")");

            // Table des items associés aux catégories
            stmt.execute("CREATE TABLE IF NOT EXISTS category_items (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "category_id INT NOT NULL, " +
                    "item_name VARCHAR(255) NOT NULL, " +
                    "FOREIGN KEY (category_id) REFERENCES shop_categories(id) ON DELETE CASCADE, " +
                    "UNIQUE KEY unique_category_item (category_id, item_name)" +
                    ")");

            // Table des permissions de shop
            stmt.execute("CREATE TABLE IF NOT EXISTS shop_permissions (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "permission_name VARCHAR(255) NOT NULL UNIQUE, " +
                    "items_allowed TEXT, " +
                    "discount_percentage DECIMAL(5,2) DEFAULT 0.0" +
                    ")");

            // Création des index pour optimiser les performances
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_seller ON transactions(seller_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_buyer ON transactions(buyer_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_item ON transactions(item_name)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_transactions_date ON transactions(transaction_date)");
            stmt.execute("CREATE INDEX IF NOT EXISTS idx_player_balances_name ON player_balances(player_name)");

            plugin.getLogger().log(Level.INFO, "[SP_Shop] Toutes les tables ont été initialisées avec succès!");

        } catch (SQLException e) {
            plugin.getLogger().severe("[SP_Shop] Erreur lors de l'initialisation de la base de données : " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Insère des données de test dans la base de données
     * À utiliser uniquement en environnement de développement
     */
    public void insertTestData() {
        if (!plugin.getConfig().getBoolean("database.insert_test_data", false)) {
            return;
        }

        try (Connection conn = dbManager.getConnection();
             Statement stmt = conn.createStatement()) {

            plugin.getLogger().log(Level.INFO, "[SP_Shop] Insertion de données de test...");

            // Insertion d'items et leurs prix
            stmt.execute("INSERT IGNORE INTO item_prices (item_name, buy_price, sell_price) VALUES " +
                    "('DIAMOND', 100.00, 80.00), " +
                    "('IRON_INGOT', 20.00, 15.00), " +
                    "('GOLD_INGOT', 50.00, 40.00), " +
                    "('EMERALD', 120.00, 100.00), " +
                    "('COAL', 5.00, 3.00)");

            // Insertion de catégories
            stmt.execute("INSERT IGNORE INTO shop_categories (category_name, display_item, display_order) VALUES " +
                    "('Minerais', 'DIAMOND_ORE', 1), " +
                    "('Nourriture', 'BREAD', 2), " +
                    "('Armes', 'DIAMOND_SWORD', 3), " +
                    "('Outils', 'DIAMOND_PICKAXE', 4), " +
                    "('Blocs', 'STONE', 5)");

            // Association des items aux catégories
            stmt.execute("INSERT IGNORE INTO category_items (category_id, item_name) " +
                    "SELECT 1, 'DIAMOND' FROM dual WHERE NOT EXISTS (SELECT 1 FROM category_items WHERE category_id = 1 AND item_name = 'DIAMOND')");
            stmt.execute("INSERT IGNORE INTO category_items (category_id, item_name) " +
                    "SELECT 1, 'IRON_INGOT' FROM dual WHERE NOT EXISTS (SELECT 1 FROM category_items WHERE category_id = 1 AND item_name = 'IRON_INGOT')");
            stmt.execute("INSERT IGNORE INTO category_items (category_id, item_name) " +
                    "SELECT 1, 'GOLD_INGOT' FROM dual WHERE NOT EXISTS (SELECT 1 FROM category_items WHERE category_id = 1 AND item_name = 'GOLD_INGOT')");
            stmt.execute("INSERT IGNORE INTO category_items (category_id, item_name) " +
                    "SELECT 1, 'EMERALD' FROM dual WHERE NOT EXISTS (SELECT 1 FROM category_items WHERE category_id = 1 AND item_name = 'EMERALD')");
            stmt.execute("INSERT IGNORE INTO category_items (category_id, item_name) " +
                    "SELECT 1, 'COAL' FROM dual WHERE NOT EXISTS (SELECT 1 FROM category_items WHERE category_id = 1 AND item_name = 'COAL')");

            plugin.getLogger().log(Level.INFO, "[SP_Shop] Données de test insérées avec succès!");

        } catch (SQLException e) {
            plugin.getLogger().severe("[SP_Shop] Erreur lors de l'insertion des données de test : " + e.getMessage());
            e.printStackTrace();
        }
    }
}