/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author Netsu_ - main
 * @author JemY5
 * 
 * 
 * 
 * 
 * 
 * 
 */
package fr.silvercore.sP_Shop;

import org.bukkit.plugin.java.JavaPlugin;

import fr.silvercore.sP_Shop.commands.CommandSell;
import fr.silvercore.sP_Shop.commands.CommandShop;

public class SP_Shop extends JavaPlugin {

    static SP_Shop instance;

    @Override
    public void onEnable() {

        instance = this;

        getCommand("shop").setExecutor(new CommandShop());
        getCommand("sell").setExecutor(new CommandSell());

    }

    @Override
    public void onDisable() {

    }

    public static SP_Shop getInstance() {
        return instance;
    }
}