package edu.boudoux;

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

    public static String hash(byte[] input) {
        if (input == null) return null;

        int[] paddedContent = applyPadding(input);
        int[][] blocks = parseMessage(paddedContent);

        int[] currentHash = H_0;
        int[] dividedBlock;

        for (int[] block: blocks) {
            dividedBlock = divideBlock(block);
            currentHash = processBlock(dividedBlock, currentHash);
        }

        return toString(currentHash);
    }

    static int[][] parseMessage(int[] paddedContent) {
        int[][] block = new int[paddedContent.length / 64][64];

        for (int i = 0; i < block.length; i++) {
            System.arraycopy(paddedContent, i * 64, block[i], 0, 64);
        }

        return block;
    }

    static int[] processBlock(int[] dividedBlock, int[] previousHash) {
        int[] word = new int[80];

        int a = previousHash[0];
        int b = previousHash[1];
        int c = previousHash[2];
        int d = previousHash[3];
        int e = previousHash[4];

        int T;

        for (int i = 0; i < 4; i++) {
            for (int j = i * 20; j < 20 + i * 20; j++) {
                word[j] = messageSchedule(dividedBlock, j, word);

                T = rotl(a, 5) + applyF(b, c, d, i) + e + K[i] + word[j];
                e = d;
                d = c;
                c = rotl(b, 30);
                b = a;
                a = T;
            }
        }

        a = a + previousHash[0];
        b = b + previousHash[1];
        c = c + previousHash[2];
        d = d + previousHash[3];
        e = e + previousHash[4];

        return new int[] {a, b, c, d, e};
    }

    static int messageSchedule(int[] dividedBlock, int j, int[] word) {
        if (j < 16)
            return dividedBlock[j];

        int result = word[j - 3] ^ word[j - 8] ^ word[j - 14] ^ word[j - 16];

        return rotl(result, 1);
    }

    static int[] divideBlock(int[] block) {
        int[] result = new int[16];

        for (int mainIndex = 0, blockIndex = 0; mainIndex < 16; mainIndex++, blockIndex += 4) {
            long value = block[blockIndex];
            value <<= 8;
            value += block[blockIndex + 1];
            value <<= 8;
            value += block[blockIndex + 2];
            value <<= 8;
            value += block[blockIndex + 3];

            result[mainIndex] = (int) value;
        }

        return result;
    }

    static int[] getPaddingBytes(long contentLength) {
        long kBits = mod(448L - (contentLength * 8L + 1L), 512L);
        long paddingSizeInBits = 1 + kBits + 64;
        int arrSize = (int) (paddingSizeInBits / 8 + (paddingSizeInBits % 8 == 0 ? 0 : 1));
        int[] padding = new int[arrSize];

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

    static int rotl(int value, int n) {
        return value << n | value >>> (32 - n);
    }

    static int applyF(int b, int c, int d, int stageIndex) {
        if (stageIndex == 0)
            return (b & c) ^ (~b & d);
        else if (stageIndex == 1)
            return b ^ c ^ d;
        else if (stageIndex == 2)
            return (b & c) ^ (b & d) ^ (c & d);

        return b ^ c ^ d;
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

    private static long mod(long value, long modValue) {
        if (value >= 0) return value % modValue;

        return -(-value % modValue) + modValue;
    }
}
