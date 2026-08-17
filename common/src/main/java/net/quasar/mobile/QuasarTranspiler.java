package net.quasar.mobile;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.shader.ShaderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quasar Mobile - fork modification
 * Shader transpiler routing and GLSL rules implementation (R1-R12).
 */
public class QuasarTranspiler {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static final Pattern DRAWBUFFERS_PATTERN = Pattern.compile("/\\*\\s*DRAWBUFFERS:([0-9A-Fa-f]+)\\s*\\*/");

	public static String transpile(String source, ShaderType type, String programName) {
		return transpile(source, type, programName, 0);
	}

	public static String transpile(String source, ShaderType type, String programName, int ladderLevel) {
		if (!QuasarContext.getInstance().isGLES()) {
			return source;
		}

		String cacheKey = computeHash(source + "_" + QuasarContext.getInstance().getEsMajor() + "." + QuasarContext.getInstance().getEsMinor() + "_L" + ladderLevel);
		String cached = getCachedShader(cacheKey);
		if (cached != null) {
			LOGGER.info("[Quasar] " + programName + " (" + type + "): loaded from cache (L" + ladderLevel + ")");
			return cached;
		}

		String result = transpileJava(source, type, programName, ladderLevel);
		saveCachedShader(cacheKey, result);
		LOGGER.info("[Quasar] " + programName + " (" + type + "): transpiled via java (L" + ladderLevel + ")");
		return result;
	}

	private static String transpileJava(String source, ShaderType type, String programName, int ladderLevel) {
		boolean isFragment = (type == ShaderType.FRAGMENT);
		boolean isVertex = (type == ShaderType.VERTEX);

		String esVersion = QuasarContext.getInstance().isAtLeastES(3, 2) ? "#version 320 es" : "#version 300 es";

		StringBuilder body = new StringBuilder();
		String[] lines = source.split("\r?\n");

		boolean hasDerivatives = false;
		boolean usesMatrixTransforms = false;
		boolean usesNoise = false;
		boolean usesBuiltinAttribs = false;

		List<Integer> drawBufferDigits = new ArrayList<>();

		for (String line : lines) {
			String trimmed = line.trim();

			// R1 & R6: remove existing #version and forbidden extensions
			if (trimmed.startsWith("#version")) continue;
			if (trimmed.startsWith("#extension GL_ARB_") || trimmed.startsWith("#extension GL_NV_") || trimmed.startsWith("#extension GL_EXT_gpu_shader4")) {
				continue;
			}

			// Parse DRAWBUFFERS comment
			Matcher dbMatcher = DRAWBUFFERS_PATTERN.matcher(line);
			if (dbMatcher.find()) {
				String digits = dbMatcher.group(1);
				for (char c : digits.toCharArray()) {
					if (Character.isDigit(c)) {
						int d = Character.getNumericValue(c);
						if (!drawBufferDigits.contains(d)) drawBufferDigits.add(d);
					}
				}
			}

			// R3: Qualifiers
			if (isVertex) {
				line = line.replaceAll("\\battribute\\b", "in");
				line = line.replaceAll("\\bvarying\\b", "out");
			} else if (isFragment) {
				line = line.replaceAll("\\bvarying\\b", "in");
			}

			// R4: Frag output replacements
			if (isFragment) {
				line = line.replaceAll("\\bgl_FragColor\\b", "quasar_out0");
				line = line.replaceAll("gl_FragData\\[\\s*(\\d+)\\s*\\]", "quasar_out$1");
			}

			// R5: Texture functions
			line = line.replaceAll("\\btexture2DProj\\b", "textureProj")
					.replaceAll("\\btexture2DLod\\b", "textureLod")
					.replaceAll("\\btexture2DGrad\\b", "textureGrad")
					.replaceAll("\\btexture2D\\b", "texture")
					.replaceAll("\\btextureCubeLod\\b", "textureLod")
					.replaceAll("\\btextureCube\\b", "texture")
					.replaceAll("\\btexture3D\\b", "texture")
					.replaceAll("\\btexture1D\\b", "texture")
					.replaceAll("\\bshadow2D\\b", "texture")
					.replaceAll("\\bshadow2DLod\\b", "textureLod")
					.replaceAll("\\bshadow2DProj\\b", "textureProj");

			// R7: Legacy matrix builtins
			if (line.contains("gl_ModelViewProjectionMatrix")) { line = line.replaceAll("gl_ModelViewProjectionMatrix", "quasar_MVP"); usesMatrixTransforms = true; }
			if (line.contains("gl_ModelViewMatrix")) { line = line.replaceAll("gl_ModelViewMatrix", "quasar_MV"); usesMatrixTransforms = true; }
			if (line.contains("gl_ProjectionMatrix")) { line = line.replaceAll("gl_ProjectionMatrix", "quasar_P"); usesMatrixTransforms = true; }
			if (line.contains("gl_NormalMatrix")) { line = line.replaceAll("gl_NormalMatrix", "mat3(quasar_MV)"); usesMatrixTransforms = true; }
			if (line.contains("gl_TextureMatrix[0]")) { line = line.replaceAll("gl_TextureMatrix\\[0\\]", "mat4(1.0)"); }

			// R8: Vertex builtins & ftransform()
			if (isVertex) {
				if (line.contains("gl_Vertex")) { line = line.replaceAll("gl_Vertex", "quasar_Vertex"); usesBuiltinAttribs = true; }
				if (line.contains("gl_MultiTexCoord0")) { line = line.replaceAll("gl_MultiTexCoord0", "quasar_MultiTexCoord0"); usesBuiltinAttribs = true; }
				if (line.contains("gl_Color")) { line = line.replaceAll("gl_Color", "quasar_Color"); usesBuiltinAttribs = true; }
				if (line.contains("gl_Normal")) { line = line.replaceAll("gl_Normal", "quasar_Normal"); usesBuiltinAttribs = true; }
				if (line.contains("ftransform()")) {
					line = line.replaceAll("ftransform\\(\\)", "(quasar_P * quasar_MV * quasar_Vertex)");
					usesMatrixTransforms = true;
					usesBuiltinAttribs = true;
				}
			}

			if (line.contains("dFdx") || line.contains("dFdy") || line.contains("fwidth")) {
				hasDerivatives = true;
			}
			if (line.contains("noise1") || line.contains("noise2") || line.contains("noise3") || line.contains("noise4")) {
				usesNoise = true;
			}

			// R12: Format downgrades & shadow clamps
			if (line.contains("shadowMapResolution")) {
				line = line.replaceAll("const\\s+int\\s+shadowMapResolution\\s*=\\s*\\d+;", "const int shadowMapResolution = 1024;");
			}

			body.append(line).append("\n");
		}

		StringBuilder header = new StringBuilder();
		header.append(esVersion).append("\n");

		// R10: Derivatives extension
		if (hasDerivatives && !QuasarContext.getInstance().isAtLeastES(3, 0)) {
			header.append("#extension GL_OES_standard_derivatives : enable\n");
		}

		// R2: Precision defaults
		if (isFragment) {
			header.append("precision highp float;\n");
			header.append("precision highp int;\n");
			header.append("precision highp sampler2D;\n");
			header.append("precision highp sampler2DShadow;\n");
		} else {
			header.append("precision highp float;\n");
			header.append("precision highp int;\n");
		}

		// R4: MRT Declarations
		if (isFragment) {
			if (drawBufferDigits.isEmpty()) {
				drawBufferDigits.add(0);
			}
			if (!drawBufferDigits.contains(0)) drawBufferDigits.add(0);

			int maxBuffers = QuasarCapabilities.getInstance().getMaxDrawBuffers();
			for (int digit : drawBufferDigits) {
				if (digit < maxBuffers) {
					header.append("layout(location = ").append(digit).append(") out vec4 quasar_out").append(digit).append(";\n");
				}
			}
		}

		// R7: Uniform declarations
		if (usesMatrixTransforms) {
			header.append("uniform mat4 quasar_MVP;\n");
			header.append("uniform mat4 quasar_MV;\n");
			header.append("uniform mat4 quasar_P;\n");
		}

		// R8: Attribute inputs
		if (isVertex && usesBuiltinAttribs) {
			header.append("layout(location = 0) in vec4 quasar_Vertex;\n");
			header.append("layout(location = 1) in vec4 quasar_MultiTexCoord0;\n");
			header.append("layout(location = 2) in vec4 quasar_Color;\n");
			header.append("layout(location = 3) in vec3 quasar_Normal;\n");
		}

		// Ladder flags (L4, L5, L6)
		if (ladderLevel >= 4) header.append("#define QUASAR_NO_VOLUMETRICS 1\n");
		if (ladderLevel >= 5) header.append("#define QUASAR_NO_SHADOWS 1\n");
		if (ladderLevel >= 6) header.append("#define QUASAR_NO_REFLECTIONS 1\n");

		// R9: Hash-based noise fallback
		if (usesNoise) {
			header.append("float quasar_hash(vec2 p) { return fract(sin(dot(p, vec2(12.9898, 78.233))) * 43758.5453) * 2.0 - 1.0; }\n");
			header.append("float noise1(float x) { return quasar_hash(vec2(x, 0.0)); }\n");
			header.append("float noise1(vec2 p) { return quasar_hash(p); }\n");
			header.append("float noise1(vec3 p) { return quasar_hash(p.xy + p.z); }\n");
			header.append("vec2 noise2(vec2 p) { return vec2(quasar_hash(p), quasar_hash(p + 1.0)); }\n");
			header.append("vec3 noise3(vec3 p) { return vec3(quasar_hash(p.xy), quasar_hash(p.yz), quasar_hash(p.zx)); }\n");
			header.append("vec4 noise4(vec4 p) { return vec4(quasar_hash(p.xy), quasar_hash(p.zw), quasar_hash(p.xz), quasar_hash(p.yw)); }\n");
		}

		return header.toString() + body.toString();
	}

	private static String computeHash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			return String.valueOf(input.hashCode());
		}
	}

	private static String getCachedShader(String hash) {
		try {
			Path cacheDir = Iris.getShaderpacksDirectory().getParent().resolve("quasar_shaders");
			Path file = cacheDir.resolve(hash + ".glsl");
			if (Files.exists(file)) {
				return Files.readString(file, StandardCharsets.UTF_8);
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static void saveCachedShader(String hash, String content) {
		try {
			Path cacheDir = Iris.getShaderpacksDirectory().getParent().resolve("quasar_shaders");
			if (!Files.exists(cacheDir)) {
				Files.createDirectories(cacheDir);
			}
			Path file = cacheDir.resolve(hash + ".glsl");
			Files.writeString(file, content, StandardCharsets.UTF_8);
		} catch (Exception ignored) {
		}
	}
}
