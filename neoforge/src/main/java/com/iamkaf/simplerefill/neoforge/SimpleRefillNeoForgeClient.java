package com.iamkaf.simplerefill.neoforge;

import com.iamkaf.konfig.neoforge.api.v1.KonfigNeoForgeClientScreens;
import com.iamkaf.simplerefill.SimpleRefill;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = SimpleRefill.MOD_ID, dist = Dist.CLIENT)
public final class SimpleRefillNeoForgeClient {
    public SimpleRefillNeoForgeClient(ModContainer container) {
        KonfigNeoForgeClientScreens.register(container, SimpleRefill.MOD_ID);
    }
}
