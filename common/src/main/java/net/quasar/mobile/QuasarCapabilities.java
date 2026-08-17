package net.quasar.mobile;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * Quasar Mobile - fork modification
 * Capability wrapper and GLES limit reporting.
 */
public class QuasarCapabilities {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static QuasarCapabilities instance;

	private final boolean canRenderFloat16;
	private final boolean canRenderFloat32;
	private final int maxDrawBuffers;
	private final int maxTextureUnits;
	private final int maxCombinedTextureUnits;
	private final int maxTextureSize;
	private final int maxUniformComponents;
	private final Set<String> extensions = new HashSet<>();

	private QuasarCapabilities() {
		QuasarContext context = QuasarContext.getInstance();

		int rawDrawBuffers = getIntegerv(0x8824, 8); // GL_MAX_DRAW_BUFFERS
		int rawTexUnits = getIntegerv(GL20.GL_MAX_TEXTURE_IMAGE_UNITS, 16);
		int rawCombinedTexUnits = getIntegerv(GL20.GL_MAX_COMBINED_TEXTURE_IMAGE_UNITS, 32);
		int rawTexSize = getIntegerv(GL11.GL_MAX_TEXTURE_SIZE, 8192);
		int rawUniforms = getIntegerv(0x8B4A, 4096); // GL_MAX_VERTEX_UNIFORM_COMPONENTS

		this.maxDrawBuffers = Math.min(8, Math.max(1, rawDrawBuffers));
		this.maxTextureUnits = Math.min(16, Math.max(8, rawTexUnits));
		this.maxCombinedTextureUnits = Math.min(32, Math.max(16, rawCombinedTexUnits));
		this.maxTextureSize = Math.min(8192, Math.max(2048, rawTexSize));
		this.maxUniformComponents = Math.min(4096, Math.max(1024, rawUniforms));

		// Core GLES required extensions mock/passthrough
		extensions.add("GL_ARB_shader_texture_lod");
		extensions.add("GL_ARB_draw_buffers");
		extensions.add("GL_ARB_explicit_attrib_location");
		extensions.add("GL_ARB_shading_language_420pack");

		if (context.isAtLeastES(3, 1)) {
			extensions.add("GL_ARB_compute_shader");
			extensions.add("GL_ARB_shader_image_load_store");
			extensions.add("GL_ARB_shader_storage_buffer_object");
		}
		if (context.isAtLeastES(3, 2)) {
			extensions.add("GL_ARB_geometry_shader4");
			extensions.add("GL_ARB_tessellation_shader");
		}

		boolean f16 = false;
		boolean f32 = false;

		if (context.isGLES()) {
			try {
				String extStr = GL11.glGetString(GL11.GL_EXTENSIONS);
				if (extStr != null) {
					for (String ext : extStr.split(" ")) {
						if (!ext.isEmpty()) {
							extensions.add(ext);
						}
					}
				}
			} catch (Throwable ignored) {
			}

			f16 = hasExtension("GL_EXT_color_buffer_half_float") || hasExtension("GL_OES_texture_half_float") || context.isAtLeastES(3, 2);
			f32 = hasExtension("GL_EXT_color_buffer_float") || hasExtension("GL_OES_texture_float");
		} else {
			f16 = true;
			f32 = true;
		}

		this.canRenderFloat16 = f16;
		this.canRenderFloat32 = f32;

		LOGGER.info("[Quasar] CAPS: GLES=" + context.isGLES() + " | drawBuffers=" + maxDrawBuffers + " | texUnits=" + maxTextureUnits + " | maxTexSize=" + maxTextureSize + " | Float16=" + canRenderFloat16 + " | Float32=" + canRenderFloat32);
	}

	public static synchronized QuasarCapabilities getInstance() {
		if (instance == null) {
			instance = new QuasarCapabilities();
		}
		return instance;
	}

	private static int getIntegerv(int pname, int fallback) {
		try {
			return GL11.glGetInteger(pname);
		} catch (Throwable t) {
			return fallback;
		}
	}

	public boolean hasExtension(String extension) {
		return extensions.contains(extension);
	}

	public boolean canRenderFloat16() {
		return canRenderFloat16;
	}

	public boolean canRenderFloat32() {
		return canRenderFloat32;
	}

	public int getMaxDrawBuffers() {
		return maxDrawBuffers;
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}

	public int getMaxCombinedTextureUnits() {
		return maxCombinedTextureUnits;
	}

	public int getMaxTextureSize() {
		return maxTextureSize;
	}

	public int getMaxUniformComponents() {
		return maxUniformComponents;
	}

	public Set<String> getExtensions() {
		return extensions;
	}
}
