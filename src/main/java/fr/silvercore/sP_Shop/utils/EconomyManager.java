package fr.silvercore.sP_Shop.utils;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/*
 * Gestionnaire d'économie pour le plugin
 * @author SilverPlugins
 */
public class EconomyManager {

    private final SP_Shop plugin;
    private final File economyFile;
    private FileConfiguration economyConfig;
    private final Map<UUID, Integer> playerBalances = new HashMap<>();

    public EconomyManager(SP_Shop plugin) {
        this.plugin = plugin;
        this.economyFile = new File(plugin.getDataFolder(), "economy.yml");

        // Créer le fichier s'il n'existe pas
        if (!economyFile.exists()) {
            try {
                economyFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Impossible de créer le fichier economy.yml!");
                e.printStackTrace();
            }
        }

        this.economyConfig = YamlConfiguration.loadConfiguration(economyFile);
        loadBalances();
    }

    //Charge les soldes depuis le fichier de configuration
    private void loadBalances() {
        playerBalances.clear();
        if (economyConfig.contains("balances")) {
            for (String uuidString : economyConfig.getConfigurationSection("balances").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    int balance = economyConfig.getInt("balances." + uuidString);
                    playerBalances.put(uuid, balance);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("UUID invalide dans le fichier economy.yml: " + uuidString);
                }
            }
        }
    }

    //Sauvegarde les soldes dans le fichier de configuration
    public void saveBalances() {
        for (Map.Entry<UUID, Integer> entry : playerBalances.entrySet()) {
            economyConfig.set("balances." + entry.getKey().toString(), entry.getValue());
        }

        try {
            economyConfig.save(economyFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Impossible de sauvegarder le fichier economy.yml!");
            e.printStackTrace();
        }
    }

    /*
     * Récupère le solde d'un joueur
     * @param uuid UUID du joueur
     * @return Le solde du joueur, ou la valeur par défaut s'il n'existe pas
     */
    public int getMoney(UUID uuid) {
        return playerBalances.getOrDefault(uuid, plugin.getConfig().getInt("economy.starting-balance", 0));
    }

    /*
     * Définit le solde d'un joueur
     * @param uuid UUID du joueur
     * @param amount Nouveau solde
     */
    public void setMoney(UUID uuid, int amount) {
        playerBalances.put(uuid, amount);
        saveBalances();
    }

    /*
     * Ajoute de l'argent à un joueur
     * @param uuid UUID du joueur
     * @param amount Montant à ajouter
     */
    public void addMoney(UUID uuid, int amount) {
        int currentBalance = getMoney(uuid);
        setMoney(uuid, currentBalance + amount);
    }

    /*
     * Retire de l'argent à un joueur
     * @param uuid UUID du joueur
     * @param amount Montant à retirer
     * @return true si le joueur avait assez d'argent, false sinon
     */
    public boolean removeMoney(UUID uuid, int amount) {
        int currentBalance = getMoney(uuid);
        if (currentBalance >= amount) {
            setMoney(uuid, currentBalance - amount);
            return true;
        }
        return false;
    }

    /*
     * Vérifie si un joueur a suffisamment d'argent
     * @param uuid UUID du joueur
     * @param amount Montant à vérifier
     * @return true si le joueur a au moins le montant spécifié
     */
    public boolean hasMoney(UUID uuid, int amount) {
        return getMoney(uuid) >= amount;
    }

    //Recharge les données d'économie depuis le fichier
    public void reloadEconomyData() {
        economyConfig = YamlConfiguration.loadConfiguration(economyFile);
        loadBalances();
    }
}