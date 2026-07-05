package net.lordimass.dialogue.ui;

import au.ellie.hyui.builders.*;
import au.ellie.hyui.html.TemplateProcessor;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.protocol.packets.interface_.CustomPageLifetime;
import com.hypixel.hytale.protocol.packets.interface_.CustomUIEventBindingType;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import net.lordimass.dialogue.codec.DialogueAsset;
import net.lordimass.dialogue.codec.DialogueEntry;
import net.lordimass.dialogue.component.NPCDialogueComponent;
import net.lordimass.dialogue.parameter.ParameterRegister;
import net.lordimass.dialogue.system.DialogueTypingSystem;
import net.lordimass.dialogue.util.TokenString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

import static net.lordimass.dialogue.util.TranslationUtils.translate;
import static net.lordimass.dialogue.util.TranslationUtils.translateWithHYUIML;

public class DialoguePageManager {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final int MAX_CHOICES = 4;

    @Getter private final PlayerRef playerRef;
    @Getter private final Ref<EntityStore> npcRef;
    @Getter private final PortraitElementManager portraitManager;
    @Getter private HyUIPage hyUIPage;
    @Getter private DialogueTypingSystem.TypewriterEffectInfo typewriterEffectInfo;
    @Getter private DialogueAsset dialogue;

    private PageBuilder builder;

    public DialoguePageManager(@Nonnull PlayerRef playerRef, @Nullable Ref<EntityStore> npcRef,
                               DialogueAsset dialogue
                        ) {
        this.playerRef = playerRef;
        this.npcRef = npcRef;
        this.portraitManager = new PortraitElementManager();
        openDialogue(dialogue);
    }

    private void openDialogue(DialogueAsset dialogue) {
        this.dialogue = dialogue;
        if (dialogue == null) {if (hyUIPage != null) close();  return;}

        builder = PageBuilder
            .pageForPlayer(playerRef)
            .loadHtml("Pages/Dialogue.html", new TemplateProcessor())
            .enableRuntimeTemplateUpdates(true)
            .onDismiss(this::closeCallback)
            .withLifetime(CustomPageLifetime.CanDismiss)
            .enablePersistentElementEdits(true);
        hyUIPage = builder.open(Objects.requireNonNull(playerRef.getReference()).getStore());
        portraitManager.updatePortraits(builder, this);
        assert hyUIPage != null;

        switch (this.dialogue.getType()) {
            case Dialogue -> populateDialogue();
            case Choice -> populateChoices();
            default -> buildNEXTButton();
        }


        hyUIPage.updatePage(true);

        NPCDialogueComponent.update(npcRef, dialogue, playerRef);
    }

    private void populateDialogue() {
        StringBuilder entries = new StringBuilder();
        for (DialogueEntry entry : dialogue.getEntries()) {
            entries
                .append(translateWithHYUIML(entry.getContent(), playerRef))
                .append("\n");
        }
        entries.delete(entries.length()-1, entries.length());
        String completeDialogueString = entries.toString();
        String inProgressString = completeDialogueString;
        if (dialogue.isTypewriterEffect()) {
            DialogueTypingSystem.tickingPageManagers.add(this);
            typewriterEffectInfo = new DialogueTypingSystem.TypewriterEffectInfo(completeDialogueString, this);
            inProgressString = "";
        }

        builder.getTemplateProcessor()
            .setVariable("title", translateWithHYUIML(dialogue.getCharacter().getName(), playerRef))
            .setVariable("content", inProgressString);
        buildNEXTButton();
    }

    private void populateChoices() {
        TemplateProcessor template = builder.getTemplateProcessor();
        if (dialogue.getEntries().length >  MAX_CHOICES) {
            LOGGER.atWarning().log(
                "Dialogue choice page only supports up to 4 entries. "
                    + dialogue.getId()
                    + " has "
                    + dialogue.getEntries().length);
        }
        for (int i = 0; i < Math.min(dialogue.getEntries().length, MAX_CHOICES); i++) {
            DialogueEntry entry = dialogue.getEntries()[i];
            String content = translateWithHYUIML(entry.getContent(), playerRef);
            template
                .setVariable("choice"+i, content)
                .setVariable("choice"+i+"Display", "block");
            builder.addEventListener("choice"+i, CustomUIEventBindingType.Activating, _ -> {
                handleDoAfter(entry.getDoAfter());
                if (entry.getNext() == null) {close();return;}
                openDialogue(entry.getNext());
            });
        }
    }

    private void buildNEXTButton() {
        boolean isNextExists = dialogue.getNext() != null;
        builder.getTemplateProcessor()
            .setVariable("nextButtonDisplay", "block")
            .setVariable("nextButtonText", isNextExists ? "NEXT" : "CLOSE");
        hyUIPage.updatePage(false);
        builder.addEventListener("next-button", CustomUIEventBindingType.Activating, _ -> {
            handleDoAfter(dialogue.getDoAfter());
            if (isNextExists) openDialogue(dialogue.getNext());
            else close();
        });
    }

    private void closeCallback(HyUIPage hyUIPage, Boolean aBoolean) {
        NPCDialogueComponent.clear(npcRef);
        int index = DialogueTypingSystem.tickingPageManagers.indexOf(this);
        if (index >= 0) {
            DialogueTypingSystem.tickingPageManagers.remove(index);
        }
    }

    private void handleDoAfter(@Nullable String doAfter) {
        if (doAfter == null) return;
        String placeholdersHandled = translateWithHYUIML(doAfter, playerRef);
        TokenString tokenised = new TokenString(placeholdersHandled);
        while (!tokenised.isComplete()) {
            String token = tokenised.next();
            ParameterRegister.processEventTag(this, token);
        }
    }

    public void close() {
        hyUIPage.close();
    }
}
