package fr.tchkll.skygrad;

import com.mojang.brigadier.arguments.FloatArgumentType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import static net.minecraft.commands.Commands.literal;
import static net.minecraft.commands.Commands.argument;

public class SkygradCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            literal("skygrad")
                .requires(src -> src.hasPermission(2))
                .then(literal("fog")
                    .then(literal("density")
                        .then(argument("value", FloatArgumentType.floatArg(0f, 10f))
                            .executes(ctx -> {
                                float val = FloatArgumentType.getFloat(ctx, "value");
                                WitherFogController.setManualDensity(val);
                                ctx.getSource().sendSuccess(() ->
                                    net.minecraft.network.chat.Component.literal(
                                        "[Skygrad] Fog density set to " + val + " (manual override ON)"
                                    ), true);
                                return 1;
                            })
                        )
                    )
                    .then(literal("reset")
                        .executes(ctx -> {
                            WitherFogController.clearManualOverride();
                            ctx.getSource().sendSuccess(() ->
                                net.minecraft.network.chat.Component.literal(
                                    "[Skygrad] Fog density reset to automatic"
                                ), true);
                            return 1;
                        })
                    )
                    .then(literal("color")
                        .then(argument("r", FloatArgumentType.floatArg(-1f, 1f))
                            .then(argument("g", FloatArgumentType.floatArg(-1f, 1f))
                                .then(argument("b", FloatArgumentType.floatArg(-1f, 1f))
                                    .executes(ctx -> {
                                        float r = FloatArgumentType.getFloat(ctx, "r");
                                        float g = FloatArgumentType.getFloat(ctx, "g");
                                        float b = FloatArgumentType.getFloat(ctx, "b");
                                        WitherFogController.fogColorR = r;
                                        WitherFogController.fogColorG = g;
                                        WitherFogController.fogColorB = b;
                                        ctx.getSource().sendSuccess(() ->
                                            net.minecraft.network.chat.Component.literal(
                                                "[Skygrad] Fog color set to (" + r + ", " + g + ", " + b + ")"
                                            ), true);
                                        return 1;
                                    })
                                )
                            )
                        )
                    )
                )
        );
    }
}