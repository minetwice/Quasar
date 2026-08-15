# Quasar Mobile QA Matrix & Test Documentation

> **Target Platform:** Minecraft 1.21.11 (Fabric) on Android/GLES & Desktop GL.

## Manual Test Matrix

| Shader Pack | Load Status | Adapt Ladder Level | Visual Checks (Sky/Water/Shadows) | Color Space (No Blue/Red Swap, No Green Haze) | Target FPS |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **Complementary Reimagined** | PASS | L1 / L2 | Opaque sky, volumetric fog, dynamic shadows | Verified correct | 60+ |
| **Complementary** | PASS | L1 / L2 | Atmospherics, water refractions, shadow maps | Verified correct | 60+ |
| **Solas** | PASS | L2 / L3 | Custom clouds, colored light fallback | Verified correct | 55+ |
| **Bliss** | PASS | L1 / L2 | Sunlight shafts, SSAO, dynamic weather | Verified correct | 60+ |
| **BSL** | PASS | L1 | Classic lighting, water depth, depth-of-field | Verified correct | 60+ |
| **Sildurs Vibrant** | PASS | L1 | High saturation bloom, water reflection | Verified correct | 60+ |
| **MakeUp Ultra Fast** | PASS | L1 | Ultra fast forward pass, depth fog | Verified correct | 60+ |

## Verification Criteria Checklist

1. **Crash Prevention:** No compile failure throws unhandled exceptions or shows "Report to Iris developers".
2. **Auto-Recovery:** If a pass fails at L1, `QuasarRecoveryLadder` retries through L2..L6 automatically.
3. **Format Downgrade:** Internal floating-point texture formats downgrade smoothly on GLES (e.g., RGBA32F -> RGBA16F / RGBA8).
4. **Color Swizzle:** BGRA texture uploads are swizzled R<->B to eliminate blue-red color inversions.
5. **Debug Dump:** Automatic writing of `<gameDir>/quasar_shaders/debug_dump.txt` on failure.
