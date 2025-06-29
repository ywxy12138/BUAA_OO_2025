public class TransferArea {
    private int transferFloor;
    private String side;
    private boolean beginA;
    private boolean beginB;
    private boolean realBegin;
    private boolean endA;
    private boolean endB;

    public TransferArea() {
        transferFloor = -5;
        side = "none";
        realBegin = false;
        beginA = false;
        beginB = false;
        endA = false;
        endB = false;
    }

    public int getTraFlo() {
        return transferFloor;
    }

    public synchronized void setTransferFloor(int transferFloor) {
        this.transferFloor = transferFloor;
        notifyAll();
    }

    public synchronized void setBegin(String side) {
        if (side.equals("up")) {
            beginA = true;
        }
        else if (side.equals("down")) {
            beginB = true;
        }
        notifyAll();
    }

    public synchronized void setEnd(String side) {
        if (side.equals("up")) {
            endA = true;
        }
        else if (side.equals("down")) {
            endB = true;
        }
        notifyAll();
    }

    public synchronized void setRealBegin() {
        realBegin = true;
        notifyAll();
    }

    public synchronized void setSide(String side) {
        this.side = side;
        notifyAll();
    }

    public synchronized String getSide() {
        return side;
    }

    public synchronized boolean getBegin() {
        return beginA && beginB;
    }

    public synchronized boolean getRealBegin() {
        return beginA && beginB && realBegin;
    }

    public synchronized boolean getEnd() {
        return endA && endB;
    }
}
