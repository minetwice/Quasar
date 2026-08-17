package net.quasar.mobile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quasar Mobile - fork modification
 * Hardware profiling and tier calculation (Tier 0-3).
 */
public class QuasarProfiler {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static QuasarProfiler instance;

	private final int deviceTier; // 0: Ultra-low, 1: Low, 2: Medium/Default, 3: Flagship

	private QuasarProfiler() {
		int cores = Runtime.getRuntime().availableProcessors();
		long maxMemoryMb = Runtime.getRuntime().maxMemory() / (1024 * 1024);

		int tier = 2;
		if (cores <= 4 || maxMemoryMb < 3000) {
			tier = 0;
		} else if (cores <= 6 || maxMemoryMb < 5000) {
			tier = 1;
		} else if (cores >= 8 && maxMemoryMb >= 6000) {
			tier = 3;
		}

		this.deviceTier = tier;
		LOGGER.info("[Quasar] Profiler initialized: Cores=" + cores + " | MaxMemory=" + maxMemoryMb + "MB => DeviceTier=" + deviceTier);
	}

	public static synchronized QuasarProfiler getInstance() {
		if (instance == null) {
			instance = new QuasarProfiler();
		}
		return instance;
	}

	public int getDeviceTier() {
		return deviceTier;
	}

	public int getDefaultShadowResolution() {
		return switch (deviceTier) {
			case 0 -> 512;
			case 1 -> 1024;
			case 2 -> 1024;
			case 3 -> 2048;
			default -> 1024;
		};
	}

	public float getDefaultRenderScale() {
		return switch (deviceTier) {
			case 0 -> 0.7f;
			case 1 -> 0.85f;
			default -> 1.0f;
		};
	}
}
