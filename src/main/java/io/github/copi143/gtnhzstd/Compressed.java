package io.github.copi143.gtnhzstd;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.InflaterInputStream;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;

import com.github.luben.zstd.ZstdInputStream;
import com.github.luben.zstd.ZstdOutputStream;

public final class Compressed {

    private Compressed() {}

    public static InputStream wrapInput(InputStream input) throws IOException {
        InputStream buffered = makeBufferIfNeeded(input);
        if (!buffered.markSupported()) {
            buffered = new BufferedInputStream(buffered);
        }

        buffered.mark(4);

        byte[] header = new byte[4];
        int read = buffered.read(header);
        buffered.reset();

        if (isZstd(header, read)) {
            return new ZstdInputStream(buffered);
        }
        if (isGzip(header, read)) {
            return new GZIPInputStream(buffered);
        }
        if (isDeflate(header, read)) {
            return new InflaterInputStream(buffered);
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < header.length; i++) {
            sb.append(String.format("%02X", header[i]));
            if (i < header.length - 1) sb.append(" ");
        }

        throw new IOException("Unknown compression format, the file header is: " + sb);
    }

    public static InputStream wrapInput(byte[] input) throws IOException {
        return wrapInput(new ByteArrayInputStream(input));
    }

    public static DataInputStream wrapDataInput(InputStream input) throws IOException {
        return new DataInputStream(wrapInput(input));
    }

    public static DataInputStream wrapDataInput(byte[] input) throws IOException {
        return new DataInputStream(wrapInput(input));
    }

    public static OutputStream wrapOutput(OutputStream output, CompressionAlgorithm defaultAlgorithm)
        throws IOException {
        return switch (Config.compressionMode.toLowerCase()) {
            case "zstd" -> new ZstdOutputStream(output).setLevel(Config.zstdCompressionLevel);
            case "gzip" -> new GZIPOutputStream(output);
            case "deflate" -> new DeflaterOutputStream(output);
            default -> switch (defaultAlgorithm) {
                    case ZSTD -> new ZstdOutputStream(output).setLevel(Config.zstdCompressionLevel);
                    case GZIP -> new GZIPOutputStream(output);
                    case DEFLATE -> new DeflaterOutputStream(output);
                };
        };
    }

    public static DataOutputStream wrapDataOutput(OutputStream output, CompressionAlgorithm defaultAlgorithm)
        throws IOException {
        return new DataOutputStream(wrapOutput(output, defaultAlgorithm));
    }

    public static boolean isZstd(byte[] header, int read) {
        if (header.length < 4 || read < 4) return false;
        return (header[0] == (byte) 0x28) && (header[1] == (byte) 0xB5)
            && (header[2] == (byte) 0x2F)
            && (header[3] == (byte) 0xFD);
    }

    public static boolean isGzip(byte[] header, int read) {
        if (header.length < 3 || read < 3) return false;
        return (header[0] == (byte) 0x1F) && (header[1] == (byte) 0x8B) && (header[2] == (byte) 0x08);
    }

    public static boolean isDeflate(byte[] header, int read) {
        if (header.length < 2 || read < 2) return false;
        int b0 = header[0] & 0xFF;
        int b1 = header[1] & 0xFF;
        if ((b0 & 0x0F) != 0x08) return false;
        if ((b0 & 0xF0) > 0x70) return false;
        int check = ((header[0] << 8) + b1) & 0xFFFF;
        return check % 31 == 0;
    }

    private static InputStream makeBufferIfNeeded(InputStream input) {
        if (input instanceof BufferedInputStream) return input;
        if (input instanceof ByteArrayInputStream) return input;
        if (input instanceof CipherInputStream) return input;
        if (input instanceof DigestInputStream) return input;
        if (input instanceof PushbackInputStream) return input;
        return new BufferedInputStream(input);
    }

    private static OutputStream makeBufferIfNeeded(OutputStream output) {
        if (output instanceof BufferedOutputStream) return output;
        if (output instanceof ByteArrayOutputStream) return output;
        if (output instanceof CipherOutputStream) return output;
        if (output instanceof DigestOutputStream) return output;
        return new BufferedOutputStream(output);
    }
}
