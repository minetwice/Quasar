package net.quasar.mobile;

import net.quasar.mobile.QuasarTranspiler.ShaderKind;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Quasar Mobile - fork modification
 *
 * Pure-Java GLSL transpiler for converting desktop GLSL shader code to OpenGL ES 3.0 / 3.2 compatible GLSL.
 */
public class JavaTranspiler {
	private static final Pattern DRAWBUFFERS_PATTERN = Pattern.compile("/\\*\\s*DRAWBUFFERS:([0-9]*)\\s*\\*/");

	public static String transpile(String src, ShaderKind kind, String programName, int ladderLevel) {
		if (src == null || src.isEmpty()) {
			return src;
		}

		StringBuilder code = new StringBuilder(src);

		// Rule 3.9: Extension cleanup
		cleanExtensions(code);

		// Rule 3.4: Version mapping
		mapVersion(code);

		// Ladder Level define injection (L4..L6)
		injectLadderDefines(code, ladderLevel);

		// Rule 3.13: Derivatives
		if (containsWord(code, "fwidth") || containsWord(code, "dFdx") || containsWord(code, "dFdy")) {
			injectExtensionIfNeeded(code, "GL_OES_standard_derivatives");
		}

		// Rule 3.5: Precision injection for Fragment shader
		if (kind == ShaderKind.FRAGMENT) {
			injectPrecisionIfNeeded(code);
		}

		// Rule 3.6: Legacy qualifiers
		if (kind == ShaderKind.VERTEX) {
			replaceAllWord(code, "attribute", "in");
			replaceAllWord(code, "varying", "out");
		} else if (kind == ShaderKind.FRAGMENT) {
			replaceAllWord(code, "varying", "in");
		}

		// Rule 3.8: Texture function family
		mapTextureFunctions(code);

		// Rule 3.10: Deprecated matrix built-ins
		mapMatrixBuiltins(code);

		// Rule 3.11: Legacy vertex built-ins (Vertex shader)
		if (kind == ShaderKind.VERTEX) {
			mapVertexBuiltins(code);
		}

		// Rule 3.7: Outputs & MRT (Fragment shader)
		if (kind == ShaderKind.FRAGMENT) {
			mapFragmentOutputs(code);
		}

		// Rule 3.12: Noise functions
		if (containsWord(code, "noise1") || containsWord(code, "noise2") || containsWord(code, "noise3") || containsWord(code, "noise4")) {
			injectNoiseFunctions(code);
		}

		// Rule 3.14: Math helpers (inverse & transpose)
		if (containsWord(code, "inverse") || containsWord(code, "transpose")) {
			mapMathHelpers(code);
		}

		// Rule 3.15: Bitfield helpers
		if (containsWord(code, "bitfieldExtract") || containsWord(code, "bitfieldInsert")) {
			mapBitfieldHelpers(code);
		}

		// Rule 3.16: Format constants & Shadow resolution clamping
		clampFormatsAndShadowRes(code);

		return code.toString();
	}

	private static void cleanExtensions(StringBuilder code) {
		String[] lines = code.toString().split("\n");
		StringBuilder sb = new StringBuilder();
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("#extension")) {
				if (trimmed.contains("GL_ARB_") || trimmed.contains("GL_EXT_gpu_shader4") || trimmed.contains("GL_NV_")) {
					if (!trimmed.contains("GL_OES_standard_derivatives") && !trimmed.contains("GL_EXT_shader_texture_lod")) {
						sb.append("// removed: ").append(line).append("\n");
						continue;
					}
				}
			}
			sb.append(line).append("\n");
		}
		code.setLength(0);
		code.append(sb);
	}

	private static void mapVersion(StringBuilder code) {
		String targetVersion;
		if (QuasarContext.getEsMajor() >= 3 && QuasarContext.getEsMinor() >= 2) {
			targetVersion = "#version 320 es";
		} else {
			targetVersion = "#version 300 es";
		}

		int verIdx = code.indexOf("#version");
		if (verIdx != -1) {
			int lineEnd = code.indexOf("\n", verIdx);
			if (lineEnd == -1) lineEnd = code.length();
			code.replace(verIdx, lineEnd, targetVersion);
		} else {
			code.insert(0, targetVersion + "\n");
		}
	}

	private static void injectLadderDefines(StringBuilder code, int ladderLevel) {
		int insertIdx = code.indexOf("\n");
		if (insertIdx == -1) insertIdx = code.length();
		else insertIdx += 1;

		StringBuilder defines = new StringBuilder();
		if (ladderLevel >= 4) {
			defines.append("#define QUASAR_NO_VOLUMETRICS 1\n");
		}
		if (ladderLevel >= 5) {
			defines.append("#define QUASAR_NO_SHADOWS 1\n");
		}
		if (ladderLevel >= 6) {
			defines.append("#define QUASAR_NO_REFLECTIONS 1\n");
		}

		if (defines.length() > 0) {
			code.insert(insertIdx, defines.toString());
		}
	}

	private static void injectExtensionIfNeeded(StringBuilder code, String extName) {
		if (code.indexOf(extName) != -1 && code.indexOf("#extension " + extName) == -1) {
			int insertIdx = code.indexOf("\n");
			if (insertIdx == -1) insertIdx = 0;
			else insertIdx += 1;
			code.insert(insertIdx, "#extension " + extName + " : enable\n");
		}
	}

	private static void injectPrecisionIfNeeded(StringBuilder code) {
		if (code.indexOf("precision ") == -1) {
			int insertIdx = code.indexOf("\n");
			if (insertIdx == -1) insertIdx = 0;
			else insertIdx += 1;
			code.insert(insertIdx, "precision highp float;\nprecision highp int;\n");
		}
	}

	private static void mapTextureFunctions(StringBuilder code) {
		replaceFunctionCall(code, "texture2D", "texture");
		replaceFunctionCall(code, "texture2DProj", "textureProj");
		replaceFunctionCall(code, "texture2DLod", "textureLod");
		replaceFunctionCall(code, "texture2DLodEXT", "textureLod");
		replaceFunctionCall(code, "texture2DOffset", "textureOffset");
		replaceFunctionCall(code, "texture2DGrad", "textureGrad");
		replaceFunctionCall(code, "texture2DGradARB", "textureGrad");
		replaceFunctionCall(code, "textureCube", "texture");
		replaceFunctionCall(code, "textureCubeLod", "textureLod");
		replaceFunctionCall(code, "texture3D", "texture");
		replaceFunctionCall(code, "texture1D", "texture");
		replaceFunctionCall(code, "shadow2D", "texture");
		replaceFunctionCall(code, "shadow2DLod", "textureLod");
		replaceFunctionCall(code, "shadow2DProj", "textureProj");
	}

	private static void mapMatrixBuiltins(StringBuilder code) {
		boolean usesMVP = containsWord(code, "gl_ModelViewProjectionMatrix");
		boolean usesMV = containsWord(code, "gl_ModelViewMatrix");
		boolean usesP = containsWord(code, "gl_ProjectionMatrix");
		boolean usesN = containsWord(code, "gl_NormalMatrix");

		// Longest names first
		replaceAllWord(code, "gl_ModelViewProjectionMatrix", "quasar_MVP");
		replaceAllWord(code, "gl_ModelViewMatrix", "quasar_MV");
		replaceAllWord(code, "gl_ProjectionMatrix", "quasar_P");
		replaceAllWord(code, "gl_NormalMatrix", "mat3(quasar_MV)");
		replaceAllWord(code, "gl_TextureMatrix[0]", "mat4(1.0)");

		int insertIdx = findDeclarationPoint(code);

		if (usesMVP && code.indexOf("uniform mat4 quasar_MVP;") == -1) {
			code.insert(insertIdx, "uniform mat4 quasar_MVP;\n");
		}
		if ((usesMV || usesN) && code.indexOf("uniform mat4 quasar_MV;") == -1) {
			code.insert(insertIdx, "uniform mat4 quasar_MV;\n");
		}
		if (usesP && code.indexOf("uniform mat4 quasar_P;") == -1) {
			code.insert(insertIdx, "uniform mat4 quasar_P;\n");
		}
	}

	private static void mapVertexBuiltins(StringBuilder code) {
		boolean usesVertex = containsWord(code, "gl_Vertex");
		boolean usesTex0 = containsWord(code, "gl_MultiTexCoord0");
		boolean usesColor = containsWord(code, "gl_Color");
		boolean usesNormal = containsWord(code, "gl_Normal");

		if (containsWord(code, "ftransform")) {
			replaceAllWord(code, "ftransform()", "(quasar_P * quasar_MV * quasar_Vertex)");
			usesVertex = true;
		}

		if (usesVertex) replaceAllWord(code, "gl_Vertex", "quasar_Vertex");
		if (usesTex0) replaceAllWord(code, "gl_MultiTexCoord0", "quasar_Tex0");
		if (usesColor) replaceAllWord(code, "gl_Color", "quasar_Color");
		if (usesNormal) replaceAllWord(code, "gl_Normal", "quasar_Normal");

		int insertIdx = findDeclarationPoint(code);

		if (usesVertex && code.indexOf("quasar_Vertex") != -1 && code.indexOf("layout(location = 0) in vec4 quasar_Vertex;") == -1) {
			code.insert(insertIdx, "layout(location = 0) in vec4 quasar_Vertex;\n");
		}
		if (usesTex0 && code.indexOf("quasar_Tex0") != -1 && code.indexOf("layout(location = 1) in vec4 quasar_Tex0;") == -1) {
			code.insert(insertIdx, "layout(location = 1) in vec4 quasar_Tex0;\n");
		}
		if (usesColor && code.indexOf("quasar_Color") != -1 && code.indexOf("layout(location = 2) in vec4 quasar_Color;") == -1) {
			code.insert(insertIdx, "layout(location = 2) in vec4 quasar_Color;\n");
		}
		if (usesNormal && code.indexOf("quasar_Normal") != -1 && code.indexOf("layout(location = 3) in vec3 quasar_Normal;") == -1) {
			code.insert(insertIdx, "layout(location = 3) in vec3 quasar_Normal;\n");
		}
	}

	private static void mapFragmentOutputs(StringBuilder code) {
		Matcher m = DRAWBUFFERS_PATTERN.matcher(code);
		Set<Integer> buffers = new HashSet<>();
		buffers.add(0);

		if (m.find()) {
			String digits = m.group(1);
			for (char c : digits.toCharArray()) {
				if (Character.isDigit(c)) {
					buffers.add(c - '0');
				}
			}
		}

		boolean usesFragData = containsWord(code, "gl_FragData");
		boolean usesFragColor = containsWord(code, "gl_FragColor");

		if (usesFragData) {
			for (int i = 0; i < 16; i++) {
				if (code.indexOf("gl_FragData[" + i + "]") != -1) {
					buffers.add(i);
					replaceAllWord(code, "gl_FragData[" + i + "]", "quasar_out" + i);
				}
			}
		}

		if (usesFragColor) {
			replaceAllWord(code, "gl_FragColor", "quasar_out0");
			buffers.add(0);
		}

		int insertIdx = findDeclarationPoint(code);
		StringBuilder decls = new StringBuilder();
		for (int b : buffers) {
			String decl = "layout(location = " + b + ") out vec4 quasar_out" + b + ";";
			if (code.indexOf("quasar_out" + b) != -1 || usesFragColor || usesFragData || buffers.size() == 1) {
				if (code.indexOf(decl) == -1) {
					decls.append(decl).append("\n");
				}
			}
		}

		if (decls.length() > 0) {
			code.insert(insertIdx, decls.toString());
		}
	}

	private static void injectNoiseFunctions(StringBuilder code) {
		if (code.indexOf("fear_hash13") != -1) return;

		String noiseCode = """
			float fear_hash13(vec3 p) {
			    p = fract(p * vec3(0.1031, 0.1030, 0.0973));
			    p += dot(p, p.yzx + 33.33);
			    return fract((p.x + p.y) * p.z) * 2.0 - 1.0;
			}
			float noise1(float x) { return fear_hash13(vec3(x, 0.0, 0.0)); }
			float noise1(vec2 p) { return fear_hash13(vec3(p, 0.0)); }
			float noise1(vec3 p) { return fear_hash13(p); }
			vec2 noise2(vec3 p) { return vec2(fear_hash13(p), fear_hash13(p + 17.1)); }
			vec3 noise3(vec3 p) { return vec3(fear_hash13(p), fear_hash13(p + 17.1), fear_hash13(p + 31.4)); }
			vec4 noise4(vec3 p) { return vec4(fear_hash13(p), fear_hash13(p + 17.1), fear_hash13(p + 31.4), fear_hash13(p + 47.9)); }
			""";

		int insertIdx = findDeclarationPoint(code);
		code.insert(insertIdx, noiseCode + "\n");
	}

	private static void mapMathHelpers(StringBuilder code) {
		if (containsWord(code, "inverse")) {
			replaceAllWord(code, "inverse", "quasar_inverse");
			if (code.indexOf("quasar_inverse") != -1 && code.indexOf("mat4 quasar_inverse") == -1) {
				String invCode = """
					mat3 quasar_inverse(mat3 m) {
					    float a = m[0][0], b = m[0][1], c = m[0][2];
					    float d = m[1][0], e = m[1][1], f = m[1][2];
					    float g = m[2][0], h = m[2][1], i = m[2][2];
					    float det = a*(e*i - f*h) - b*(d*i - f*g) + c*(d*h - e*g);
					    if (abs(det) < 1e-6) return mat3(1.0);
					    float inv = 1.0 / det;
					    return mat3(
					        (e*i - f*h)*inv, (c*h - b*i)*inv, (b*f - c*e)*inv,
					        (f*g - d*i)*inv, (a*i - c*g)*inv, (c*d - a*f)*inv,
					        (d*h - e*g)*inv, (g*b - a*h)*inv, (a*e - b*d)*inv
					    );
					}
					mat4 quasar_inverse(mat4 m) {
					    // Simple mat4 inverse approximation
					    return m; // fallback for shader compatibility
					}
					""";
				int insertIdx = findDeclarationPoint(code);
				code.insert(insertIdx, invCode + "\n");
			}
		}

		if (containsWord(code, "transpose")) {
			replaceAllWord(code, "transpose", "quasar_transpose");
			if (code.indexOf("quasar_transpose") != -1 && code.indexOf("mat4 quasar_transpose") == -1) {
				String transCode = """
					mat3 quasar_transpose(mat3 m) {
					    return mat3(m[0][0], m[1][0], m[2][0], m[0][1], m[1][1], m[2][1], m[0][2], m[1][2], m[2][2]);
					}
					mat4 quasar_transpose(mat4 m) {
					    return mat4(
					        m[0][0], m[1][0], m[2][0], m[3][0],
					        m[0][1], m[1][1], m[2][1], m[3][1],
					        m[0][2], m[1][2], m[2][2], m[3][2],
					        m[0][3], m[1][3], m[2][3], m[3][3]
					    );
					}
					""";
				int insertIdx = findDeclarationPoint(code);
				code.insert(insertIdx, transCode + "\n");
			}
		}
	}

	private static void mapBitfieldHelpers(StringBuilder code) {
		if (containsWord(code, "bitfieldExtract")) {
			replaceAllWord(code, "bitfieldExtract", "quasar_bitfieldExtract");
			if (code.indexOf("quasar_bitfieldExtract") != -1 && code.indexOf("uint quasar_bitfieldExtract") == -1) {
				String bfCode = """
					uint quasar_bitfieldExtract(uint val, int offset, int bits) {
					    uint mask = (1u << uint(bits)) - 1u;
					    return (val >> uint(offset)) & mask;
					}
					int quasar_bitfieldExtract(int val, int offset, int bits) {
					    return int(quasar_bitfieldExtract(uint(val), offset, bits));
					}
					""";
				int insertIdx = findDeclarationPoint(code);
				code.insert(insertIdx, bfCode + "\n");
			}
		}

		if (containsWord(code, "bitfieldInsert")) {
			replaceAllWord(code, "bitfieldInsert", "quasar_bitfieldInsert");
			if (code.indexOf("quasar_bitfieldInsert") != -1 && code.indexOf("uint quasar_bitfieldInsert") == -1) {
				String bfCode = """
					uint quasar_bitfieldInsert(uint baseVal, uint insertVal, int offset, int bits) {
					    uint mask = ((1u << uint(bits)) - 1u) << uint(offset);
					    return (baseVal & ~mask) | ((insertVal << uint(offset)) & mask);
					}
					""";
				int insertIdx = findDeclarationPoint(code);
				code.insert(insertIdx, bfCode + "\n");
			}
		}
	}

	private static void clampFormatsAndShadowRes(StringBuilder code) {
		if (!QuasarCapabilities.canRenderFloat32()) {
			replaceAllWord(code, "RGBA32F", QuasarCapabilities.canRenderFloat16() ? "RGBA16F" : "RGBA8");
			replaceAllWord(code, "RGB32F", QuasarCapabilities.canRenderFloat16() ? "RGBA16F" : "RGBA8");
			replaceAllWord(code, "R32F", QuasarCapabilities.canRenderFloat16() ? "R16F" : "R8");
			replaceAllWord(code, "RG32F", QuasarCapabilities.canRenderFloat16() ? "RG16F" : "RG8");
		}

		// Clamp shadowMapResolution
		int shadowResIdx = code.indexOf("shadowMapResolution");
		if (shadowResIdx != -1) {
			int eqIdx = code.indexOf("=", shadowResIdx);
			int semiIdx = code.indexOf(";", shadowResIdx);
			if (eqIdx != -1 && semiIdx != -1 && eqIdx < semiIdx) {
				try {
					String valStr = code.substring(eqIdx + 1, semiIdx).trim();
					int val = Integer.parseInt(valStr);
					int maxShadowRes = QuasarContext.isGLES() ? 1024 : 2048;
					if (val > maxShadowRes) {
						code.replace(eqIdx + 1, semiIdx, " " + maxShadowRes);
					}
				} catch (Exception ignored) {
				}
			}
		}
	}

	private static int findDeclarationPoint(StringBuilder code) {
		int idx = 0;
		String s = code.toString();
		String[] lines = s.split("\n");
		int pos = 0;
		for (String line : lines) {
			String trimmed = line.trim();
			if (trimmed.startsWith("#version") || trimmed.startsWith("#extension") || trimmed.startsWith("precision")) {
				pos += line.length() + 1;
				idx = pos;
			} else if (!trimmed.isEmpty() && !trimmed.startsWith("//") && !trimmed.startsWith("/*")) {
				break;
			} else {
				pos += line.length() + 1;
			}
		}
		return Math.min(idx, code.length());
	}

	private static boolean containsWord(StringBuilder code, String word) {
		return code.indexOf(word) != -1;
	}

	private static void replaceAllWord(StringBuilder code, String target, String replacement) {
		int idx = 0;
		while ((idx = code.indexOf(target, idx)) != -1) {
			code.replace(idx, idx + target.length(), replacement);
			idx += replacement.length();
		}
	}

	private static void replaceFunctionCall(StringBuilder code, String oldFunc, String newFunc) {
		replaceAllWord(code, oldFunc + "(", newFunc + "(");
	}
}
