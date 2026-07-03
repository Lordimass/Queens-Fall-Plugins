package net.lordimass.dialogue.eventTags;

import com.hypixel.hytale.server.core.command.system.CommandManager;
import com.hypixel.hytale.server.core.console.ConsoleSender;
import com.hypixel.hytale.server.core.universe.PlayerRef;

import java.util.Map;

public class RunCommand {
    public static boolean runCommandEvent(PlayerRef playerRef, Map<String, String> params) {
        if (!params.containsKey("is")) return false;
        CommandManager.get().handleCommand(ConsoleSender.INSTANCE, params.get("is"));
        return true;
    }
}
