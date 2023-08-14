package edu.boudoux;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class SHA1 {

    /**
     * The initial 160bits hash
     */
    private static final int[] H_0 = new int[] {
            0x67452301,
            0xefcdab89,
            0x98badcfe,
            0x10325476,
            0xc3d2e1f0
    };

    /**
     * The constants applied in each of the four stages.
     * Each stage is represented by an index in this array.
     */
    private static final int[] K = new int[] {
            0x5a827999,
            0x6ed9eba1,
            0x8f1bbcdc,
            0xca62c1d6
    };

    private static final long _2power32 = (long) Math.pow(2d, 32d);

    private SHA1() {}

    public static String hash(String input) {
        return hash(input.getBytes());
    }

    public static String hash(byte[] input) {
        if (input == null) return null;

        int[] paddedContent = applyPadding(input);
        int[][] blocks = createBlocks(paddedContent);

        int[] currentHash = _hashBlocks(blocks, H_0);

        return toString(currentHash);
    }

    private static int[] _hashBlocks(int[][] blocks, int[] initialHash) {
        if (blocks == null) return null;

        int[] currentHash = initialHash;
        int[] dividedBlock;

        for (int[] block: blocks) {
            dividedBlock = divideBlock(block);
            currentHash = processBlock(dividedBlock, currentHash);
        }

        return currentHash;
    }

    public static String hash(File file) throws IOException {
        if (file == null || ! file.exists()) return null;

        int[] padding = getPaddingBytes(file.length());
        int[] currentHash = H_0;
        int[][] blocks = new int[1][];
        try (Scanner fileScanner = new Scanner(file)) {
            int[] paddingBlock = null;
            int[] block;

            while ((block = readNextBlock(fileScanner)) != null || paddingBlock != null) {
                // if last block
                if (block != null && block.length < 64) {
                    // this might produce one additional block
                    int[][] joiningBlock = joinPadding(block, padding);

                    block = joiningBlock[0];
                    paddingBlock = joiningBlock[1];

                } else if (paddingBlock != null) {
                    block = paddingBlock;
                    paddingBlock = null;
                }

                blocks[0] = block;
                currentHash = _hashBlocks(blocks, currentHash);
            }
        }

        return toString(currentHash);
    }

    static String toString(int[] currentHash) {
        return hex32Pad(Integer.toHexString(currentHash[0])) +
                hex32Pad(Integer.toHexString(currentHash[1])) +
                hex32Pad(Integer.toHexString(currentHash[2])) +
                hex32Pad(Integer.toHexString(currentHash[3])) +
                hex32Pad(Integer.toHexString(currentHash[4]));
    }

    static String hex32Pad(String value) {
        return String.format("%8s", value).replace(" ", "0");
    }

    static int[][] createBlocks(int[] paddedContent) {
        int[][] block = new int[paddedContent.length / 64][64];

        for (int i = 0; i < block.length; i++) {
            System.arraycopy(paddedContent, i * 64, block[i], 0, 64);
        }

        return block;
    }

    /**
     *
     */
    static int[] processBlock(int[] dividedBlock, int[] previousHash) {
        int[] word = new int[80];

        int a = previousHash[0];
        int b = previousHash[1];
        int c = previousHash[2];
        int d = previousHash[3];
        int e = previousHash[4];

        int _a = 0, _b = 0, _c = 0, _d = 0, _e = 0;

        for (int i = 0; i < 4; i++) {
            for (int j = i * 20; j < 20 + i * 20; j++) {
                word[j] = messageSchedule(dividedBlock, j, word);

                _a = e + applyF(b, c, d, i) + (a << 5 | a >>> 27) + word[j] + K[i];
                _b = a;
                _c = b << 30 | b >>> 2;
                _d = c;
                _e = d;

                a = _a;
                b = _b;
                c = _c;
                d = _d;
                e = _e;
            }
        }

        _a = (int) mod(((long) _a) + previousHash[0], _2power32);
        _b = (int) mod(((long) _b) + previousHash[1], _2power32);
        _c = (int) mod(((long) _c) + previousHash[2], _2power32);
        _d = (int) mod(((long) _d) + previousHash[3], _2power32);
        _e = (int) mod(((long) _e) + previousHash[4], _2power32);

        return new int[] {_a, _b, _c, _d, _e};
    }

    static int applyF(int b, int c, int d, int stageIndex) {
        if (stageIndex == 0)
            return (b & c) | (~b & d);
        else if (stageIndex == 1 || stageIndex == 3)
            return b ^ c ^ d;
        else if (stageIndex == 2)
            return (b & c) | (b & d) | (c & d);

        throw new IllegalArgumentException("Invalid value for stateIndex");
    }

    static int messageSchedule(int[] dividedBlock, int j, int[] word) {
        if (j < 16)
            return dividedBlock[j];

        int result = word[j - 16] ^ word[j - 14] ^ word[j - 8] ^ word[j - 3];

        return result << 1 | result >>> 31;
    }

    static int[] divideBlock(int[] block) {
        int[] result = new int[16];

        for (int mainIndex = 0, blockIndex = 0; mainIndex < 16; mainIndex++, blockIndex += 4) {
            result[mainIndex] = block[blockIndex];
            result[mainIndex] <<= 8;
            result[mainIndex] |= block[blockIndex + 1];
            result[mainIndex] <<= 8;
            result[mainIndex] |= block[blockIndex + 2];
            result[mainIndex] <<= 8;
            result[mainIndex] |= block[blockIndex + 3];
        }

        return result;
    }

    static int[][] joinPadding(int[] block, int[] padding) {
        int arrSize = 1;

        if (block.length + padding.length > 64) {
            arrSize = 2;
        }

        int[][] result = new int[arrSize][64];

        System.arraycopy(block, 0, result[0], 0, block.length);
        System.arraycopy(padding, 0, result[0], block.length, 64 - block.length);

        if (arrSize > 1) {
            System.arraycopy(padding, 64 - block.length, result[1], 0, 64);
        }

        return result;
    }

    /**
     * Reads a block from the file.
     *
     * @param fileScanner
     * @return an array of at maximum 64 positions (512 bits).
     */
    static int[] readNextBlock(Scanner fileScanner) {
        if (! fileScanner.hasNext()) return null;

        int[] block = new int[64];
        int index = 0;
        do {
            block[index++] = fileScanner.nextByte();
        } while (index != 64 && fileScanner.hasNext());

        return index == 64 ? block : Arrays.copyOfRange(block, 0, index);
    }

    private static long mod(long value, long modValue) {
        if (value >= 0) return value % modValue;

        return -(-value % modValue) + modValue;
    }

    static int[] getPaddingBytes(long contentLength) {
        long kBits = mod(448L - (contentLength * 8L + 1L), 512L);
        long paddingSizeInBits = 1 + kBits + 64;
        int arrSize = (int) (paddingSizeInBits / 8 + (paddingSizeInBits % 8 == 0 ? 0 : 1));
        int[] padding = new int[arrSize];

        assert ((contentLength + padding.length) * 8L) % 512L == 0;

        // padding = bit 1 + 0 kBits + contentLength total bits in 64bits representation

        // fill the initial positions: bit 1 + kBits bit 0 (MSB)
        padding[0] = 0x80;

        // fill the 64 (LSB) - contentLength total bits in 64bits representation
        int offset = arrSize - 8;
        long bitsInContentLength = contentLength * 8;
        double calc, sum = 0;
        for (int i = 1, arrIndex = offset, power = 63; i <= 8; i++, arrIndex++) {
            for (int j = 1; j <= 8; j++) {
                padding[arrIndex] = padding[arrIndex] << 1;

                calc = Math.pow(2, power--);
                if (sum + calc <= bitsInContentLength) {
                    sum += calc;
                    padding[arrIndex] = (padding[arrIndex] | 0x01);
                }
            }
        }

        return padding;
    }

    static int[] applyPadding(byte[] value) {
        int contentLength = value.length;
        int[] padding = getPaddingBytes(contentLength);

        int[] result = new int[contentLength + padding.length];
        for (int i = 0; i < contentLength; i++)
            result[i] = value[i];

        System.arraycopy(padding, 0, result, contentLength, padding.length);

        return result;
    }
}