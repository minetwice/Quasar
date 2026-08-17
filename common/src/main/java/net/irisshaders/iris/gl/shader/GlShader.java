// This file is based on code from Sodium by JellySquid, licensed under the LGPLv3 license.

package net.irisshaders.iris.gl.shader;

import com.mojang.blaze3d.opengl.GlStateManager;
import net.irisshaders.iris.gl.GLDebug;
import net.irisshaders.iris.gl.GlResource;
import net.irisshaders.iris.gl.IrisRenderSystem;
import net.quasar.mobile.QuasarRecoveryLadder;
import net.quasar.mobile.QuasarTranspiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.opengl.KHRDebug;

import java.util.Locale;

/**
 * A compiled OpenGL shader object.
 */
public class GlShader extends GlResource {
	private static final Logger LOGGER = LogManager.getLogger(GlShader.class);

	private final String name;

	public GlShader(ShaderType type, String name, String src) {
		super(createShader(type, name, src));

		this.name = name;
	}

	public static QuasarTranspiler.ShaderKind toQuasarKind(ShaderType type) {
		if (type == ShaderType.VERTEX) return QuasarTranspiler.ShaderKind.VERTEX;
		if (type == ShaderType.FRAGMENT) return QuasarTranspiler.ShaderKind.FRAGMENT;
		if (type == ShaderType.GEOMETRY) return QuasarTranspiler.ShaderKind.GEOMETRY;
		if (type == ShaderType.COMPUTE) return QuasarTranspiler.ShaderKind.COMPUTE;
		if (type == ShaderType.TESSELATION_CONTROL) return QuasarTranspiler.ShaderKind.TESS_CONTROL;
		if (type == ShaderType.TESSELATION_EVAL) return QuasarTranspiler.ShaderKind.TESS_EVAL;
		return QuasarTranspiler.ShaderKind.OTHER;
	}

	private static int createShader(ShaderType type, String name, String src) {
		return QuasarRecoveryLadder.compileWithLadder(type, name, src, transpiledSrc -> {
			int handle = GlStateManager.glCreateShader(type.id);
			ShaderWorkarounds.safeShaderSource(handle, transpiledSrc);
			GlStateManager.glCompileShader(handle);

			GLDebug.nameObject(KHRDebug.GL_SHADER, handle, name + "(" + type.name().toLowerCase(Locale.ROOT) + ")");

			String log = IrisRenderSystem.getShaderInfoLog(handle);

			if (!log.isEmpty()) {
				LOGGER.warn("Shader compilation log for " + name + ": " + log);
			}

			int result = GlStateManager.glGetShaderi(handle, GL20C.GL_COMPILE_STATUS);

			if (result != GL20C.GL_TRUE) {
				GlStateManager.glDeleteShader(handle);
				throw new ShaderCompileException(name, log);
			}

			return handle;
		});
	}

	public String getName() {
		return this.name;
	}

	public int getHandle() {
		return this.getGlId();
	}

	@Override
	protected void destroyInternal() {
		GlStateManager.glDeleteShader(this.getGlId());
	}
}
