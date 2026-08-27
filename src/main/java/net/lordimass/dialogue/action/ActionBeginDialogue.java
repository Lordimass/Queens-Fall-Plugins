package net.lordimass.dialogue.action;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.corecomponents.ActionBase;
import com.hypixel.hytale.server.npc.instructions.ExecutionSupport;
import com.hypixel.hytale.server.npc.role.Role;
import com.hypixel.hytale.server.npc.sensorinfo.InfoProvider;
import net.lordimass.dialogue.action.builder.BuilderActionBeginDialogue;
import net.lordimass.dialogue.codec.DialogueAsset;
import net.lordimass.dialogue.ui.DialoguePageManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ActionBeginDialogue extends ActionBase {
    protected final String dialogueId;

    public ActionBeginDialogue(@Nonnull BuilderActionBeginDialogue builder, @Nonnull BuilderSupport support) {
        super(builder);
        this.dialogueId = builder.getDialogId(support);
    }

    @Override
    public boolean canExecute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        return super.canExecute(ref, executionSupport, sensorInfo, dt, store) && executionSupport.getStateSupport().getInteractionIterationTarget() != null;
    }

    @Override
    public boolean execute(@Nonnull Ref<EntityStore> ref, @Nonnull ExecutionSupport executionSupport, @Nullable InfoProvider sensorInfo, double dt, @Nonnull Store<EntityStore> store) {
        if (!canExecute(ref, executionSupport, sensorInfo, dt, store)) return false;

        Ref<EntityStore> playerReference = executionSupport.getStateSupport().getInteractionIterationTarget();
        if (playerReference == null) return false;

        PlayerRef playerRef = store.getComponent(playerReference, PlayerRef.getComponentType());
        if (playerRef == null) return false;

        new DialoguePageManager(playerRef, ref, DialogueAsset.getAsset(this.dialogueId));

        super.execute(ref, executionSupport, sensorInfo, dt, store);
        return true;
    }
}
