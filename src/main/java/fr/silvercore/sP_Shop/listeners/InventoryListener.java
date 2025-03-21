package fr.silvercore.sP_Shop.listeners;

import fr.silvercore.sP_Shop.SP_Shop;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
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
                    || event.getCurrentItem().getType().name().contains("GLASS_PANE")) {
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

            Economy economy = SP_Shop.getEconomy();

            // Déterminer la quantité d'achat/vente en fonction du clic
            int quantity = 1;
            if (event.isShiftClick()) {
                quantity = 64; // Achat/vente en lot avec Shift+clic
            }

            if (event.isLeftClick()) {
                // Acheter
                int totalCost = buyPrice * quantity;
                if (economy.has(player, totalCost)) {
                    EconomyResponse response = economy.withdrawPlayer(player, totalCost);
                    if (response.transactionSuccess()) {
                        // Vérifier l'espace disponible dans l'inventaire
                        if (hasInventorySpace(player, material, quantity)) {
                            player.getInventory().addItem(new ItemStack(material, quantity));
                            player.sendMessage("§aVous avez acheté §e" + quantity + "x §a" + material.name() + " pour §e" + totalCost + " §apièces.");
                        } else {
                            // Rembourser si pas assez d'espace
                            economy.depositPlayer(player, totalCost);
                            player.sendMessage("§cVous n'avez pas assez d'espace dans votre inventaire!");
                        }
                    } else {
                        player.sendMessage("§cErreur lors de l'achat: " + response.errorMessage);
                    }
                } else {
                    player.sendMessage("§cVous n'avez pas assez d'argent pour acheter cet item.");
                }
            } else if (event.isRightClick()) {
                // Vendre
                int itemCount = countItems(player, material);

                // Limiter la quantité à vendre au nombre d'items possédés
                if (quantity > itemCount) {
                    quantity = itemCount;
                }

                if (quantity > 0) {
                    int totalProfit = sellPrice * quantity;

                    // Retirer les items de l'inventaire
                    removeItems(player, material, quantity);

                    // Ajouter l'argent
                    EconomyResponse response = economy.depositPlayer(player, totalProfit);
                    if (response.transactionSuccess()) {
                        player.sendMessage("§aVous avez vendu §e" + quantity + "x §a" + material.name() + " pour §e" + totalProfit + " §apièces.");
                    } else {
                        player.sendMessage("§cErreur lors de la vente: " + response.errorMessage);
                        // Rendre les items au joueur
                        player.getInventory().addItem(new ItemStack(material, quantity));
                    }
                } else {
                    player.sendMessage("§cVous n'avez pas cet item dans votre inventaire.");
                }
            }
        }
    }

    // Méthode pour vérifier si le joueur a assez d'espace dans son inventaire
    private boolean hasInventorySpace(Player player, Material material, int amount) {
        // Calculer combien d'emplacements sont nécessaires (en fonction de la taille de stack)
        int maxStackSize = material.getMaxStackSize();
        int requiredSlots = (amount + maxStackSize - 1) / maxStackSize; // Arrondi supérieur

        // Compter les emplacements vides et les emplacements avec le même matériau
        int availableSlots = 0;
        for (ItemStack item : player.getInventory().getStorageContents()) {
            if (item == null || item.getType() == Material.AIR) {
                availableSlots++;
            } else if (item.getType() == material && item.getAmount() < maxStackSize) {
                // Cet emplacement peut encore contenir plus d'items
                availableSlots++;
            }
        }

        return availableSlots >= requiredSlots;
    }

    // Méthode pour compter le nombre d'items d'un certain type dans l'inventaire
    private int countItems(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

    // Méthode pour retirer un nombre spécifique d'items de l'inventaire
    private void removeItems(Player player, Material material, int amount) {
        int remaining = amount;

        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= remaining) {
                    remaining -= item.getAmount();
                    player.getInventory().remove(item);
                } else {
                    item.setAmount(item.getAmount() - remaining);
                    remaining = 0;
                }

                if (remaining <= 0) {
                    break;
                }
            }
        }
    }
}