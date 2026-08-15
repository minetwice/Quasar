package net.quasar.mobile;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.gl.shader.ShaderCompileException;
import net.irisshaders.iris.gl.shader.ShaderType;
import net.quasar.mobile.QuasarTranspiler.ShaderKind;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Quasar Mobile - fork modification
 *
 * Compile recovery ladder system (L1-L6) that retries shader compilation with increasing fallback defines
 * before gracefully marking a pass as unsupported instead of crashing.
 */
public class QuasarRecoveryLadder {
	private static final Map<String, Integer> winningLevels = new ConcurrentHashMap<>();
	private static final Map<String, String> failedPasses = new ConcurrentHashMap<>();

	@FunctionalInterface
	public interface CompileAction {
		int compile(String transpiledSource) throws ShaderCompileException, RuntimeException;
	}

	public static int compileWithLadder(ShaderType type, String name, String rawSource, CompileAction action) throws ShaderCompileException {
		ShaderKind kind = GlShaderKindMapper.toQuasarKind(type);
		int startLevel = winningLevels.getOrDefault(name, 1);

		String lastErrorLog = "";

		for (int level = startLevel; level <= 6; level++) {
			try {
				String transpiled = QuasarTranspiler.transpile(rawSource, kind, name, level);
				int handle = action.compile(transpiled);
				if (handle >= 0) {
					winningLevels.put(name, level);
					return handle;
				}
			} catch (ShaderCompileException e) {
				lastErrorLog = e.getError();
				Iris.logger.warn("Shader " + name + " failed at ladder level L" + level + ", retrying next level...");
			} catch (Exception e) {
				lastErrorLog = e.getMessage();
				Iris.logger.warn("Shader " + name + " failed at ladder level L" + level + " (" + e.getMessage() + "), retrying next level...");
			}
		}

		// If startLevel > 1 failed, try levels 1..startLevel-1
		if (startLevel > 1) {
			for (int level = 1; level < startLevel; level++) {
				try {
					String transpiled = QuasarTranspiler.transpile(rawSource, kind, name, level);
					int handle = action.compile(transpiled);
					if (handle >= 0) {
						winningLevels.put(name, level);
						return handle;
					}
				} catch (Exception e) {
					lastErrorLog = e.getMessage();
				}
			}
		}

		// All levels failed: log error and mark pass as unsupported without crashing
		Iris.logger.error("All recovery ladder levels failed for " + name + ". Log: " + lastErrorLog);
		failedPasses.put(name, lastErrorLog != null && !lastErrorLog.isEmpty() ? lastErrorLog : "Shader compile failure across all levels");

		return -1;
	}

	public static Map<String, String> getFailedPasses() {
		return Collections.unmodifiableMap(failedPasses);
	}

	public static void clearFailedPasses() {
		failedPasses.clear();
	}

	public static Map<String, Integer> getWinningLevels() {
		return Collections.unmodifiableMap(winningLevels);
	}

	private static class GlShaderKindMapper {
		static ShaderKind toQuasarKind(ShaderType type) {
			if (type == ShaderType.VERTEX) return ShaderKind.VERTEX;
			if (type == ShaderType.FRAGMENT) return ShaderKind.FRAGMENT;
			if (type == ShaderType.GEOMETRY) return ShaderKind.GEOMETRY;
			if (type == ShaderType.COMPUTE) return ShaderKind.COMPUTE;
			if (type == ShaderType.TESSELATION_CONTROL) return ShaderKind.TESS_CONTROL;
			if (type == ShaderType.TESSELATION_EVAL) return ShaderKind.TESS_EVAL;
			return ShaderKind.OTHER;
		}
	}
}
