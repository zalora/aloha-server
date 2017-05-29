package com.zalora.aloha.compressor;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.zalora.aloha.memcached.MemcachedItem;
import lombok.extern.slf4j.Slf4j;
import net.jpountz.lz4.*;

/**
 * (De)-Compression done as in zcast's CompressionInterceptor
 * This class only works with php-memcached because it relies on the data types encoded in the flags
 *
 * @author Wolfram Huesken <wolfram.huesken@zalora.com>
 * @link https://github.com/zalora/zcast
 */
@Slf4j
public class Lz4 implements Compressor {

    private static final long PHP_FLAG_STRING = 0;
    private static final long PHP_FLAG_LONG = 1;
    private static final long PHP_FLAG_DOUBLE = 2;
    private static final long PHP_FLAG_PHP_SERIALIZED = 4;
    private static final long PHP_FLAG_JSON_SERIALIZED = 6;

    private static final long ZCAST_FLAG_STRING = 1;
    private static final long ZCAST_FLAG_PHP_SERIALIZED = 2;
    private static final long ZCAST_FLAG_JSON_SERIALIZED = 4;

    private static final ImmutableList<Long> PHP_FLAGS = ImmutableList.of(
        PHP_FLAG_STRING, PHP_FLAG_LONG, PHP_FLAG_DOUBLE, PHP_FLAG_PHP_SERIALIZED, PHP_FLAG_JSON_SERIALIZED
    );

    private static final ImmutableMap<Long, Long> ZCAST_PHP_FLAGS_MAP = ImmutableMap.of(
        ZCAST_FLAG_STRING, PHP_FLAG_STRING,
        ZCAST_FLAG_PHP_SERIALIZED, PHP_FLAG_PHP_SERIALIZED,
        ZCAST_FLAG_JSON_SERIALIZED, PHP_FLAG_JSON_SERIALIZED
    );

    private static final ImmutableMap<Long, Long> PHP_ZCAST_FLAGS_MAP = ImmutableMap.of(
        PHP_FLAG_STRING, ZCAST_FLAG_STRING,
        PHP_FLAG_PHP_SERIALIZED, ZCAST_FLAG_PHP_SERIALIZED,
        PHP_FLAG_JSON_SERIALIZED, ZCAST_FLAG_JSON_SERIALIZED
    );

    private static final int COMPRESSION_THRESHOLD = 2048;

    private static final LZ4Factory lz4Factory;
    private static final LZ4Compressor compressor;
    private static final LZ4FastDecompressor fastDecompressor;

    static {
        lz4Factory = LZ4Factory.fastestInstance();
        compressor = lz4Factory.fastCompressor();
        fastDecompressor = lz4Factory.fastDecompressor();
    }

    @Override
    public void afterGet(MemcachedItem item) {
        if (item == null) {
            return;
        }

        // Item is not compressed
        final long flag = item.getFlags();
        if (PHP_FLAGS.contains(flag)) {
            return;
        }

        final byte[] compressed = item.getData();
        final int decompressedLength = getOriginalEntrySize(flag);
        byte[] uncompressed = new byte[decompressedLength];

        try {
            fastDecompressor.decompress(compressed, uncompressed, decompressedLength);
        } catch (LZ4Exception lex) {
            log.error("Decompression failed", lex);
            return;
        }

        // Return uncompressed item to memcached client
        long originalFlag = getOriginalFlag(flag);

        item.setData(uncompressed);
        item.setFlags(originalFlag);
    }

    @Override
    public void beforePut(MemcachedItem item) {
        if (item == null) {
            return;
        }

        final long flag = item.getFlags();

        // If flags are not PHP values, then it's probably already compressed, so we don't touch it
        if (!PHP_FLAGS.contains(flag)) {
            return;
        }

        final byte[] data = item.getData();
        final int decompressedLength = data.length;

        // If data length is below the threshold, we leave it uncompressed
        if (decompressedLength < COMPRESSION_THRESHOLD) {
            return;
        }

        // Do the compression magic
        int maxCompressedLength = compressor.maxCompressedLength(decompressedLength);
        byte[] compressed = new byte[maxCompressedLength];
        int compressedLength = compressor.compress(
            data, 0, decompressedLength, compressed, 0, maxCompressedLength
        );

        byte[] trimmedCompressed = new byte[compressedLength];
        System.arraycopy(compressed, 0, trimmedCompressed, 0, compressedLength);

        // Put compressed item in hz
        item.setFlags(getNewFlag(flag, decompressedLength));
        item.setData(trimmedCompressed);
    }

    /**
     * As the last 3 bits are reserved for the data type, we can kick them out by shifting 3 bits to the right
     *
     * @param flag encoded flag
     * @return the uncompressed size in bytes
     */
    private int getOriginalEntrySize(long flag) {
        return (int) (flag >> 3);
    }

    /**
     * Magic number 7 leaves the last 3 bits intact
     *
     * @param flag encoded flag
     * @return return the data type set by PHP
     */
    private long getOriginalFlag(long flag) {
        flag &= 7;

        if (!ZCAST_PHP_FLAGS_MAP.containsKey(flag)) {
            throw new RuntimeException(String.format("ZCast Flag doesn't exist: %d", flag));
        }

        return ZCAST_PHP_FLAGS_MAP.get(flag);
    }

    /**
     * Encode uncompressed size and data type with a bitmask (3 bit for 3 datatypes)
     *
     * @param flag   The current flag set by php-memcached
     * @param length The uncompressed length we want to preserve
     * @return the encoded result
     */
    private long getNewFlag(long flag, int length) {
        if (!PHP_ZCAST_FLAGS_MAP.containsKey(flag)) {
            throw new RuntimeException(String.format("PHP Flag doesn't exist: %d", flag));
        }

        long newFlag = length << 3;
        newFlag |= PHP_ZCAST_FLAGS_MAP.get(flag);

        return newFlag;
    }

}
