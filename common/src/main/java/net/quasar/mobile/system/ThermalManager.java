package net.quasar.mobile.system;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;

/**
 * Quasar Mobile - fork modification
 * Thermal and battery monitoring for mobile dynamic quality adjustment.
 */
public class ThermalManager {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quasar");
	private static ThermalManager instance;

	private float currentTemperatureCelsius = 35.0f;
	private boolean HighThermalTriggered = false;

	private ThermalManager() {
	}

	public static synchronized ThermalManager getInstance() {
		if (instance == null) {
			instance = new ThermalManager();
		}
		return instance;
	}

	public void tick() {
		float temp = readThermalSys();
		this.currentTemperatureCelsius = temp;

		if (temp > 45.0f && !HighThermalTriggered) {
			HighThermalTriggered = true;
			LOGGER.warn("[Quasar][THERMAL] Reduced quality due to heat (" + temp + "°C)");
		} else if (temp < 40.0f && HighThermalTriggered) {
			HighThermalTriggered = false;
			LOGGER.info("[Quasar][THERMAL] Temperature normalized (" + temp + "°C)");
		}
	}

	private float readThermalSys() {
		try {
			File thermalDir = new File("/sys/class/thermal/");
			if (thermalDir.exists() && thermalDir.isDirectory()) {
				File[] zones = thermalDir.listFiles((dir, name) -> name.startsWith("thermal_zone"));
				if (zones != null) {
					for (File zone : zones) {
						File tempFile = new File(zone, "temp");
						if (tempFile.exists()) {
							String valStr = Files.readString(tempFile.toPath()).trim();
							float val = Float.parseFloat(valStr);
							if (val > 1000) val /= 1000.0f;
							if (val > 20 && val < 100) {
								return val;
							}
						}
					}
				}
			}
		} catch (Throwable ignored) {
		}
		return 35.0f;
	}

	public boolean isThermalThrottled() {
		return HighThermalTriggered;
	}

	public float getCurrentTemperatureCelsius() {
		return currentTemperatureCelsius;
	}
}
