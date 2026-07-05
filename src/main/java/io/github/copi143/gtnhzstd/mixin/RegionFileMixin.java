package io.github.copi143.gtnhzstd.mixin;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

import net.minecraft.world.chunk.storage.RegionFile;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import io.github.copi143.gtnhzstd.Compressed;
import io.github.copi143.gtnhzstd.CompressionAlgorithm;
import io.github.copi143.gtnhzstd.ZstdChunkBuffer;
import io.github.copi143.gtnhzstd.access.IRegionFileMixin;

@Mixin(value = RegionFile.class, priority = 900)
public class RegionFileMixin implements IRegionFileMixin {

    @Shadow
    private RandomAccessFile dataFile;

    @Shadow
    private ArrayList<Boolean> sectorFree;

    @Shadow
    private boolean outOfBounds(int x, int z) {
        throw new AssertionError("Shadowed method");
    }

    @Shadow
    private int getOffset(int x, int z) {
        throw new AssertionError("Shadowed method");
    }

    @Shadow
    @SuppressWarnings("RedundantThrows")
    protected void write(int x, int z, byte[] data, int length) throws IOException {
        throw new AssertionError("Shadowed method");
    }

    /**
     * @author copi143
     * @reason zstd
     */
    @Overwrite
    public synchronized DataInputStream getChunkDataInputStream(int x, int z) {
        if (this.outOfBounds(x, z)) {
            return null;
        }

        try {
            int offset = this.getOffset(x, z);
            if (offset == 0) {
                return null;
            }

            int sectorNumber = offset >> 8;
            int numSectors = offset & 255;
            if (sectorNumber + numSectors > this.sectorFree.size()) {
                return null;
            }

            this.dataFile.seek((long) sectorNumber * 4096L);
            int length = this.dataFile.readInt();
            if (length > 4096 * numSectors || length <= 0) {
                return null;
            }

            byte version = this.dataFile.readByte();
            if (version != 2 && version != 3) {
                return null;
            }

            byte[] data = new byte[length - 1];
            this.dataFile.read(data);

            return new DataInputStream(Compressed.wrapInput(data));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @author copi143
     * @reason 统一使用 zstd 压缩新写入的区块数据，避免 gzip/zlib 的性能问题，同时仍保持对原有压缩格式的兼容。
     */
    @Overwrite
    public DataOutputStream getChunkDataOutputStream(int x, int z) throws IOException {
        if (this.outOfBounds(x, z)) return null;
        return Compressed.wrapDataOutput(new ZstdChunkBuffer(this, x, z), CompressionAlgorithm.DEFLATE);
    }

    @Inject(method = "chunkExists", at = @At("RETURN"), cancellable = true, remap = false)
    private void gtnhzstd$chunkExistsZstd(int x, int z, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            return;
        }

        if (this.outOfBounds(x, z)) {
            return;
        }

        try {
            int offset = this.getOffset(x, z);
            if (offset == 0) {
                return;
            }

            int sectorNumber = offset >> 8;
            int numSectors = offset & 255;
            if (sectorNumber + numSectors > this.sectorFree.size()) {
                return;
            }

            this.dataFile.seek((long) (sectorNumber * 4096L));
            int length = this.dataFile.readInt();
            if (length > 4096 * numSectors || length <= 0) {
                return;
            }

            byte version = this.dataFile.readByte();
            if (version == 3) {
                cir.setReturnValue(true);
            }
        } catch (IOException e) {
            // Ignore and keep vanilla result.
        }
    }

    @Override
    public void gtnhzstd$writeChunk(int chunkX, int chunkZ, byte[] data, int length) throws IOException {
        this.write(chunkX, chunkZ, data, length);
    }
}
