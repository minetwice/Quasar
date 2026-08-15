package net.quasar.mobile;

import net.irisshaders.iris.Iris;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Quasar Mobile - fork modification
 *
 * Device profiler, performance defaults, async thread pool, and thermal guard.
 */
public class QuasarProfiler {
	private static boolean initialized = false;
	private static int deviceTier = 1;
	private static float renderScale = 1.0f;
	private static int defaultShadowRes = 1024;
	private static ExecutorService compileThreadPool;
	private static float currentTemperature = 35.0f;

	public static synchronized void init() {
		if (initialized) return;

		int cores = Runtime.getRuntime().availableProcessors();
		long maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);

		if (cores <= 4 || maxMemoryMb < 3000) {
			deviceTier = 0;
			defaultShadowRes = 512;
			renderScale = 0.75f;
		} else if (cores <= 6 || maxMemoryMb < 4000) {
			deviceTier = 1;
			defaultShadowRes = 1024;
			renderScale = 0.80f;
		} else if (cores <= 8 || maxMemoryMb < 6000) {
			deviceTier = 2;
			defaultShadowRes = 1024;
			renderScale = 0.90f;
		} else {
			deviceTier = 3;
			defaultShadowRes = 2048;
			renderScale = 1.0f;
		}

		if (QuasarContext.isDesktop()) {
			deviceTier = 3;
			defaultShadowRes = 2048;
			renderScale = 1.0f;
		}

		compileThreadPool = Executors.newFixedThreadPool(Math.max(2, cores));

		initialized = true;

		Iris.logger.info("Profiler: Tier=" + deviceTier + " Cores=" + cores + " RAM=" + maxMemoryMb + "MB DefaultShadowRes=" + defaultShadowRes + " RenderScale=" + renderScale);
	}

	public static int getDeviceTier() {
		if (!initialized) init();
		return deviceTier;
	}

	public static float getRenderScale() {
		if (!initialized) init();
		return renderScale;
	}

	public static void setRenderScale(float scale) {
		renderScale = Math.max(0.25f, Math.min(1.0f, scale));
	}

	public static int getDefaultShadowRes() {
		if (!initialized) init();
		return defaultShadowRes;
	}

	public static ExecutorService getCompileThreadPool() {
		if (!initialized) init();
		return compileThreadPool;
	}

	public static float pollTemperature() {
		try {
			// Read Linux / Android thermal zone if accessible
			File tz = new File("/sys/class/thermal/thermal_zone0/temp");
			if (tz.exists()) {
				String val = Files.readString(tz.toPath()).trim();
				float temp = Float.parseFloat(val);
				if (temp > 1000.0f) temp /= 1000.0f; // millidegrees C
				currentTemperature = temp;
			}
		} catch (Throwable ignored) {
		}

		if (currentTemperature > 45.0f && renderScale > 0.50f) {
			renderScale -= 0.10f;
			Iris.logger.info("[ADAPT] Thermal threshold exceeded (" + currentTemperature + "C), reduced render scale to " + renderScale);
		}

		return currentTemperature;
	}
}
