package io.github.copi143.gtnhzstd.access;

import java.io.IOException;

public interface IRegionFileMixin {

    void gtnhzstd$writeChunk(int chunkX, int chunkZ, byte[] data, int length) throws IOException;
}
