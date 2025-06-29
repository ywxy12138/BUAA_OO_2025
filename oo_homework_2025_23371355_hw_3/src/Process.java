public class Process { //作为字符串预处理的类

    public static String process(String string) {
        return getOutMoreSign(getOutMoreBlank(string));
    }

    public static String getOutMoreBlank(String str) {
        int len = str.length();
        String newStr = "";
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == ' ' || str.charAt(i) == '\t') {
                continue;
            }
            newStr += str.charAt(i);
        }
        return newStr;
    }

    public static String getOutMoreSign(String str) {
        int len = str.length();
        String newStr = "";
        for (int i = 0; i < len; i++) {
            if (str.charAt(i) == '+' || str.charAt(i) == '-') {
                int sign = 1;
                while (i < len && str.charAt(i) == '+' || str.charAt(i) == '-') {
                    sign *= ((str.charAt(i) == '+') ? 1 : -1);
                    i++;
                }
                newStr += ((sign > 0) ? "+" : "-");
            }
            newStr += str.charAt(i);
        }
        return newStr;
    }
}
