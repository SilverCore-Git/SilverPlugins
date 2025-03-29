package fr.silvercore.sp_velocitysync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.event.player.PlayerJoinEvent;
import redis.clients.jedis.Jedis;
import org.slf4j.Logger;

import java.util.UUID;

@Plugin(
        id = "sp_velocitysync",
        name = "sp_velocitysync",
        version = BuildConstants.VERSION,
        authors = {"SilverCore", "SilverPlugins", "MisterPapaye"}
)
public class Sp_velocitysync {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    private RedisService redisService;

    @Inject
    public Sp_velocitysync(ProxyServer proxy, Logger logger) {
        this.proxy = proxy;
        this.logger = logger;
        this.redisService = new RedisService(logger);
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Plugin Sp_velocitysync initialisé !");
    }

    @Subscribe
    public void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Créer un objet PlayerData avec l'UUID, le nom du joueur, l'inv et l'armur
        PlayerData playerData = new PlayerData(
                player.getUsername(), // récupération des data user
                player.getInventory().getContents(),
                player.getEquipment().getHelmet()
        );

        // Sauvegarder ces données dans Redis
        redisService.savePlayerData(playerUUID, playerData);
    }
}
