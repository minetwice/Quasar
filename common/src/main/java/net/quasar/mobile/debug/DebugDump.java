package net.quasar.mobile.debug;

import net.irisshaders.iris.Iris;
import net.quasar.mobile.QuasarCapabilities;
import net.quasar.mobile.QuasarContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Quasar Mobile - fork modification
 * Auto debug log dumper writing to <cache>/quasar/debug_dump.txt.
 */
public class DebugDump {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");

	public static void dump(String reason) {
		try {
			Path cacheDir = Iris.getShaderpacksDirectory().getParent().resolve("quasar");
			if (!Files.exists(cacheDir)) {
				Files.createDirectories(cacheDir);
			}
			Path dumpFile = cacheDir.resolve("debug_dump.txt");

			List<String> lines = new ArrayList<>();
			lines.add("=== QUASAR MOBILE DEBUG DUMP ===");
			lines.add("Reason: " + reason);
			lines.add("GL Version: " + QuasarContext.getInstance().getGlVersion());
			lines.add("GL Renderer: " + QuasarContext.getInstance().getGlRenderer());
			lines.add("GL Vendor: " + QuasarContext.getInstance().getGlVendor());
			lines.add("isGLES: " + QuasarContext.getInstance().isGLES());
			lines.add("Max Draw Buffers: " + QuasarCapabilities.getInstance().getMaxDrawBuffers());
			lines.add("Max Texture Size: " + QuasarCapabilities.getInstance().getMaxTextureSize());
			lines.add("\n=== RECOVERY LOGS ===");
			lines.addAll(ErrorRecovery.getRecoveryLogs());

			Files.write(dumpFile, lines);
			LOGGER.info("[Quasar] Debug dump written to " + dumpFile.toAbsolutePath());
		} catch (Throwable t) {
			LOGGER.error("[Quasar] Failed to write debug dump: ", t);
		}
	}
}
