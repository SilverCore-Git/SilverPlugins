/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.utils;
import fr.silvercore.sP_Shop.SP_Shop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;


//Utilitaire pour gérer le fichier .env du plugin
public class EnvFileCreator {

    private SP_Shop plugin;

    public EnvFileCreator(SP_Shop plugin) {
        this.plugin = plugin;
    }

    /*
     * Vérifie si le fichier .env existe et le crée avec des valeurs par défaut si ce n'est pas le cas
     * @return Les propriétés chargées du fichier .env
     */
    public Properties createOrLoadEnvFile() {
        File envFile = new File(plugin.getDataFolder(), ".env");
        Properties envProperties = new Properties();

        // Vérifie si le répertoire du plugin existe, sinon le crée
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
            plugin.getLogger().log(Level.INFO, "[SP_Shop] Création du répertoire du plugin");
        }

        // Si le fichier .env existe, le charger
        if (envFile.exists()) {
            try (FileInputStream fis = new FileInputStream(envFile)) {
                envProperties.load(fis);
                plugin.getLogger().log(Level.INFO, "[SP_Shop] Fichier .env chargé avec succès");
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "[SP_Shop] Erreur lors du chargement du fichier .env: " + e.getMessage());
            }
        } else {
            // Créer un fichier .env par défaut
            try {
                // Récupération des valeurs de configuration du plugin pour les paramètres par défaut
                String dbType = plugin.getConfig().getString("database.type", "mysql");
                String host = plugin.getConfig().getString("database.host", "localhost");
                String port = String.valueOf(plugin.getConfig().getInt("database.port", 3306));
                String database = plugin.getConfig().getString("database.name", "silverplugin");
                String username = plugin.getConfig().getString("database.username", "USER");
                String password = plugin.getConfig().getString("database.password", "passwd");

                if (envFile.createNewFile()) {
                    envProperties.setProperty("DB_TYPE", dbType);
                    envProperties.setProperty("DB_HOST", host);
                    envProperties.setProperty("DB_PORT", port);
                    envProperties.setProperty("DB_DBNAME", database);
                    envProperties.setProperty("DB_USER", username);
                    envProperties.setProperty("DB_PASSWD", password);

                    try (FileOutputStream fos = new FileOutputStream(envFile)) {
                        envProperties.store(fos, "Configuration de la base de données SP_Shop");
                        plugin.getLogger().log(Level.INFO, "[SP_Shop] Fichier .env créé avec la configuration par défaut");
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors de la création du fichier .env: " + e.getMessage());
            }
        }

        return envProperties;
    }

    /*
     * Met à jour le fichier .env avec les nouvelles valeurs
     * @param properties Les propriétés à stocker dans le fichier
     * @return true si la mise à jour a réussi, false sinon
     */
    public boolean updateEnvFile(Properties properties) {
        File envFile = new File(plugin.getDataFolder(), ".env");

        try (FileOutputStream fos = new FileOutputStream(envFile)) {
            properties.store(fos, "Configuration de la base de données SP_Shop - Mise à jour");
            plugin.getLogger().log(Level.INFO, "[SP_Shop] Fichier .env mis à jour avec succès");
            return true;
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors de la mise à jour du fichier .env: " + e.getMessage());
            return false;
        }
    }
}