package com.iamkaf.minirefill.fabric;

import com.iamkaf.minirefill.MiniRefill;
import net.fabricmc.api.ModInitializer;

public final class MiniRefillFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        MiniRefill.init();
    }
}
