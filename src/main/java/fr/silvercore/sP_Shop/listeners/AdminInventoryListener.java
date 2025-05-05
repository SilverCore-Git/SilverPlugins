/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */

package fr.silvercore.sP_Shop.listeners;

import fr.silvercore.sP_Shop.SP_Shop;
import fr.silvercore.sP_Shop.utils.PriceManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;


public class AdminInventoryListener implements Listener {

    private SP_Shop plugin;
    private Map<UUID, AdminSessionType> adminSessions = new HashMap<>();
    private Map<UUID, Material> selectedMaterial = new HashMap<>();
    private Map<UUID, PriceEditType> priceEditSessions = new HashMap<>();

    public enum AdminSessionType { // enum => object
        MAIN_MENU,
        ADD_ITEM,
        EDIT_PRICES,
        REMOVE_ITEM,
        PRICE_EDITOR
    }

    public enum PriceEditType {
        BUY,
        SELL
    }

    public AdminInventoryListener(SP_Shop plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        UUID playerUUID = player.getUniqueId();
        String title = event.getView().getTitle();

        // Vérifier si c'est une interface admin
        if (title.startsWith("§4§lSP_Shop Admin")) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            // Menu principal d'administration
            if (title.equals("§4§lSP_Shop Admin")) {
                handleMainMenuClick(player, clickedItem);
            }
            // Interface d'ajout d'items
            else if (title.equals("§4§lSP_Shop Admin - Ajouter")) {
                handleAddItemClick(player, clickedItem, event.getSlot());
            }
            // Interface de modification des prix
            else if (title.equals("§4§lSP_Shop Admin - Modifier")) {
                handleEditPricesClick(player, clickedItem, event.getSlot());
            }
            // Interface de suppression d'items
            else if (title.equals("§4§lSP_Shop Admin - Supprimer")) {
                handleRemoveItemClick(player, clickedItem, event.getSlot());
            }
            // Interface d'édition des prix
            else if (title.startsWith("§4§lSP_Shop Admin - Prix")) {
                handlePriceEditorClick(player, clickedItem, event.getSlot());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getPlayer();
        UUID playerUUID = player.getUniqueId();

        // Nettoyage des sessions si le joueur ferme l'inventaire
        if (event.getView().getTitle().startsWith("§4§lSP_Shop Admin")) {
            adminSessions.remove(playerUUID);
            selectedMaterial.remove(playerUUID);
            priceEditSessions.remove(playerUUID);
        }
    }

    // Gestion des clics dans le menu principal
    private void handleMainMenuClick(Player player, ItemStack clickedItem) {
        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        String itemName = clickedItem.getItemMeta().getDisplayName();

        if (itemName.equals("§aAjouter un item")) {
            openAddItemMenu(player);
        } else if (itemName.equals("§eModifier les prix")) {
            openEditPricesMenu(player);
        } else if (itemName.equals("§cSupprimer un item")) {
            openRemoveItemMenu(player);
        }
    }

    // Gestion des clics dans le menu d'ajout d'items
    private void handleAddItemClick(Player player, ItemStack clickedItem, int slot) {
        Material material = clickedItem.getType();
        PriceManager priceManager = plugin.getPriceManager();

        // Retour au menu principal
        if (material == Material.BARRIER) {
            openAdminMainMenu(player);
            return; // Ajout du return pour éviter l'exécution du code suivant
        }

        // Ignorer les items de décoration
        if (material == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Si l'item est déjà dans la boutique, informer le joueur
        if (priceManager.hasPrice(material)) {
            player.sendMessage("§cCet item est déjà dans la boutique. Utilisez le menu de modification des prix.");
            return; // Ajout du return manquant
        }

        // Ouvrir l'interface d'édition des prix pour l'ajout
        selectedMaterial.put(player.getUniqueId(), material);
        openPriceEditorMenu(player, material, true);
    }

    // Gestion des clics dans le menu de modification des prix
    private void handleEditPricesClick(Player player, ItemStack clickedItem, int slot) {
        Material material = clickedItem.getType();
        PriceManager priceManager = plugin.getPriceManager();

        // Retour au menu principal
        if (material == Material.BARRIER) {
            openAdminMainMenu(player);
            return; // Ajout du return pour éviter l'exécution du code suivant
        }

        // Ignorer les items de décoration
        if (material == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Vérifier si l'item est dans la boutique
        if (!priceManager.hasPrice(material)) {
            player.sendMessage("§cCet item n'est pas disponible dans la boutique.");
            return; // Ajout du return manquant
        }

        // Ouvrir l'interface d'édition des prix
        selectedMaterial.put(player.getUniqueId(), material);
        openPriceEditorMenu(player, material, false);
    }

    // Gestion des clics dans le menu de suppression d'items
    private void handleRemoveItemClick(Player player, ItemStack clickedItem, int slot) {
        Material material = clickedItem.getType();
        PriceManager priceManager = plugin.getPriceManager();

        // Retour au menu principal
        if (material == Material.BARRIER) {
            openAdminMainMenu(player);
            return; // Ajout du return pour éviter l'exécution du code suivant
        }

        // Ignorer les items de décoration
        if (material == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        // Vérifier si l'item est dans la boutique
        if (!priceManager.hasPrice(material)) {
            player.sendMessage("[shop] §cCet item n'est pas disponible dans la boutique.");
            return; // Ajout du return manquant
        }

        // Supprimer l'item
        priceManager.removePrice(material);
        priceManager.savePrices();
        player.sendMessage("[shop] §aL'item §e" + material.name() + " §aa été supprimé de la boutique.");

        // Rafraîchir le menu
        openRemoveItemMenu(player);
    }

    // Gestion des clics dans l'éditeur de prix
    private void handlePriceEditorClick(Player player, ItemStack clickedItem, int slot) {
        UUID playerUUID = player.getUniqueId();
        Material material = selectedMaterial.get(playerUUID);
        if (material == null) {
            openAdminMainMenu(player);
            return; // Ajout du return pour éviter l'exécution du code suivant
        }

        if (clickedItem == null || !clickedItem.hasItemMeta() || !clickedItem.getItemMeta().hasDisplayName()) {
            return;
        }

        PriceManager priceManager = plugin.getPriceManager();
        String itemName = clickedItem.getItemMeta().getDisplayName();

        // Retour
        if (clickedItem.getType() == Material.BARRIER) {
            openAdminMainMenu(player);
            return; // Ajout du return pour éviter l'exécution du code suivant
        }

        boolean isNewItem = !priceManager.hasPrice(material);

        // Édition du prix d'achat
        if (itemName.equals("§aModifier le prix d'achat")) {
            player.closeInventory();
            priceEditSessions.put(playerUUID, PriceEditType.BUY);
            player.sendMessage("[shop] §aEntrez le nouveau prix d'achat pour §e" + material.name() + " §adans le chat:");
        }
        // Édition du prix de vente
        else if (itemName.equals("§cModifier le prix de vente")) {
            player.closeInventory();
            priceEditSessions.put(playerUUID, PriceEditType.SELL);
            player.sendMessage("[shop] §aEntrez le nouveau prix de vente pour §e" + material.name() + " §adans le chat:");
        }
        // Confirmer l'ajout
        else if (itemName.equals("§aConfirmer l'ajout")) {
            double buyPrice = priceManager.getBuyPrice(material);
            double sellPrice = priceManager.getSellPrice(material);

            // Vérifier si les prix sont configurés
            if (buyPrice <= 0 || sellPrice <= 0) {
                player.sendMessage("[shop] §cVous devez configurer les deux prix avant de confirmer.");
                return;
            }

            // Ajoutons l'item en sauvegardant les prix actuels
            priceManager.setBuyPrice(material, buyPrice);
            priceManager.setSellPrice(material, sellPrice);
            priceManager.savePrices();
            player.sendMessage("[shop] §aL'item §e" + material.name() + " §aa été ajouté à la boutique.");
            openAdminMainMenu(player);
        }
        // Enregistrer les modifications
        else if (itemName.equals("§aSauvegarder")) {
            priceManager.savePrices();
            player.sendMessage("[shop] §aLes modifications ont été sauvegardées.");
            openAdminMainMenu(player);
        }
    }

    // Ouvrir le menu principal d'administration
    public void openAdminMainMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9 * 3, "§4§lSP_Shop Admin");
        adminSessions.put(player.getUniqueId(), AdminSessionType.MAIN_MENU);

        // Item pour ajouter un nouvel item
        ItemStack addItem = new ItemStack(Material.EMERALD, 1);
        ItemMeta addMeta = addItem.getItemMeta();
        addMeta.setDisplayName("§aAjouter un item");
        List<String> addLore = new ArrayList<>();
        addLore.add("§7Cliquez pour ouvrir l'interface");
        addLore.add("§7d'ajout d'un nouvel item.");
        addMeta.setLore(addLore);
        addItem.setItemMeta(addMeta);
        inventory.setItem(11, addItem);

        // Item pour modifier les prix
        ItemStack editItem = new ItemStack(Material.WRITABLE_BOOK, 1);
        ItemMeta editMeta = editItem.getItemMeta();
        editMeta.setDisplayName("§eModifier les prix");
        List<String> editLore = new ArrayList<>();
        editLore.add("§7Cliquez pour ouvrir l'interface");
        editLore.add("§7de modification des prix.");
        editMeta.setLore(editLore);
        editItem.setItemMeta(editMeta);
        inventory.setItem(13, editItem);

        // Item pour supprimer un item
        ItemStack removeItem = new ItemStack(Material.BARRIER, 1);
        ItemMeta removeMeta = removeItem.getItemMeta();
        removeMeta.setDisplayName("§cSupprimer un item");
        List<String> removeLore = new ArrayList<>();
        removeLore.add("§7Cliquez pour ouvrir l'interface");
        removeLore.add("§7de suppression d'items.");
        removeMeta.setLore(removeLore);
        removeItem.setItemMeta(removeMeta);
        inventory.setItem(15, removeItem);

        player.openInventory(inventory);
    }

    // Ouvrir le menu d'ajout d'items
    private void openAddItemMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, "§4§lSP_Shop Admin - Ajouter");
        adminSessions.put(player.getUniqueId(), AdminSessionType.ADD_ITEM);

        // Remplir avec des items courants
        Material[] commonItems = {
                Material.DIAMOND, Material.IRON_INGOT, Material.GOLD_INGOT, Material.COAL,
                Material.OAK_LOG, Material.STONE, Material.COBBLESTONE, Material.SAND,
                Material.DIRT, Material.GRAVEL, Material.WHEAT, Material.CARROT,
                Material.POTATO, Material.BEETROOT, Material.PUMPKIN, Material.MELON,
                Material.RED_MUSHROOM, Material.BROWN_MUSHROOM, Material.SUGAR_CANE,
                Material.LEATHER, Material.FEATHER, Material.STRING, Material.GUNPOWDER,
                Material.BONE, Material.ROTTEN_FLESH, Material.SPIDER_EYE, Material.SLIME_BALL,
                Material.BLAZE_ROD, Material.ENDER_PEARL, Material.LAPIS_LAZULI, Material.REDSTONE,
                Material.EMERALD, Material.QUARTZ/*, Material.NETHERITE_INGOT*/, Material.OBSIDIAN
        };

        for (int i = 0; i < commonItems.length && i < 45; i++) {
            inventory.setItem(i, new ItemStack(commonItems[i], 1));
        }

        // Bouton de retour
        ItemStack backButton = new ItemStack(Material.BARRIER, 1);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);

        // Remplir le reste avec du verre gris
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);
    }

    // Ouvrir le menu de modification des prix
    private void openEditPricesMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, "§4§lSP_Shop Admin - Modifier");
        adminSessions.put(player.getUniqueId(), AdminSessionType.EDIT_PRICES);

        PriceManager priceManager = plugin.getPriceManager();
        Set<Material> items = priceManager.getAllPrices().keySet();

        // Ajouter les items disponibles
        int slot = 0;
        for (Material material : items) {
            if (slot >= 45) break; // Limite pour l'interface

            ItemStack itemStack = new ItemStack(material, 1);
            ItemMeta meta = itemStack.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add("§aPrix d'achat: §e" + priceManager.getBuyPrice(material));
            lore.add("§cPrix de vente: §e" + priceManager.getSellPrice(material));

            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            inventory.setItem(slot++, itemStack);
        }

        // Bouton de retour
        ItemStack backButton = new ItemStack(Material.BARRIER, 1);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);

        // Remplir le reste avec du verre gris
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);
    }

    // Ouvrir le menu de suppression d'items
    private void openRemoveItemMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9 * 6, "§4§lSP_Shop Admin - Supprimer");
        adminSessions.put(player.getUniqueId(), AdminSessionType.REMOVE_ITEM);

        PriceManager priceManager = plugin.getPriceManager();
        Set<Material> items = priceManager.getAllPrices().keySet();

        // Ajouter les items disponibles
        int slot = 0;
        for (Material material : items) {
            if (slot >= 45) break; // Limite pour l'interface

            ItemStack itemStack = new ItemStack(material, 1);
            ItemMeta meta = itemStack.getItemMeta();

            List<String> lore = new ArrayList<>();
            lore.add("§aPrix d'achat: §e" + priceManager.getBuyPrice(material));
            lore.add("§cPrix de vente: §e" + priceManager.getSellPrice(material));
            lore.add("§7Cliquez pour supprimer cet item");

            meta.setLore(lore);
            itemStack.setItemMeta(meta);

            inventory.setItem(slot++, itemStack);
        }

        // Bouton de retour
        ItemStack backButton = new ItemStack(Material.BARRIER, 1);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        backButton.setItemMeta(backMeta);
        inventory.setItem(49, backButton);

        // Remplir le reste avec du verre gris
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);
    }

    // Ouvrir l'interface d'édition des prix pour un item spécifique
    private void openPriceEditorMenu(Player player, Material material, boolean isNewItem) {
        Inventory inventory = Bukkit.createInventory(null, 9 * 3, "§4§lSP_Shop Admin - Prix de " + material.name());
        adminSessions.put(player.getUniqueId(), AdminSessionType.PRICE_EDITOR);

        PriceManager priceManager = plugin.getPriceManager();

        // Item visuel
        ItemStack materialItem = new ItemStack(material, 1);
        ItemMeta materialMeta = materialItem.getItemMeta();

        List<String> materialLore = new ArrayList<>();
        if (isNewItem) {
            materialLore.add("§7Nouvel item à ajouter");
            materialLore.add("§aPrix d'achat actuel: §eNon défini");
            materialLore.add("§cPrix de vente actuel: §eNon défini");
        } else {
            materialLore.add("§aPrix d'achat actuel: §e" + priceManager.getBuyPrice(material));
            materialLore.add("§cPrix de vente actuel: §e" + priceManager.getSellPrice(material));
        }

        materialMeta.setLore(materialLore);
        materialItem.setItemMeta(materialMeta);
        inventory.setItem(4, materialItem);

        // Bouton pour modifier le prix d'achat
        ItemStack buyButton = new ItemStack(Material.EMERALD, 1);
        ItemMeta buyMeta = buyButton.getItemMeta();
        buyMeta.setDisplayName("§aModifier le prix d'achat");
        buyButton.setItemMeta(buyMeta);
        inventory.setItem(11, buyButton);

        // Bouton pour modifier le prix de vente
        ItemStack sellButton = new ItemStack(Material.GOLD_INGOT, 1);
        ItemMeta sellMeta = sellButton.getItemMeta();
        sellMeta.setDisplayName("§cModifier le prix de vente");
        sellButton.setItemMeta(sellMeta);
        inventory.setItem(15, sellButton);

        // Bouton pour confirmer ou sauvegarder
        ItemStack confirmButton;
        if (isNewItem) {
            confirmButton = new ItemStack(Material.LIME_WOOL, 1);
            ItemMeta confirmMeta = confirmButton.getItemMeta();
            confirmMeta.setDisplayName("§aConfirmer l'ajout");
            confirmButton.setItemMeta(confirmMeta);
        } else {
            confirmButton = new ItemStack(Material.LIME_WOOL, 1);
            ItemMeta confirmMeta = confirmButton.getItemMeta();
            confirmMeta.setDisplayName("§aSauvegarder");
            confirmButton.setItemMeta(confirmMeta);
        }
        inventory.setItem(22, confirmButton);

        // Bouton pour retourner au menu principal
        ItemStack backButton = new ItemStack(Material.BARRIER, 1);
        ItemMeta backMeta = backButton.getItemMeta();
        backMeta.setDisplayName("§cRetour");
        backButton.setItemMeta(backMeta);
        inventory.setItem(26, backButton);

        // Remplir le reste avec du verre gris
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta fillerMeta = filler.getItemMeta();
        fillerMeta.setDisplayName(" ");
        filler.setItemMeta(fillerMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, filler);
            }
        }

        player.openInventory(inventory);
    }

    // Méthode publique pour traiter le chat après une demande d'édition de prix
    public boolean handleChatInput(Player player, String message) {
        UUID playerUUID = player.getUniqueId();

        // Vérifier si le joueur est en mode d'édition de prix
        if (!priceEditSessions.containsKey(playerUUID) || !selectedMaterial.containsKey(playerUUID)) {
            return false;
        }

        Material material = selectedMaterial.get(playerUUID);
        PriceEditType editType = priceEditSessions.get(playerUUID);

        try {
            double price = Double.parseDouble(message);

            if (price < 0) {
                player.sendMessage("[shop] §cLe prix doit être un nombre positif.");
                return true; // Correction du return manquant + valeur retournée
            }

            PriceManager priceManager = plugin.getPriceManager();

            if (editType == PriceEditType.BUY) {
                priceManager.setBuyPrice(material, price);
                player.sendMessage("[shop] §aPrix d'achat de §e" + material.name() + " §afixé à §e" + price);
            } else {
                priceManager.setSellPrice(material, price);
                player.sendMessage("[shop] §aPrix de vente de §e" + material.name() + " §afixé à §e" + price);
            }

            // Ouvrir à nouveau l'interface d'édition des prix
            openPriceEditorMenu(player, material, !priceManager.hasPrice(material));

            // Nettoyer la session d'édition
            priceEditSessions.remove(playerUUID);

            return true; // Correction de la valeur retournée
        } catch (NumberFormatException e) {
            player.sendMessage("[shop] §cVeuillez entrer un nombre valide.");
            return true; // Correction du return manquant + valeur retournée
        }
    }
}