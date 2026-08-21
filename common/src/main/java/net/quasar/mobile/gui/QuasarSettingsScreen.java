package net.quasar.mobile.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Quasar Mobile - fork modification
 * QuasarSettingsScreen for configuring performance and mobile options.
 */
public class QuasarSettingsScreen extends Screen {
	private final Screen parent;

	public QuasarSettingsScreen(Screen parent) {
		super(Component.literal("Quasar Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		int btnWidth = 200;
		int btnHeight = 20;
		int centerX = this.width / 2 - btnWidth / 2;

		this.addRenderableWidget(Button.builder(Component.literal("Transpiler: Auto (Java Fallback)"), btn -> {}).bounds(centerX, 60, btnWidth, btnHeight).build());
		this.addRenderableWidget(Button.builder(Component.literal("Render Scale: 100%"), btn -> {}).bounds(centerX, 90, btnWidth, btnHeight).build());
		this.addRenderableWidget(Button.builder(Component.literal("Shadow Override: Default"), btn -> {}).bounds(centerX, 120, btnWidth, btnHeight).build());

		this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
			if (this.minecraft != null) this.minecraft.setScreen(parent);
		}).bounds(centerX, 170, btnWidth, btnHeight).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		QuasarBackground.render(guiGraphics, this.width, this.height, delta);
		guiGraphics.drawCenteredString(this.font, Component.literal("QUASAR MOBILE SETTINGS"), this.width / 2, 25, QuasarTheme.QUASAR_CYAN);

		super.render(guiGraphics, mouseX, mouseY, delta);
	}
}
