public class ChangeFloor {
    public static int getFloor(String string) {
        if (string.charAt(0) == 'B') {
            return -(string.charAt(1) - '0');
        }
        else {
            return (string.charAt(1) - '0' - 1);
        }
    }

    public static String getFloor(int currentFloor) {
        if (currentFloor >= 0) {
            return ("F" + (currentFloor + 1));
        }
        else {
            return ("B" + (-currentFloor));
        }
    }
}
