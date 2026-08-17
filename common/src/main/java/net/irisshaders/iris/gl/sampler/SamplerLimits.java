package net.irisshaders.iris.gl.sampler;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.quasar.mobile.QuasarCapabilities;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.GL45C;

public class SamplerLimits {
	private static SamplerLimits instance;
	private final int maxTextureUnits;
	private final int maxDrawBuffers;
	private final int maxShaderStorageUnits;

	private SamplerLimits() {
		this.maxTextureUnits = QuasarCapabilities.getMaxTextureImageUnits();
		this.maxDrawBuffers = QuasarCapabilities.getMaxDrawBuffers();
		this.maxShaderStorageUnits = IrisRenderSystem.supportsSSBO() ? GlStateManager._getInteger(GL45C.GL_MAX_SHADER_STORAGE_BUFFER_BINDINGS) : 0;
	}

	public static SamplerLimits get() {
		if (instance == null) {
			instance = new SamplerLimits();
		}

		return instance;
	}

	public int getMaxTextureUnits() {
		return maxTextureUnits;
	}

	public int getMaxDrawBuffers() {
		return maxDrawBuffers;
	}

	public int getMaxShaderStorageUnits() {
		return maxShaderStorageUnits;
	}
}
