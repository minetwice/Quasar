package net.quasar.mobile.gui;

import net.minecraft.client.gui.GuiGraphics;

/**
 * Quasar Mobile - fork modification
 * Animated cosmic background with stars and nebula gradient.
 */
public class QuasarBackground {
	public static void render(GuiGraphics graphics, int width, int height, float delta) {
		// Fill background space black
		graphics.fill(0, 0, width, height, QuasarTheme.SPACE_BLACK);

		long time = System.currentTimeMillis();

		// Parallax star points
		for (int i = 0; i < 40; i++) {
			int x = (int) ((i * 37 + time / 50) % width);
			int y = (int) ((i * 59 + time / 70) % height);
			int alpha = (int) (100 + 155 * Math.sin(time / 300.0 + i));
			int color = (alpha << 24) | 0xF5F7FF;
			graphics.fill(x, y, x + 2, y + 2, color);
		}

		// Top nebula highlight line
		graphics.fill(0, 0, width, 3, QuasarTheme.QUASAR_CYAN);
	}
}
