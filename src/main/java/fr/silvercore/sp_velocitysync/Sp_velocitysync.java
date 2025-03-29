package fr.silvercore.sp_velocitysync;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import org.slf4j.Logger;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.Player;

import java.util.concurrent.TimeUnit;
import java.util.Optional;


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

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) { // function éxécuter lors du lancement du proxy

        proxy.getScheduler().buildTask(this, this::logServerAndPlayerInfo) // créer la tache : logServerAndPlayerInfo

                .repeat(10, TimeUnit.SECONDS) // la tache doit s'éxécuter toute les 10 en seconde
                .schedule();  // lance la planification de la tache

    }


    private void logServerAndPlayerInfo() {  // function appeler par onProxyInitialization

        logger.info("=== Serveurs enregistrés ==="); // "console.log"

        for (RegisteredServer server : proxy.getAllServers()) { // récupération des servers

            ServerInfo info = server.getServerInfo(); // récupération des info des servers


            logger.info("Nom: " + info.getName()); // log server info ...
            logger.info("Adresse: " + info.getAddress()); // ...

        }

        logger.info("=== Joueurs connectés ==="); // "console.log"

        for (Player player : proxy.getAllPlayers()) { // récupération des info des joueurs de tout les server

            logger.info("Pseudo: " + player.getUsername()); // log players info
            logger.info("UUID: " + player.getUniqueId()); // ...
            logger.info("Serveur actuel: " + (player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Aucun")); // ...

        }
    }
}
