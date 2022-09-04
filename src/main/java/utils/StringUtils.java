package utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class StringUtils {

    public static Set<String> stopWords = new HashSet<>();

    static {
        stopWords.addAll(Arrays.asList("", "!", "\"", "#", "$", "%", "&", "'", "(", ")", "*", "+", ",", "-", ".", "/", ":", ";", "<",
                "=", ">", "?", "@", "[", "\\", "]", "^", "_", "`", "{", "|", "}", "~"));
    }

    public static boolean isStopWord(String word) {
        return stopWords.contains(word.trim());
    }

    public static int occurTimes(String string, String pattern) {
        int cnt = 0;
        int i = -1;
        while ((i = string.indexOf(pattern, i + 1)) != -1) {
            ++cnt;
        }
        return cnt;
    }

    public static int occurTimes(String string, char c) {
        int cnt = 0;
        for (int i = 0; i < string.length(); ++i) {
            if (string.charAt(i) == c) {
                ++cnt;
            }
        }
        return cnt;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static String repeat(String s, int n) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }

    public static boolean isEqualXPath(String xpath1, String xpath2) {
        String[] xpath1s = xpath1.split("/");
        String[] xpath2s = xpath2.split("/");
        int index1 = 0, index2 = 0;
        if (xpath1s.length > 0 && StringUtils.isBlank(xpath1s[0])) {
            index1 = 1;
        }
        if (xpath2s.length > 0 && StringUtils.isBlank(xpath2s[0])) {
            index2 = 1;
        }
        if (xpath1s.length - index1 != xpath2s.length - index2) {
            return false;
        }
        for (int i = 0; i < xpath1s.length - index1; ++i) {
            String s1 = xpath1s[i + index1].trim();
            String s2 = xpath2s[i + index2].trim();
            if (!s1.equals(s2) && ! (s1 + "[1]").equals(s2) && !s1.equals(s2 + "[1]")) {
                return false;
            }
        }
        return true;
    }

    public static boolean isTruncateEqualXpath(String xpath1, String xpath2, int trCnt2) {
        String[] xpath1s = xpath1.split("/");
        String[] xpath2s = xpath2.split("/");
        int index1 = 0, index2 = 0;
        if (xpath1s.length > 0 && StringUtils.isBlank(xpath1s[0])) {
            index1 = 1;
        }
        if (xpath2s.length > 0 && StringUtils.isBlank(xpath2s[0])) {
            index2 = 1;
        }
        if (xpath1s.length - index1 < xpath2s.length - index2 - trCnt2 || xpath1s.length - index1 > xpath2s.length - index2) {
            return false;
        }
        for (int i = 0; i < Math.max(xpath1s.length - index1, xpath2s.length - index2 - trCnt2); ++i) {
            String s1 = xpath1s[i + index1].trim();
            String s2 = xpath2s[i + index2].trim();
            if (!s1.equals(s2) && ! (s1 + "[1]").equals(s2) && !s1.equals(s2 + "[1]")) {
                return false;
            }
        }
        return true;
    }

}
