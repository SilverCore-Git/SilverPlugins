/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author Netsu_ - main
 * @author JemY5
 */

package fr.silvercore.sP_Shop.commands;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.silvercore.sP_Shop.database.TransactionDatabaseManager;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.ArrayList;
import java.util.List;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

public class CommandShop implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player) sender;

            // Créer l'inventaire
            Inventory inventory = Bukkit.createInventory(null, 9*6, "§2§lBoutique");

            // Ajouter des items en verre pour décoration
            ItemStack glass = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
            ItemMeta meta = glass.getItemMeta();
            meta.setDisplayName(" ");
            glass.setItemMeta(meta);

            for(int i = 0; i < 6*9; i++) {
                inventory.setItem(i, glass);
            }

            //Récupérer la configuration des prix
            ConfigurationSection itemsSection = SP_Shop.getInstance().getPricesConfig().getConfigurationSection("items");

            // S'il y a des items configurés, les ajouter à l'inventaire
            if (itemsSection != null) {
                int slot = 10; // Position de départ pour les items

                for (String materialName : itemsSection.getKeys(false)) {
                    try {
                        Material material = Material.valueOf(materialName);
                        int buyPrice = itemsSection.getInt(materialName + ".buy");
                        int sellPrice = itemsSection.getInt(materialName + ".sell");

                        ItemStack item = new ItemStack(material, 1);
                        ItemMeta itemMeta = item.getItemMeta();

                        List<String> lore = new ArrayList<>();
                        lore.add("§aPrix d'achat: §e" + buyPrice);
                        lore.add("§cPrix de vente: §e" + sellPrice);
                        lore.add(" ");
                        lore.add("§7Clic gauche pour acheter");
                        lore.add("§7Clic droit pour vendre");

                        itemMeta.setLore(lore);
                        item.setItemMeta(itemMeta);

                        inventory.setItem(slot, item);

                        // Passer au prochain slot (sauter les bordures)
                        slot++;
                        if (slot % 9 == 8) slot += 2;
                    } catch (IllegalArgumentException e) {
                        SP_Shop.getInstance().getLogger().warning("Matériel inconnu dans la configuration: " + materialName);
                    }

                }
            }

            player.openInventory(inventory);
            return true;
        }
        return false;
    }
}