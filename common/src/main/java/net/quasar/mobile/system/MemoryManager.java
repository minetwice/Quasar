package net.quasar.mobile.system;

import net.irisshaders.iris.Iris;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quasar Mobile - fork modification
 * Memory usage tracking and OOM prevention manager.
 */
public class MemoryManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static MemoryManager instance;

	private MemoryManager() {
	}

	public static synchronized MemoryManager getInstance() {
		if (instance == null) {
			instance = new MemoryManager();
		}
		return instance;
	}

	public void checkMemory() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		long allocatedMemory = runtime.totalMemory() - runtime.freeMemory();
		double usageRatio = (double) allocatedMemory / maxMemory;

		if (usageRatio > 0.90) {
			LOGGER.warn("[Quasar][MEMORY] Memory usage critical (" + (int) (usageRatio * 100) + "%). Disabling shader pack to prevent OOM.");
			try {
				Iris.getIrisConfig().setShadersEnabled(false);
				Iris.getIrisConfig().save();
			} catch (Throwable t) {
				LOGGER.error("[Quasar][MEMORY] Failed to disable shaders on critical memory state: ", t);
			}
		} else if (usageRatio > 0.80) {
			LOGGER.warn("[Quasar][MEMORY] Memory usage high (" + (int) (usageRatio * 100) + "%). Suggesting garbage collection.");
			System.gc();
		}
	}
}
