package io.github.copi143.gtnhzstd.mixin;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import io.github.copi143.gtnhzstd.Compressed;
import io.github.copi143.gtnhzstd.CompressionAlgorithm;

@Mixin(value = CompressedStreamTools.class, priority = Integer.MAX_VALUE)
public class CompressedStreamToolsMixin {

    /**
     * 从 InputStream 读取压缩的 NBTTagCompound
     *
     * @author copi143
     * @reason Add zstd read support while keeping gzip compatibility.
     */
    @Overwrite
    public static NBTTagCompound readCompressed(InputStream input) throws IOException {
        try (var dataInputStream = Compressed.wrapDataInput(input)) {
            return CompressedStreamTools.func_152456_a(dataInputStream, NBTSizeTracker.field_152451_a);
        }
    }

    /**
     * 将 NBTTagCompound 压缩后写到 OutputStream
     *
     * @author copi143
     * @reason Switch NBT compression to zstd by default.
     */
    @Overwrite
    public static void writeCompressed(NBTTagCompound tag, OutputStream output) throws IOException {
        try (DataOutputStream dataOutputStream = Compressed.wrapDataOutput(output, CompressionAlgorithm.GZIP)) {
            CompressedStreamTools.write(tag, dataOutputStream);
        }
    }

    /**
     * 从内存读取压缩的 NBTTagCompound
     *
     * @author copi143
     * @reason Support zstd payloads while retaining gzip compatibility.
     */
    @Overwrite
    public static NBTTagCompound func_152457_a(byte[] data, NBTSizeTracker tracker) throws IOException {
        try (DataInputStream dataInputStream = Compressed.wrapDataInput(data)) {
            return CompressedStreamTools.func_152456_a(dataInputStream, tracker);
        }
    }

    /**
     * 将 NBTTagCompound 压缩到内存中的字节数组
     *
     * @author copi143
     * @reason Use zstd for compressed NBT payloads.
     */
    @Overwrite
    public static byte[] compress(NBTTagCompound tag) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (DataOutputStream dataOutputStream = Compressed
            .wrapDataOutput(byteArrayOutputStream, CompressionAlgorithm.GZIP)) {
            CompressedStreamTools.write(tag, dataOutputStream);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * 安全写入 NBTTagCompound 到文件
     *
     * @author copi143
     * @reason Make it safer.
     */
    @Overwrite
    public static void safeWrite(NBTTagCompound tag, File file) throws IOException {
        File tmp = new File(file.getAbsolutePath() + ".tmp");
        if (!tmp.exists() && !tmp.delete()) throw new IOException("Failed to delete " + tmp);
        CompressedStreamTools.write(tag, tmp);
        Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }
}
