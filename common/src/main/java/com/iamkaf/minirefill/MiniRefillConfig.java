package com.iamkaf.minirefill;

import com.iamkaf.konfig.api.v1.ConfigBuilder;
import com.iamkaf.konfig.api.v1.ConfigHandle;
import com.iamkaf.konfig.api.v1.ConfigScope;
import com.iamkaf.konfig.api.v1.ConfigValue;
import com.iamkaf.konfig.api.v1.Konfig;
import com.iamkaf.konfig.api.v1.SyncMode;

public final class MiniRefillConfig {
    public static final ConfigHandle HANDLE;
    public static final ConfigValue<Boolean> REFILL_BLOCKS;
    public static final ConfigValue<Boolean> REFILL_TOOLS;
    public static final ConfigValue<Boolean> REFILL_CONSUMABLES;
    public static final ConfigValue<Boolean> REFILL_GENERIC_USE_ITEMS;
    public static final ConfigValue<Boolean> SEARCH_HOTBAR_FIRST;

    static {
        ConfigBuilder builder = Konfig.builder(MiniRefill.MOD_ID, "common")
                .scope(ConfigScope.COMMON)
                .syncMode(SyncMode.LOGIN)
                .comment("Server-authoritative refill settings.");

        builder.push("refill");
        builder.categoryComment("Choose which emptied held-item categories can be refilled.");
        REFILL_BLOCKS = builder.bool("refill_blocks", true)
                .comment("Refill blocks after the held stack is placed.")
                .sync(true)
                .build();
        REFILL_TOOLS = builder.bool("refill_tools", true)
                .comment("Refill a broken held tool with a component-compatible copy.")
                .sync(true)
                .build();
        REFILL_CONSUMABLES = builder.bool("refill_consumables", true)
                .comment("Refill food and other items after a held-use action consumes the stack.")
                .sync(true)
                .build();
        REFILL_GENERIC_USE_ITEMS = builder.bool("refill_generic_use_items", false)
                .comment("Refill other right-click items. Disabled by default for broad mod compatibility.")
                .sync(true)
                .build();
        builder.pop();

        builder.push("inventory");
        builder.categoryComment("Control where replacement stacks are found.");
        SEARCH_HOTBAR_FIRST = builder.bool("search_hotbar_first", true)
                .comment("Search other hotbar slots before the main inventory.")
                .sync(true)
                .build();
        builder.pop();

        HANDLE = builder.build();
    }

    private MiniRefillConfig() {
    }

    public static void init() {
    }
}
