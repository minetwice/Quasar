package net.quasar.mobile.gui.anim;

/**
 * Quasar Mobile - fork modification
 *
 * Time-based easing and animation system for Quasar UI.
 */
public class QuasarAnimator {

	public static float easeOutCubic(float x) {
		float x1 = 1.0f - x;
		return 1.0f - x1 * x1 * x1;
	}

	public static float easeInOutSine(float x) {
		return (float) (-(Math.cos(Math.PI * x) - 1.0) / 2.0);
	}

	public static float easeOutBack(float x) {
		float c1 = 1.70158f;
		float c3 = c1 + 1.0f;
		float x1 = x - 1.0f;
		return 1.0f + c3 * x1 * x1 * x1 + c1 * x1 * x1;
	}

	public static float easeOutElastic(float x) {
		float c4 = (float) ((2.0 * Math.PI) / 3.0);
		if (x == 0) return 0;
		if (x == 1.0f) return 1.0f;
		return (float) (Math.pow(2.0, -10.0 * x) * Math.sin((x * 10.0f - 0.75f) * c4) + 1.0);
	}

	public static float getProgress(long openTimeMs, long durationMs, long delayMs) {
		long elapsed = System.currentTimeMillis() - openTimeMs - delayMs;
		if (elapsed <= 0) return 0.0f;
		if (elapsed >= durationMs) return 1.0f;
		return (float) elapsed / (float) durationMs;
	}

	public static float getProgress(long openTimeMs, long durationMs) {
		return getProgress(openTimeMs, durationMs, 0);
	}
}
