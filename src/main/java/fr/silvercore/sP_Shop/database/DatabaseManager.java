/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.database;

import fr.silvercore.sP_Shop.SP_Shop;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;

public class DatabaseManager {

    private SP_Shop plugin;
    private Connection connection;
    private String dbType;
    private String host;
    private int port;
    private String database;
    private String username;
    private String password;
    private DatabaseSetup databaseSetup;

    public DatabaseManager(SP_Shop plugin) {
        this.plugin = plugin;

        // Essayer de charger depuis les propriétés .env déjà chargées dans SP_Shop
        Properties envProperties = plugin.getEnvProperties();

        if (envProperties != null && !envProperties.isEmpty()) {
            // Charger les paramètres depuis le fichier .env
            this.loadFromEnvProperties(envProperties);
        } else {
            // Si les propriétés ne sont pas disponibles, charger depuis la config
            this.loadConfig();
        }

        plugin.getLogger().log(Level.INFO, "[SP_Shop] Configuration base de données chargée: " +
                dbType + " sur " + host + ":" + port);

        // Initialiser le setup de la base de données
        this.databaseSetup = new DatabaseSetup(plugin, this);
        this.initialize();
    }

    /**
     * Charge les informations de connexion depuis les propriétés .env
     * @param envProperties Les propriétés chargées du fichier .env
     */
    private void loadFromEnvProperties(Properties envProperties) {
        this.dbType = envProperties.getProperty("DB_TYPE", "mysql");
        this.host = envProperties.getProperty("DB_HOST", "localhost");
        this.port = Integer.parseInt(envProperties.getProperty("DB_PORT", "3306"));
        this.database = envProperties.getProperty("DB_DBNAME", "shop_db");
        this.username = envProperties.getProperty("DB_USER", "root");
        this.password = envProperties.getProperty("DB_PASSWD", "");
    }

    private void loadConfig() {
        // Chargement des informations de connexion depuis la configuration
        this.dbType = plugin.getConfig().getString("database.type", "mysql");
        this.host = plugin.getConfig().getString("database.host", "localhost");
        this.port = plugin.getConfig().getInt("database.port", 3306);
        this.database = plugin.getConfig().getString("database.name", "shop_db");
        this.username = plugin.getConfig().getString("database.username", "root");
        this.password = plugin.getConfig().getString("database.password", "");
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        switch (dbType.toLowerCase()) {
            case "mysql":
                connection = DriverManager.getConnection(
                        "jdbc:mysql://" + host + ":" + port + "/" + database +
                                "?useSSL=false&autoReconnect=true&useUnicode=true&characterEncoding=UTF-8",
                        username, password);
                break;
            case "postgresql":
                connection = DriverManager.getConnection(
                        "jdbc:postgresql://" + host + ":" + port + "/" + database,
                        username, password);
                break;
            case "sqlite":
            default:
                connection = DriverManager.getConnection(
                        "jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + "/" + database + ".db");
                break;
        }

        plugin.getLogger().log(Level.INFO, "[SP_Shop] Connexion établie avec la base de données " + dbType);
        return connection;
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().log(Level.INFO, "[SP_Shop] Connexion à la base de données fermée");
            } catch (SQLException e) {
                plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors de la fermeture de la connexion", e);
            }
        }
    }

    public String getDbType() {
        return this.dbType;
    }

    public void initialize() {
        try {
            // Vérifier que la connexion fonctionne
            getConnection();

            // Initialiser la structure de la base de données
            databaseSetup.initializeDatabase();

            // Insérer des données de test si configuré pour le faire
            if (plugin.getConfig().getBoolean("database.insert_test_data", false)) {
                databaseSetup.insertTestData();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "[SP_Shop] Erreur lors de l'initialisation de la base de données", e);
        }
    }
}