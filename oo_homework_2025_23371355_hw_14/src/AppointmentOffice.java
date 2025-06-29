import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryTrace;
import com.oocourse.library2.annotation.Trigger;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.Iterator;

public class AppointmentOffice {
    private HashMap<User, Book> appointedBooks;
    private HashMap<LibraryBookIsbn, Queue<User>> registeredBooks;

    public AppointmentOffice() {
        appointedBooks = new HashMap<>();
        registeredBooks = new HashMap<>();
    }

    public void handle4Ordered(User user, LibraryBookIsbn isbn) {
        Queue<User> queue = new LinkedList<>();
        if (registeredBooks.containsKey(isbn)) {
            queue = registeredBooks.get(isbn);
        }
        queue.add(user);
        registeredBooks.put(isbn, queue);
    }

    @Trigger(from = "onBs", to = "onAo")
    @Trigger(from = "onAo", to = "onBs")
    public ArrayList<LibraryMoveInfo> arrange4Appoint(LocalDate nowDate) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        Bookshelf bs = Bookshelf.getInstance();
        //先把逾期的书返回书架
        Iterator<HashMap.Entry<User, Book>> iterator = appointedBooks.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<User, Book> entry = iterator.next();
            User user = entry.getKey();
            Book book = entry.getValue();
            boolean isExpired = book.isExpired(nowDate);
            if (isExpired) {
                //从预约处取出并返回书架，记录移动
                iterator.remove();
                bs.addBook(book);
                infos.add(new LibraryMoveInfo(book.getId(),
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
                book.addPath(new LibraryTrace(nowDate,
                    LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
                //此时该用户取消预定过书籍的状态
                user.cancelAppointment();
            }
        }
        //把可预约的书放在预约处
        Iterator<HashMap.Entry<LibraryBookIsbn, Queue<User>>> it =
            registeredBooks.entrySet().iterator();
        HashMap<LibraryBookIsbn, Queue<User>> newRegisteredBooks = new HashMap<>();
        while (it.hasNext()) {
            HashMap.Entry<LibraryBookIsbn, Queue<User>> entry = it.next();
            LibraryBookIsbn isbn = entry.getKey();
            Queue<User> q = entry.getValue();
            Book book;
            while (!q.isEmpty()) {
                //从书架上拿该isbn的书，没有便退出该循环
                book = bs.pop(isbn);
                if (book == null) {
                    break;
                }
                //有书分配给先来预约的用户
                User user = q.poll();
                appointedBooks.put(user, book);
                //设定预约逾期时间
                book.setExpireTime(nowDate.plusDays(4));
                //增加路径，此处注意来处是要根据图书的fromShelf属性来确定的
                infos.add(new LibraryMoveInfo(book.getId(), book.getShelf(),
                    LibraryBookState.APPOINTMENT_OFFICE, user.getId()));
                book.addPath(new LibraryTrace(nowDate, book.getShelf(),
                    LibraryBookState.APPOINTMENT_OFFICE));
            }
            if (!q.isEmpty()) {
                newRegisteredBooks.put(isbn, q);
            }
        }
        registeredBooks = newRegisteredBooks;
        return infos;
    }

    public Book handle4Picked(User user, LibraryBookIsbn isbn) {
        Iterator<HashMap.Entry<User, Book>> iterator = appointedBooks.entrySet().iterator();
        Book ans = null;
        while (iterator.hasNext()) {
            HashMap.Entry<User, Book> entry = iterator.next();
            Book book = entry.getValue();
            //不仅要有为这个人分配了书，还要分配的书刚好是预约的那种书才可以取走
            if (entry.getKey().getId().equals(user.getId())
                && isbn.equals(book.getId().getBookIsbn())) {
                iterator.remove();
                ans = book;
                break;
            }
        }
        return ans;
    }
}
