package net.lordimass.dialogue.system;

import au.ellie.hyui.builders.HyUIPage;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.DelayedEntitySystem;
import com.hypixel.hytale.server.core.entity.entities.Player;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import lombok.Getter;
import net.lordimass.dialogue.eventTags.VoiceHandler;
import net.lordimass.dialogue.ui.DialoguePageManager;
import net.lordimass.dialogue.util.TokenString;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static net.lordimass.dialogue.parameter.ParameterRegister.processEventTag;

/**
 * Handles the typewriter effect for any dialogues added to its <code>tickingPageManagers</code>
 * static list.
 */
public class DialogueTypingSystem extends DelayedEntitySystem<EntityStore> {
    private static final float CHARS_PER_SECOND = 16.0F;
    private static final Set<Character> DELAY_CHARACTERS = Stream.of('.', ',', ';', '!', '?')
        .collect(Collectors.toUnmodifiableSet());
    public static ArrayList<DialoguePageManager> tickingPageManagers = new ArrayList<>();

    public DialogueTypingSystem() {
        super(1.0F / CHARS_PER_SECOND);
    }

    @Override
    public void tick(
        float dt,
        int index,
        @NonNull ArchetypeChunk<EntityStore> archetypeChunk,
        @NonNull Store<EntityStore> store,
        @NonNull CommandBuffer<EntityStore> commandBuffer
    ) {
        ArrayList<DialoguePageManager> newTickingPageManagers = new ArrayList<>();
        tickingPageManagers.forEach((pageManager) -> {
            if (tickPageManger(pageManager)) newTickingPageManagers.add(pageManager);
        });
        tickingPageManagers = new ArrayList<>(newTickingPageManagers);
    }

    private static boolean tickPageManger(DialoguePageManager pageManager) {
        TypewriterEffectInfo info = pageManager.getTypewriterEffectInfo();
        TokenString tokenString = info.getTokenString();
        String nextToken = tokenString.next();
        if (nextToken == null) return false;

        updateGui(pageManager, tokenString.getInProgressString());
        boolean isEventTag = processEventTag(pageManager, nextToken);
        boolean isDelayChar = nextToken.length() == 1 && DELAY_CHARACTERS.contains(nextToken.charAt(0));
        if (isDelayChar || isEventTag) info.delayNext = true;

        pageManager.getTypewriterEffectInfo().getVoiceHandler().play(nextToken.charAt(0));

        return !tokenString.isComplete();
    }

    private static void updateGui(DialoguePageManager pageManager, String content) {
        HyUIPage hyUIPage = pageManager.getHyUIPage();
        hyUIPage.getTemplateProcessor()
            .setVariable("content", content);
        hyUIPage.updatePage(false);
    }

    @Override
    public @Nullable Query<EntityStore> getQuery() {
        return Player.getComponentType();
    }

    public static class TypewriterEffectInfo {
        @Getter
        TokenString tokenString;
        @Getter
        private final VoiceHandler voiceHandler;
        protected boolean delayNext;

        public TypewriterEffectInfo(@Nonnull String completeDialogueString, @Nonnull DialoguePageManager pageManager) {
            this.tokenString = new TokenString(completeDialogueString);
            this.voiceHandler = new VoiceHandler(pageManager.getDialogue(), pageManager.getPlayerRef());
        }
    }
}
