package com.shadowvale.dadmod;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

// dist = DEDICATED_SERVER: this class (and therefore the whole mod, since it's
// the only mod class) never loads on any client, so vanilla clients can connect freely.
@Mod(value = DadMod.MODID, dist = Dist.DEDICATED_SERVER)
public class DadMod {
    public static final String MODID = "dadmod";
    private static final Logger LOGGER = LogUtils.getLogger();

    private record PendingReply(int fireAtTick, String message) {
    }

    private final Queue<PendingReply> pendingReplies = new ArrayDeque<>();

    public DadMod(ModContainer modContainer) {
        LOGGER.info("Hi Console! I'm dadmod!");
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerChat(ServerChatEvent event) {
        UUID playerId = event.getPlayer().getUUID();
        if (isIgnored(playerId)) {
            return;
        }

        String name = findDetectedName(event.getRawText());
        if (name == null) {
            return;
        }

        String reply = Config.REPLY_FORMAT.get().replace("{name}", name);
        MinecraftServer server = event.getPlayer().level().getServer();
        int fireAtTick = server.getTickCount() + Config.REPLY_DELAY_TICKS.get();
        pendingReplies.add(new PendingReply(fireAtTick, reply));
    }

    // Chat messages go through async signing/decoration before they're actually broadcast, so a
    // reply sent inline from onServerChat can reach clients before the triggering message does.
    // Queueing it a few ticks out avoids that race.
    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        if (pendingReplies.isEmpty()) {
            return;
        }

        MinecraftServer server = event.getServer();
        int currentTick = server.getTickCount();
        while (!pendingReplies.isEmpty() && pendingReplies.peek().fireAtTick() <= currentTick) {
            PendingReply pending = pendingReplies.poll();
            server.getPlayerList().broadcastSystemMessage(Component.literal(pending.message()), false);
        }
    }

    private static boolean isIgnored(UUID playerId) {
        for (String ignored : Config.IGNORED_PLAYER_UUIDS.get()) {
            try {
                if (UUID.fromString(ignored).equals(playerId)) {
                    return true;
                }
            } catch (IllegalArgumentException e) {
                // malformed entry in a hand-edited config; skip it
            }
        }
        return false;
    }

    private static String findDetectedName(String rawText) {
        for (String patternString : Config.TRIGGER_PATTERNS.get()) {
            Pattern pattern;
            try {
                pattern = Pattern.compile(patternString, Pattern.CASE_INSENSITIVE);
            } catch (PatternSyntaxException e) {
                continue;
            }

            Matcher matcher = pattern.matcher(rawText);
            if (!matcher.find()) {
                continue;
            }

            String captured;
            try {
                captured = matcher.group(1);
            } catch (IndexOutOfBoundsException e) {
                continue;
            }

            String name = captured.replaceAll("^[^A-Za-z0-9_]+|[^A-Za-z0-9_]+$", "");
            if (!name.isEmpty()) {
                return name;
            }
        }
        return null;
    }
}
