package net.quasar.mobile.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Quasar Mobile - fork modification
 * QuasarCompatScreen displaying card status of passes and recovery buttons.
 */
public class QuasarCompatScreen extends Screen {
	private final Screen parent;
	private final List<String> statusLines = new ArrayList<>();

	public QuasarCompatScreen(Screen parent, List<String> statusLines) {
		super(Component.literal("Quasar Compatibility"));
		this.parent = parent;
		if (statusLines != null) {
			this.statusLines.addAll(statusLines);
		}
	}

	@Override
	protected void init() {
		super.init();
		int btnY = this.height - 40;
		int widthPerBtn = 110;

		this.addRenderableWidget(Button.builder(Component.literal("Retry Safe Profile"), btn -> {
			if (this.minecraft != null) this.minecraft.setScreen(parent);
		}).bounds(this.width / 2 - 170, btnY, widthPerBtn, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Continue"), btn -> {
			if (this.minecraft != null) this.minecraft.setScreen(parent);
		}).bounds(this.width / 2 - 50, btnY, widthPerBtn, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Copy Log"), btn -> {
			if (this.minecraft != null) {
				this.minecraft.keyboardHandler.setClipboard(String.join("\n", statusLines));
			}
		}).bounds(this.width / 2 + 70, btnY, widthPerBtn, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		this.renderBackground(guiGraphics, mouseX, mouseY, delta);
		guiGraphics.drawCenteredString(this.font, Component.literal("Quasar Mobile Shader Compatibility Report"), this.width / 2, 20, 0x00E5FF);

		int y = 50;
		for (String line : statusLines) {
			if (y > this.height - 60) break;
			int color = line.contains("FAILED") ? 0xFF3DF2 : line.contains("ADAPTED") ? 0xFFD700 : 0x00E5FF;
			guiGraphics.drawString(this.font, line, 30, y, color);
			y += 14;
		}

		super.render(guiGraphics, mouseX, mouseY, delta);
	}
}
