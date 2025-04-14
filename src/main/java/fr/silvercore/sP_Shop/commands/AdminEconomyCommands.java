package fr.silvercore.sP_Shop.commands;

import fr.silvercore.sP_Shop.SP_Shop;
import fr.silvercore.sP_Shop.utils.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Gère les commandes administratives liées à l'économie
 * @author SilverPlugins
 */
public class AdminEconomyCommands implements CommandExecutor, TabCompleter {

    private final SP_Shop plugin;
    private final EconomyManager economyManager;

    public AdminEconomyCommands(SP_Shop plugin) {
        this.plugin = plugin;
        this.economyManager = plugin.getEconomyManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Vérifier les permissions
        if (!sender.hasPermission("sp_shop.admin")) {
            sender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande.");
            return true;
        }

        // Vérifier qu'il y a au moins un argument
        if (args.length < 1) {
            sendHelpMessage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "add":
                return handleAddMoney(sender, args);
            case "set":
                return handleSetMoney(sender, args);
            case "remove":
                return handleRemoveMoney(sender, args);
            case "check":
                return handleCheckMoney(sender, args);
            case "reset":
                return handleResetMoney(sender, args);
            case "help":
            default:
                sendHelpMessage(sender);
                return true;
        }
    }

    /**
     * Gère la commande pour ajouter de l'argent à un joueur
     */
    private boolean handleAddMoney(CommandSender sender, String[] args) {
        // Usage: /eco add <player> <amount>
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco add <joueur> <montant>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        // Vérifier si le joueur existe
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " n'est pas en ligne.");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);

            // Vérifier que le montant est positif
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Le montant doit être supérieur à 0.");
                return true;
            }

            // Ajouter l'argent
            economyManager.addMoney(target.getUniqueId(), amount);

            // Messages de confirmation
            sender.sendMessage(ChatColor.GREEN + "Vous avez ajouté " + amount + " à " + target.getName() + ".");
            target.sendMessage(ChatColor.GREEN + "Un administrateur vous a donné " + amount + ".");

            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Le montant doit être un nombre valide.");
            return true;
        }
    }

    /**
     * Gère la commande pour définir l'argent d'un joueur
     */
    private boolean handleSetMoney(CommandSender sender, String[] args) {
        // Usage: /eco set <player> <amount>
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco set <joueur> <montant>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        // Vérifier si le joueur existe
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " n'est pas en ligne.");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);

            // Vérifier que le montant est positif
            if (amount < 0) {
                sender.sendMessage(ChatColor.RED + "Le montant ne peut pas être négatif.");
                return true;
            }

            // Définir l'argent
            economyManager.setMoney(target.getUniqueId(), amount);

            // Messages de confirmation
            sender.sendMessage(ChatColor.GREEN + "Vous avez défini le solde de " + target.getName() + " à " + amount + ".");
            target.sendMessage(ChatColor.YELLOW + "Un administrateur a défini votre solde à " + amount + ".");

            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Le montant doit être un nombre valide.");
            return true;
        }
    }

    /**
     * Gère la commande pour retirer de l'argent à un joueur
     */
    private boolean handleRemoveMoney(CommandSender sender, String[] args) {
        // Usage: /eco remove <player> <amount>
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco remove <joueur> <montant>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        // Vérifier si le joueur existe
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " n'est pas en ligne.");
            return true;
        }

        try {
            int amount = Integer.parseInt(args[2]);

            // Vérifier que le montant est positif
            if (amount <= 0) {
                sender.sendMessage(ChatColor.RED + "Le montant doit être supérieur à 0.");
                return true;
            }

            int currentBalance = economyManager.getMoney(target.getUniqueId());

            // Vérifier si le joueur a assez d'argent
            if (currentBalance < amount) {
                sender.sendMessage(ChatColor.RED + target.getName() + " ne possède que " + currentBalance +
                        " et ne peut pas perdre " + amount + ".");
                return true;
            }

            // Retirer l'argent
            economyManager.removeMoney(target.getUniqueId(), amount);

            // Messages de confirmation
            sender.sendMessage(ChatColor.GREEN + "Vous avez retiré " + amount + " à " + target.getName() + ".");
            target.sendMessage(ChatColor.RED + "Un administrateur vous a retiré " + amount + ".");

            return true;
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Le montant doit être un nombre valide.");
            return true;
        }
    }

    /**
     * Gère la commande pour vérifier le solde d'un joueur
     */
    private boolean handleCheckMoney(CommandSender sender, String[] args) {
        // Usage: /eco check <player>
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco check <joueur>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        // Vérifier si le joueur existe
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " n'est pas en ligne.");
            return true;
        }

        // Obtenir et afficher le solde
        int balance = economyManager.getMoney(target.getUniqueId());
        sender.sendMessage(ChatColor.YELLOW + "Solde de " + target.getName() + ": " + ChatColor.GREEN + balance);
        return true;
    }

    /**
     * Gère la commande pour réinitialiser l'argent d'un joueur
     */
    private boolean handleResetMoney(CommandSender sender, String[] args) {
        // Usage: /eco reset <player>
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /eco reset <joueur>");
            return true;
        }

        String playerName = args[1];
        Player target = Bukkit.getPlayer(playerName);

        // Vérifier si le joueur existe
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Le joueur " + playerName + " n'est pas en ligne.");
            return true;
        }

        // Définir le solde du joueur à la valeur par défaut (à adapter selon votre système)
        int defaultAmount = plugin.getConfig().getInt("economy.starting-balance", 0);
        economyManager.setMoney(target.getUniqueId(), defaultAmount);

        // Messages de confirmation
        sender.sendMessage(ChatColor.GREEN + "Vous avez réinitialisé le solde de " + target.getName() + " à " + defaultAmount + ".");
        target.sendMessage(ChatColor.YELLOW + "Votre solde a été réinitialisé à " + defaultAmount + ".");

        return true;
    }

    /**
     * Envoie le message d'aide à l'expéditeur
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Commandes d'Économie Admin ===");
        sender.sendMessage(ChatColor.YELLOW + "/eco add <joueur> <montant>" + ChatColor.WHITE + " - Ajoute de l'argent à un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/eco remove <joueur> <montant>" + ChatColor.WHITE + " - Retire de l'argent à un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/eco set <joueur> <montant>" + ChatColor.WHITE + " - Définit l'argent d'un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/eco check <joueur>" + ChatColor.WHITE + " - Vérifie le solde d'un joueur");
        sender.sendMessage(ChatColor.YELLOW + "/eco reset <joueur>" + ChatColor.WHITE + " - Réinitialise l'argent d'un joueur");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // Vérifier les permissions
        if (!sender.hasPermission("sp_shop.admin")) {
            return completions;
        }

        if (args.length == 1) {
            // Sous-commandes disponibles
            String[] subCommands = {"add", "set", "remove", "check", "reset", "help"};
            for (String subCommand : subCommands) {
                if (subCommand.startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2) {
            // Auto-complétion des noms de joueurs
            String partialName = args[1].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(player.getName());
                }
            }
        }

        return completions;
    }
}