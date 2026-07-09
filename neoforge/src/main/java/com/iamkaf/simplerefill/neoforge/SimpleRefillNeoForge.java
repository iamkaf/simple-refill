package com.iamkaf.simplerefill.neoforge;

import com.iamkaf.simplerefill.SimpleRefill;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(SimpleRefill.MOD_ID)
public final class SimpleRefillNeoForge {
    public SimpleRefillNeoForge(IEventBus eventBus) {
        SimpleRefill.init();
    }
}
