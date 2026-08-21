package net.quasar.mobile;

import net.irisshaders.iris.Iris;
import org.lwjgl.opengl.GL11C;

/**
 * Quasar Mobile - fork modification
 *
 * Context detection layer for Quasar.
 */
public class QuasarContext {
	private static boolean initialized = false;
	private static String glVersion = "Unknown";
	private static String glRenderer = "Unknown";
	private static String glVendor = "Unknown";
	private static boolean isGLES = false;
	private static boolean isDesktop = true;
	private static int esMajor = 0;
	private static int esMinor = 0;

	public static synchronized void init() {
		if (initialized) {
			return;
		}

		try {
			glVersion = GL11C.glGetString(GL11C.GL_VERSION);
			if (glVersion == null) glVersion = "Unknown";

			glRenderer = GL11C.glGetString(GL11C.GL_RENDERER);
			if (glRenderer == null) glRenderer = "Unknown";

			glVendor = GL11C.glGetString(GL11C.GL_VENDOR);
			if (glVendor == null) glVendor = "Unknown";
		} catch (Throwable t) {
			Iris.logger.warn("Failed to read GL strings during QuasarContext init", t);
		}

		String matchedRule = "none";
		String vLower = glVersion.toLowerCase();
		String rLower = glRenderer.toLowerCase();

		if (glVersion.startsWith("OpenGL ES")) {
			isGLES = true;
			matchedRule = "GL_VERSION starts with OpenGL ES";
		} else if (containsAny(vLower, "openltw", "ltw", "fogltlogles", "mali", "adreno", "powervr", "tegra", "virgl", "zink", "llvmpipe") ||
		           containsAny(rLower, "openltw", "ltw", "fogltlogles", "mali", "adreno", "powervr", "tegra", "virgl", "zink", "llvmpipe")) {
			isGLES = true;
			matchedRule = "Mobile/Translation GPU driver match in version or renderer string";
		} else {
			String osName = System.getProperty("os.name", "").toLowerCase();
			String runtimeName = System.getProperty("java.runtime.name", "").toLowerCase();
			if (osName.contains("android") || (osName.contains("linux") && runtimeName.contains("android"))) {
				isGLES = true;
				matchedRule = "Android OS / Runtime detected";
			}
		}

		if (isGLES) {
			parseESVersion(glVersion);
		}
		isDesktop = !isGLES;

		initialized = true;

		Iris.logger.info("Context: isGLES=" + isGLES + " (ES " + esMajor + "." + esMinor + ") | Renderer=" + glRenderer + " | Vendor=" + glVendor + " | matchedRule=" + matchedRule);
	}

	private static boolean containsAny(String input, String... targets) {
		for (String target : targets) {
			if (input.contains(target)) {
				return true;
			}
		}
		return false;
	}

	private static void parseESVersion(String versionString) {
		try {
			// Typical GLES version string: "OpenGL ES 3.2 V@0615.0 ..."
			String prefix = "OpenGL ES ";
			int start = versionString.indexOf(prefix);
			if (start != -1) {
				String ver = versionString.substring(start + prefix.length()).trim();
				String[] parts = ver.split(" ")[0].split("\\.");
				if (parts.length >= 1) {
					esMajor = Integer.parseInt(parts[0]);
				}
				if (parts.length >= 2) {
					esMinor = Integer.parseInt(parts[1]);
				}
			} else {
				esMajor = 3;
				esMinor = 2;
			}
		} catch (Exception e) {
			esMajor = 3;
			esMinor = 2;
		}
	}

	public static boolean isGLES() {
		if (!initialized) init();
		return isGLES;
	}

	public static boolean isDesktop() {
		if (!initialized) init();
		return isDesktop;
	}

	public static String getGlVersion() {
		if (!initialized) init();
		return glVersion;
	}

	public static String getGlRenderer() {
		if (!initialized) init();
		return glRenderer;
	}

	public static String getGlVendor() {
		if (!initialized) init();
		return glVendor;
	}

	public static int getEsMajor() {
		if (!initialized) init();
		return esMajor;
	}

	public static int getEsMinor() {
		if (!initialized) init();
		return esMinor;
	}

	public static String getEsVersionString() {
		return esMajor + "." + esMinor;
	}
}
