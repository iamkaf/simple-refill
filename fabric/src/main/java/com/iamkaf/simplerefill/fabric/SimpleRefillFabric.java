package com.iamkaf.simplerefill.fabric;

import com.iamkaf.simplerefill.SimpleRefill;
import net.fabricmc.api.ModInitializer;

public final class SimpleRefillFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        SimpleRefill.init();
    }
}
