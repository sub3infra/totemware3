package com.example.inventorytotem;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class InventoryTotemClient implements ClientModInitializer {
    // Slot layout of the player's InventoryMenu:
    // 0 = craft result, 1-4 = craft grid, 5-8 = armor, 9-35 = main inventory, 36-44 = hotbar, 45 = offhand
    private static final int MAIN_START = 9, MAIN_END = 35;
    private static final int HOTBAR_START = 36, HOTBAR_END = 44;
    private static final int OFFHAND_SWAP_BUTTON = 40;

    private static long openedAt = -1;
    private static long lastAction = 0;
    private static boolean openSettingsNextTick = false;

    @Override
    public void onInitializeClient() {
        TotemConfig.load();

        ClientTickEvents.END_CLIENT_TICK.register(InventoryTotemClient::onTick);

        // /autototem opens the settings screen
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                dispatcher.register(ClientCommandManager.literal("autototem").executes(ctx -> {
                    openSettingsNextTick = true; // chat closes after the command, so open next tick
                    return 1;
                })));

        // Small settings button inside the inventory screen
        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof InventoryScreen) {
                Button button = Button.builder(Component.literal("Totem Settings"),
                                b -> client.setScreen(new TotemConfigScreen(screen)))
                        .bounds(4, 4, 90, 20)
                        .build();
                Screens.getButtons(screen).add(button);
            }
        });
    }

    private static void onTick(Minecraft mc) {
        if (openSettingsNextTick) {
            openSettingsNextTick = false;
            mc.setScreen(new TotemConfigScreen(null));
            return;
        }

        TotemConfig cfg = TotemConfig.get();
        LocalPlayer player = mc.player;

        if (!cfg.enabled || player == null || mc.gameMode == null
                || !(mc.screen instanceof InventoryScreen)) {
            openedAt = -1;
            return;
        }

        long now = System.currentTimeMillis();
        if (openedAt < 0) openedAt = now;

        if (now - openedAt < cfg.delayMs) return;
        if (now - lastAction < Math.max(cfg.delayMs, 100)) return;

        InventoryMenu menu = player.inventoryMenu;
        if (!menu.getCarried().isEmpty()) return; // don't interfere while you're holding an item

        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(Items.TOTEM_OF_UNDYING)) return;
        if (!offhand.isEmpty() && !cfg.replaceOffhandItem) return;

        int slot = findTotemSlot(menu, cfg);
        if (slot < 0) return;

        // SWAP with button 40 swaps the clicked slot with the offhand
        mc.gameMode.handleInventoryMouseClick(menu.containerId, slot, OFFHAND_SWAP_BUTTON, ClickType.SWAP, player);
        lastAction = now;

        if (cfg.closeAfterEquip) {
            player.closeContainer();
        }
    }

    private static int findTotemSlot(InventoryMenu menu, TotemConfig cfg) {
        if (cfg.searchMainInventory) {
            for (int i = MAIN_START; i <= MAIN_END; i++) {
                if (menu.getSlot(i).getItem().is(Items.TOTEM_OF_UNDYING)) return i;
            }
        }
        if (cfg.searchHotbar) {
            for (int i = HOTBAR_START; i <= HOTBAR_END; i++) {
                if (menu.getSlot(i).getItem().is(Items.TOTEM_OF_UNDYING)) return i;
            }
        }
        return -1;
    }
}
