package Client;

import java.util.Random;

public class ZigZag {
    static public String DeCrypte(String str, int key) {
        int s = key * 2 - 2;
        StringBuilder res = new StringBuilder(str);
        int index = 0;
        for (int i = 0; i < key; i++) {
            int t = s - (2 * i);
            int j = i;
            while (j < str.length()) {
                if (t == s || t == 0) {
                    res.setCharAt(j, str.charAt(index));
                    j += s;
                    index++;
                } else {
                    res.setCharAt(j, str.charAt(index));

                    j += t;
                    index++;
                    if (j < str.length()) {
                        res.setCharAt(j, str.charAt(index));
                        j += s - t;
                        index++;
                    }
                }
            }
        }
        return res.toString();
    }

    static public String Cryptage(String str, int key) {
        int s = key * 2 - 2;
        String res = "";
        for (int i = 0; i < key; i++) {
            int j = i;
            while (j < str.length()) {
                int t = s - 2 * i;
                if (t == s || t == 0) {
                    res += str.charAt(j);
                    j += s;
                } else {
                    res += str.charAt(j);
                    j += t;
                    if (j < str.length())
                        res += str.charAt(j);
                    j += (s - t);
                }
            }
        }
        return res;
    }

    public static String GenerateRandomString(int length) {

        int leftLimit = 48; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = length;
        Random random = new Random();
        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int) (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}
