package fr.silvercore.sp_velocitysync;

import com.velocitypowered.api.proxy.Player;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerData {

    private final String username;
    private final List<String> inventory;  // On va stocker l'inventaire sous forme de String (par exemple : identifiant des objets)
    private final String helmet;            // Idem pour les pièces d'armure
    private final String chestplate;
    private final String leggings;
    private final String boots;

    public PlayerData(String username, List<String> inventory, String helmet, String chestplate, String leggings, String boots) {
        this.username = username;
        this.inventory = inventory;
        this.helmet = helmet;
        this.chestplate = chestplate;
        this.leggings = leggings;
        this.boots = boots;
    }

    public String getUsername() {
        return username;
    }

    public List<String> getInventory() {
        return inventory;
    }

    public String getHelmet() {
        return helmet;
    }

    public String getChestplate() {
        return chestplate;
    }

    public String getLeggings() {
        return leggings;
    }

    public String getBoots() {
        return boots;
    }

    public String toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("username", username);

        // Convertir l'inventaire en un tableau JSON
        jsonObject.put("inventory", inventoryToJson(inventory));

        // Ajouter l'armure
        jsonObject.put("helmet", helmet);
        jsonObject.put("chestplate", chestplate);
        jsonObject.put("leggings", leggings);
        jsonObject.put("boots", boots);

        return jsonObject.toString();
    }

    public static PlayerData fromJson(String jsonString) {
        JSONObject jsonObject = new JSONObject(jsonString);
        String username = jsonObject.getString("username");

        // Convertir l'inventaire depuis le JSON
        List<String> inventory = jsonToInventory(jsonObject.getJSONArray("inventory"));

        // Récupérer les pièces d'armure
        String helmet = jsonObject.getString("helmet");
        String chestplate = jsonObject.getString("chestplate");
        String leggings = jsonObject.getString("leggings");
        String boots = jsonObject.getString("boots");

        return new PlayerData(username, inventory, helmet, chestplate, leggings, boots);
    }

    private static List<String> jsonToInventory(JSONArray inventoryArray) {
        return inventoryArray.toList().stream()
                .map(Object::toString)  // Convertir les objets en Strings (par exemple, identifiants d'objets)
                .collect(Collectors.toList());
    }

    private static JSONArray inventoryToJson(List<String> inventory) {
        return new JSONArray(inventory);  // Convertir la liste en JSON
    }

    // Méthode pour extraire l'inventaire d'un joueur
    public static List<String> getInventoryFromPlayer(Player player) {
        return player.getInventory().getContents().stream()
                .map(item -> item.getType().toString())  // Récupérer les types d'items sous forme de String
                .collect(Collectors.toList());
    }

    // Méthodes pour récupérer les pièces d'armure du joueur
    public static String getHelmetFromPlayer(Player player) {
        return player.getHelmet().map(item -> item.getType().toString()).orElse("none");
    }

    public static String getChestplateFromPlayer(Player player) {
        return player.getChestplate().map(item -> item.getType().toString()).orElse("none");
    }

    public static String getLeggingsFromPlayer(Player player) {
        return player.getLeggings().map(item -> item.getType().toString()).orElse("none");
    }

    public static String getBootsFromPlayer(Player player) {
        return player.getBoots().map(item -> item.getType().toString()).orElse("none");
    }
}
