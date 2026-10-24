package dev.fpsmode;

import dev.fpsmode.client.ClientSetup;
import net.minecraftforge.fml.common.Mod;

@Mod("fpsmode")
public final class FPSModeMod {
    public FPSModeMod() {
        ClientSetup.init();
    }
}