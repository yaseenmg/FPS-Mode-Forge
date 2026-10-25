package dev.fpsmode.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.HugeExplosionParticle;
import net.minecraft.client.particle.HugeExplosionSeedParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/** Client-only effects that work without Embeddium and never reload resource packs. */
final class ClientPerformanceEffects {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Field TEXTURES_BY_PATH = findField(TextureManager.class, "byPath");
    private static final Field ATLAS_ANIMATED_TEXTURES = findField(TextureAtlas.class, "animatedTextures");
    private static final Field PARTICLES_BY_RENDER_TYPE = findField(ParticleEngine.class, "particles");
    private static final Field PARTICLES_TO_ADD = findField(ParticleEngine.class, "particlesToAdd");
    private static final Map<TextureAtlas, List<TextureAtlasSprite.Ticker>> PAUSED_TICKERS = new IdentityHashMap<>();
    private static boolean reflectionWarningLogged;

    private ClientPerformanceEffects() {}

    static void tick(boolean enabled) {
        if (enabled) {
            freezeAnimatedTextures();
            cullLargeExplosionParticles();
        } else {
            restoreAnimations();
        }
    }

    static void restoreAnimations() {
        if (ATLAS_ANIMATED_TEXTURES == null) {
            PAUSED_TICKERS.clear();
            return;
        }
        for (Map.Entry<TextureAtlas, List<TextureAtlasSprite.Ticker>> entry : PAUSED_TICKERS.entrySet()) {
            try {
                ATLAS_ANIMATED_TEXTURES.set(entry.getKey(), new ArrayList<>(entry.getValue()));
            } catch (IllegalAccessException | RuntimeException error) {
                warnReflection("Could not restore animated textures.", error);
            }
        }
        PAUSED_TICKERS.clear();
    }

    private static void freezeAnimatedTextures() {
        if (TEXTURES_BY_PATH == null || ATLAS_ANIMATED_TEXTURES == null) return;
        Minecraft minecraft = Minecraft.getInstance();
        try {
            Object value = TEXTURES_BY_PATH.get(minecraft.getTextureManager());
            if (!(value instanceof Map<?, ?> textures)) return;
            for (Object texture : textures.values()) {
                if (!(texture instanceof TextureAtlas atlas)) continue;
                Object tickerValue = ATLAS_ANIMATED_TEXTURES.get(atlas);
                if (!(tickerValue instanceof List<?> tickers) || tickers.isEmpty()) continue;

                List<TextureAtlasSprite.Ticker> original = new ArrayList<>();
                for (Object ticker : tickers) {
                    if (ticker instanceof TextureAtlasSprite.Ticker spriteTicker) original.add(spriteTicker);
                }
                if (original.isEmpty()) continue;
                // Replace the saved list after a resource reload, so disabling restores the newest atlas.
                PAUSED_TICKERS.put(atlas, original);
                tickers.clear();
            }
        } catch (IllegalAccessException | RuntimeException error) {
            warnReflection("Could not freeze animated textures.", error);
        }
    }

    private static void cullLargeExplosionParticles() {
        if (PARTICLES_BY_RENDER_TYPE == null || PARTICLES_TO_ADD == null) return;
        try {
            ParticleEngine engine = Minecraft.getInstance().particleEngine;
            Object value = PARTICLES_BY_RENDER_TYPE.get(engine);
            if (value instanceof Map<?, ?> particleLayers) {
                for (Object layer : particleLayers.values()) {
                    if (layer instanceof Collection<?> particles) {
                        particles.removeIf(ClientPerformanceEffects::isLargeExplosionParticle);
                    }
                }
            }
            Object pendingValue = PARTICLES_TO_ADD.get(engine);
            if (pendingValue instanceof Queue<?> pending) {
                pending.removeIf(ClientPerformanceEffects::isLargeExplosionParticle);
            }
        } catch (IllegalAccessException | RuntimeException error) {
            warnReflection("Could not cull large explosion particles.", error);
        }
    }

    private static boolean isLargeExplosionParticle(Object particle) {
        return particle instanceof HugeExplosionParticle || particle instanceof HugeExplosionSeedParticle;
    }

    private static Field findField(Class<?> owner, String name) {
        try {
            return ObfuscationReflectionHelper.findField(owner, name);
        } catch (RuntimeException error) {
            warnReflection("Could not find performance field " + owner.getSimpleName() + "." + name + ".", error);
            return null;
        }
    }

    private static void warnReflection(String message, Throwable error) {
        if (!reflectionWarningLogged) {
            reflectionWarningLogged = true;
            LOGGER.warn(message, error);
        }
    }
}
