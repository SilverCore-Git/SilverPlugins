/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author Netsu_ - main
 * @author JemY5
 */
package fr.silvercore.sP_Shop;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

//import fr.silvercore.sP_Shop.commands.CommandSell;
//import fr.silvercore.sP_Shop.commands.CommandShop;

public class SP_Shop extends JavaPlugin {

   //   static SP_Shop instance;

    @Override
    public void onEnable() {
        Bukkit.getLogger().log(level.INFO, "SilverPlugins : SP_Shop a démarré avec succès (le frère de réussite).");
        instance = this;

        //getCommand("shop").setExecutor(new CommandShop());
        //getCommand("sell").setExecutor(new CommandSell());

    }

    @Override
    public void onDisable() {
        Bukkit.getLogger().log(level.INFO, "SilverPlugins : SP_Shop a désactivé avec réussite (le frère de succès).");
    }

    //public static SP_Shop getInstance() {return instance;
    //}
}