package net.quasar.mobile.gui.anim;

import net.quasar.mobile.gui.theme.QuasarTheme;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Random;

/**
 * Quasar Mobile - fork modification
 *
 * Animated space background with parallax starfields and drifting nebula.
 * Pure DrawContext/GuiGraphicsExtractor rendering, Android/GLES safe.
 */
public class QuasarBackground {
	private static final int STAR_COUNT_L1 = 60;
	private static final int STAR_COUNT_L2 = 40;
	private static final int STAR_COUNT_L3 = 25;

	private static final float[] starX1 = new float[STAR_COUNT_L1];
	private static final float[] starY1 = new float[STAR_COUNT_L1];
	private static final float[] starX2 = new float[STAR_COUNT_L2];
	private static final float[] starY2 = new float[STAR_COUNT_L2];
	private static final float[] starX3 = new float[STAR_COUNT_L3];
	private static final float[] starY3 = new float[STAR_COUNT_L3];

	private static boolean initialized = false;

	private static synchronized void initStars() {
		if (initialized) return;
		Random rng = new Random(1337);

		for (int i = 0; i < STAR_COUNT_L1; i++) {
			starX1[i] = rng.nextFloat();
			starY1[i] = rng.nextFloat();
		}
		for (int i = 0; i < STAR_COUNT_L2; i++) {
			starX2[i] = rng.nextFloat();
			starY2[i] = rng.nextFloat();
		}
		for (int i = 0; i < STAR_COUNT_L3; i++) {
			starX3[i] = rng.nextFloat();
			starY3[i] = rng.nextFloat();
		}
		initialized = true;
	}

	public static void render(GuiGraphicsExtractor graphics, int width, int height) {
		initStars();
		long time = System.currentTimeMillis();

		// 1. Base gradient
		graphics.fillGradient(0, 0, width, height, QuasarTheme.SPACE_BLACK, QuasarTheme.SPACE_BG_BOTTOM);

		// 2. Drifting soft nebula
		float nebulaTime = (time % 60000) / 60000.0f;
		int nebulaX = (int) (width * 0.5f + Math.sin(nebulaTime * Math.PI * 2) * (width * 0.2f));
		int nebulaY = (int) (height * 0.4f + Math.cos(nebulaTime * Math.PI * 2) * (height * 0.15f));
		int nebulaRadius = Math.min(width, height) / 2;

		graphics.fillGradient(nebulaX - nebulaRadius, nebulaY - nebulaRadius, nebulaX + nebulaRadius, nebulaY + nebulaRadius,
			QuasarTheme.withAlpha(QuasarTheme.NEBULA_PURPLE, 0.15f), 0x00000000);

		// 3. Render 3 parallax star layers
		renderStarLayer(graphics, width, height, starX1, starY1, STAR_COUNT_L1, time, 1, 0.00005f, 1);
		renderStarLayer(graphics, width, height, starX2, starY2, STAR_COUNT_L2, time, 2, 0.00008f, 2);
		renderStarLayer(graphics, width, height, starX3, starY3, STAR_COUNT_L3, time, 3, 0.00012f, 2);
	}

	private static void renderStarLayer(GuiGraphicsExtractor graphics, int width, int height, float[] starX, float[] starY, int count, long time, int layer, float speed, int size) {
		for (int i = 0; i < count; i++) {
			float x = (starX[i] + time * speed * (layer * 0.5f)) % 1.0f;
			float y = (starY[i] + time * speed * 0.2f) % 1.0f;

			int px = (int) (x * width);
			int py = (int) (y * height);

			float twinkle = (float) (0.4f + 0.6f * Math.abs(Math.sin((time * 0.002f) + i * 1.7f)));
			int color = QuasarTheme.withAlpha(QuasarTheme.STAR_WHITE, twinkle);

			graphics.fill(px, py, px + size, py + size, color);
		}
	}
}
