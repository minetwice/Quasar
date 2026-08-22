package net.quasar.mobile;

import com.mojang.blaze3d.platform.GlStateManager;
import net.irisshaders.iris.Iris;
import org.lwjgl.opengl.GL11C;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL30C;

import java.util.HashSet;
import java.util.Set;

/**
 * Quasar Mobile - fork modification
 *
 * Capabilities layer for mapping GLES capability reporting to desktop equivalent extensions
 * while respecting hardware limits.
 */
public class QuasarCapabilities {
	private static boolean initialized = false;

	private static boolean canRenderFloat16 = false;
	private static boolean canRenderFloat32 = false;

	private static int maxDrawBuffers = 8;
	private static int maxTextureImageUnits = 16;
	private static int maxCombinedTextureImageUnits = 32;
	private static int maxTextureSize = 8192;
	private static int maxFragmentUniformComponents = 4096;
	private static int maxVertexUniformComponents = 4096;

	private static final Set<String> glesAdvertisedExtensions = new HashSet<>();

	public static synchronized void init() {
		if (initialized) {
			return;
		}

		QuasarContext.init();

		if (QuasarContext.isDesktop()) {
			// On desktop GL, probe real limits & float support without modifying caps
			try {
				int realDrawBuffers = GlStateManager._getInteger(GL20C.GL_MAX_DRAW_BUFFERS);
				maxDrawBuffers = realDrawBuffers;
				maxTextureImageUnits = GlStateManager._getInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
				maxCombinedTextureImageUnits = GlStateManager._getInteger(GL20C.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
				maxTextureSize = GlStateManager._getInteger(GL11C.GL_MAX_TEXTURE_SIZE);
				maxFragmentUniformComponents = GlStateManager._getInteger(GL20C.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS);
				maxVertexUniformComponents = GlStateManager._getInteger(GL20C.GL_MAX_VERTEX_UNIFORM_COMPONENTS);
			} catch (Throwable ignored) {
			}
			canRenderFloat16 = true;
			canRenderFloat32 = true;
			initialized = true;

			Iris.logger.info("CAPS: MRT=" + maxDrawBuffers + " float16=y float32=y compute=y geometry=y ES=desktop");
			return;
		}

		// GLES mode: read real limits and clamp them according to Section 2.5
		try {
			int realDraw = GlStateManager._getInteger(GL20C.GL_MAX_DRAW_BUFFERS);
			maxDrawBuffers = Math.min(8, realDraw > 0 ? realDraw : 8);

			int realTex = GlStateManager._getInteger(GL20C.GL_MAX_TEXTURE_IMAGE_UNITS);
			maxTextureImageUnits = Math.min(16, realTex > 0 ? realTex : 16);

			int realCombined = GlStateManager._getInteger(GL20C.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS);
			maxCombinedTextureImageUnits = Math.min(32, realCombined > 0 ? realCombined : 32);

			int realTexSize = GlStateManager._getInteger(GL11C.GL_MAX_TEXTURE_SIZE);
			maxTextureSize = Math.min(8192, realTexSize > 0 ? realTexSize : 8192);

			int realFragUniforms = GlStateManager._getInteger(GL20C.GL_MAX_FRAGMENT_UNIFORM_COMPONENTS);
			maxFragmentUniformComponents = Math.min(4096, realFragUniforms > 0 ? realFragUniforms : 4096);

			int realVertUniforms = GlStateManager._getInteger(GL20C.GL_MAX_VERTEX_UNIFORM_COMPONENTS);
			maxVertexUniformComponents = Math.min(4096, realVertUniforms > 0 ? realVertUniforms : 4096);
		} catch (Throwable t) {
			Iris.logger.warn("Failed reading some GL limits on GLES, using safe defaults", t);
		}

		// Build extension set for GLES
		glesAdvertisedExtensions.add("GL_ARB_shader_texture_lod");
		glesAdvertisedExtensions.add("GL_ARB_draw_buffers");
		glesAdvertisedExtensions.add("GL_ARB_explicit_attrib_location");
		glesAdvertisedExtensions.add("GL_ARB_shading_language_420pack");

		int esMajor = QuasarContext.getEsMajor();
		int esMinor = QuasarContext.getEsMinor();
		boolean es31OrHigher = (esMajor > 3) || (esMajor == 3 && esMinor >= 1);
		boolean es32OrHigher = (esMajor > 3) || (esMajor == 3 && esMinor >= 2);

		if (es31OrHigher) {
			glesAdvertisedExtensions.add("GL_ARB_compute_shader");
			glesAdvertisedExtensions.add("GL_ARB_shader_storage_buffer_object");
			glesAdvertisedExtensions.add("GL_ARB_shader_image_load_store");
		}

		if (es32OrHigher) {
			glesAdvertisedExtensions.add("GL_ARB_geometry_shader");
			glesAdvertisedExtensions.add("GL_ARB_tessellation_shader");
		}

		// Probe EXT_color_buffer_float / EXT_color_buffer_half_float
		probeFloatRenderTargets();

		initialized = true;

		Iris.logger.info("CAPS: MRT=" + maxDrawBuffers +
			" float16=" + (canRenderFloat16 ? "y" : "n") +
			" float32=" + (canRenderFloat32 ? "y" : "n") +
			" compute=" + (es31OrHigher ? "y" : "n") +
			" geometry=" + (es32OrHigher ? "y" : "n") +
			" ES=" + QuasarContext.getEsVersionString());
	}

	private static void probeFloatRenderTargets() {
		// Read real GL extension string or num extensions
		Set<String> realExts = new HashSet<>();
		try {
			int numExts = GlStateManager._getInteger(GL30C.GL_NUM_EXTENSIONS);
			for (int i = 0; i < numExts; i++) {
				String ext = GL30C.glGetStringi(GL11C.GL_EXTENSIONS, i);
				if (ext != null) {
					realExts.add(ext);
				}
			}
		} catch (Throwable ignored) {
			try {
				String extStr = GL11C.glGetString(GL11C.GL_EXTENSIONS);
				if (extStr != null) {
					for (String s : extStr.split(" ")) {
						realExts.add(s.trim());
					}
				}
			} catch (Throwable ignored2) {
			}
		}

		canRenderFloat32 = realExts.contains("GL_EXT_color_buffer_float") || realExts.contains("GL_OES_texture_float");
		canRenderFloat16 = realExts.contains("GL_EXT_color_buffer_half_float") || canRenderFloat32 || QuasarContext.getEsMajor() >= 3;
	}

	public static String getVersionString() {
		if (QuasarContext.isGLES()) {
			return "4.6 (Quasar Mobile)";
		}
		return QuasarContext.getGlVersion();
	}

	public static String getShadingLanguageVersionString() {
		if (QuasarContext.isGLES()) {
			return "4.60";
		}
		try {
			return GL11C.glGetString(25712); // GL_SHADING_LANGUAGE_VERSION = 0x8B8C
		} catch (Throwable t) {
			return "4.60";
		}
	}

	public static boolean isExtensionSupported(String extension) {
		if (!initialized) init();
		if (QuasarContext.isDesktop()) {
			return true;
		}
		return glesAdvertisedExtensions.contains(extension);
	}

	public static boolean canRenderFloat16() {
		if (!initialized) init();
		return canRenderFloat16;
	}

	public static boolean canRenderFloat32() {
		if (!initialized) init();
		return canRenderFloat32;
	}

	public static int getMaxDrawBuffers() {
		if (!initialized) init();
		return maxDrawBuffers;
	}

	public static int getMaxTextureImageUnits() {
		if (!initialized) init();
		return maxTextureImageUnits;
	}

	public static int getMaxCombinedTextureImageUnits() {
		if (!initialized) init();
		return maxCombinedTextureImageUnits;
	}

	public static int getMaxTextureSize() {
		if (!initialized) init();
		return maxTextureSize;
	}

	public static int getMaxFragmentUniformComponents() {
		if (!initialized) init();
		return maxFragmentUniformComponents;
	}

	public static int getMaxVertexUniformComponents() {
		if (!initialized) init();
		return maxVertexUniformComponents;
	}
}
