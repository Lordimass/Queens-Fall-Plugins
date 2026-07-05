package net.lordimass.dialogue.action.builder;

import com.google.gson.JsonElement;
import com.hypixel.hytale.server.npc.asset.builder.BuilderDescriptorState;
import com.hypixel.hytale.server.npc.asset.builder.BuilderSupport;
import com.hypixel.hytale.server.npc.asset.builder.InstructionType;
import com.hypixel.hytale.server.npc.asset.builder.holder.AssetHolder;
import com.hypixel.hytale.server.npc.corecomponents.builders.BuilderActionBase;
import com.hypixel.hytale.server.npc.instructions.Action;
import net.lordimass.dialogue.action.ActionBeginDialogue;
import net.lordimass.dialogue.codec.validator.DialogueExistsValidator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumSet;

public class BuilderActionBeginDialogue extends BuilderActionBase {
    protected final AssetHolder dialogueId = new AssetHolder();

    @Nullable
    @Override
    public String getShortDescription() {
        return "Begin the dialogue for the current player";
    }

    @Nullable
    @Override
    public String getLongDescription() {
        return getShortDescription();
    }

    @Nullable
    @Override
    public Action build(@Nonnull BuilderSupport builderSupport) {
        return new ActionBeginDialogue(this, builderSupport);
    }

    @Override
    @Nonnull
    public BuilderDescriptorState getBuilderDescriptorState() {
        return BuilderDescriptorState.Stable;
    }

    @Nonnull
    public BuilderActionBeginDialogue readConfig(@Nonnull JsonElement data) {
        this.requireAsset(data, "Dialogue", this.dialogueId, DialogueExistsValidator.required(), BuilderDescriptorState.Stable, "The dialogue to begin", null);
        this.requireInstructionType(EnumSet.of(InstructionType.Interaction));
        return this;
    }

    public String getDialogId(@Nonnull BuilderSupport support) {
        return this.dialogueId.get(support.getExecutionContext());
    }
}
