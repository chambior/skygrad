package fr.tchkll.skygrad.client;
import com.mojang.blaze3d.systems.RenderSystem;

import fr.tchkll.skygrad.WitherFogController;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.lwjgl.opengl.GL46;
import org.lwjgl.system.MemoryUtil;
import java.nio.FloatBuffer;

public class SkygraduSSBO {

    private static int ssboId = -1;
    private static FloatBuffer buffer;

    // valeurs modifiables depuis ton mod
    public static float fogDensity = 1.0f;
    public static float fogHeightPercent = 1.0f;
    public static float fogColorR = 0.2f;
    public static float fogColorG = -1.0f;
    public static float fogColorB = 0.08f;
    

    public static void init() {
        
        System.out.println("[Skygrad] SSBO INIT");
        RenderSystem.assertOnRenderThread();
        ssboId = GL46.glGenBuffers();
        buffer = MemoryUtil.memAllocFloat(8);
        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, ssboId);
        GL46.glBufferData(GL46.GL_SHADER_STORAGE_BUFFER, 32, GL46.GL_DYNAMIC_DRAW);
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 1, ssboId);
        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, 0);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) return;

        if (ssboId == -1) {
            init();
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            WitherFogController.updateShader(mc.level.getDayTime() % WitherFogController.DAY_LENGTH);
        }
        buffer.clear();
        buffer.put(fogDensity);
        buffer.put(fogHeightPercent);
        buffer.put(0f);
        buffer.put(0f);
        buffer.put(fogColorR);
        buffer.put(fogColorG);
        buffer.put(fogColorB);
        buffer.put(0f);
        buffer.flip();

        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, ssboId);
        GL46.glBufferSubData(GL46.GL_SHADER_STORAGE_BUFFER, 0, buffer);
        GL46.glBindBufferBase(GL46.GL_SHADER_STORAGE_BUFFER, 1, ssboId);
        GL46.glBindBuffer(GL46.GL_SHADER_STORAGE_BUFFER, 0);
    }
}