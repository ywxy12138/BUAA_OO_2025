import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryTrace;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryReqCmd;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static com.oocourse.library1.LibraryIO.PRINTER;
import static com.oocourse.library1.LibraryIO.SCANNER;

public class Library {
    private Bookshelf bookshelf;
    private BorrowReturnOffice borrowReturnOffice;
    private AppointmentOffice appointmentOffice;
    private HashMap<String, User> users;
    private HashMap<LibraryBookId, Book> books;
    private LocalDate date;

    public Library(Bookshelf bookshelf,
        BorrowReturnOffice borrowReturnOffice, AppointmentOffice appointmentOffice) {
        this.bookshelf = bookshelf;
        this.borrowReturnOffice = borrowReturnOffice;
        this.appointmentOffice = appointmentOffice;
        this.users = new HashMap<>();
        this.books = new HashMap<>();
        this.date = LocalDate.now();
    }

    public void run() {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        for (LibraryBookIsbn isbn : bookList.keySet()) {
            Integer num = bookList.get(isbn);
            for (Integer i = 1; i.compareTo(num) <= 0; i++) {
                String copyId = ((i != 10) ? "0" : "") + i;
                Book book = new Book(new LibraryBookId(isbn.getType(), isbn.getUid(), copyId));
                books.put(book.getId(), book);
                bookshelf.addBook(book);
            }
        }
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            date = command.getDate();
            if (command instanceof LibraryOpenCmd) {
                arrange4Open();
            } else if (command instanceof LibraryReqCmd) {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType();
                if (type.equals(LibraryReqCmd.Type.QUERIED)) {
                    handle4Queried(req);
                }
                else if (type.equals(LibraryReqCmd.Type.BORROWED)) {
                    handle4Borrowed(req);
                }
                else if (type.equals(LibraryReqCmd.Type.ORDERED)) {
                    handle4Ordered(req);
                }
                else if (type.equals(LibraryReqCmd.Type.RETURNED)) {
                    handle4Returned(req);
                }
                else if (type.equals(LibraryReqCmd.Type.PICKED)) {
                    handle4Picked(req);
                }
            }
            else {
                //闭馆什么操作都不做，闭馆就是下班，谁下班干活啊(原来是我啊)
                System.out.println(0);
            }
        }
    }

    private void handle4Queried(LibraryReqCmd request) {
        //查询，貌似跟用户无关(图书馆并不care谁查)
        LibraryBookId id = request.getBookId();
        Book book = books.get(id);
        PRINTER.info(date, id, book.getPath());
    }

    private void handle4Borrowed(LibraryReqCmd request) {
        //借书
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookIsbn isbn = request.getBookIsbn();
        Bookshelf bookshelf = Bookshelf.getInstance();
        if (!isbn.isTypeA() && user.canBorrowBook(isbn)) {
            //书架对应的书移动到用户手上，也有可能根本没拿到书
            Book book = bookshelf.popBook(isbn);
            if (book != null) {
                user.addBook(book);
                //书的路径增加了从书架到用户手上
                book.addPath(new LibraryTrace(date, LibraryBookState.BOOKSHELF,
                    LibraryBookState.USER));
                PRINTER.accept(request, book.getId());
            }
            else {
                //满足条件，但没取到书也是失败
                //分支判断一定要全
                PRINTER.reject(request);
            }
        }
        else {
            //不满足条件输出借书失败
            PRINTER.reject(request);
        }
        users.put(userId, user);
    }

    private void handle4Ordered(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookIsbn isbn = request.getBookIsbn();
        if (!isbn.isTypeA() && user.canOrderBook(isbn)) {
            //预约成功，处于已预定书籍的状态，取消时间很关键
            //应该在保留期结束或者取完预约的书的时候取消
            user.addAppointment();
            //预约处登记一下，随便输出预约成功
            appointmentOffice.handle4Ordered(user, isbn);
            PRINTER.accept(request);
        }
        else {
            //否则预约失败
            PRINTER.reject(request);
        }
        users.put(userId, user);
    }

    private void handle4Returned(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookId id = request.getBookId();
        Book book = user.popBook(id);
        //还书只有成功一说，还能不让人还书？
        borrowReturnOffice.handle4Returned(book);
        //书新增一条从用户手上到借还处的路径
        book.addPath(new LibraryTrace(date, LibraryBookState.USER,
            LibraryBookState.BORROW_RETURN_OFFICE));
        PRINTER.accept(request);
        users.put(userId, user);
    }

    private void handle4Picked(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookIsbn isbn = request.getBookIsbn();
        if (!isbn.isTypeA() && user.canBorrowBook(isbn)) {
            //从预约处把预约的书拿走
            Book book = appointmentOffice.handle4Picked(user, isbn);
            //当然有可能取不到书，可能已经逾期了，可能压根没预约这种书
            if (book != null) {
                user.addBook(book);
                //取走预约的书，就脱离了预约过书籍的状态了，可以继续预约了
                user.cancelAppointment();
                //书增加一条从预约处到用户手上的路径
                book.addPath(new LibraryTrace(date, LibraryBookState.APPOINTMENT_OFFICE,
                    LibraryBookState.USER));
                PRINTER.accept(request, book.getId());
            }
            else {
                //分支判断一定要全
                PRINTER.reject(request);
            }
        }
        else {
            PRINTER.reject(request);
        }
        users.put(userId, user);
    }

    private void arrange4Open() {
        ArrayList<LibraryMoveInfo> totList = new ArrayList<>();
        //先将借还处的书归还
        totList.addAll(borrowReturnOffice.arrange4Return(date));
        //再处理预约处的书
        totList.addAll(appointmentOffice.arrange4Appoint(date));
        PRINTER.move(date, totList);
    }
}

