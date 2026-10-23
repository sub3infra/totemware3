package com.example.inventorytotem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class TotemConfigScreen extends Screen {
    private final Screen parent;

    public TotemConfigScreen(Screen parent) {
        super(Component.literal("Inventory Totem Settings"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        TotemConfig cfg = TotemConfig.get();
        int w = 220, h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 6;
        int step = 24;

        addRenderableWidget(CycleButton.onOffBuilder(cfg.enabled)
                .create(x, y, w, h, Component.literal("Enabled"), (b, v) -> cfg.enabled = v));
        y += step;

        addRenderableWidget(new DelaySlider(x, y, w, h, cfg));
        y += step;

        addRenderableWidget(CycleButton.onOffBuilder(cfg.replaceOffhandItem)
                .create(x, y, w, h, Component.literal("Replace Offhand Item"), (b, v) -> cfg.replaceOffhandItem = v));
        y += step;

        addRenderableWidget(CycleButton.onOffBuilder(cfg.searchHotbar)
                .create(x, y, w, h, Component.literal("Search Hotbar"), (b, v) -> cfg.searchHotbar = v));
        y += step;

        addRenderableWidget(CycleButton.onOffBuilder(cfg.searchMainInventory)
                .create(x, y, w, h, Component.literal("Search Main Inventory"), (b, v) -> cfg.searchMainInventory = v));
        y += step;

        addRenderableWidget(CycleButton.onOffBuilder(cfg.closeAfterEquip)
                .create(x, y, w, h, Component.literal("Close Inventory After Equip"), (b, v) -> cfg.closeAfterEquip = v));
        y += step + 12;

        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(x, y, w, h).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFFFF);
    }

    @Override
    public void onClose() {
        TotemConfig.save();
        this.minecraft.setScreen(parent);
    }

    /** Slider for 0-1000 ms in 10 ms steps. */
    private static class DelaySlider extends AbstractSliderButton {
        private final TotemConfig cfg;

        DelaySlider(int x, int y, int w, int h, TotemConfig cfg) {
            super(x, y, w, h, Component.empty(), cfg.delayMs / 1000.0);
            this.cfg = cfg;
            updateMessage();
        }

        private int ms() {
            return (int) (Math.round(this.value * 100) * 10);
        }

        @Override
        protected void updateMessage() {
            setMessage(Component.literal("Delay: " + ms() + " ms"));
        }

        @Override
        protected void applyValue() {
            cfg.delayMs = ms();
        }
    }
}
