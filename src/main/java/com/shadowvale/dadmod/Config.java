package com.shadowvale.dadmod;

import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    private static final String DEFAULT_PATTERN = "\\bi(?:['’]m|m|\\s+am)\\b\\s+(\\S+)";

    public static final ModConfigSpec.ConfigValue<String> REPLY_FORMAT = BUILDER
            .comment("Reply broadcast to chat when a trigger phrase is detected. {name} is replaced with the detected name.")
            .define("replyFormat", "[Dad] Hi {name}, I'm dad!");

    public static final ModConfigSpec.IntValue REPLY_DELAY_TICKS = BUILDER
            .comment(
                    "Delay (in ticks, 20 = 1 second) before sending the reply.",
                    "Without this, the reply can reach clients before the triggering chat message does."
            )
            .defineInRange("replyDelayTicks", 10, 0, 200);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> TRIGGER_PATTERNS = BUILDER
            .comment(
                    "Regexes checked against each chat message, matched case-insensitively.",
                    "Each pattern must contain exactly one capture group: the detected name.",
                    "Default matches \"IM x\", \"I'm x\" and \"I am x\"."
            )
            .defineListAllowEmpty("triggerPatterns", List.of(DEFAULT_PATTERN), () -> DEFAULT_PATTERN, Config::isValidPattern);

    public static final ModConfigSpec.ConfigValue<List<? extends String>> IGNORED_PLAYER_UUIDS = BUILDER
            .comment("Player UUIDs (dashed form) that never get a reply, even if they say a trigger phrase.")
            .defineListAllowEmpty("ignoredPlayerUuids", List.of(), () -> "00000000-0000-0000-0000-000000000000", Config::isValidUuid);

    public static final ModConfigSpec SPEC = BUILDER.build();

    private static boolean isValidPattern(Object obj) {
        if (!(obj instanceof String str)) {
            return false;
        }
        try {
            Pattern.compile(str);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    private static boolean isValidUuid(Object obj) {
        if (!(obj instanceof String str)) {
            return false;
        }
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
