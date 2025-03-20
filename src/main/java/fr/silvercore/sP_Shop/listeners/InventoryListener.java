package fr.silvercore.sP_Shop.listeners;

import fr.silvercore.sP_Shop.SP_Shop;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§2§lBoutique")) {
            event.setCancelled(true); // Empêcher de prendre les items

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR
                    || event.getCurrentItem().getType().name().contains("STAINED_GLASS_PANE")) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemStack clickedItem = event.getCurrentItem();
            Material material = clickedItem.getType();

            // Chercher le prix dans la configuration
            int buyPrice = SP_Shop.getInstance().getPricesConfig().getInt("items." + material.name() + ".buy", -1);
            int sellPrice = SP_Shop.getInstance().getPricesConfig().getInt("items." + material.name() + ".sell", -1);

            if (buyPrice == -1 || sellPrice == -1) {
                player.sendMessage("§cCet item n'est pas disponible à l'achat ou à la vente.");
                return;
            }

            // Implémentez ici la logique d'achat/vente selon votre système d'économie
            // Exemple avec Vault (vous devrez ajouter Vault comme dépendance) :

            if (event.isLeftClick()) {
                // Acheter
                player.sendMessage("§aVous avez acheté un(e) " + material.name() + " pour " + buyPrice + " pièces.");
            } else if (event.isRightClick()) {
                // Vendre
                player.sendMessage("§aVous avez vendu un(e) " + material.name() + " pour " + sellPrice + " pièces.");
            }
        }
    }
}