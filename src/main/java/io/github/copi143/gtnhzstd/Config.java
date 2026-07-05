package io.github.copi143.gtnhzstd;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {

    public static String compressionMode = "zstd";
    public static int zstdCompressionLevel = 3;

    public static void synchronizeConfiguration(File configFile) {
        Configuration configuration = new Configuration(configFile);

        compressionMode = configuration.getString(
            "compressionMode",
            Configuration.CATEGORY_GENERAL,
            compressionMode,
            "Compression mode for data writes: zstd, gzip, deflate, default.");
        zstdCompressionLevel = configuration.getInt(
            "zstdCompressionLevel",
            Configuration.CATEGORY_GENERAL,
            zstdCompressionLevel,
            -7,
            22,
            "Compression level for Zstandard (if enabled).");

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }
}
