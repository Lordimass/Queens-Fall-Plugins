package net.lordimass.dialogue.codec;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.validation.Validators;
import lombok.Getter;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Getter
public class DialogueEntry {
    public static final BuilderCodec<DialogueEntry> CODEC =
        BuilderCodec
            .builder(DialogueEntry.class, DialogueEntry::new)
            .append(
                new KeyedCodec<>("Content", Codec.STRING),
                (obj, val) -> obj.content = val,
                obj -> obj.content
            )
            .addValidator(Validators.nonNull())
            .add()
            .append(
                new KeyedCodec<>("NextId", Codec.STRING),
                (obj, val) -> obj.nextId = val,
                obj -> obj.nextId
            )
            .documentation("Only applicable if this is an entry of a `Choice` dialogue. The asset ID of the next dialogue that should open when this entry is picked.")
            .add()
            .append(
                new KeyedCodec<>("Next", new DialogueAsset.LazyCodec()),
                (asset, s) -> asset.next = s,
                asset -> asset.next
            )
            .documentation("Only applicable if this is an entry of a `Choice` dialogue. The next dialogue to open when this entry is picked. Use `NextId` instead to reference a separate dialogue asset instead of inlining it here.")
            .add()
            .append(new KeyedCodec<>("Do", Codec.STRING),
                (obj, val) -> obj.doAfter = val,
                obj -> obj.doAfter
            )
            .documentation("Do something after the player clicks \"Next\" on this dialogue." +
                "This should contain an event tag string, like `<command is=\"say Hello World!\">`")
            .add()
            .build();

    @Getter private String content;
    @Nullable private String nextId;
    @Nullable private DialogueAsset next;
    @Getter @Nullable private String doAfter;

    public DialogueEntry(String content, @Nullable String nextId) {
        this.content = content;
        this.nextId = nextId;
    }

    protected DialogueEntry() {
    }

    @Nonnull
    public String toString() {
        return "DialogueEntry{next=" + this.nextId + ", content=" + this.content + "}";
    }

    public DialogueAsset getNext() {
        if (this.next != null) {
            return this.next;
        } else if (this.nextId != null) {
            return DialogueAsset.getAsset(this.nextId);
        }
        return null;
    }
}