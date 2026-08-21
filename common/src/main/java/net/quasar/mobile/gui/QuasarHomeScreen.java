package net.quasar.mobile.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Quasar Mobile - fork modification
 * QuasarHomeScreen with cosmic theme and pack management controls.
 */
public class QuasarHomeScreen extends Screen {
	private final Screen parent;

	public QuasarHomeScreen(Screen parent) {
		super(Component.literal("Quasar Mobile Shader Engine"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		int btnWidth = 160;
		int btnHeight = 24;
		int centerX = this.width / 2 - btnWidth / 2;

		this.addRenderableWidget(Button.builder(Component.literal("Shader Packs..."), btn -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new net.irisshaders.iris.gui.screen.ShaderPackScreen(this));
			}
		}).bounds(centerX, 80, btnWidth, btnHeight).build());

		this.addRenderableWidget(Button.builder(Component.literal("Quasar Settings"), btn -> {
			if (this.minecraft != null) {
				this.minecraft.setScreen(new QuasarSettingsScreen(this));
			}
		}).bounds(centerX, 115, btnWidth, btnHeight).build());

		this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> {
			if (this.minecraft != null) this.minecraft.setScreen(parent);
		}).bounds(centerX, 160, btnWidth, btnHeight).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		QuasarBackground.render(guiGraphics, this.width, this.height, delta);
		guiGraphics.drawCenteredString(this.font, Component.literal("QUASAR ENGINE"), this.width / 2, 30, QuasarTheme.QUASAR_CYAN);
		guiGraphics.drawCenteredString(this.font, Component.literal("Desktop light, any device."), this.width / 2, 45, QuasarTheme.STAR_WHITE);

		super.render(guiGraphics, mouseX, mouseY, delta);
	}
}
