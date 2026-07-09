package com.iamkaf.simplerefill;

import com.iamkaf.amber.api.core.v2.AmberInitializer;
import com.iamkaf.amber.api.event.v1.events.common.ServerTickEvents;

public final class SimpleRefill {
    public static final String MOD_ID = "simplerefill";

    private SimpleRefill() {
    }

    public static void init() {
        AmberInitializer.initialize(MOD_ID);
        SimpleRefillConfig.init();
        ServerTickEvents.END_SERVER_TICK.register(RefillQueue::drain);
    }
}
