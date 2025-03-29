package fr.silvercore.sp_velocitysync;

import org.slf4j.Logger;
import redis.clients.jedis.Jedis;

import java.util.UUID;

public class RedisService {
    private final Logger logger;
    private Jedis jedis;

    public RedisService(Logger logger) {
        this.logger = logger;
        this.jedis = new Jedis("localhost");  // Assurez-vous d'utiliser la bonne adresse Redis
    }

    public void savePlayerData(UUID playerUUID, PlayerData playerData) {
        try {
            // Convertir l'objet PlayerData en JSON ou un format adapté pour Redis
            String key = "player_data:" + playerUUID.toString();
            String value = playerData.toJson();  // Méthode à ajouter dans PlayerData pour convertir l'objet en JSON

            jedis.set(key, value);
            logger.info("Données du joueur sauvegardées pour UUID: " + playerUUID);
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde des données dans Redis pour UUID: " + playerUUID, e);
        }
    }
}
