package net.irisshaders.iris.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gui.screen.ShaderPackScreen;
import net.quasar.mobile.gui.screen.QuasarHomeScreen;

public class ModMenuIntegration implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return parent -> {
			if (Iris.getIrisConfig() != null && Iris.getIrisConfig().isUseLegacyUi()) {
				return new ShaderPackScreen(parent);
			}
			return new QuasarHomeScreen(parent);
		};
	}
}
