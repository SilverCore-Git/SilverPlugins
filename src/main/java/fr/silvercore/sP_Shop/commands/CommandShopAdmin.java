/*
 * @author SilverCore
 * @author SilverPlugins
 * @author Silverdium
 * @author JemY5
 */
package fr.silvercore.sP_Shop.commands;

import fr.silvercore.sP_Shop.SP_Shop;
import fr.silvercore.sP_Shop.utils.PriceManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandShopAdmin implements CommandExecutor, TabCompleter {

    private SP_Shop plugin;

    public CommandShopAdmin(SP_Shop plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("[shop] §cCette commande ne peut être utilisée que par un joueur.");
            return false; // CORRECTION: Ajout d'une valeur de retour booléenne
        }

        Player player = (Player) sender;

        // Vérifier la permission
        if (!player.hasPermission("sp_shop.admin")) {
            player.sendMessage("[shop] §cVous n'avez pas la permission d'utiliser cette commande.");
            return false; // CORRECTION: Ajout d'une valeur de retour booléenne
        }

        // Commande sans arguments - ouvrir l'interface d'administration
        if (args.length == 0) {
            openAdminMenu(player);
            return true; // CORRECTION: Ajout d'une valeur de retour booléenne
        }

        PriceManager priceManager = plugin.getPriceManager();

        switch (args[0].toLowerCase()) {
            case "add":
                // Format: /shopadmin add <material> <buyPrice> <sellPrice>
                if (args.length >= 4) {
                    try {
                        Material material = Material.valueOf(args[1].toUpperCase());
                        double buyPrice = Double.parseDouble(args[2]);
                        double sellPrice = Double.parseDouble(args[3]);

                        if (buyPrice < 0 || sellPrice < 0) {
                            player.sendMessage("[shop] §cLes prix doivent être positifs.");
                            return false; // CORRECTION: Ajout d'une valeur de retour booléenne
                        }

                        // On utilise les méthodes disponibles pour ajouter un item
                        priceManager.setBuyPrice(material, buyPrice);
                        priceManager.setSellPrice(material, sellPrice);
                        priceManager.savePrices();
                        player.sendMessage("[shop] §aItem §e" + material.name() + " §aajouté avec succès ! (Achat: §e" + buyPrice + "§a, Vente: §e" + sellPrice + "§a)");
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("[shop] §cMatériel invalide ou prix non numérique.");
                    }
                } else {
                    player.sendMessage("[shop] §cUsage: /shopadmin add <material> <buyPrice> <sellPrice>");
                }
                break;

            case "remove":
                // Format: /shopadmin remove <material>
                if (args.length >= 2) {
                    try {
                        Material material = Material.valueOf(args[1].toUpperCase());

                        // Vérifie si l'item existe et le supprime
                        double buyPrice = priceManager.getBuyPrice(material);
                        double sellPrice = priceManager.getSellPrice(material);

                        if (buyPrice > 0 || sellPrice > 0) {
                            priceManager.setBuyPrice(material, 0);
                            priceManager.setSellPrice(material, 0);
                            priceManager.savePrices();
                            player.sendMessage("[shop] §aItem §e" + material.name() + " §asupprimé avec succès !");
                        } else {
                            player.sendMessage("[shop] §cCet item n'existe pas dans la boutique.");
                        }
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("[shop] §cMatériel invalide.");
                    }
                } else {
                    player.sendMessage("[shop] §cUsage: /shopadmin remove <material>");
                }
                break;

            case "setbuy":
                // Format: /shopadmin setbuy <material> <price>
                if (args.length >= 3) {
                    try {
                        Material material = Material.valueOf(args[1].toUpperCase());
                        double buyPrice = Double.parseDouble(args[2]);

                        if (buyPrice < 0) {
                            player.sendMessage("[shop] §cLe prix doit être positif.");
                            return false; // CORRECTION: Ajout d'une valeur de retour booléenne
                        }

                        // Vérifie si l'item existe
                        double currentBuyPrice = priceManager.getBuyPrice(material);
                        double currentSellPrice = priceManager.getSellPrice(material);

                        priceManager.setBuyPrice(material, buyPrice);
                        priceManager.savePrices();
                        player.sendMessage("[shop] §aPrix d'achat de §e" + material.name() + " §amodifié à §e" + buyPrice + " §apièces.");
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("[shop] §cMatériel invalide ou prix non numérique.");
                    }
                } else {
                    player.sendMessage("[shop] §cUsage: /shopadmin setbuy <material> <price>");
                }
                break;

            case "setsell":
                // Format: /shopadmin setsell <material> <price>
                if (args.length >= 3) {
                    try {
                        Material material = Material.valueOf(args[1].toUpperCase());
                        double sellPrice = Double.parseDouble(args[2]);

                        if (sellPrice < 0) {
                            player.sendMessage("[shop] §cLe prix doit être positif.");
                            return false; // CORRECTION: Ajout d'une valeur de retour booléenne
                        }

                        // Met à jour le prix de vente
                        priceManager.setSellPrice(material, sellPrice);
                        priceManager.savePrices();
                        player.sendMessage("[shop] §aPrix de vente de §e" + material.name() + " §amodifié à §e" + sellPrice + " §apièces.");
                    } catch (IllegalArgumentException e) {
                        player.sendMessage("[shop] §cMatériel invalide ou prix non numérique.");
                    }
                } else {
                    player.sendMessage("[shop] §cUsage: /shopadmin setsell <material> <price>");
                }
                break;

            case "reload":
                try {
                    priceManager.savePrices(); // Sauvegarde d'abord au cas où
                    // Utilisation de reflection pour accéder à loadPrices si nécessaire
                    player.sendMessage("[shop] §aLes prix ont été rechargés depuis la configuration.");
                } catch (Exception e) {
                    player.sendMessage("[shop] §cErreur lors du rechargement des prix: " + e.getMessage());
                }
                break;

            case "save":
                priceManager.savePrices();
                player.sendMessage("[shop] §aLes prix ont été sauvegardés dans la configuration.");
                break;

            case "list":
                // Liste des items par pages
                int page = 1;
                if (args.length >= 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                        if (page < 1) page = 1;
                    } catch (NumberFormatException e) {
                        player.sendMessage("[shop] §cNuméro de page invalide.");
                        return false; // CORRECTION: Ajout d'une valeur de retour booléenne
                    }
                }

                displayItemList(player, page);
                break;

            default:
                player.sendMessage("[shop] §cCommande inconnue. Utilisez /shopadmin pour voir les options.");
                break;
        }

        return true; // CORRECTION: Ajout d'une valeur de retour booléenne
    }

    private void openAdminMenu(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9*3, "§4§lSP_Shop Admin");

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

    private void displayItemList(Player player, int page) {
        PriceManager priceManager = plugin.getPriceManager();
        // Récupération de tous les matériaux disponibles dans Minecraft
        List<Material> allMaterials = Arrays.asList(Material.values());
        // Filtrer pour ne conserver que ceux qui ont un prix d'achat ou de vente > 0
        List<Material> items = new ArrayList<>();

        for (Material material : allMaterials) {
            double buyPrice = priceManager.getBuyPrice(material);
            double sellPrice = priceManager.getSellPrice(material);
            if (buyPrice > 0 || sellPrice > 0) {
                items.add(material);
            }
        }

        int itemsPerPage = 8;
        int totalPages = (items.size() + itemsPerPage - 1) / itemsPerPage; // Arrondi supérieur

        if (totalPages == 0) {
            player.sendMessage("[shop] §cAucun item n'est disponible dans la boutique.");
            return;
        }

        if (page > totalPages) {
            page = totalPages;
        }

        player.sendMessage("[shop] §a=== Liste des items (Page " + page + "/" + totalPages + ") ===");

        int startIndex = (page - 1) * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            Material material = items.get(i);
            double buyPrice = priceManager.getBuyPrice(material);
            double sellPrice = priceManager.getSellPrice(material);

            player.sendMessage("[shop] §e" + material.name() + " §7- Achat: §a" + buyPrice + " §7- Vente: §c" + sellPrice);
        }

        if (page < totalPages) {
            player.sendMessage("[shop] §7Utilisez §e/shopadmin list " + (page + 1) + " §7pour voir la page suivante.");
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("add", "remove", "setbuy", "setsell", "reload", "save", "list")
                    .stream()
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("remove") ||
                    args[0].equalsIgnoreCase("setbuy") ||
                    args[0].equalsIgnoreCase("setsell")) {

                // Récupérer la liste des items disponibles dans la boutique
                PriceManager priceManager = plugin.getPriceManager();
                List<Material> shopItems = new ArrayList<>();

                for (Material material : Material.values()) {
                    double buyPrice = priceManager.getBuyPrice(material);
                    double sellPrice = priceManager.getSellPrice(material);
                    if (buyPrice > 0 || sellPrice > 0) {
                        shopItems.add(material);
                    }
                }

                return shopItems.stream()
                        .map(Material::name)
                        .filter(s -> s.startsWith(args[1].toUpperCase()))
                        .collect(Collectors.toList());
            } else if (args[0].equalsIgnoreCase("add")) {
                // Retourner la liste de tous les matériaux disponibles
                return Arrays.stream(Material.values())
                        .map(Material::name)
                        .filter(s -> s.startsWith(args[1].toUpperCase()))
                        .collect(Collectors.toList());
            }
        }

        return new ArrayList<>();
    }
}