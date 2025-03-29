package fr.silvercore.sp_velocitysync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.Player;

import java.util.concurrent.TimeUnit;
import fr.silvercore.sp_velocitysync.Sync_op;

@Plugin(
        id = "sp_velocitysync",
        name = "sp_velocitysync",
        version = BuildConstants.VERSION
)
public class Sp_velocitysync {

    @Inject
    private Logger logger;

    @Inject
    private ProxyServer proxy;

    private final Sync_op syncOp;  // Nom de la variable syncOp en camelCase

    @Inject
    public Sp_velocitysync(ProxyServer proxy, Logger logger, Sync_op syncOp) {
        this.proxy = proxy;
        this.logger = logger;
        this.syncOp = syncOp;  // Injection de Sync_op
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        proxy.getScheduler().buildTask(this, this::logServerAndPlayerInfo)
                .repeat(10, TimeUnit.SECONDS)
                .schedule();

        // Synchronisation des opérateurs lors de l'initialisation du proxy ( a revoir)
        // syncOp.syncOpOnAllServers();  // Appel de la méthode syncOpOnAllServers
    }

    private void logServerAndPlayerInfo() {
        logger.info("=== Serveurs enregistrés ===");

        for (RegisteredServer server : proxy.getAllServers()) {
            ServerInfo info = server.getServerInfo();
            logger.info("Nom: " + info.getName());
            logger.info("Adresse: " + info.getAddress());
        }

        logger.info("=== Joueurs connectés ===");

        for (Player player : proxy.getAllPlayers()) {
            logger.info("Pseudo: " + player.getUsername());
            logger.info("UUID: " + player.getUniqueId());
            logger.info("Serveur actuel: " + (player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Aucun"));
        }
    }
}
