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
 * Gradient fill track slider widget with glowing thumb. Minimum 44px height.
 */
public class QuasarSlider extends AbstractWidget {
	private float value; // 0.0 to 1.0
	private final Consumer<Float> onChange;

	public QuasarSlider(int x, int y, int width, int height, Component message, float initialValue, Consumer<Float> onChange) {
		super(x, y, width, Math.max(44, height), message);
		this.value = Math.max(0.0f, Math.min(1.0f, initialValue));
		this.onChange = onChange;
	}

	private void setValueFromMouse(double mouseX) {
		int trackX = getX() + 120;
		int trackWidth = getWidth() - 130;
		if (trackWidth <= 0) return;

		float newVal = (float) (mouseX - trackX) / (float) trackWidth;
		this.value = Math.max(0.0f, Math.min(1.0f, newVal));
		if (onChange != null) {
			onChange.accept(this.value);
		}
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int trackX = getX() + 120;
		int trackWidth = getWidth() - 130;
		int trackY = getY() + (getHeight() - 8) / 2;
		int trackHeight = 8;

		// Label
		graphics.text(Minecraft.getInstance().font, getMessage(), getX() + 10, trackY, QuasarTheme.STAR_WHITE, false);

		// Track base
		graphics.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, QuasarTheme.GLASS_PANEL);

		// Track fill
		int fillWidth = (int) (trackWidth * value);
		if (fillWidth > 0) {
			graphics.fillGradient(trackX, trackY, trackX + fillWidth, trackY + trackHeight, QuasarTheme.NEBULA_PURPLE, QuasarTheme.QUASAR_CYAN);
		}

		// Thumb
		int thumbX = trackX + fillWidth - 6;
		int thumbY = trackY - 4;
		graphics.fill(thumbX, thumbY, thumbX + 12, thumbY + 16, QuasarTheme.QUASAR_CYAN);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
