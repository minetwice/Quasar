package net.quasar.mobile;

import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quasar Mobile - fork modification
 * Singleton tracking OpenGL / GLES context state and version details.
 */
public class QuasarContext {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static QuasarContext instance;

	private final String glVersion;
	private final String glRenderer;
	private final String glVendor;
	private final boolean isGLES;
	private final int esMajor;
	private final int esMinor;
	private final boolean initialized;

	private QuasarContext() {
		this.glVersion = probeGlVersion();
		this.glRenderer = probeGlRenderer();
		this.glVendor = probeGlVendor();
		String glslVersion = probeGlslVersion();

		String matchReason = checkMobileMatch(this.glVersion, this.glRenderer, this.glVendor, glslVersion);
		this.isGLES = (matchReason != null) || Boolean.getBoolean("quasar.force_gles");

		int maj = 3;
		int min = 2;
		if (this.isGLES) {
			try {
				String v = this.glVersion;
				if (v.startsWith("OpenGL ES ")) {
					v = v.substring("OpenGL ES ".length());
					String[] parts = v.split(" ")[0].split("\\.");
					if (parts.length >= 1) maj = Integer.parseInt(parts[0].trim());
					if (parts.length >= 2) min = Integer.parseInt(parts[1].trim());
				}
			} catch (Exception e) {
				maj = 3;
				min = 2;
			}
		} else {
			maj = 0;
			min = 0;
		}

		this.esMajor = maj;
		this.esMinor = min;

		if (!"Unknown".equals(this.glVersion)) {
			this.initialized = true;
			if (this.isGLES) {
				LOGGER.info("[Quasar] Mobile context detected via: " + (matchReason != null ? matchReason : "force_gles") + " | isGLES=true es=" + esMajor + "." + esMinor);
			}
			LOGGER.info("[Quasar] Context: isGLES=" + this.isGLES + " (ES " + esMajor + "." + esMinor + ") | Renderer=" + this.glRenderer + " | Vendor=" + this.glVendor + " | Version=" + this.glVersion);
		} else {
			this.initialized = false;
		}
	}

	private static String checkMobileMatch(String version, String renderer, String vendor, String glslVersion) {
		String combined = (version + " " + renderer + " " + vendor + " " + glslVersion).toLowerCase();
		String[] mobileKeywords = {"openltw", "ltw", "fogltlogles", "mali", "adreno", "powervr", "snapdragon", "tegra", "llvmpipe", "virgl", "opengl es", "glsl es"};
		for (String kw : mobileKeywords) {
			if (combined.contains(kw)) {
				return "keyword (" + kw + ")";
			}
		}

		String osName = System.getProperty("os.name", "").toLowerCase();
		if (osName.contains("linux") && (System.getProperty("java.runtime.name", "").toLowerCase().contains("android") || "android".equals(System.getProperty("lwjgl.platform")))) {
			return "android system environment";
		}
		return null;
	}

	private static String probeGlslVersion() {
		try {
			String v = GL11.glGetString(35724); // GL_SHADING_LANGUAGE_VERSION
			return v != null ? v : "";
		} catch (Throwable ignored) {
			return "";
		}
	}

	private static String probeGlVersion() {
		try {
			String v = GL11.glGetString(GL11.GL_VERSION);
			return v != null ? v : "Unknown";
		} catch (Throwable t) {
			LOGGER.warn("[Quasar] GL context not ready at init, deferring context detection: " + t.getMessage());
			return "Unknown";
		}
	}

	private static String probeGlRenderer() {
		try {
			String r = GL11.glGetString(GL11.GL_RENDERER);
			return r != null ? r : "Unknown";
		} catch (Throwable ignored) {
			return "Unknown";
		}
	}

	private static String probeGlVendor() {
		try {
			String v = GL11.glGetString(GL11.GL_VENDOR);
			return v != null ? v : "Unknown";
		} catch (Throwable ignored) {
			return "Unknown";
		}
	}

	public static synchronized QuasarContext getInstance() {
		if (instance == null || !instance.initialized) {
			instance = new QuasarContext();
		}
		return instance;
	}

	public boolean isGLES() {
		return isGLES;
	}

	public int getEsMajor() {
		return esMajor;
	}

	public int getEsMinor() {
		return esMinor;
	}

	public boolean isAtLeastES(int major, int minor) {
		if (!isGLES) return true;
		if (esMajor > major) return true;
		if (esMajor == major) return esMinor >= minor;
		return false;
	}

	public String getGlVersion() {
		return glVersion;
	}

	public String getGlRenderer() {
		return glRenderer;
	}

	public String getGlVendor() {
		return glVendor;
	}
}
