package net.lordimass.dialogue.codec.validator;

import com.hypixel.hytale.server.npc.asset.builder.validators.AssetValidator;
import net.lordimass.dialogue.codec.DialogueAsset;

import javax.annotation.Nonnull;
import java.util.EnumSet;

public class DialogueExistsValidator extends AssetValidator {
    private static final DialogueExistsValidator DEFAULT_INSTANCE = new DialogueExistsValidator();

    private DialogueExistsValidator() {
    }

    private DialogueExistsValidator(EnumSet<Config> config) {
        super(config);
    }

    public static DialogueExistsValidator required() {
        return DEFAULT_INSTANCE;
    }

    @Nonnull
    public static DialogueExistsValidator withConfig(EnumSet<Config> config) {
        return new DialogueExistsValidator(config);
    }

    @Override
    @Nonnull
    public String getDomain() {
        return "Dialogue";
    }

    @Override
    public boolean test(String marker) {
        return DialogueAsset.getAssetMap().getAsset(marker) != null;
    }

    @Override
    @Nonnull
    public String errorMessage(String marker, String attributeName) {
        return "The Dialogue asset with the name \"" + marker + "\" does not exist for attribute \"" + attributeName + "\"";
    }

    @Override
    @Nonnull
    public String getAssetName() {
        return DialogueAsset.class.getSimpleName();
    }
}
