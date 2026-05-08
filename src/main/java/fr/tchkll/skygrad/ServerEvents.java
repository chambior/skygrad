package fr.tchkll.skygrad;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Server-wide game-loop hooks.  No block-entity needed — these run for every
 * player on every tick, regardless of where they are or what's loaded.
 *
 * <p><b>Low-altitude wither aura:</b> any player below {@code Y_THRESHOLD}
 * who isn't currently regenerating receives Wither I.  Re-applied once per
 * second so the effect always overlaps itself; no flicker.</p>
 */
@EventBusSubscriber(modid = Skygrad.MODID)
@SuppressWarnings("unused")
public final class ServerEvents {

    private ServerEvents() {}

    /** Players with {@code y < Y_THRESHOLD} are subject to the wither aura. */
    private static final int Y_THRESHOLD          = 200;
    /** Re-apply cadence in ticks — 20 ticks = 1 second. */
    private static final int APPLY_INTERVAL_TICKS = 20;
    /** Effect duration must exceed the apply interval so consecutive applies overlap. */
    private static final int WITHER_DURATION      = APPLY_INTERVAL_TICKS + 5;

    // ── day-cycle constants (Minecraft: 24000 ticks/day, 20 ticks/second) ───
    /** Length of one full day-night cycle in ticks. */
    private static final long DAY_LENGTH       = 24000L;
    /** Tick at which moon rises (= sunset = end of day). */
    private static final long MOONRISE_TICK    = 12000L;
    /** One real-life minute in ticks (1200 = 60 × 20). */
    private static final long ONE_MINUTE_TICKS = 1200L;

    @SubscribeEvent
    public static void onPlayerTickPost(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        if (player.level().isClientSide()) return;
        if (player.level().getGameTime() % APPLY_INTERVAL_TICKS != 0) return;

        if (player.isCreative() || player.isSpectator()) return;
        if (player.getY() >= Y_THRESHOLD) return;
        if (player.hasEffect(MobEffects.REGENERATION)) return;

        long dayTime = player.level().getDayTime() % DAY_LENGTH;
        int amplifier = witherAmplifierForTime(dayTime);
        if (amplifier < 0) return; // night — no effect

        player.addEffect(new MobEffectInstance(
                MobEffects.WITHER,
                WITHER_DURATION + 5,
                amplifier,
                false,   // ambient — no
                true     // show particles
        ));
    }

    // ── elytra flight gate ────────────────────────────────────────────────────

    /** NBT key on a vanilla elytra that marks it as mod-crafted and flight-capable. */
    private static final String MECHANICAL_TAG = "skygrad_mechanical";
    /** {@link net.minecraft.world.entity.Entity}'s shared-flag index for fall-flying. */
    private static final int FALL_FLYING_FLAG  = 7;

    /**
     * Disables fall-flying on every plain {@code minecraft:elytra}.
     * An elytra crafted via the {@code mechanical_elytra} recipe carries a
     * {@code custom_data} flag named {@value #MECHANICAL_TAG} and is allowed
     * to glide; everything else is force-stopped each tick.
     *
     * <p>Uses the {@code Entity#setSharedFlag} access transformer to clear the
     * fall-flying bit directly — no event for "don't start fall flying" exists
     * in 1.21.1, so we react every tick instead.</p>
     */
    @SubscribeEvent
    public static void onPlayerTickPreElytra(PlayerTickEvent.Pre event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;
        if (!player.isFallFlying()) return;

        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        if (!chest.is(Items.ELYTRA)) return;            // some other flight item — leave alone

        if (isMechanical(chest)) return;                // mod-crafted elytra — allow

        player.setSharedFlag(FALL_FLYING_FLAG, false);  // plain vanilla elytra — kill the glide
    }

    /** True iff the stack carries the {@code skygrad_mechanical} custom-data flag. */
    private static boolean isMechanical(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data != null && data.getUnsafe().getBoolean(MECHANICAL_TAG);
    }

    /**
     * Maps a normalised day-time tick (0 ≤ t < {@value #DAY_LENGTH}) to the
     * Wither amplifier that should be applied at that moment.
     *
     * <p>Returns {@code -1} during night, when no aura should fire.</p>
     *
     * <p>Schedule (with {@code 1 min = 1200 ticks}):</p>
     * <ul>
     *   <li>{@code [0, 1 min)} → Wither I — opening minute of dawn</li>
     *   <li>{@code [1 min, 4 min)} → Wither II — morning</li>
     *   <li>{@code [4 min, moonrise − 4 min)} → Wither III — midday peak</li>
     *   <li>{@code [moonrise − 4 min, moonrise − 1 min)} → Wither II — late afternoon ramp-down</li>
     *   <li>{@code [moonrise − 1 min, moonrise)} → Wither I — final minute before sunset</li>
     *   <li>{@code [moonrise, day end)} → night, no effect ({@code -1})</li>
     * </ul>
     */
    private static int witherAmplifierForTime(long dayTime) {
        if (dayTime >= MOONRISE_TICK)                       return -1;  // night
        if (dayTime <  1L * ONE_MINUTE_TICKS)               return 0;   // Wither I
        if (dayTime <  4L * ONE_MINUTE_TICKS)               return 1;   // Wither II
        if (dayTime <  MOONRISE_TICK - 4L * ONE_MINUTE_TICKS) return 2; // Wither III
        return 1;                                                       // Wither II
    }
}
