package net.quasar.mobile.gui.screen;

import net.irisshaders.iris.Iris;
import net.quasar.mobile.QuasarRecoveryLadder;
import net.quasar.mobile.gui.QuasarCompatScreen;
import net.quasar.mobile.gui.anim.QuasarAnimator;
import net.quasar.mobile.gui.anim.QuasarBackground;
import net.quasar.mobile.gui.theme.QuasarTheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Quasar Mobile - fork modification
 *
 * Premium cosmic-themed main shader pack selection screen.
 */
public class QuasarHomeScreen extends Screen {
	private final Screen parent;
	private final long openTimeMs;

	private final List<PackCard> packCards = new ArrayList<>();
	private int selectedIndex = -1;
	private float scrollOffset = 0;
	private float scrollVelocity = 0;
	private boolean isDragging = false;
	private double lastTouchY = 0;

	public QuasarHomeScreen(Screen parent) {
		super(Component.literal("QUASAR"));
		this.parent = parent;
		this.openTimeMs = System.currentTimeMillis();
	}

	@Override
	protected void init() {
		super.init();
		packCards.clear();

		// Populate shader pack list
		List<String> packNames = new ArrayList<>();
		try {
			packNames = Iris.getShaderpacksDirectoryManager().enumerate();
		} catch (Exception e) {
			Iris.logger.error("Failed enumerating shader packs", e);
		}

		String currentName = Iris.getCurrentPackName();

		for (int i = 0; i < packNames.size(); i++) {
			String name = packNames.get(i);
			Path p = Iris.getShaderpacksDirectory().resolve(name);
			boolean isCurrent = name.equalsIgnoreCase(currentName);
			if (isCurrent) selectedIndex = i;

			// Status chip logic
			String status = "FULL";
			int statusColor = QuasarTheme.QUASAR_CYAN;
			Integer winLevel = QuasarRecoveryLadder.getWinningLevels().get(name);
			if (winLevel != null && winLevel > 1) {
				status = "ADAPTED L" + winLevel;
				statusColor = QuasarTheme.NEBULA_PURPLE;
			}
			if (QuasarRecoveryLadder.getFailedPasses().containsKey(name)) {
				status = "PARTIAL";
				statusColor = QuasarTheme.MAGENTA_ACC;
			}

			packCards.add(new PackCard(name, p, isCurrent, status, statusColor));
		}

		int sidebarWidth = Math.min(200, this.width / 3);
		int sidebarX = this.width - sidebarWidth - 15;
		int btnY = 50;
		int btnHeight = 36;
		int btnGap = 10;

		// Toggle Shaders Button
		boolean shadersEnabled = Iris.getIrisConfig() != null && Iris.getIrisConfig().areShadersEnabled();
		this.addRenderableWidget(Button.builder(Component.literal("Shaders: " + (shadersEnabled ? "ON" : "OFF")), btn -> {
			try {
				Iris.toggleShaders(this.minecraft, !Iris.getIrisConfig().areShadersEnabled());
				btn.setMessage(Component.literal("Shaders: " + (Iris.getIrisConfig().areShadersEnabled() ? "ON" : "OFF")));
			} catch (Exception e) {
				Iris.logger.error("Failed toggling shaders", e);
			}
		}).bounds(sidebarX, btnY, sidebarWidth, btnHeight).build());

		btnY += btnHeight + btnGap;

		// Settings Button
		this.addRenderableWidget(Button.builder(Component.literal("Settings"), btn -> {
			this.minecraft.setScreen(new QuasarSettingsScreen(this));
		}).bounds(sidebarX, btnY, sidebarWidth, btnHeight).build());

		btnY += btnHeight + btnGap;

		// Reload Packs
		this.addRenderableWidget(Button.builder(Component.literal("Reload Packs"), btn -> {
			try {
				Iris.reload();
			} catch (Exception e) {
				Iris.logger.error("Failed reloading packs", e);
			}
		}).bounds(sidebarX, btnY, sidebarWidth, btnHeight).build());

		btnY += btnHeight + btnGap;

		// Compat Report
		this.addRenderableWidget(Button.builder(Component.literal("Compat Report"), btn -> {
			this.minecraft.setScreen(new QuasarCompatScreen(this, QuasarRecoveryLadder.getFailedPasses()));
		}).bounds(sidebarX, btnY, sidebarWidth, btnHeight).build());

		btnY += btnHeight + btnGap;

		// Done / Back
		this.addRenderableWidget(Button.builder(Component.literal("Done"), btn -> {
			this.minecraft.setScreen(parent);
		}).bounds(sidebarX, this.height - 45, sidebarWidth, btnHeight).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		// 1. Cosmic background
		QuasarBackground.render(graphics, this.width, this.height);

		// 2. Animated header title with cyan glow
		float titleAnim = QuasarAnimator.easeOutBack(QuasarAnimator.getProgress(openTimeMs, 600));
		int titleY = (int) (15 + (1.0f - titleAnim) * -20);
		graphics.centeredText(this.font, Component.literal("✦ QUASAR ✦"), this.width / 2, titleY, QuasarTheme.QUASAR_CYAN);

		// 3. Render Pack Cards List (Left panel)
		int listX = 15;
		int listY = 50;
		int listWidth = this.width - Math.min(200, this.width / 3) - 45;
		int listHeight = this.height - 65;

		int cardHeight = 48;
		int cardGap = 8;

		// Apply inertia scroll
		scrollOffset += scrollVelocity;
		scrollVelocity *= 0.85f;
		if (Math.abs(scrollVelocity) < 0.05f) scrollVelocity = 0;

		float maxScroll = Math.max(0, packCards.size() * (cardHeight + cardGap) - listHeight);
		scrollOffset = Math.max(0, Math.min(maxScroll, scrollOffset));

		int startY = listY - (int) scrollOffset;

		for (int i = 0; i < packCards.size(); i++) {
			PackCard card = packCards.get(i);
			int cy = startY + i * (cardHeight + cardGap);
			if (cy + cardHeight < listY || cy > listY + listHeight) continue;

			boolean isHovered = mouseX >= listX && mouseX <= listX + listWidth && mouseY >= cy && mouseY <= cy + cardHeight;
			int bgColor = isHovered ? QuasarTheme.GLASS_PANEL_HOVER : QuasarTheme.GLASS_PANEL;
			int borderColor = (i == selectedIndex) ? QuasarTheme.GLASS_BORDER_ACTIVE : (isHovered ? QuasarTheme.GLASS_BORDER : QuasarTheme.withAlpha(QuasarTheme.GLASS_BORDER, 0.3f));

			// Glass card panel fill
			graphics.fill(listX, cy, listX + listWidth, cy + cardHeight, bgColor);
			// 1px border
			graphics.fill(listX, cy, listX + listWidth, cy + 1, borderColor);
			graphics.fill(listX, cy + cardHeight - 1, listX + listWidth, cy + cardHeight, borderColor);
			graphics.fill(listX, cy, listX + 1, cy + cardHeight, borderColor);
			graphics.fill(listX + listWidth - 1, cy, listX + listWidth, cy + cardHeight, borderColor);

			// Pack Name
			graphics.text(this.font, Component.literal(card.name), listX + 12, cy + 10, QuasarTheme.STAR_WHITE, false);

			// Status Chip
			int chipWidth = 70;
			int chipX = listX + listWidth - chipWidth - 10;
			graphics.fill(chipX, cy + 12, chipX + chipWidth, cy + 32, QuasarTheme.withAlpha(card.statusColor, 0.2f));
			graphics.centeredText(this.font, Component.literal("[" + card.status + "]"), chipX + chipWidth / 2, cy + 17, card.statusColor);
		}

		super.render(graphics, mouseX, mouseY, delta);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		double mouseX = event.x();
		double mouseY = event.y();
		int listX = 15;
		int listY = 50;
		int listWidth = this.width - Math.min(200, this.width / 3) - 45;
		int cardHeight = 48;
		int cardGap = 8;

		if (mouseX >= listX && mouseX <= listX + listWidth && mouseY >= listY && mouseY <= this.height - 15) {
			isDragging = true;
			lastTouchY = mouseY;

			int clickedIndex = (int) ((mouseY - listY + scrollOffset) / (cardHeight + cardGap));
			if (clickedIndex >= 0 && clickedIndex < packCards.size()) {
				selectedIndex = clickedIndex;
				PackCard card = packCards.get(clickedIndex);
				if (Iris.getIrisConfig() != null) {
					Iris.getIrisConfig().setShaderPackName(card.name);
					try {
						Iris.getIrisConfig().save();
						Iris.reload();
					} catch (Exception e) {
						Iris.logger.error("Failed selecting pack " + card.name, e);
					}
				}
				return true;
			}
		}

		return super.mouseClicked(event, doubleClick);
	}

	private record PackCard(String name, Path path, boolean selected, String status, int statusColor) {
	}
}
