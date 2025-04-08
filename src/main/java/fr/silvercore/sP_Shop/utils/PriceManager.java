/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.utils;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


// Gestionnaire de prix avec système de cache pour améliorer les performances
public class PriceManager {
    private SP_Shop plugin;
    private Map<Material, ItemPrice> priceCache = new HashMap<>();
    private boolean isDirty = false; // Indique si le cache a été modifié et doit être sauvegardé

     //Classe interne pour stocker les prix d'achat et de vente
    public static class ItemPrice {
        private int buyPrice;
        private int sellPrice;

        public ItemPrice(int buyPrice, int sellPrice) {
            this.buyPrice = buyPrice;
            this.sellPrice = sellPrice;
        }

        public int getBuyPrice() {
            return buyPrice;
        }

        public int getSellPrice() {
            return sellPrice;
        }

        public void setBuyPrice(int buyPrice) {
            this.buyPrice = buyPrice;
        }

        public void setSellPrice(int sellPrice) {
            this.sellPrice = sellPrice;
        }
    }

    public PriceManager(SP_Shop plugin) {
        this.plugin = plugin;
        loadPrices();
    }

    //Charge tous les prix depuis la configuration dans le cache

    public void loadPrices() {
        priceCache.clear();
        ConfigurationSection itemsSection = plugin.getPricesConfig().getConfigurationSection("items");

        if (itemsSection != null) {
            for (String materialName : itemsSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(materialName);
                    int buyPrice = itemsSection.getInt(materialName + ".buy", 0);
                    int sellPrice = itemsSection.getInt(materialName + ".sell", 0);

                    priceCache.put(material, new ItemPrice(buyPrice, sellPrice));
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[SP_Shop] : Matériel inconnu dans la configuration: " + materialName);
                }
            }
        }

        isDirty = false;
    }

    /**
     * Sauvegarde les prix du cache dans le fichier de configuration
     */
    public void savePrices() {
        if (!isDirty) {
            return; // Ne sauvegarde que si des modifications ont été faites
        }

        // Réinitialiser la section des items
        plugin.getPricesConfig().set("items", null);

        // Sauvegarder chaque prix du cache
        for (Map.Entry<Material, ItemPrice> entry : priceCache.entrySet()) {
            Material material = entry.getKey();
            ItemPrice price = entry.getValue();

            plugin.getPricesConfig().set("items." + material.name() + ".buy", price.getBuyPrice());
            plugin.getPricesConfig().set("items." + material.name() + ".sell", price.getSellPrice());
        }

        // Sauvegarder le fichier
        plugin.savePricesConfig();
        isDirty = false;
    }

    /**
     * Obtenir le prix d'achat d'un item
     */
    public int getBuyPrice(Material material) {
        ItemPrice price = priceCache.get(material);
        return price != null ? price.getBuyPrice() : -1;
    }

    /**
     * Obtenir le prix de vente d'un item
     */
    public int getSellPrice(Material material) {
        ItemPrice price = priceCache.get(material);
        return price != null ? price.getSellPrice() : -1;
    }

    /**
     * Définir le prix d'achat d'un item
     */
    public void setBuyPrice(Material material, int price) {
        ItemPrice itemPrice = priceCache.get(material);

        if (itemPrice == null) {
            itemPrice = new ItemPrice(price, 0);
            priceCache.put(material, itemPrice);
        } else {
            itemPrice.setBuyPrice(price);
        }

        isDirty = true;
    }

    /**
     * Définir le prix de vente d'un item
     */
    public void setSellPrice(Material material, int price) {
        ItemPrice itemPrice = priceCache.get(material);

        if (itemPrice == null) {
            itemPrice = new ItemPrice(0, price);
            priceCache.put(material, itemPrice);
        } else {
            itemPrice.setSellPrice(price);
        }

        isDirty = true;
    }

    /**
     * Ajouter un nouvel item avec ses prix
     */
    public void addItem(Material material, int buyPrice, int sellPrice) {
        priceCache.put(material, new ItemPrice(buyPrice, sellPrice));
        isDirty = true;
    }

    /**
     * Supprimer un item du shop
     */
    public void removeItem(Material material) {
        priceCache.remove(material);
        isDirty = true;
    }

    /**
     * Vérifier si un item est dans le shop
     */
    public boolean hasItem(Material material) {
        return priceCache.containsKey(material);
    }

    /**
     * Obtenir tous les matériaux disponibles dans le shop
     */
    public Set<Material> getAvailableItems() {
        return priceCache.keySet();
    }
}