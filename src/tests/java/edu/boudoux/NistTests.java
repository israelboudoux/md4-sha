package edu.boudoux;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class NistTests {

    private final TestCase testCase;

    public NistTests (TestCase testCase) {
        this.testCase = testCase;
    }

    private static class TestCase {
        public byte[] value;
        public String result;
        public TestCase(byte[] value, String result) {
            this.value = value;
            this.result = result;
        }
    }

    @Parameterized.Parameters
    public static Collection<TestCase[]> testCases() throws Exception {
        List<TestCase[]> result = new ArrayList<>();

        for (TestCase testCase: defaultTestCases())
            result.add(new TestCase[] {testCase});

        for (TestCase testCase: readTestCases("/SHA1/SHA1ShortMsg.rsp"))
            result.add(new TestCase[] {testCase});

        for (TestCase testCase: readTestCases("/SHA1/SHA1LongMsg.rsp"))
            result.add(new TestCase[] {testCase});

        return result;
    }

    private static TestCase[] defaultTestCases() {
        TestCase[] result = new TestCase[10];

        result[0] = new TestCase("abc".getBytes(), "a9993e364706816aba3e25717850c26c9cd0d89d");
        result[1] = new TestCase("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".getBytes(), "84983e441c3bd26ebaae4aa1f95129e5e54670f1");
        result[2] = new TestCase("abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu".getBytes(), "a49b2446a02c645bf419f995b67091253a04a259");
        result[3] = new TestCase(StringUtils.leftPad("a", 1000000, "a").getBytes(), "34aa973cd4c4daa4f61eeb2bdbad27316534016f");
        result[4] = new TestCase("".getBytes(), "da39a3ee5e6b4b0d3255bfef95601890afd80709");
        result[5] = new TestCase(StringUtils.leftPad("1", 55, "1").getBytes(), "6b10c9ff4e3356b38d340918f62de8de87af9c6d");
        result[6] = new TestCase(StringUtils.leftPad("1", 56, "1").getBytes(), "b8e49fda8515cc13b4d6bf80d97fca7a24732514");
        result[7] = new TestCase(StringUtils.leftPad("1", 64, "1").getBytes(), "07a1a50a6273e6bc2eb94d647810cdc5b275b924");
        result[8] = new TestCase(new byte[] {126, 61, 123, 62, 83, 87, 120, 102}, "fd0e6b0db87237f444fdd09a9540d47f32cd60ee");
        result[9] = new TestCase("ßKÒ".getBytes(), "17d7a151c1b0f84ad647ea17ef9aae2109cee2bc");

        return result;
    }

    private static byte[] fromHex2Bytes(String hexString) {
        hexString = hexString.replaceAll("^(00)+", "");
        byte[] bytes = new byte[hexString.length() / 2];
        for (int i = 0; i < hexString.length(); i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    private static Collection<? extends TestCase> readTestCases(String filePath) throws Exception {
        List<String> contentList = Files.readAllLines(Path.of(NistTests.class.getResource(filePath).toURI()));
        List<TestCase> result = new ArrayList<>();

        for(int i = 0; i < contentList.size(); i++) {
            if (! contentList.get(i).contains("Msg = ")) continue;

            result.add(new TestCase(
                    fromHex2Bytes(contentList.get(i++).split(" = ")[1]), // Msg = xxx
                    contentList.get(i).split(" = ")[1].trim())   // MD = xxx
            );
        }

        return result;
    }

    @Test
    public void runNistTests() {
        Assert.assertEquals(
                String.format("Hashing message '%s' - string rep: %s", Arrays.toString(testCase.value), new String(testCase.value)),
                testCase.result,
                SHA1.hash(testCase.value)
        );
    }
}
