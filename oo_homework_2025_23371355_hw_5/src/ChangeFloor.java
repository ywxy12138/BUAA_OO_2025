public class ChangeFloor {
    public static int getFloor(String string) {
        switch (string.charAt(0)) { //-4到-1代表B4-B1,0-6代表F1-F7
            case 'B' :
                return -(string.charAt(1) - '0');
            case 'F' :
                return (string.charAt(1) - '0' - 1);
            default:
                return  0;
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
