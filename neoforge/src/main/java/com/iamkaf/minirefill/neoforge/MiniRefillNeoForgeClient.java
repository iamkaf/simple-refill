package com.iamkaf.minirefill.neoforge;

import com.iamkaf.konfig.neoforge.api.v1.KonfigNeoForgeClientScreens;
import com.iamkaf.minirefill.MiniRefill;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = MiniRefill.MOD_ID, dist = Dist.CLIENT)
public final class MiniRefillNeoForgeClient {
    public MiniRefillNeoForgeClient(ModContainer container) {
        KonfigNeoForgeClientScreens.register(container, MiniRefill.MOD_ID);
    }
}
