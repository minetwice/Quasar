package net.quasar.mobile.debug;

import net.minecraft.client.Minecraft;
import net.quasar.mobile.gui.QuasarCompatScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Quasar Mobile - fork modification
 * Shader compilation failure recovery manager and user-friendly error handler.
 */
public class ErrorRecovery {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static final List<String> RECOVERY_LOGS = new ArrayList<>();

	public static void handleShaderCompileFailure(String programName, String errorLog) {
		LOGGER.error("[Quasar] Shader compilation failed for " + programName + ":\n" + errorLog);
		RECOVERY_LOGS.add("Program: " + programName + " -> " + errorLog);

		List<String> userReports = new ArrayList<>();
		userReports.add("[Quasar] Pass adapted: " + programName);
		userReports.add("Reason: Shader feature not supported by GPU");

		try {
			if (Minecraft.getInstance() != null) {
				Minecraft.getInstance().execute(() -> {
					Minecraft.getInstance().setScreen(new QuasarCompatScreen(Minecraft.getInstance().screen, userReports));
				});
			}
		} catch (Throwable ignored) {
		}
	}

	public static List<String> getRecoveryLogs() {
		return RECOVERY_LOGS;
	}
}
