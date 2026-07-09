package com.iamkaf.simplerefill.forge;

import com.iamkaf.konfig.forge.api.v1.KonfigForgeClientScreens;
import com.iamkaf.simplerefill.SimpleRefill;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod(SimpleRefill.MOD_ID)
public final class SimpleRefillForge {
    public SimpleRefillForge() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            KonfigForgeClientScreens.register(SimpleRefill.MOD_ID);
        }
        SimpleRefill.init();
    }
}
