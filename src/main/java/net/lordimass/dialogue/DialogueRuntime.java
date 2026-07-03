package net.lordimass.dialogue;

import com.creditor.Creditor;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hypixel.hytale.server.core.event.events.player.PlayerConnectEvent;
import com.hypixel.hytale.server.core.event.events.player.PlayerDisconnectEvent;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.Interaction;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.npc.NPCPlugin;
import net.lordimass.dialogue.action.builder.BuilderActionBeginDialogue;
import net.lordimass.dialogue.codec.CharacterAsset;
import net.lordimass.dialogue.codec.DialogueAsset;
import net.lordimass.dialogue.component.NPCDialogueComponent;
import net.lordimass.dialogue.eventTags.RunCommand;
import net.lordimass.dialogue.ui.DialoguePageManager;
import net.lordimass.dialogue.player.DialoguePlayer;
import net.lordimass.dialogue.player.DialoguePlayerConfig;
import net.lordimass.dialogue.player.commands.DialogueCommand;
import net.lordimass.dialogue.sensor.builder.BuilderSensorDialogue;
import net.lordimass.dialogue.system.DialogueAnimationSystem;
import net.lordimass.dialogue.system.DialogueTypingSystem;
import net.lordimass.dialogue.eventTags.VoiceHandler;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static net.lordimass.dialogue.parameter.ParameterRegister.registerEventTag;
import static net.lordimass.dialogue.parameter.ParameterRegister.registerReplacementParameter;

/**
 * Class to hold methods and functionality which must be run on plugin boot. Separated from
 * <code>DialogueMod</code> to allow this mod to be used as a Java library and packaged as part
 * of other mods.
 * <br><br>
 * Call <code>DialogueRuntime.setup</code> and <code>DialogueRuntime.start</code> from inside
 * your plugin's <code>setup</code> and <code>start</code> methods respectively to initialise the
 * mod if it's being used as a library.
 */
public class DialogueRuntime {
    private static final Map<PlayerRef, DialoguePlayer> dialoguePlayerMap = new ConcurrentHashMap<>();

    private DialogueRuntime() {}

    public static void setup(JavaPlugin host) {
        registerReplacementParameter("{username}", PlayerRef.class, PlayerRef::getUsername);
        registerReplacementParameter("{uuid}", PlayerRef.class, p -> p.getUuid().toString());
        registerReplacementParameter("{lang}", PlayerRef.class, PlayerRef::getLanguage);

        registerEventTag("sound", DialoguePageManager.class, VoiceHandler::playSoundEvent, new String[]{"is"});
        registerEventTag("command", PlayerRef.class, RunCommand::runCommandEvent, new String[]{"is"});

        DialogueMod.dialogueComponentType = host.getEntityStoreRegistry().registerComponent(NPCDialogueComponent.class, NPCDialogueComponent::new);

        host.getCommandRegistry().registerCommand(new DialogueCommand());

        NPCPlugin.get().registerCoreComponentType("Dialogue", BuilderSensorDialogue::new);
        NPCPlugin.get().registerCoreComponentType("BeginDialogue", BuilderActionBeginDialogue::new);

        registerAssetTypes(host);
        registerEvents(host);
        Creditor.setup(host);
    }

    public static void start(JavaPlugin host) {
        host.getEntityStoreRegistry().registerSystem(new DialogueTypingSystem());
        host.getEntityStoreRegistry().registerSystem(new DialogueAnimationSystem());
        Creditor.start(host);
    }

    private static void registerAssetTypes(JavaPlugin host) {
        host.getAssetRegistry().register(
            HytaleAssetStore.builder(CharacterAsset.class, new DefaultAssetMap<>())
                .setPath("Dialogue/Character")
                .setCodec(CharacterAsset.ASSET_BUILDER_CODEC)
                .setKeyFunction(CharacterAsset::getId)
                .loadsAfter(Interaction.class)
                .build()
        );

        host.getAssetRegistry().register(
                HytaleAssetStore.builder(DialogueAsset.class, new DefaultAssetMap<>())
                .setPath("Dialogue")
                .setCodec(DialogueAsset.ASSET_BUILDER_CODEC)
                .setKeyFunction(DialogueAsset::getId)
                .loadsAfter(CharacterAsset.class)
                .build()
        );
    }

    private static void registerEvents(JavaPlugin host) {
        host.getEventRegistry().register(
            PlayerConnectEvent.class,
            playerConnectEvent ->
                dialoguePlayerMap.putIfAbsent(
                    playerConnectEvent.getPlayerRef(),
                    new DialoguePlayer(playerConnectEvent.getPlayerRef())
                )
        );

        host.getEventRegistry().register(
            PlayerDisconnectEvent.class,
            playerDisconnectEvent -> {
                DialoguePlayer player = dialoguePlayerMap.get(playerDisconnectEvent.getPlayerRef());

                com.hypixel.hytale.server.core.util.Config<DialoguePlayerConfig> cfg = new com.hypixel.hytale.server.core.util.Config<>(
                    new File("config/dialogue/player_data/").toPath(),
                    playerDisconnectEvent.getPlayerRef().getUsername(),
                    DialoguePlayerConfig.CODEC
                );

                cfg.load()
                    .thenAccept((_cfg) -> _cfg.setUuid(
                        player.getConfig().get().playerUuid)
                    )
                    .thenAccept(
                        (_) -> cfg.save().thenAccept(
                            (_) -> dialoguePlayerMap.remove(
                                playerDisconnectEvent.getPlayerRef()
                            )
                        )
                    );
            }
        );
    }
}
