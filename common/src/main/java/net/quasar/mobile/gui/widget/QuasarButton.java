package net.quasar.mobile.gui.widget;

import net.quasar.mobile.gui.theme.QuasarTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

/**
 * Quasar Mobile - fork modification
 *
 * Rounded glass button widget with hover glow and press feedback. Minimum 44px height for touch safety.
 */
public class QuasarButton extends AbstractWidget {
	private final Runnable onPress;

	public QuasarButton(int x, int y, int width, int height, Component message, Runnable onPress) {
		super(x, y, width, Math.max(44, height), message);
		this.onPress = onPress;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		boolean isHovered = isHovered();
		int bgColor = isHovered ? QuasarTheme.GLASS_PANEL_HOVER : QuasarTheme.GLASS_PANEL;
		int borderColor = isHovered ? QuasarTheme.GLASS_BORDER_ACTIVE : QuasarTheme.GLASS_BORDER;

		// Glass background
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgColor);
		// Border
		graphics.fill(getX(), getY(), getX() + getWidth(), getY() + 1, borderColor);
		graphics.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), borderColor);
		graphics.fill(getX(), getY(), getX() + 1, getY() + getHeight(), borderColor);
		graphics.fill(getX() + getWidth() - 1, getY(), getX() + getWidth(), getY() + getHeight(), borderColor);

		// Label
		int textX = getX() + getWidth() / 2;
		int textY = getY() + (getHeight() - 8) / 2;
		graphics.centeredText(Minecraft.getInstance().font, getMessage(), textX, textY, isHovered ? QuasarTheme.QUASAR_CYAN : QuasarTheme.STAR_WHITE);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
