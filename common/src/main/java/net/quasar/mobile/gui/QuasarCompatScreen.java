package net.quasar.mobile.gui;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gui.element.screen.IrisButton;
import net.quasar.mobile.QuasarRecoveryLadder;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.TextAlignment;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineLabel;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.Map;

/**
 * Quasar Mobile - fork modification
 *
 * Quasar Compatibility Report UI replacing crash screens for shader failures.
 */
public class QuasarCompatScreen extends Screen {
	private final Screen parent;
	private final Map<String, String> failedPasses;
	private MultiLineLabel messageLabel;

	public QuasarCompatScreen(Screen parent, Map<String, String> failedPasses) {
		super(Component.literal("Quasar Compatibility Report"));
		this.parent = parent;
		this.failedPasses = failedPasses != null ? failedPasses : QuasarRecoveryLadder.getFailedPasses();
	}

	@Override
	protected void init() {
		super.init();

		StringBuilder sb = new StringBuilder("The following shader passes had compatibility issues:\n");
		if (failedPasses.isEmpty()) {
			sb.append("- No critical pass failures recorded.");
		} else {
			for (Map.Entry<String, String> entry : failedPasses.entrySet()) {
				String pass = entry.getKey();
				String reason = entry.getValue();
				if (reason.length() > 80) {
					reason = reason.substring(0, 80) + "...";
				}
				sb.append("• ").append(pass).append(": ").append(reason).append("\n");
			}
		}

		this.messageLabel = MultiLineLabel.create(this.font, this.width - 60, Component.literal(sb.toString()));

		int btnWidth = 140;
		int btnY = this.height - 40;
		int spacing = 10;
		int totalWidth = btnWidth * 3 + spacing * 2;
		int startX = (this.width - totalWidth) / 2;

		this.addRenderableWidget(Button.builder(Component.literal("Retry (Safe Profile)"), btn -> {
			Iris.resetShaderPackOptionsOnNextReload();
			try {
				Iris.reload();
			} catch (Exception e) {
				Iris.logger.error("Failed reloading shaders with safe profile", e);
			}
			this.minecraft.setScreen(parent);
		}).bounds(startX, btnY, btnWidth, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Continue Anyway"), btn -> {
			this.minecraft.setScreen(parent);
		}).bounds(startX + btnWidth + spacing, btnY, btnWidth, 20).build());

		this.addRenderableWidget(Button.builder(Component.literal("Copy Log"), btn -> {
			StringBuilder logBuilder = new StringBuilder("=== Quasar Compatibility Report ===\n");
			for (Map.Entry<String, String> entry : failedPasses.entrySet()) {
				logBuilder.append("Pass: ").append(entry.getKey()).append("\nDetails: ").append(entry.getValue()).append("\n\n");
			}
			this.minecraft.keyboardHandler.setClipboard(logBuilder.toString());
		}).bounds(startX + (btnWidth + spacing) * 2, btnY, btnWidth, 20).build());
	}

	@Override
	public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
		this.extractBackground(guiGraphics, mouseX, mouseY, delta);
		ActiveTextCollector textCollector = guiGraphics.textRenderer();
		guiGraphics.centeredText(this.font, this.title, this.width / 2, 20, 0xFFFF5555);
		if (messageLabel != null) {
			messageLabel.visitLines(TextAlignment.CENTER, this.width / 2, 45, 11, textCollector);
		}
		super.render(guiGraphics, mouseX, mouseY, delta);
	}
}
