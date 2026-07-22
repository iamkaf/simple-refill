package com.iamkaf.minirefill;

import com.iamkaf.amber.api.core.v2.AmberInitializer;
import com.iamkaf.amber.api.event.v1.events.common.ServerTickEvents;

public final class MiniRefill {
    public static final String MOD_ID = "minirefill";

    private MiniRefill() {
    }

    public static void init() {
        AmberInitializer.initialize(MOD_ID);
        MiniRefillConfig.init();
        ServerTickEvents.END_SERVER_TICK.register(RefillQueue::drain);
    }
}
