package net.quasar.mobile.gui.widget;

import net.quasar.mobile.gui.theme.QuasarTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;

/**
 * Quasar Mobile - fork modification
 *
 * Glass popup dropdown selection widget. Minimum 44px touch height.
 */
public class QuasarDropdown extends AbstractWidget {
	private final List<String> options;
	private int selectedIndex;
	private final Consumer<Integer> onSelect;

	public QuasarDropdown(int x, int y, int width, int height, Component message, List<String> options, int initialIndex, Consumer<Integer> onSelect) {
		super(x, y, width, Math.max(44, height), message);
		this.options = options;
		this.selectedIndex = initialIndex >= 0 && initialIndex < options.size() ? initialIndex : 0;
		this.onSelect = onSelect;
	}

	@Override
	protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		int boxX = getX() + getWidth() - 110;
		int boxY = getY() + (getHeight() - 28) / 2;
		int boxWidth = 100;
		int boxHeight = 28;

		graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, QuasarTheme.GLASS_PANEL);
		graphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, QuasarTheme.GLASS_BORDER);
		graphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, QuasarTheme.GLASS_BORDER);
		graphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, QuasarTheme.GLASS_BORDER);
		graphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, QuasarTheme.GLASS_BORDER);

		String currentVal = options.isEmpty() ? "None" : options.get(selectedIndex);
		graphics.text(Minecraft.getInstance().font, getMessage(), getX() + 10, boxY + 8, QuasarTheme.STAR_WHITE, false);
		graphics.centeredText(Minecraft.getInstance().font, Component.literal(currentVal), boxX + boxWidth / 2, boxY + 8, QuasarTheme.QUASAR_CYAN);
	}

	@Override
	protected void updateWidgetNarration(NarrationElementOutput output) {
		defaultButtonNarrationText(output);
	}
}
