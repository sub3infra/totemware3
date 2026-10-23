package com.example.inventorytotem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TotemConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("inventorytotem.json");

    private static TotemConfig instance = new TotemConfig();

    /** Master switch. */
    public boolean enabled = true;
    /** Wait this long (ms) after opening the inventory before equipping, and between actions. */
    public int delayMs = 100;
    /** If false, only equips when the offhand is empty. If true, swaps out whatever is there. */
    public boolean replaceOffhandItem = true;
    /** Search the hotbar for totems. */
    public boolean searchHotbar = true;
    /** Search the main inventory (the 3x9 grid) for totems. */
    public boolean searchMainInventory = true;
    /** Close the inventory right after equipping. */
    public boolean closeAfterEquip = false;

    public static TotemConfig get() {
        return instance;
    }

    public static void load() {
        try {
            if (Files.exists(PATH)) {
                TotemConfig loaded = GSON.fromJson(Files.readString(PATH), TotemConfig.class);
                if (loaded != null) instance = loaded;
            }
        } catch (Exception e) {
            System.err.println("[InventoryTotem] Failed to load config, using defaults: " + e);
        }
        instance.delayMs = Math.max(0, Math.min(1000, instance.delayMs));
        save();
    }

    public static void save() {
        try {
            Files.writeString(PATH, GSON.toJson(instance));
        } catch (IOException e) {
            System.err.println("[InventoryTotem] Failed to save config: " + e);
        }
    }
}
