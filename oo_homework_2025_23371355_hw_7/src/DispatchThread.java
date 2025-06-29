public class DispatchThread extends Thread {
    private final RequestTable requests;

    public DispatchThread(RequestTable requests) {
        this.requests = requests;
    }

    public void run() {
        while (true) {
            //总候乘表为空且没有输入了且所有的电梯线程的内外都没人了，说明输入结束，调度器调度结束
            //告知分候乘表输入结束
            if (requests.isEnd() && requests.isAllWaiting() && requests.isEmpty()) {
                AllElevators.getInstance().setEnd();
                break;
            }
            while (!requests.isAllWaiting() && requests.isEmpty()) {
                synchronized (requests) {
                    try {
                        requests.wait();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            Person person;
            //从总候乘表拿一个请求
            person = requests.getPerson();
            if (person == null) {
                continue;
            }
            AllElevators.getInstance().dispatch(person);
        }
    }
}
