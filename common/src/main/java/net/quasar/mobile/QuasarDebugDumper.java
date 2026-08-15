package net.quasar.mobile;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.platform.IrisPlatformHelpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

/**
 * Quasar Mobile - fork modification
 *
 * Automatic debug dump writer for failure diagnostics.
 */
public class QuasarDebugDumper {

	public static void dump(String reason) {
		try {
			Path dumpFile = IrisPlatformHelpers.getInstance().getGameDir().resolve("quasar_shaders").resolve("debug_dump.txt");

			StringBuilder sb = new StringBuilder();
			sb.append("=== QUASAR DEBUG DUMP ===\n");
			sb.append("Timestamp: ").append(Instant.now()).append("\n");
			sb.append("Reason: ").append(reason).append("\n\n");

			sb.append("--- CONTEXT ---\n");
			sb.append("Version: ").append(QuasarContext.getGlVersion()).append("\n");
			sb.append("Renderer: ").append(QuasarContext.getGlRenderer()).append("\n");
			sb.append("Vendor: ").append(QuasarContext.getGlVendor()).append("\n");
			sb.append("isGLES: ").append(QuasarContext.isGLES()).append(" (ES ").append(QuasarContext.getEsVersionString()).append(")\n\n");

			sb.append("--- CAPABILITIES ---\n");
			sb.append("Max Draw Buffers: ").append(QuasarCapabilities.getMaxDrawBuffers()).append("\n");
			sb.append("Float16 Render: ").append(QuasarCapabilities.canRenderFloat16()).append("\n");
			sb.append("Float32 Render: ").append(QuasarCapabilities.canRenderFloat32()).append("\n");
			sb.append("Device Tier: ").append(QuasarProfiler.getDeviceTier()).append("\n\n");

			sb.append("--- RECOVERY LADDER HISTORY ---\n");
			for (Map.Entry<String, Integer> entry : QuasarRecoveryLadder.getWinningLevels().entrySet()) {
				sb.append("Pass: ").append(entry.getKey()).append(" -> Level L").append(entry.getValue()).append("\n");
			}

			sb.append("\n--- FAILED PASSES ---\n");
			for (Map.Entry<String, String> entry : QuasarRecoveryLadder.getFailedPasses().entrySet()) {
				sb.append("Pass: ").append(entry.getKey()).append("\nLog:\n").append(entry.getValue()).append("\n\n");
			}

			Files.createDirectories(dumpFile.getParent());
			Files.writeString(dumpFile, sb.toString(), StandardCharsets.UTF_8);
			Iris.logger.info("Debug dump written to: " + dumpFile.toAbsolutePath());
		} catch (IOException e) {
			Iris.logger.error("Failed writing debug dump", e);
		}
	}
}
