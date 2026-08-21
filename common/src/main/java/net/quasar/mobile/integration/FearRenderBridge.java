package net.quasar.mobile.integration;

import net.quasar.mobile.QuasarContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quasar Mobile - fork modification
 * Fear Render translation layer integration bridge.
 */
public class FearRenderBridge {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static boolean fearRenderActive = false;

	public static void init() {
		String renderer = QuasarContext.getInstance().getGlRenderer();
		if (renderer != null && (renderer.contains("Fear") || renderer.contains("ANGLE") || renderer.contains("Zink") || renderer.contains("VirGL") || renderer.contains("Mesa"))) {
			fearRenderActive = true;
			LOGGER.info("[Quasar][FEAR_RENDER] Translation layer detected (" + renderer + "). Enabling Fear Render optimizations.");
		}
	}

	public static boolean isFearRenderActive() {
		return fearRenderActive;
	}
}
