package net.lordimass.dialogue.eventTags;

import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import net.lordimass.dialogue.codec.DialogueAsset;
import net.lordimass.dialogue.ui.DialoguePageManager;
import java.util.Map;

public class VoiceHandler {
    DialogueAsset dialogue;
    PlayerRef playerRef;

    public VoiceHandler(DialogueAsset dialogue, PlayerRef playerRef) {
        this.dialogue = dialogue;
        this.playerRef = playerRef;
    }

    public void play(char c) {
        int soundIndex = SoundEvent.getAssetMap().getIndex(
            dialogue.getCharacter().getVoice() +
                "_Voice_" +
                Character.toUpperCase(c)
        );
        if (soundIndex == Integer.MIN_VALUE) {
            soundIndex = SoundEvent.getAssetMap()
                .getIndex("Voice_" + Character.toUpperCase(c));
        }


        SoundUtil.playSoundEvent2dToPlayer(playerRef, soundIndex, SoundCategory.SFX);
    }

    public static boolean playSoundEvent(DialoguePageManager pageManager, Map<String, String> params) {
        if (!params.containsKey("is")) return false;
        String soundEvent = params.get("is");

        int id = SoundEvent.getAssetMap().getIndex(soundEvent);
        SoundUtil.playSoundEvent2dToPlayer(pageManager.getPlayerRef(), id, SoundCategory.SFX);
        return id != Integer.MIN_VALUE;
    }
}
