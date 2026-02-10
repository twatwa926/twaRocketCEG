package com.example.examplemod.client;

import com.example.examplemod.network.RocketLaunchPacket;
import com.example.examplemod.network.RocketNetwork;
import com.example.examplemod.network.RocketProgramUpdatePacket;
import com.example.examplemod.rocket.RocketAvionicsMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class RocketAvionicsScreen extends AbstractContainerScreen<RocketAvionicsMenu> {
    private static final String DEFAULT_PROGRAM = "target_orbit=2000\nturn_start=200\nturn_end=1200\nmax_thrust=1.0";
    private EditBox targetOrbitBox;
    private EditBox turnStartBox;
    private EditBox turnEndBox;
    private EditBox maxThrustBox;

    public RocketAvionicsScreen(RocketAvionicsMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos + 12;
        int top = this.topPos + 32;

        addRenderableWidget(Button.builder(Component.literal("重新组装"), button -> triggerAction(0))
                .bounds(left, top, 72, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("航电准备"), button -> triggerAction(1))
                .bounds(left, top + 24, 72, 20)
                .build());
        addRenderableWidget(Button.builder(Component.literal("发射"), button -> triggerAction(2))
                .bounds(left, top + 48, 72, 20)
                .build());

        int scriptLeft = left + 84;
        int lineHeight = 22;
        int boxTop = top;
        targetOrbitBox = new EditBox(this.font, scriptLeft, boxTop, 80, 16, Component.literal("目标轨道"));
        targetOrbitBox.setValue("2000");
        addRenderableWidget(targetOrbitBox);

        turnStartBox = new EditBox(this.font, scriptLeft, boxTop + lineHeight, 80, 16, Component.literal("转向起始"));
        turnStartBox.setValue("200");
        addRenderableWidget(turnStartBox);

        turnEndBox = new EditBox(this.font, scriptLeft, boxTop + lineHeight * 2, 80, 16, Component.literal("转向结束"));
        turnEndBox.setValue("1200");
        addRenderableWidget(turnEndBox);

        maxThrustBox = new EditBox(this.font, scriptLeft, boxTop + lineHeight * 3, 80, 16, Component.literal("最大推力"));
        maxThrustBox.setValue("1.0");
        addRenderableWidget(maxThrustBox);
    }

    private void triggerAction(int id) {
        if (this.minecraft == null) return;
        if (id == 0) {
            if (this.minecraft.gameMode != null) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
            }
            return;
        }
        if (this.menu.getShipId() >= 0L) {
            RocketNetwork.CHANNEL.sendToServer(new RocketProgramUpdatePacket(
                    this.menu.getShipId(),
                    buildProgramScript(),
                    id == 2));
        } else {
            RocketNetwork.CHANNEL.sendToServer(new RocketProgramUpdatePacket(
                    this.menu.getOrigin(),
                    buildProgramScript(),
                    id == 2));
        }
        if (id == 1 && this.minecraft.player != null) {
            this.minecraft.player.displayClientMessage(Component.translatable("message.rocketwa.avionics_prepared"), true);
        }
        if (id == 2 && this.minecraft.player != null) {
            if (this.menu.getShipId() >= 0L) {
                RocketNetwork.CHANNEL.sendToServer(new RocketLaunchPacket(this.menu.getShipId()));
            } else if (this.menu.getOrigin() != null) {
                RocketNetwork.CHANNEL.sendToServer(new RocketLaunchPacket(this.menu.getOrigin()));
            }
            this.minecraft.player.displayClientMessage(Component.translatable("message.rocketwa.launch_sent"), true);
        }
    }

    private String buildProgramScript() {
        if (targetOrbitBox == null || turnStartBox == null || turnEndBox == null || maxThrustBox == null) {
            return DEFAULT_PROGRAM;
        }
        return "target_orbit=" + targetOrbitBox.getValue().trim() + "\n"
                + "turn_start=" + turnStartBox.getValue().trim() + "\n"
                + "turn_end=" + turnEndBox.getValue().trim() + "\n"
                + "max_thrust=" + maxThrustBox.getValue().trim();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xCC0B0F1A);
        guiGraphics.fill(leftPos + 6, topPos + 6, leftPos + imageWidth - 6, topPos + imageHeight - 6, 0xFF1B2234);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(this.font, this.title, 10, 8, 0xE4EEF9, false);
        int rightCol = 96;
        int rowH = 22;
        int firstLabelY = 22;
        guiGraphics.drawString(this.font, Component.literal("目标轨道"), rightCol, firstLabelY, 0x6D89B3, false);
        guiGraphics.drawString(this.font, Component.literal("转向起始"), rightCol, firstLabelY + rowH, 0x6D89B3, false);
        guiGraphics.drawString(this.font, Component.literal("转向结束"), rightCol, firstLabelY + rowH * 2, 0x6D89B3, false);
        guiGraphics.drawString(this.font, Component.literal("最大推力"), rightCol, firstLabelY + rowH * 3, 0x6D89B3, false);
    }
}
