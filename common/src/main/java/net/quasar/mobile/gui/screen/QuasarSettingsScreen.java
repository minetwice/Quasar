package net.quasar.mobile.gui.screen;

import net.irisshaders.iris.Iris;
import net.quasar.mobile.QuasarCapabilities;
import net.quasar.mobile.QuasarContext;
import net.quasar.mobile.QuasarProfiler;
import net.quasar.mobile.gui.anim.QuasarBackground;
import net.quasar.mobile.gui.theme.QuasarTheme;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Quasar Mobile - fork modification
 *
 * Cosmic-themed Settings screen with tabbed categories (Quality, Performance, Mobile, Debug, About).
 */
public class QuasarSettingsScreen extends Screen {
	private final Screen parent;
	private int activeTab = 0; // 0=Quality, 1=Performance, 2=Mobile, 3=Debug, 4=About

	private static final String[] TAB_NAMES = {"Quality", "Performance", "Mobile", "Debug", "About"};

	public QuasarSettingsScreen(Screen parent) {
		super(Component.literal("Quasar Settings"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		super.init();
		this.clearWidgets();

		int sidebarWidth = 110;
		int tabY = 50;
		int tabHeight = 32;

		// Category Tab Buttons
		for (int i = 0; i < TAB_NAMES.length; i++) {
			final int tabIndex = i;
			this.addRenderableWidget(Button.builder(Component.literal(TAB_NAMES[i]), btn -> {
				activeTab = tabIndex;
				initTabWidgets();
			}).bounds(15, tabY + i * (tabHeight + 6), sidebarWidth, tabHeight).build());
		}

		// Back Button
		this.addRenderableWidget(Button.builder(Component.literal("Back"), btn -> {
			this.minecraft.setScreen(parent);
		}).bounds(15, this.height - 40, sidebarWidth, 30).build());

		initTabWidgets();
	}

	private void initTabWidgets() {
		int contentX = 140;
		int contentY = 50;
		int contentWidth = this.width - contentX - 20;

		switch (activeTab) {
			case 0 -> { // Quality
				this.addRenderableWidget(Button.builder(Component.literal("Render Scale: " + (int)(QuasarProfiler.getRenderScale() * 100) + "%"), btn -> {
					float current = QuasarProfiler.getRenderScale();
					float next = current >= 1.0f ? 0.50f : current + 0.10f;
					QuasarProfiler.setRenderScale(next);
					btn.setMessage(Component.literal("Render Scale: " + (int)(QuasarProfiler.getRenderScale() * 100) + "%"));
				}).bounds(contentX, contentY, contentWidth, 36).build());

				this.addRenderableWidget(Button.builder(Component.literal("Max Shadow Res: " + QuasarProfiler.getDefaultShadowRes()), btn -> {
				}).bounds(contentX, contentY + 46, contentWidth, 36).build());
			}
			case 1 -> { // Performance
				this.addRenderableWidget(Button.builder(Component.literal("Async Compile: ENABLED"), btn -> {
				}).bounds(contentX, contentY, contentWidth, 36).build());

				this.addRenderableWidget(Button.builder(Component.literal("Thermal Guard: ENABLED"), btn -> {
				}).bounds(contentX, contentY + 46, contentWidth, 36).build());
			}
			case 2 -> { // Mobile
				this.addRenderableWidget(Button.builder(Component.literal("Transpiler Mode: AUTO"), btn -> {
				}).bounds(contentX, contentY, contentWidth, 36).build());

				boolean legacy = Iris.getIrisConfig() != null && Iris.getIrisConfig().isUseLegacyUi();
				this.addRenderableWidget(Button.builder(Component.literal("Legacy Iris UI: " + (legacy ? "ON" : "OFF")), btn -> {
					if (Iris.getIrisConfig() != null) {
						Iris.getIrisConfig().setUseLegacyUi(!Iris.getIrisConfig().isUseLegacyUi());
						try {
							Iris.getIrisConfig().save();
						} catch (Exception ignored) {}
						btn.setMessage(Component.literal("Legacy Iris UI: " + (Iris.getIrisConfig().isUseLegacyUi() ? "ON" : "OFF")));
					}
				}).bounds(contentX, contentY + 46, contentWidth, 36).build());
			}
			case 3 -> { // Debug
				boolean debug = Iris.getIrisConfig() != null && Iris.getIrisConfig().areDebugOptionsEnabled();
				this.addRenderableWidget(Button.builder(Component.literal("Debug Options: " + (debug ? "ON" : "OFF")), btn -> {
					boolean next = !Iris.getIrisConfig().areDebugOptionsEnabled();
					Iris.setDebug(next);
					btn.setMessage(Component.literal("Debug Options: " + (next ? "ON" : "OFF")));
				}).bounds(contentX, contentY, contentWidth, 36).build());
			}
			case 4 -> { // About
				// Content rendered in render
			}
		}
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		QuasarBackground.render(graphics, this.width, this.height);

		graphics.centeredText(this.font, Component.literal("✦ QUASAR SETTINGS ✦"), this.width / 2, 15, QuasarTheme.QUASAR_CYAN);

		int contentX = 140;
		int contentY = 50;

		if (activeTab == 4) { // About Tab Info
			graphics.text(this.font, Component.literal("Quasar Shader Engine"), contentX, contentY, QuasarTheme.QUASAR_CYAN, false);
			graphics.text(this.font, Component.literal("Fork of Iris (LGPL-3.0) for Mobile & GLES compatibility."), contentX, contentY + 18, QuasarTheme.STAR_WHITE, false);
			graphics.text(this.font, Component.literal("GLES Context: " + QuasarContext.getGlVersion()), contentX, contentY + 38, QuasarTheme.STAR_WHITE, false);
			graphics.text(this.font, Component.literal("Renderer: " + QuasarContext.getGlRenderer()), contentX, contentY + 54, QuasarTheme.STAR_WHITE, false);
			graphics.text(this.font, Component.literal("Caps: MRT=" + QuasarCapabilities.getMaxDrawBuffers() +
				" float16=" + (QuasarCapabilities.canRenderFloat16() ? "y" : "n") +
				" float32=" + (QuasarCapabilities.canRenderFloat32() ? "y" : "n") +
				" DeviceTier=" + QuasarProfiler.getDeviceTier()), contentX, contentY + 70, QuasarTheme.STAR_WHITE, false);
		}

		super.render(graphics, mouseX, mouseY, delta);
	}
}
