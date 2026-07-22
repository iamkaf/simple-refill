package com.iamkaf.minirefill.neoforge;

import com.iamkaf.minirefill.MiniRefill;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(MiniRefill.MOD_ID)
public final class MiniRefillNeoForge {
    public MiniRefillNeoForge(IEventBus eventBus) {
        MiniRefill.init();
    }
}
