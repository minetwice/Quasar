package net.quasar.mobile;

import net.irisshaders.iris.Iris;

/**
 * Quasar Mobile - fork modification
 *
 * Native transpiler wrapper binding to native library 'quasar_gl' if available.
 */
public class NativeTranspiler {
	private static boolean available = false;
	private static boolean triedLoad = false;

	private static synchronized void checkLoad() {
		if (triedLoad) return;
		triedLoad = true;
		try {
			System.loadLibrary("quasar_gl");
			available = true;
			Iris.logger.info("Loaded native quasar_gl transpiler library successfully.");
		} catch (Throwable t) {
			available = false;
			Iris.logger.info("Native quasar_gl library not present, falling back to JavaTranspiler.");
		}
	}

	public static boolean isAvailable() {
		checkLoad();
		return available;
	}

	public static String transpile(String src, int kind, int esVer) {
		if (!isAvailable()) {
			throw new UnsupportedOperationException("NativeTranspiler unavailable");
		}
		return nativeTranspile(src, kind, esVer);
	}

	private static native String nativeTranspile(String src, int kind, int esVer);
}
