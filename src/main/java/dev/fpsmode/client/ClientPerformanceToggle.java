package dev.fpsmode.client;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;

final class ClientPerformanceToggle {
    private static final Logger LOGGER = LogUtils.getLogger();

    private ClientPerformanceToggle() {}

    static void toggle() {
        try {
            if (PerformanceProfile.isEnabled()) PerformanceProfile.disable();
            else PerformanceProfile.enable();
        } catch (IOException | RuntimeException error) {
            LOGGER.error("Could not toggle FPS Mode with F8.", error);
        }
    }
}
