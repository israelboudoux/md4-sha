package edu.boudoux;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
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

        for (TestCase testCase: readTestCases("/SHA1/SHA1ShortMsg.rsp"))
            result.add(new TestCase[] {testCase});

        for (TestCase testCase: readTestCases("/SHA1/SHA1LongMsg.rsp"))
            result.add(new TestCase[] {testCase});

        return result;
    }

    private static byte[] fromHex2Bytes(String hexString) {
        byte[] result = new byte[hexString.length() / 2];
        for(int i = 0; i < result.length; i++) {
            result[i] = (byte) Short.parseShort(hexString.substring(i * 2, i * 2 + 2), 16);
        }
        return result;
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
                String.format("Test failed for '%s'", testCase.value),
                testCase.result,
                SHA1.hash(testCase.value)
        );
    }
}
