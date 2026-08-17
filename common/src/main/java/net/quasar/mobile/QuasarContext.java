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

		this.isGLES = this.glVersion.startsWith("OpenGL ES") || this.glVersion.contains("GLES") || Boolean.getBoolean("quasar.force_gles");

		int maj = 0;
		int min = 0;
		if (this.isGLES) {
			try {
				String v = this.glVersion;
				if (v.startsWith("OpenGL ES ")) {
					v = v.substring("OpenGL ES ".length());
				}
				String[] parts = v.split(" ")[0].split("\\.");
				if (parts.length >= 1) maj = Integer.parseInt(parts[0].trim());
				if (parts.length >= 2) min = Integer.parseInt(parts[1].trim());
			} catch (Exception e) {
				LOGGER.warn("[Quasar] Could not parse ES version from " + this.glVersion + ", defaulting to 3.0");
				maj = 3;
				min = 0;
			}
		}

		this.esMajor = maj;
		this.esMinor = min;

		if (!"Unknown".equals(this.glVersion)) {
			this.initialized = true;
			LOGGER.info("[Quasar] Context: isGLES=" + this.isGLES + " (ES " + esMajor + "." + esMinor + ") | Renderer=" + this.glRenderer + " | Vendor=" + this.glVendor + " | Version=" + this.glVersion);
		} else {
			this.initialized = false;
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
