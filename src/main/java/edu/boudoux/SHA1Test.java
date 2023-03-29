package edu.boudoux;

public class SHA1Test {

    private static final TestCase[] TEST_CASES = new TestCase[] {
            new TestCase("0", "b6589fc6ab0dc82cf12099d1c2d40ab994e8410c"),
            new TestCase("abc", "a9993e364706816aba3e25717850c26c9cd0d89d"),
            new TestCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "0098ba824b5c16427bd7a1122a5a442a25ec644d"),
            new TestCase("1", "356a192b7913b04c54574d18c28d46e6395428ab"),
            new TestCase("abcd", "81fe8bfe87576c3ecb22426f8e57847382917acf"),
            new TestCase("abcdefghjklmnopqrstuvxwyz", "4c207e44c6be6e4f53b01744cc014b05204b8967"),
            new TestCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "11655326c708d70319be2610e8a57d9a5b959d3b"),
            new TestCase("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa", "03f09f5b158a7a8cdad920bddc29b81c18a551f5"),
            new TestCase("abcdefghjklmnopqrstuvxwyzabcdefghjklmnopqrstuvxwyz", "e874e94110c564dd2e6d6141121dc0fc3b4d0855"),
            new TestCase("abcdefghjklmnopqrstuvxwyzabcdefghjklmnopqrstuvxwyzabcdefghjklmnopqrstuvxwyz", "c68b47dcb5ea9124deafac6145eba92e33ed637e")
    };

    private static class TestCase {
        public String value;
        public String result;
        public TestCase(String value, String result) {
            this.value = value;
            this.result = result;
        }
    }

    public static void main(String[] args) {
        for (TestCase testCase: TEST_CASES) {
            String hash = SHA1.hash(testCase.value);
            String expectedResult = testCase.result;

            if (! hash.equals(expectedResult))
                System.err.printf("Hash('%s') = %s notEq %s\n", testCase.value, hash, expectedResult);
        }
    }
}
