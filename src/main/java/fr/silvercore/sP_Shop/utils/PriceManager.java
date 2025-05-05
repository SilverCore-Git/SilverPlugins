/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.utils;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class PriceManager {

    private final SP_Shop plugin;
    private final Map<Material, Price> pricesCache;
    private boolean isDirty = false;

    public PriceManager(SP_Shop plugin) {
        this.plugin = plugin;
        this.pricesCache = new ConcurrentHashMap<>();
        this.loadPrices();
    }


    //Charge les prix depuis la configuration et la base de données
    private void loadPrices() {
        plugin.getLogger().log(Level.INFO, "[SP_Shop] Chargement des prix des items...");

        // D'abord, charger depuis le fichier
        loadPricesFromConfig();

        // Ensuite, synchroniser avec la base de données (les prix de la BD ont priorité)
        loadPricesFromDatabase();
    }

    //Charge les prix depuis le fichier de configuration

    private void loadPricesFromConfig() {
        FileConfiguration config = plugin.getPricesConfig();
        if (config.contains("items")) {
            for (String itemKey : config.getConfigurationSection("items").getKeys(false)) {
                try {
                    Material material = Material.valueOf(itemKey);
                    double buyPrice = config.getDouble("items." + itemKey + ".buy", 0);
                    double sellPrice = config.getDouble("items." + itemKey + ".sell", 0);

                    pricesCache.put(material, new Price(buyPrice, sellPrice));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "[SP_Shop] Item invalide dans la configuration: " + itemKey);
                }
            }
        }
    }
    //Charge les prix depuis la base de données

    private void loadPricesFromDatabase() {
        String query = "SELECT item_name, buy_price, sell_price FROM item_prices";

        try {
            // Utilisons directement la classe qui gère les requêtes SQL
            ResultSet rs = plugin.getTransactionDatabase().executeQuery(query);

            while (rs.next()) {
                try {
                    String itemName = rs.getString("item_name");
                    double buyPrice = rs.getDouble("buy_price");
                    double sellPrice = rs.getDouble("sell_price");

                    Material material = Material.valueOf(itemName);
                    pricesCache.put(material, new Price(buyPrice, sellPrice));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().log(Level.WARNING, "[SP_Shop] Item invalide dans la base de données: " + rs.getString("item_name"));
                }
            }
            rs.close();

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors du chargement des prix depuis la base de données", e);
        }
    }

    /*
     * Sauvegarde les prix dans la configuration et la base de données
     */
    public void savePrices() {
        if (!isDirty) {
            return; // Rien à sauvegarder
        }

        plugin.getLogger().log(Level.INFO, "[SP_Shop] Sauvegarde des prix des items...");

        // Sauvegarder dans le fichier de configuration
        FileConfiguration config = plugin.getPricesConfig();
        for (Map.Entry<Material, Price> entry : pricesCache.entrySet()) {
            Material material = entry.getKey();
            Price price = entry.getValue();

            config.set("items." + material.name() + ".buy", price.getBuyPrice());
            config.set("items." + material.name() + ".sell", price.getSellPrice());
        }
        plugin.savePricesConfig();

        // Synchroniser avec la base de données
        synchronizeWithDatabase();

        isDirty = false;
    }

    /*
     * Synchronise les prix avec la base de données
     */
    private void synchronizeWithDatabase() {
        // On doit passer par le TransactionDatabaseManager pour exécuter notre batch
        for (Map.Entry<Material, Price> entry : pricesCache.entrySet()) {
            Material material = entry.getKey();
            Price price = entry.getValue();

            String query = "INSERT INTO item_prices (item_name, buy_price, sell_price) VALUES ('" +
                    material.name() + "', " + price.getBuyPrice() + ", " + price.getSellPrice() + ") " +
                    "ON DUPLICATE KEY UPDATE buy_price = VALUES(buy_price), sell_price = VALUES(sell_price)";

            try {
                plugin.getTransactionDatabase().executeUpdate(query);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors de la synchronisation du prix pour " + material.name(), e);
            }
        }
    }

    /*
     * Définit le prix d'achat d'un item
     * @param material Le matériau (item)
     * @param price Le prix d'achat
     */
    public void setBuyPrice(Material material, double price) {
        Price itemPrice = pricesCache.getOrDefault(material, new Price(0, 0));
        itemPrice.setBuyPrice(price);
        pricesCache.put(material, itemPrice);
        isDirty = true;
    }

    /*
     * Définit le prix de vente d'un item
     * @param material Le matériau (item)
     * @param price Le prix de vente
     */
    public void setSellPrice(Material material, double price) {
        Price itemPrice = pricesCache.getOrDefault(material, new Price(0, 0));
        itemPrice.setSellPrice(price);
        pricesCache.put(material, itemPrice);
        isDirty = true;
    }

    /*
     * Obtient le prix d'achat d'un item
     * @param material Le matériau (item)
     * @return Le prix d'achat
     */
    public double getBuyPrice(Material material) {
        Price price = pricesCache.get(material);
        return price != null ? price.getBuyPrice() : 0;
    }

    /*
     * Obtient le prix de vente d'un item
     * @param material Le matériau (item)
     * @return Le prix de vente
     */
    public double getSellPrice(Material material) {
        Price price = pricesCache.get(material);
        return price != null ? price.getSellPrice() : 0;
    }

    /*
     * Vérifie si un item a un prix défini
     * @param material Le matériau (item)
     * @return true si l'item a un prix, false sinon
     */
    public boolean hasPrice(Material material) {
        return pricesCache.containsKey(material);
    }

    /*
     * Supprime le prix d'un item
     * @param material Le matériau (item)
     */
    public void removePrice(Material material) {
        if (pricesCache.remove(material) != null) {
            isDirty = true;

            // Supprimer également de la base de données
            String query = "DELETE FROM item_prices WHERE item_name = '" + material.name() + "'";

            try {
                plugin.getTransactionDatabase().executeUpdate(query);
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors de la suppression du prix de la base de données", e);
            }
        }
    }

    /*
     * Obtient tous les items avec leurs prix
     * @return Map contenant tous les items et leurs prix
     */
    public Map<Material, Price> getAllPrices() {
        return new HashMap<>(pricesCache);
    }

    /**
     * Classe représentant le prix d'un item
     */
    public static class Price {
        private double buyPrice;
        private double sellPrice;

        public Price(double buyPrice, double sellPrice) {
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }

        public double getBuyPrice() {
            return buyPrice;
        }

        public void setBuyPrice(double buyPrice) {
            this.buyPrice = buyPrice;
        }

        public double getSellPrice() {
            return sellPrice;
        }

        public void setSellPrice(double sellPrice) {
            this.sellPrice = sellPrice;
        }
    }
}