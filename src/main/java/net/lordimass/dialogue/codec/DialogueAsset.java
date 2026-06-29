package net.lordimass.dialogue.codec;

import com.hypixel.hytale.assetstore.AssetExtraInfo;
import com.hypixel.hytale.assetstore.AssetKeyValidator;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.AssetStore;
import com.hypixel.hytale.assetstore.codec.AssetBuilderCodec;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.assetstore.map.JsonAssetWithMap;
import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.ExtraInfo;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.codecs.array.ArrayCodec;
import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.Validator;
import com.hypixel.hytale.codec.validation.validator.NonEmptyStringValidator;
import com.hypixel.hytale.codec.validation.validator.NonNullValidator;
import com.hypixel.hytale.logger.HytaleLogger;
import lombok.Getter;
import lombok.ToString;
import net.lordimass.dialogue.DialogueMod;
import org.bson.BsonValue;
import org.jspecify.annotations.NonNull;
import javax.annotation.Nullable;

@ToString
public class DialogueAsset implements JsonAssetWithMap<String, DefaultAssetMap<String, DialogueAsset>> {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    public static final EnumCodec<DialogueType> DIALOG_TYPE_ENUM_CODEC = new EnumCodec<>(DialogueType.class);

    public static final BuilderCodec<DialogueAsset> CODEC =
        BuilderCodec.builder(DialogueAsset.class, DialogueAsset::new)
            .append(
                new KeyedCodec<>("Type", DIALOG_TYPE_ENUM_CODEC),
                (obj, val) -> obj.type = val,
                obj -> obj.type
            )
            .add()
            .append(
                new KeyedCodec<>("Entries", new ArrayCodec<>(DialogueEntry.CODEC, DialogueEntry[]::new)),
                (asset, entries) -> {
                    asset.entries = entries;
                },
                asset -> asset.entries
            )
            .documentation("Content of the dialogue.")
            .add()
            .append(
                new KeyedCodec<>("Character", Codec.STRING),
                (asset, s) -> asset.characterId = s,
                asset -> asset.characterId
            )
            .documentation("The ID of the character asset to use for this Dialogue.")
            .addValidator(new AssetKeyValidator<>(CharacterAsset::getAssetStore))
            .addValidator(NonEmptyStringValidator.INSTANCE)
            .add()
            .append(
                new KeyedCodec<>("Character2", Codec.STRING),
                (asset, s) -> asset.character2Id = s,
                asset -> asset.character2Id
            )
            .documentation("The ID of the second character asset to use for this Dialogue." +
                "Optional, but adds an additional character portrait as the character being" +
                "spoken to.")
            .addValidator(new AssetKeyValidator<>(CharacterAsset::getAssetStore))
            .add()
            .append(
                new KeyedCodec<>("NextId", Codec.STRING),
                (asset, s) -> asset.nextId = s,
                asset -> asset.nextId
            )
            .documentation("The asset ID of the next dialogue that should open after continuing.")
            .addValidatorLate(() -> new AssetKeyValidator<>(DialogueAsset::getAssetStore).late())
            .add()
            .append(
                new KeyedCodec<>("Next", new LazyCodec()),
                (asset, s) -> asset.next = s,
                asset -> asset.next
            )
            .documentation("The next dialogue to open after this one. Use `NextId` instead to " +
                "reference a separate dialogue asset instead of inlining it here.")
            .add()
            .append(new KeyedCodec<>("TypewriterEffect", Codec.BOOLEAN),
                (obj, val) -> obj.typewriterEffect = val,
                obj -> obj.typewriterEffect
            )
            .documentation("Should the dialogue be written over time like a typewriter?")
            .add()
            .append(new KeyedCodec<>("BlockIdentifier", Codec.STRING),
                (obj, val) -> obj.blockId = val,
                obj -> obj.blockId
            )
            .documentation("A unique identifier for this specific dialogue block. If left " +
                "undefined, it will default to the ID of the asset (i.e. the JSON file name).")
            .add()
            .build();


    public static final AssetBuilderCodec<String, DialogueAsset> ASSET_BUILDER_CODEC =
        AssetBuilderCodec.wrap(
            DialogueAsset.CODEC,
            Codec.STRING,
            (asset, s) -> asset.id = s,
            asset -> asset.id,
            (asset, data) -> asset.extraData = data,
            asset -> asset.extraData
        );

    private static AssetStore<String, DialogueAsset, DefaultAssetMap<String, DialogueAsset>> ASSET_STORE;
    private AssetExtraInfo.Data extraData;
    @Getter private DialogueType type = DialogueType.Dialogue;
    @Getter private String id;
    @Getter private DialogueEntry[] entries;
    private String nextId;
    private DialogueAsset next;
    private String blockId;
    @Getter private boolean typewriterEffect = true;
    private String characterId;
    private CharacterAsset character;
    private String character2Id;
    private CharacterAsset character2;

    protected DialogueAsset() {}

    public String getBlockId() {
        return blockId == null ? id : blockId;
    }

    public static AssetStore<String, DialogueAsset, DefaultAssetMap<String, DialogueAsset>> getAssetStore() {
        if (ASSET_STORE == null) ASSET_STORE = AssetRegistry.getAssetStore(DialogueAsset.class);
        return ASSET_STORE;
    }

    public static DefaultAssetMap<String, DialogueAsset> getAssetMap() {
        return DialogueAsset.getAssetStore().getAssetMap();
    }

    @Nullable
    public static DialogueAsset getAsset(String key) {
        DialogueAsset asset = AssetRegistry
            .getAssetStore(DialogueAsset.class)
            .getAssetMap()
            .getAsset(key);
        if (asset == null) LOGGER.atSevere().log("DialogueAsset '"+key+"' could not be found");
        return asset;
    }

    @Nullable
    public DialogueAsset getNext() {
        if (this.next != null) {
            return this.next;
        } else if (this.nextId != null) {
            return getAsset(this.nextId);
        }
        return null;
    }

    public CharacterAsset getCharacter() {
        if (characterId == null) return null;
        if (character != null && character.getId().equals(characterId)) return character;
        character = CharacterAsset.getAsset(characterId);
        return character;
    }

    public CharacterAsset getCharacter2() {
        if (character2Id == null) return null;
        if (character2 != null && character2.getId().equals(character2Id)) return character2;
        character2 = CharacterAsset.getAsset(character2Id);
        return character2;
    }

    /** Lazy DialogueAsset.CODEC work around so that the codec can be self-referential. */
    public static class LazyCodec implements Codec<DialogueAsset> {
        public @NonNull Schema toSchema(@NonNull SchemaContext schemaContext) {
            return Schema.ref("common.json#/definitions/DialogueAsset");
//            return DialogueAsset.CODEC.toSchema(schemaContext);
        }
        public @Nullable DialogueAsset decode(BsonValue bsonValue, ExtraInfo extraInfo) {return DialogueAsset.CODEC.decode(bsonValue, extraInfo);}
        public BsonValue encode(DialogueAsset dialogueAsset, ExtraInfo extraInfo) {return DialogueAsset.CODEC.encode(dialogueAsset, extraInfo);}
    }

}
