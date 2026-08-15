package net.quasar.mobile.gui.theme;

/**
 * Quasar Mobile - fork modification
 *
 * Design Language palette constants for the Quasar UI.
 */
public class QuasarTheme {
	public static final int SPACE_BLACK = 0xFF0B0D17;
	public static final int SPACE_BG_BOTTOM = 0xFF1A0B2E;
	public static final int NEBULA_PURPLE = 0xFF7B2FBE;
	public static final int QUASAR_CYAN = 0xFF00E5FF;
	public static final int STAR_WHITE = 0xFFF5F7FF;
	public static final int MAGENTA_ACC = 0xFFFF3DF2;
	public static final int GLASS_PANEL = 0x90101430;
	public static final int GLASS_PANEL_HOVER = 0xB01A1E48;
	public static final int GLASS_BORDER = 0x6000E5FF;
	public static final int GLASS_BORDER_ACTIVE = 0xFF00E5FF;

	public static int withAlpha(int color, float alpha) {
		int a = Math.min(255, Math.max(0, (int) (alpha * 255)));
		return (a << 24) | (color & 0x00FFFFFF);
	}
}
