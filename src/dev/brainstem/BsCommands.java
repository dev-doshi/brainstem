package dev.brainstem;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

/** /brainstem stats | reset | module &lt;name&gt; on|off | verify off|sample|all */
public final class BsCommands {
    private BsCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> d) {
        d.register(Commands.literal("brainstem")
            .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
            .then(Commands.literal("stats").executes(c -> say(c, Brainstem.statsText())))
            .then(Commands.literal("reset").executes(c -> {
                Brainstem.reset();
                return say(c, "Brainstem counters reset");
            }))
            .then(Commands.literal("module")
                .then(Commands.argument("name", StringArgumentType.word())
                    .then(Commands.literal("on").executes(c -> setModule(c, true)))
                    .then(Commands.literal("off").executes(c -> setModule(c, false)))))
            .then(Commands.literal("verify")
                .then(Commands.argument("mode", StringArgumentType.word()).executes(c -> {
                    String s = StringArgumentType.getString(c, "mode");
                    try {
                        Verify.setMode(Verify.Mode.valueOf(s.toUpperCase()));
                    } catch (IllegalArgumentException e) {
                        c.getSource().sendFailure(Component.literal("verify mode must be off, sample or all"));
                        return 0;
                    }
                    return say(c, "Brainstem verify=" + s.toLowerCase());
                }))));
    }

    private static int setModule(CommandContext<CommandSourceStack> c, boolean on) {
        Module m = Module.byKey(StringArgumentType.getString(c, "name"));
        if (m == null) {
            c.getSource().sendFailure(Component.literal("unknown module; one of mtvg, oneShotGate, competitorScan, reachProbe, reachMemo"));
            return 0;
        }
        if (!m.loaded) {
            c.getSource().sendFailure(Component.literal(m.key + " was disabled at boot; enable it in config/brainstem.json and restart"));
            return 0;
        }
        m.on = on;
        return say(c, "Brainstem " + m.key + (on ? " on" : " off"));
    }

    private static int say(CommandContext<CommandSourceStack> c, String msg) {
        c.getSource().sendSuccess(() -> Component.literal(msg), false);
        Brainstem.LOG.info(msg);
        return 1;
    }
}
