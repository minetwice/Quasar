package net.quasar.mobile.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Quasar Mobile - fork modification
 * Rolling average FPS monitoring and dynamic quality tier governor.
 */
public class PerformanceGovernor {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static PerformanceGovernor instance;

	public enum QualityTier {
		POTATO(512, 0.70f),
		LOW(1024, 0.80f),
		MEDIUM(1536, 0.90f),
		HIGH(2048, 1.00f);

		private final int shadowResolution;
		private final float renderScale;

		QualityTier(int shadowResolution, float renderScale) {
			this.shadowResolution = shadowResolution;
			this.renderScale = renderScale;
		}

		public int getShadowResolution() {
			return shadowResolution;
		}

		public float getRenderScale() {
			return renderScale;
		}
	}

	private QualityTier currentTier = QualityTier.MEDIUM;
	private float rollingFps = 60.0f;
	private long lastTierChangeTime = System.currentTimeMillis();

	private PerformanceGovernor() {
	}

	public static synchronized PerformanceGovernor getInstance() {
		if (instance == null) {
			instance = new PerformanceGovernor();
		}
		return instance;
	}

	public void updateFps(float currentFps) {
		this.rollingFps = this.rollingFps * 0.9f + currentFps * 0.1f;
		long now = System.currentTimeMillis();

		if (now - lastTierChangeTime > 5000) {
			if (rollingFps < 30.0f && currentTier.ordinal() > 0) {
				currentTier = QualityTier.values()[currentTier.ordinal() - 1];
				lastTierChangeTime = now;
				LOGGER.info("[Quasar][GOVERNOR] Lowered quality tier to " + currentTier + " (FPS: " + String.format("%.1f", rollingFps) + ")");
			} else if (rollingFps > 55.0f && currentTier.ordinal() < QualityTier.values().length - 1 && !ThermalManager.getInstance().isThermalThrottled()) {
				currentTier = QualityTier.values()[currentTier.ordinal() + 1];
				lastTierChangeTime = now;
				LOGGER.info("[Quasar][GOVERNOR] Raised quality tier to " + currentTier + " (FPS: " + String.format("%.1f", rollingFps) + ")");
			}
		}
	}

	public QualityTier getCurrentTier() {
		return currentTier;
	}

	public float getRollingFps() {
		return rollingFps;
	}
}
