package fr.tchkll.skygrad;

import fr.tchkll.skygrad.client.SkygraduSSBO;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class WitherFogController {

    // ── Day-cycle constants ──────────────────────────────────────────────────
    public static final long DAY_LENGTH       = 24000L;
    public static final long MOONRISE_TICK    = 12000L;
    public static final long ONE_MINUTE_TICKS = 1200L;

    // ── Wither aura config ───────────────────────────────────────────────────
    public static int    Y_THRESHOLD          = 120;
    public static int    APPLY_INTERVAL_TICKS = 20;
    public static int    WITHER_DURATION      = APPLY_INTERVAL_TICKS + 5;

    // ── Shader config ────────────────────────────────────────────────────────
    public static float  fogDensity           = 1.0f;
    public static float  fogHeightPercent     = 1.0f;
    public static float  fogColorR            = 0.2f;
    public static float  fogColorG            = -1.0f;
    public static float  fogColorB            = 0.08f;

    // ── Manual override (commandes) ──────────────────────────────────────────
    private static boolean manualOverride     = false;
    private static float   overrideDensity    = 1.0f;

    // ── Core logic ───────────────────────────────────────────────────────────

    public static int witherAmplifierForTime(long dayTime) {
        if (dayTime >= MOONRISE_TICK)                              return -1;
        if (dayTime <  1L * ONE_MINUTE_TICKS)                     return 0;
        if (dayTime <  4L * ONE_MINUTE_TICKS)                     return 1;
        if (dayTime <  MOONRISE_TICK - 4L * ONE_MINUTE_TICKS)     return 2;
        return 1;
    }

    public static void tickPlayer(Player player) {
        if (player.level().isClientSide()) return;
        if (player.level().getGameTime() % APPLY_INTERVAL_TICKS != 0) return;
        if (player.isCreative() || player.isSpectator()) return;
        if (player.getY() >= Y_THRESHOLD) return;
        if (player.hasEffect(MobEffects.REGENERATION)) return;

        long dayTime = player.level().getDayTime() % DAY_LENGTH;
        int amplifier = witherAmplifierForTime(dayTime);
        if (amplifier < 0) return;

        player.addEffect(new MobEffectInstance(
            MobEffects.WITHER,
            WITHER_DURATION + 5,
            amplifier,
            false,
            true
        ));
    }

    /** Calcule la densité du fog selon l'heure du jour. */
    public static float computeFogDensity(long dayTime) {
        if (manualOverride) return overrideDensity;
        if (dayTime >= MOONRISE_TICK) return 0.0f;
        int amp = witherAmplifierForTime(dayTime);
        if (amp < 0) return 0.0f;
        return 0.3f + amp * 0.35f; // I=0.3, II=0.65, III=1.0
    }

    /** Appelé côté client chaque frame pour mettre à jour le SSBO. */
    public static void updateShader(long dayTime) {
        SkygraduSSBO.fogDensity       = computeFogDensity(dayTime);
        SkygraduSSBO.fogHeightPercent = fogHeightPercent;
        SkygraduSSBO.fogColorR        = fogColorR;
        SkygraduSSBO.fogColorG        = fogColorG;
        SkygraduSSBO.fogColorB        = fogColorB;
    }

    // ── Commandes ────────────────────────────────────────────────────────────

    public static void setManualDensity(float density) {
        manualOverride  = true;
        overrideDensity = density;
    }

    public static void clearManualOverride() {
        manualOverride = false;
    }

    public static String getStatus() {
        return String.format(
            "WitherFog | density=%.2f | height=%d | manual=%s | color=(%.2f,%.2f,%.2f)",
            SkygraduSSBO.fogDensity, Y_THRESHOLD, manualOverride,
            fogColorR, fogColorG, fogColorB
        );
    }
}