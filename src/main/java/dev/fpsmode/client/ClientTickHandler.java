package dev.fpsmode.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "fpsmode", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientTickHandler {
    private ClientTickHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (ClientKeybinds.TOGGLE_PERFORMANCE.consumeClick()) {
            ClientPerformanceToggle.toggle();
        }
        ClientPerformanceEffects.tick(PerformanceProfile.isEnabled());
    }
}
