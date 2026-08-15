package net.quasar.mobile.gui;

import net.irisshaders.iris.Iris;
import net.quasar.mobile.QuasarContext;
import net.quasar.mobile.QuasarProfiler;
import net.quasar.mobile.QuasarRecoveryLadder;
import net.quasar.mobile.gui.theme.QuasarTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Quasar Mobile - fork modification
 *
 * Debug overlay HUD top-left glass panel for mobile performance & transpiler diagnostics.
 */
public class QuasarDebugOverlay {
	private static boolean enabled = false;

	public static boolean isEnabled() {
		return enabled;
	}

	public static void setEnabled(boolean value) {
		enabled = value;
	}

	public static void toggle() {
		enabled = !enabled;
	}

	public static void render(GuiGraphicsExtractor graphics) {
		if (!enabled) return;

		Minecraft mc = Minecraft.getInstance();
		int panelWidth = 210;
		int panelHeight = 85;
		int x = 10;
		int y = 10;

		// Glass background
		graphics.fill(x, y, x + panelWidth, y + panelHeight, QuasarTheme.GLASS_PANEL);
		graphics.fill(x, y, x + panelWidth, y + 1, QuasarTheme.GLASS_BORDER);
		graphics.fill(x, y + panelHeight - 1, x + panelWidth, y + panelHeight, QuasarTheme.GLASS_BORDER);
		graphics.fill(x, y, x + 1, y + panelHeight, QuasarTheme.GLASS_BORDER);
		graphics.fill(x + panelWidth - 1, y, x + panelWidth, y + panelHeight, QuasarTheme.GLASS_BORDER);

		int fps = mc.getFps();
		String packName = Iris.getCurrentPackName() != null ? Iris.getCurrentPackName() : "None";
		float temp = QuasarProfiler.pollTemperature();
		int tier = QuasarProfiler.getDeviceTier();

		int lineY = y + 6;
		graphics.text(mc.font, "Quasar Debug HUD", x + 8, lineY, QuasarTheme.QUASAR_CYAN, false); lineY += 12;
		graphics.text(mc.font, "FPS: " + fps + " | Temp: " + String.format("%.1f°C", temp), x + 8, lineY, QuasarTheme.STAR_WHITE, false); lineY += 12;
		graphics.text(mc.font, "ES: " + QuasarContext.getEsVersionString() + " | Tier: " + tier, x + 8, lineY, QuasarTheme.STAR_WHITE, false); lineY += 12;
		graphics.text(mc.font, "GPU: " + shortenRenderer(QuasarContext.getGlRenderer()), x + 8, lineY, QuasarTheme.STAR_WHITE, false); lineY += 12;
		graphics.text(mc.font, "Pack: " + packName, x + 8, lineY, QuasarTheme.STAR_WHITE, false);
	}

	private static String shortenRenderer(String r) {
		if (r == null) return "Unknown";
		if (r.length() > 25) return r.substring(0, 22) + "...";
		return r;
	}
}
