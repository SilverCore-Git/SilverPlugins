/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author Netsu_ - main
 * @author JemY5
 */
package fr.silvercore.sP_Shop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import fr.silvercore.sP_Shop.commands.CommandSell;
import fr.silvercore.sP_Shop.commands.CommandShop;
import fr.silvercore.sP_Shop.listeners.InventoryListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class SP_Shop extends JavaPlugin {

    // Fichier de configuration pour les prix des items de la boutique
    private File pricesFile;
    private FileConfiguration pricesConfig;
    private static SP_Shop instance;

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(Level.INFO, "SilverPlugins : SP_Shop a démarré avec succès (le frère de réussite).");
        instance = this;
        // Créer la configuration par défaut du plugin
        this.saveDefaultConfig();
        // Créer et charger la configuration des prix
        this.createPricesConfig();
        // Enregistrer les commandes
        getCommand("shop").setExecutor(new CommandShop());
        getCommand("sell").setExecutor(new CommandSell());
        // Enregistrer les listeners
        getServer().getPluginManager().registerEvents(new InventoryListener(), this);
    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(Level.INFO, "SilverPlugins : SP_Shop a désactivé avec réussite (le frère de succès).");
        // Sauvegarder la configuration des prix si elle a été modifiée
        savePricesConfig();
    }

    //permet de return les valeurs
    public static SP_Shop getInstance() {
        return instance;
    }

    // Méthode pour créer ou charger le fichier de configuration des prix
    public void createPricesConfig() {
        pricesFile = new File(getDataFolder(), "prices.yml");
        if (!pricesFile.exists()) {
            // Créer le dossier du plugin s'il n'existe pas
            pricesFile.getParentFile().mkdirs();

            // Sauvegarder le fichier de configuration par défaut s'il existe dans les ressources
            saveResource("prices.yml", false);
        }
        // Si le fichier not found dans les ressources, créer un fichier vide
        if (!pricesFile.exists()) {
            try {
                pricesFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Impossible de créer le fichier prices.yml!");
                e.printStackTrace();
            }
        }

        pricesConfig = YamlConfiguration.loadConfiguration(pricesFile);

        // Ajouter des valeurs par défaut si le fichier est vide
        if (pricesConfig.getKeys(false).isEmpty()) {
            pricesConfig.set("items.DIAMOND.buy", 100);
            pricesConfig.set("items.DIAMOND.sell", 80);
            pricesConfig.set("items.IRON_INGOT.buy", 20);
            pricesConfig.set("items.IRON_INGOT.sell", 15);
            savePricesConfig();
        }
    }

    public void savePricesConfig() {
        try {
            pricesConfig.save(pricesFile);
        } catch (IOException e) {
            getLogger().severe("Impossible de sauvegarder le fichier prices.yml!");
            e.printStackTrace();
        }
    }

    // Méthode pour accéder à la configuration des prix
    public FileConfiguration getPricesConfig() {
        return pricesConfig;
    }
}