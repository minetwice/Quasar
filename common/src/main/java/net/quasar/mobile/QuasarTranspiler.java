package net.quasar.mobile;

import net.irisshaders.iris.Iris;
import net.irisshaders.iris.platform.IrisPlatformHelpers;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Quasar Mobile - fork modification
 *
 * Primary transpiler entry point with native-first fallback ladder and SHA-256 caching.
 */
public class QuasarTranspiler {
	public enum ShaderKind {
		VERTEX, FRAGMENT, GEOMETRY, COMPUTE, TESS_CONTROL, TESS_EVAL, OTHER
	}

	private static Path cacheDir;

	public static String transpile(String name, String src, ShaderKind kind) {
		try {
			return transpileInternal(src, kind, name, 1);
		} catch (Throwable t) {
			net.irisshaders.iris.Iris.logger.error("[Quasar] hook QuasarTranspiler failed -> passthrough", t);
			return src;
		}
	}

	public static String transpile(String source, ShaderKind kind, String programName) {
		return transpile(programName, source, kind);
	}

	public static String transpile(String source, ShaderKind kind, String programName, int ladderLevel) {
		try {
			return transpileInternal(source, kind, programName, ladderLevel);
		} catch (Throwable t) {
			net.irisshaders.iris.Iris.logger.error("[Quasar] hook QuasarTranspiler failed -> passthrough", t);
			return source;
		}
	}

	private static String transpileInternal(String source, ShaderKind kind, String programName, int ladderLevel) {
		if (source == null || source.isEmpty()) {
			return source;
		}

		// Requirement 3.3: Desktop GL contexts -> passthrough
		if (QuasarContext.isDesktop()) {
			return source;
		}

		// Try loading from SHA-256 cache first
		String cacheKey = computeHash(source + "_" + QuasarContext.getEsVersionString() + "_L" + ladderLevel + "_" + kind);
		String cached = readFromCache(cacheKey);
		if (cached != null) {
			return cached;
		}

		String transpiled;
		String mode;

		// Requirement 3.2: Try native first; fallback to Java
		try {
			if (NativeTranspiler.isAvailable()) {
				transpiled = NativeTranspiler.transpile(source, kind.ordinal(), QuasarContext.getEsMajor() * 10 + QuasarContext.getEsMinor());
				mode = "native";
			} else {
				transpiled = JavaTranspiler.transpile(source, kind, programName, ladderLevel);
				mode = "java";
			}
		} catch (Throwable t) {
			transpiled = JavaTranspiler.transpile(source, kind, programName, ladderLevel);
			mode = "java";
		}

		saveToCache(cacheKey, transpiled);

		// Requirement 3.18
		Iris.logger.info(programName + ": transpiled via " + mode + " (L" + ladderLevel + ")");

		return transpiled;
	}

	private static synchronized Path getCacheDir() {
		if (cacheDir == null) {
			cacheDir = IrisPlatformHelpers.getInstance().getGameDir().resolve("quasar_shaders");
			try {
				if (!Files.exists(cacheDir)) {
					Files.createDirectories(cacheDir);
				}
			} catch (IOException e) {
				Iris.logger.warn("Failed creating quasar_shaders cache directory", e);
			}
		}
		return cacheDir;
	}

	private static String readFromCache(String hash) {
		try {
			Path file = getCacheDir().resolve(hash + ".glsl");
			if (Files.exists(file)) {
				return Files.readString(file, StandardCharsets.UTF_8);
			}
		} catch (Exception ignored) {
		}
		return null;
	}

	private static void saveToCache(String hash, String content) {
		try {
			Path file = getCacheDir().resolve(hash + ".glsl");
			Files.writeString(file, content, StandardCharsets.UTF_8);
		} catch (Exception e) {
			Iris.logger.warn("Failed saving transpiled shader to cache", e);
		}
	}

	private static String computeHash(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder();
			for (byte b : hash) {
				String h = Integer.toHexString(0xff & b);
				if (h.length() == 1) hex.append('0');
				hex.append(h);
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			return Integer.toHexString(input.hashCode());
		}
	}
}
