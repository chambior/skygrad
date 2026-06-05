package fr.tchkll.skygrad.client;

import dev.simulated_team.simulated.content.blocks.portable_engine.PortableEngineRenderer;
import fr.tchkll.skygrad.registration.ModBlockEntities;
import fr.tchkll.skygrad.Skygrad;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = Skygrad.MODID, value = Dist.CLIENT)
public class SkygradClient {

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.SKY_ENGINE_BE.get(), PortableEngineRenderer::new);
    }
}
