package fr.silvercore.sp_velocitysync;

import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;

import java.util.Set;
import java.util.stream.Collectors;

public class Sync_op {

    // class comment, require luckperm

//    private final ProxyServer proxy;
//    private final Logger logger;
//
//    public Sync_op(ProxyServer proxy, Logger logger) {
//        this.proxy = proxy;
//        this.logger = logger;
//    }
//
//    // Méthode pour synchroniser les opérateurs sur tous les serveurs
//    public void syncOpOnAllServers() {
//        // On récupère tous les joueurs opérateurs en vérifiant la permission "minecraft.command.op"
//        Set<Player> ops = proxy.getAllPlayers().stream()
//                .filter(player -> player.hasPermission("minecraft.command.op")) // Vérifie si le joueur a la permission OP
//                .collect(Collectors.toSet());
//
//        // Application des opérateurs à tous les serveurs
//        for (RegisteredServer server : proxy.getAllServers()) {
//            // On parcourt chaque serveur et chaque joueur OP
//            for (Player opPlayer : ops) {
//                // Synchronisation de l'OP sur chaque serveur
//                server.getPlayersConnected().forEach(player -> {
//                    // Si le joueur n'est pas déjà OP, on le rend OP
//                    if (!player.hasPermission("minecraft.command.op")) {
//                        player.setPermission("minecraft.command.op", true); // On applique l'OP => require luckperm
//                        logger.info("Le joueur " + opPlayer.getUsername() + " est maintenant OP sur le serveur " + server.getServerInfo().getName());
//                    }
//                });
//            }
//        }
//    }
}
