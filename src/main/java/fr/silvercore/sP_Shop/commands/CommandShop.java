/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author Netsu_ - main
 * @author JemY5
 */

package fr.silvercore.sP_Shop.commands;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.command.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;

public class CommandShop implements CommandExecutor {

    Inventory inventory = Bukkit.createInventory(null, 9*6, "§2§lBoutique");

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {

        if(sender instanceof Player) {

            Player player = (Player) sender;

            player.openInventory(inventory);

            ItemStack glass = new ItemStack(Material.STAINED_GLASS_PANE, (byte) 12);

            ItemMeta meta = glass.getItemMeta();

            meta.setDisplayName(" ");

            glass.setItemMeta(meta);

            for(int i = 0; i < 6*9; i++) {
                inventory.setItem(i, glass);
            }
        }

        return false;
    }
}