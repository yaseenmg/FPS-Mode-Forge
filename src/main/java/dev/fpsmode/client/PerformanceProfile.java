package dev.fpsmode.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.CloudStatus;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.ParticleStatus;
import net.minecraftforge.fml.loading.FMLPaths;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class PerformanceProfile {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Path BACKUP_FILE = FMLPaths.CONFIGDIR.get().resolve("fpsmode-original.properties");

    private PerformanceProfile() {}

    static boolean isEnabled() {
        return Files.exists(BACKUP_FILE);
    }

    static void enable() throws IOException {
        Files.createDirectories(BACKUP_FILE.getParent());
        if (!Files.exists(BACKUP_FILE)) saveOriginalSettings();
        else migrateOlderBackup();
        applyLowGraphicsProfile();
        Minecraft.getInstance().options.save();
        LOGGER.info("FPS Mode enabled: aggressive client-side visual profile applied without changing chunk distances.");
    }

    static boolean disable() throws IOException {
        if (!Files.exists(BACKUP_FILE)) {
            ClientPerformanceEffects.restoreAnimations();
            return false;
        }
        restoreOriginalSettings();
        ClientPerformanceEffects.restoreAnimations();
        Minecraft.getInstance().options.save();
        Files.deleteIfExists(BACKUP_FILE);
        LOGGER.info("FPS Mode disabled: saved client settings and animated textures restored.");
        return true;
    }

    static void reapplyOnStartup() {
        if (!isEnabled()) return;
        try {
            migrateOlderBackup();
            applyLowGraphicsProfile();
            Minecraft.getInstance().options.save();
            LOGGER.info("FPS Mode restored from its saved state.");
        } catch (IOException | RuntimeException error) {
            LOGGER.error("Could not reapply FPS Mode settings.", error);
        }
    }

    private static void saveOriginalSettings() throws IOException {
        Options options = Minecraft.getInstance().options;
        Properties p = new Properties();
        // Intentionally do not save or modify render/simulation distance or any chunk-loading option.
        p.setProperty("entityDistanceScaling", options.entityDistanceScaling().get().toString());
        p.setProperty("graphicsMode", options.graphicsMode().get().name());
        p.setProperty("cloudStatus", options.cloudStatus().get().name());
        p.setProperty("particles", options.particles().get().name());
        p.setProperty("ambientOcclusion", options.ambientOcclusion().get().toString());
        p.setProperty("mipmapLevels", options.mipmapLevels().get().toString());
        p.setProperty("biomeBlendRadius", options.biomeBlendRadius().get().toString());
        p.setProperty("entityShadows", options.entityShadows().get().toString());
        p.setProperty("enableVsync", options.enableVsync().get().toString());
        p.setProperty("framerateLimit", options.framerateLimit().get().toString());
        writeBackup(p, "FPS Mode backup. Press F8 again to restore these client settings.");
    }

    private static void migrateOlderBackup() throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(BACKUP_FILE)) {
            p.load(in);
        }
        boolean changed = false;
        Options options = Minecraft.getInstance().options;

        // Older releases changed chunk distances. Restore their saved originals once, then discard
        // those legacy keys so this release and later toggles never manage chunk settings.
        changed |= restoreLegacyChunkSetting(p, "renderDistance", options.renderDistance()::set);
        changed |= restoreLegacyChunkSetting(p, "simulationDistance", options.simulationDistance()::set);

        if (!p.containsKey("enableVsync")) {
            p.setProperty("enableVsync", options.enableVsync().get().toString());
            changed = true;
        }
        if (!p.containsKey("framerateLimit")) {
            p.setProperty("framerateLimit", options.framerateLimit().get().toString());
            changed = true;
        }
        if (changed) writeBackup(p, "FPS Mode backup migrated. Chunk settings are no longer managed.");
    }

    private static boolean restoreLegacyChunkSetting(Properties p, String key, java.util.function.Consumer<Integer> setter) {
        if (!p.containsKey(key)) return false;
        String value = p.getProperty(key);
        try {
            if (value != null && !value.isBlank()) setter.accept(Integer.parseInt(value));
        } catch (NumberFormatException error) {
            LOGGER.warn("Ignoring invalid legacy chunk setting '{}' in FPS Mode backup.", key, error);
        }
        p.remove(key);
        return true;
    }

    private static void writeBackup(Properties p, String comment) throws IOException {
        try (OutputStream out = Files.newOutputStream(BACKUP_FILE)) {
            p.store(out, comment);
        }
    }

    private static void applyLowGraphicsProfile() {
        Options options = Minecraft.getInstance().options;
        // No render-distance, simulation-distance, or chunk-loading settings are changed here.
        options.entityDistanceScaling().set(0.1);
        options.graphicsMode().set(GraphicsStatus.FAST);
        options.cloudStatus().set(CloudStatus.OFF);
        options.particles().set(ParticleStatus.MINIMAL);
        options.ambientOcclusion().set(false);
        options.mipmapLevels().set(0);
        options.biomeBlendRadius().set(0);
        options.entityShadows().set(false);
        options.enableVsync().set(false);
        options.framerateLimit().set(260);
    }

    private static void restoreOriginalSettings() throws IOException {
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(BACKUP_FILE)) {
            p.load(in);
        }
        Options options = Minecraft.getInstance().options;
        options.entityDistanceScaling().set(Double.parseDouble(required(p, "entityDistanceScaling")));
        options.graphicsMode().set(GraphicsStatus.valueOf(required(p, "graphicsMode")));
        options.cloudStatus().set(CloudStatus.valueOf(required(p, "cloudStatus")));
        options.particles().set(ParticleStatus.valueOf(required(p, "particles")));
        options.ambientOcclusion().set(Boolean.parseBoolean(required(p, "ambientOcclusion")));
        options.mipmapLevels().set(Integer.parseInt(required(p, "mipmapLevels")));
        options.biomeBlendRadius().set(Integer.parseInt(required(p, "biomeBlendRadius")));
        options.entityShadows().set(Boolean.parseBoolean(required(p, "entityShadows")));
        if (p.containsKey("enableVsync")) {
            options.enableVsync().set(Boolean.parseBoolean(required(p, "enableVsync")));
        }
        if (p.containsKey("framerateLimit")) {
            options.framerateLimit().set(Integer.parseInt(required(p, "framerateLimit")));
        }
    }

    private static String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);
        if (value == null || value.isBlank()) {
            throw new IOException("Missing setting in FPS Mode backup: " + key);
        }
        return value;
    }
}
