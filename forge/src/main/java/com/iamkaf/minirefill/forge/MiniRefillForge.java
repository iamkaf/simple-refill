package com.iamkaf.minirefill.forge;

import com.iamkaf.konfig.forge.api.v1.KonfigForgeClientScreens;
import com.iamkaf.minirefill.MiniRefill;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(MiniRefill.MOD_ID)
public final class MiniRefillForge {
    public MiniRefillForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            KonfigForgeClientScreens.register(MiniRefill.MOD_ID);
        }
        MiniRefill.init();
    }
}
