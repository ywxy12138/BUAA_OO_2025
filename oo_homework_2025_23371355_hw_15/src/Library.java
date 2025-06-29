import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryReqCmd;
import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryTrace;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static com.oocourse.library3.LibraryIO.PRINTER;
import static com.oocourse.library3.LibraryIO.SCANNER;

public class Library {
    private Bookshelf bookshelf;
    private BorrowReturnOffice borrowReturnOffice;
    private AppointmentOffice appointmentOffice;
    private ReadingRoom readingRoom;
    private HashMap<String, User> users;
    private HashMap<LibraryBookId, Book> books;
    private LocalDate date;
    private LocalDate lastDate;
    //存储上一次开馆的热门书籍
    private HashSet<LibraryBookIsbn> hotBooksList;
    //存储当天将书带到阅览室但是没有还书的用户
    private HashSet<String> readingUsers;

    public Library(Bookshelf bookshelf, BorrowReturnOffice borrowReturnOffice,
        AppointmentOffice appointmentOffice, ReadingRoom readingRoom) {
        this.bookshelf = bookshelf;
        this.borrowReturnOffice = borrowReturnOffice;
        this.appointmentOffice = appointmentOffice;
        this.readingRoom = readingRoom;
        this.users = new HashMap<>();
        this.books = new HashMap<>();
        this.date = LocalDate.now();
        this.lastDate = LocalDate.now();
        this.hotBooksList = new HashSet<>();
        this.readingUsers = new HashSet<>();
    }

    public void run() {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        for (LibraryBookIsbn isbn : bookList.keySet()) {
            for (Integer i = 1; i.compareTo(bookList.get(isbn)) <= 0; i++) {
                String copyId = ((i < 10) ? "0" : "") + i;
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
                hotBooksList.clear();
            } else if (command instanceof LibraryReqCmd) {
                LibraryReqCmd.Type type = ((LibraryReqCmd) command).getType();
                if (type.equals(LibraryReqCmd.Type.QUERIED)) {
                    handle4Queried((LibraryReqCmd) command);
                }
                else if (type.equals(LibraryReqCmd.Type.BORROWED)) {
                    handle4Borrowed((LibraryReqCmd) command);
                }
                else if (type.equals(LibraryReqCmd.Type.ORDERED)) {
                    handle4Ordered((LibraryReqCmd) command);
                }
                else if (type.equals(LibraryReqCmd.Type.RETURNED)) {
                    handle4Returned((LibraryReqCmd) command);
                }
                else if (type.equals(LibraryReqCmd.Type.PICKED)) {
                    handle4Picked((LibraryReqCmd) command);
                }
                else if (type.equals(LibraryReqCmd.Type.READ)) {
                    handle4Read((LibraryReqCmd) command);
                }
                else if (type.equals(LibraryReqCmd.Type.RESTORED)) {
                    handle4Restored((LibraryReqCmd) command);
                }
            }
            else if (command instanceof LibraryQcsCmd) {
                LibraryQcsCmd request = (LibraryQcsCmd) command;
                if (!users.containsKey(request.getStudentId())) {
                    users.put(request.getStudentId(), new User(request.getStudentId()));
                }
                PRINTER.info(request, users.get(request.getStudentId()).getScore());
            }
            else {
                System.out.println(0);
                //更新一下当天阅读过书籍但是没有归还的用户状态，他们只是忘了罢，虽然忘了，但是是以信用为代价的
                for (String name : readingUsers) {
                    users.get(name).restoreBook();
                    users.get(name).addScore(-10);
                }
                readingUsers.clear();
                lastDate = date;
            }
        }
    }

    private void handle4Queried(LibraryReqCmd request) {
        //查询，貌似跟用户无关(图书馆并不care谁查)
        LibraryBookId id = request.getBookId();
        Book book = books.get(id);
        PRINTER.info(date, id, book.getPath());
    }

    @Trigger(from = "onBs", to = "onUser")
    @SendMessage(from = "Library", to = "Bookshelf")
    @SendMessage(from = "Library", to = "User")
    public void handle4Borrowed(LibraryReqCmd request) {
        //借书
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookIsbn isbn = request.getBookIsbn();
        Bookshelf bs = Bookshelf.getInstance();
        boolean canBorrowBook = !isbn.isTypeA() &&
            user.canBorrowBook(isbn) && user.getScore() >= 60;
        if (canBorrowBook) {
            //书架对应的书移动到用户手上，也有可能根本没拿到书
            Book book = bs.pop(isbn);
            if (book != null) {
                user.addBook(book);
                //书的路径增加了从书架到用户手上
                book.addPath(new LibraryTrace(date, book.getShelf(),
                    LibraryBookState.USER));
                //借阅成功，视为热门书籍
                hotBooksList.add(isbn);
                //借阅成功，该书得上期限，B类书期限30天，C类书60天
                book.setDeadline(date.plusDays((book.getId().isTypeB()) ? 30 : 60));
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

    @SendMessage(from = "Library", to = "User")
    @SendMessage(from = "Library", to = "AppointmentOffice")
    public void handle4Ordered(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookIsbn isbn = request.getBookIsbn();
        boolean canOrderBook = !isbn.isTypeA() && user.canOrderBook(isbn) && user.getScore() >= 100;
        if (canOrderBook) {
            //预约成功，处于已预定书籍的状态，取消时间很关键
            //应该在保留期结束或者取完预约的书的时候取消
            user.orderNewBook();
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

    @Trigger(from = "onUser", to = "onBro")
    @SendMessage(from = "Library", to = "User")
    @SendMessage(from = "Library", to = "BorrowReturnOffice")
    public void handle4Returned(LibraryReqCmd request) {
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
        //判断还的书是否逾期了
        if (book.isDue(date) <= 0) {
            //没逾期，别忘了奖励老实孩子
            user.addScore(10);
            PRINTER.accept(request, "not overdue");
        }
        else {
            //在开馆的时候已经把你的逾期分给扣了，这里不再扣了
            PRINTER.accept(request, "overdue");
        }
        users.put(userId, user);
    }

    @Trigger(from = "onAo", to = "onUser")
    @SendMessage(from = "Library", to = "AppointmentOffice")
    @SendMessage(from = "Library", to = "User")
    public void handle4Picked(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookIsbn isbn = request.getBookIsbn();
        boolean canPicked = !isbn.isTypeA() && user.canBorrowBook(isbn);
        if (canPicked) {
            //从预约处把预约的书拿走
            Book book = appointmentOffice.handle4Picked(user, isbn);
            //当然有可能取不到书，可能已经逾期了，可能压根没预约这种书
            if (book != null) {
                user.addBook(book);
                //取走预约的书，就脱离了预约过书籍的状态了，可以继续预约了
                user.getOrderedBook();
                //书增加一条从预约处到用户手上的路径
                book.addPath(new LibraryTrace(date, LibraryBookState.APPOINTMENT_OFFICE,
                    LibraryBookState.USER));
                //取书成功，设置借阅期限
                book.setDeadline(date.plusDays((book.getId().isTypeB()) ? 30 : 60));
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

    @Trigger(from = "onBs", to = "onRr")
    @SendMessage(from = "Library", to = "Bookshelf")
    @SendMessage(from = "Library", to = "User")
    @SendMessage(from = "Library", to = "ReadingRoom")
    public void handle4Read(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User u = new User(userId);
        if (users.containsKey(userId)) {
            u = users.get(userId);
        }
        Book book;
        LibraryBookIsbn isbn = request.getBookIsbn();
        Bookshelf bs = Bookshelf.getInstance();
        boolean canReadBook = u.canReadBook() && ((isbn.isTypeA() && u.getScore() >= 40)
            || ((isbn.isTypeB() || isbn.isTypeC()) && u.getScore() > 0));
        if (canReadBook &&
            (book = bs.pop(isbn)) != null) {
            //多了一个在阅览室阅读书的人，状态记得更新
            readingUsers.add(userId);
            u.readBook();
            //把书移交到阅览室
            readingRoom.handle4Read(book);
            //图书新增路径
            book.addPath(new LibraryTrace(date, book.getShelf(),
                LibraryBookState.READING_ROOM));
            //热门图书清单更新
            hotBooksList.add(isbn);
            //阅读成功
            PRINTER.accept(request, book.getId());
        }
        else {
            //阅读失败
            PRINTER.reject(request);
        }
        users.put(userId, u);
    }

    @Trigger(from = "onRr", to = "onBro")
    @SendMessage(from = "Library", to = "ReadingRoom")
    @SendMessage(from = "Library", to = "BorrowReturnOffice")
    @SendMessage(from = "Library", to = "User")
    public void handle4Restored(LibraryReqCmd request) {
        String userId = request.getStudentId();
        User user = new User(userId);
        if (users.containsKey(userId)) {
            user = users.get(userId);
        }
        LibraryBookId id = request.getBookId();
        //从阅览室取走该书
        Book book = readingRoom.handle4Restored(id);
        if (book != null) {
            //把书放到借还处
            borrowReturnOffice.handle4Returned(book);
            //用户状态更新，可以继续在当天阅读书籍了
            user.restoreBook();
            //闭馆时不用更新该用户了
            readingUsers.remove(userId);
            //书籍的路径增加
            book.addPath(new LibraryTrace(date, LibraryBookState.READING_ROOM,
                LibraryBookState.BORROW_RETURN_OFFICE));
            //阅读后当天还书的好孩子应该嘉奖
            user.addScore(10);
            PRINTER.accept(request);
        }
        else {
            PRINTER.reject(request);
        }
        users.put(userId, user);
    }

    @SendMessage(from = "Library", to = "BorrowReturnOffice")
    @SendMessage(from = "Library", to = "ReadingRoom")
    @SendMessage(from = "Library", to = "AppointmentOffice")
    @SendMessage(from = "Library", to = "Bookshelf")
    @SendMessage(from = "Library", to = "User")
    public void arrange4Open() {
        ArrayList<LibraryMoveInfo> totList = new ArrayList<>();
        //先将借还处的书归还
        totList.addAll(borrowReturnOffice.arrange4Return(date));
        //再将阅览处的书归还
        totList.addAll(readingRoom.arrange4Return(date));
        //再处理预约处的书，办理逾期还书和给预约处放书
        totList.addAll(appointmentOffice.arrange4Appoint(date));
        //整理热门书籍
        totList.addAll(Bookshelf.getInstance().arrange4Open(hotBooksList, date));
        //开馆的时候给每个用户扣减一下逾期用户分
        for (User user : users.values()) {
            user.modifyScore(lastDate, date);
        }
        PRINTER.move(date, totList);
    }
}

