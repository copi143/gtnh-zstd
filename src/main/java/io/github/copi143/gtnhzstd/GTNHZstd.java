package io.github.copi143.gtnhzstd;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = GTNHZstd.MODID, version = Tags.VERSION, name = "gtnh-zstd", acceptedMinecraftVersions = "[1.7.10]")
public class GTNHZstd {

    public static final String MODID = "gtnh-zstd";

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        Config.synchronizeConfiguration(event.getSuggestedConfigurationFile());
    }
}
