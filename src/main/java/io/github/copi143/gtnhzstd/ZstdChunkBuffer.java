package io.github.copi143.gtnhzstd;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.github.copi143.gtnhzstd.access.IRegionFileMixin;

public class ZstdChunkBuffer extends ByteArrayOutputStream {

    private final IRegionFileMixin regionFile;
    private final int chunkX;
    private final int chunkZ;

    public ZstdChunkBuffer(IRegionFileMixin regionFile, int chunkX, int chunkZ) {
        super(8192);
        this.regionFile = regionFile;
        this.chunkX = chunkX;
        this.chunkZ = chunkZ;
    }

    @Override
    public void close() throws IOException {
        regionFile.gtnhzstd$writeChunk(chunkX, chunkZ, this.buf, this.count);
    }
}
