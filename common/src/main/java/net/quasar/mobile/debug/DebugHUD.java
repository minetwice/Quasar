package net.quasar.mobile.debug;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.quasar.mobile.QuasarContext;
import net.quasar.mobile.gui.QuasarTheme;
import net.quasar.mobile.system.PerformanceGovernor;
import net.quasar.mobile.system.ThermalManager;

/**
 * Quasar Mobile - fork modification
 * Debug HUD overlay toggled via F3+Q displaying active engine status.
 */
public class DebugHUD {
	private static boolean visible = false;

	public static void toggle() {
		visible = !visible;
	}

	public static boolean isVisible() {
		return visible;
	}

	public static void render(GuiGraphics graphics) {
		if (!visible) return;

		Minecraft client = Minecraft.getInstance();
		if (client == null || client.font == null) return;

		QuasarContext ctx = QuasarContext.getInstance();
		PerformanceGovernor gov = PerformanceGovernor.getInstance();
		ThermalManager thermal = ThermalManager.getInstance();

		int y = 10;
		int x = 10;
		int color = QuasarTheme.QUASAR_CYAN;

		graphics.drawString(client.font, "[QUASAR MOBILE DEBUG OVERLAY]", x, y, QuasarTheme.MAGENTA); y += 12;
		graphics.drawString(client.font, "GL Version: " + ctx.getGlVersion(), x, y, color); y += 10;
		graphics.drawString(client.font, "Renderer: " + ctx.getGlRenderer(), x, y, color); y += 10;
		graphics.drawString(client.font, "Tier: " + gov.getCurrentTier() + " | FPS: " + String.format("%.1f", gov.getRollingFps()), x, y, color); y += 10;
		graphics.drawString(client.font, "Temp: " + String.format("%.1f", thermal.getCurrentTemperatureCelsius()) + "°C", x, y, color); y += 10;
	}
}
