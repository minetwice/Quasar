package net.quasar.mobile.compat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Quasar Mobile - fork modification
 * Known issue database and workaround registry per shader pack.
 */
public class CompatibilityDB {
	private static final Map<String, Function<String, String>> WORKAROUNDS = new HashMap<>();

	static {
		register("Bliss", issue -> {
			if (issue.contains("volumetric clouds")) {
				return "Disabled volumetrics - using simple clouds";
			}
			return null;
		});

		register("Solas", issue -> {
			if (issue.contains("raytraced shadows")) {
				return "Using shadow maps instead of raytracing";
			}
			return null;
		});
	}

	public static void register(String packName, Function<String, String> handler) {
		WORKAROUNDS.put(packName.toLowerCase(), handler);
	}

	public static String checkWorkaround(String packName, String issueDescription) {
		if (packName == null || issueDescription == null) return null;
		Function<String, String> handler = WORKAROUNDS.get(packName.toLowerCase());
		if (handler != null) {
			return handler.apply(issueDescription);
		}
		return null;
	}
}
