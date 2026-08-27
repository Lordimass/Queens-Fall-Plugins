package net.lordimass.dialogue.sensor;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.corecomponents.SensorBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import net.lordimass.dialogue.component.NPCDialogueComponent;
import net.lordimass.dialogue.sensor.builder.BuilderSensorDialogue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;

public class SensorDialogue extends SensorBase {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private final String blockId;

    public SensorDialogue(@NonNull BuilderSensorDialogue builder) {
        super(builder);
        blockId = builder.getBlockId();
    }

    @Override
    public boolean matches(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, double dt, @Nonnull Store<EntityStore> store) {
        NPCDialogueComponent npcDialogueComponent = NPCDialogueComponent.get(ref, store);
        if (npcDialogueComponent == null) {
            return blockId == null || blockId.isEmpty();
        }
        return blockId.equals(npcDialogueComponent.getCurrentDialogue().getBlockId());
    }

    @Override
    public @Nullable InfoProvider getSensorInfo() {
        return null;
    }
}
