package io.github.copi143.gtnhzstd.mixin;

import java.io.ByteArrayInputStream;
import java.util.UUID;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;

import com.cubefury.vendingmachine.VendingMachine;
import com.cubefury.vendingmachine.network.PacketAssembly;

@Pseudo
@Mixin(value = PacketAssembly.class, remap = false)
public abstract class VendingMachinePacketAssemblyMixin {

    @Shadow
    public byte[] getBuffer(UUID owner) {
        throw new AssertionError("Shadowed method");
    }

    @Shadow
    public void setBuffer(UUID owner, byte[] value) {
        throw new AssertionError("Shadowed method");
    }

    @Shadow
    public void clearBuffer(UUID owner) {
        throw new AssertionError("Shadowed method");
    }

    /**
     *
     * @author copi143
     * @reason zstd
     */
    @Overwrite
    public NBTTagCompound assemblePacket(UUID owner, NBTTagCompound tags) {
        int size = tags.getInteger("size");
        int index = tags.getInteger("index");
        boolean end = tags.getBoolean("end");
        byte[] data = tags.getByteArray("data");

        byte[] tmp = getBuffer(owner);

        if (tmp == null) {
            tmp = new byte[size];
            setBuffer(owner, tmp);
        } else if (tmp.length != size) {
            VendingMachine.LOG.error("Unexpected change in BQ packet byte length: {} > {}", size, tmp.length);
            clearBuffer(owner);
            return null;
        }

        System.arraycopy(data, 0, tmp, index, data.length);
        /*
         * for(int i = 0; i < data.length && index + i < size; i++)
         * {
         * tmp[index + i] = data[i];
         * }
         */

        if (end) {
            clearBuffer(owner);

            try {
                return CompressedStreamTools.readCompressed(new ByteArrayInputStream(tmp));
            } catch (Exception e) {
                throw new RuntimeException("Unable to assemble BQ packet", e);
            }
        }

        return null;
    }
}
