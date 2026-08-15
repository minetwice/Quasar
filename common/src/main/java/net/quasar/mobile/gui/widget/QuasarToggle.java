package net.quasar.mobile.gui.widget;

import net.quasar.mobile.gui.theme.QuasarTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;

/**
 * Quasar Mobile - fork modification
 *
 * Animated toggle switch widget with glow. Minimum 44px touch target.
 */
public class QuasarToggle extends AbstractWidget {
	private boolean value;
	private final Consumer<Boolean> onChange;

	public QuasarToggle(int x, int y, int width, int height, Component message, boolean initialValue, Consumer<Boolean> onChange) {
		super(x, y, width, Math.max(44, height), message);
		this.value = initialValue;
		this.onChange = onChange;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int trackWidth = 50;
		int trackHeight = 24;
		int trackX = getX() + getWidth() - trackWidth - 10;
		int trackY = getY() + (getHeight() - trackHeight) / 2;

		int trackBg = value ? QuasarTheme.withAlpha(QuasarTheme.QUASAR_CYAN, 0.3f) : QuasarTheme.GLASS_PANEL;
		int borderColor = value ? QuasarTheme.QUASAR_CYAN : QuasarTheme.GLASS_BORDER;

		// Track fill
		graphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, trackBg);
		graphics.fill(trackX, trackY, trackX + trackWidth, trackY + 1, borderColor);
		graphics.fill(trackX, trackY + trackHeight - 1, trackX + trackWidth, trackY + trackHeight, borderColor);
		graphics.fill(trackX, trackY, trackX + 1, trackY + trackHeight, borderColor);
		graphics.fill(trackX + trackWidth - 1, trackY, trackX + trackWidth, trackY + trackHeight, borderColor);

		// Knob
		int knobSize = trackHeight - 4;
		int knobX = value ? (trackX + trackWidth - knobSize - 2) : (trackX + 2);
		int knobY = trackY + 2;
		int knobColor = value ? QuasarTheme.QUASAR_CYAN : QuasarTheme.STAR_WHITE;

		graphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, knobColor);

		// Text label on left
		int textY = getY() + (getHeight() - 8) / 2;
		graphics.text(Minecraft.getInstance().font, getMessage(), getX() + 10, textY, QuasarTheme.STAR_WHITE, false);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
