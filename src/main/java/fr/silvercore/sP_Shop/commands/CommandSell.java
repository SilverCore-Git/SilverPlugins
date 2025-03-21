/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author Netsu_ - main
 * @author JemY5
 */

package fr.silvercore.sP_Shop.commands;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import java.util.HashMap;
import java.util.Map;

public class CommandSell implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            // Obtenir la section de configuration des items
            ConfigurationSection itemsSection = SP_Shop.getInstance().getPricesConfig().getConfigurationSection("items");
            if (itemsSection == null) {
                player.sendMessage("§cAucun item n'est configuré pour la vente.");
                return true;
            }

            // Calculer les ventes
            int totalMoney = 0;
            Map<Material, Integer> soldItems = new HashMap<>();
            Map<Integer, ItemStack> itemsToRemove = new HashMap<>();

            // Parcourir l'inventaire du joueur
            for (int i = 0; i < player.getInventory().getContents().length; i++) {
                ItemStack item = player.getInventory().getContents()[i];
                if (item != null && item.getType() != Material.AIR) {
                    String materialName = item.getType().name();

                    // Vérifier si l'item peut être vendu
                    if (itemsSection.contains(materialName + ".sell")) {
                        int sellPrice = itemsSection.getInt(materialName + ".sell");
                        int amount = item.getAmount();

                        // Ajouter à la somme totale
                        int itemTotal = sellPrice * amount;
                        totalMoney += itemTotal;

                        // Ajouter à la liste des items vendus
                        soldItems.put(item.getType(), soldItems.getOrDefault(item.getType(), 0) + amount);

                        // Marquer l'item pour suppression (pour éviter ConcurrentModificationException)
                        itemsToRemove.put(i, item);
                    }
                }
            }

            // Afficher le résultat
            if (totalMoney > 0) {
                // Obtenir l'économie depuis Vault
                Economy economy = SP_Shop.getEconomy();

                // Déposer l'argent sur le compte du joueur
                EconomyResponse response = economy.depositPlayer(player, totalMoney);

                if (response.transactionSuccess()) {
                    // Supprimer les items de l'inventaire maintenant que la transaction a réussi
                    for (ItemStack item : itemsToRemove.values()) {
                        player.getInventory().remove(item);
                    }

                    player.sendMessage("§aVous avez vendu :");
                    for (Map.Entry<Material, Integer> entry : soldItems.entrySet()) {
                        player.sendMessage("§7- §e" + entry.getValue() + "x §7" + entry.getKey().name());
                    }
                    player.sendMessage("§aPour un total de §e" + totalMoney + " §apièces.");
                } else {
                    player.sendMessage("§cErreur lors de la vente: " + response.errorMessage);
                }
            } else {
                player.sendMessage("§cVous n'avez aucun item à vendre dans votre inventaire.");
            }

            return true;
        }
        return false;
    }
}