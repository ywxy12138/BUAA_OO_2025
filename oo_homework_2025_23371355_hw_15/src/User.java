import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.annotation.SendMessage;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;

public class User {
    private String userId;
    private HashMap<LibraryBookId, Book> ownedBooks;
    private boolean isOrdered;
    private boolean isRead;
    private int score;

    public User(String userId) {
        this.userId = userId;
        ownedBooks = new HashMap<>();
        isOrdered = false;
        isRead = false;
        score = 100;
    }

    public String getId() {
        return userId;
    }

    @SendMessage(from = "User", to = "Library")
    public void addBook(Book book) {
        ownedBooks.put(book.getId(), book);
    }

    @SendMessage(from = "User", to = "Library")
    public Book popBook(LibraryBookId bookId) {
        return ownedBooks.remove(bookId);
    }

    public boolean canBorrowBook(LibraryBookIsbn isbn) {
        for (Book book : ownedBooks.values()) {
            if (book.getBookType().equals(LibraryBookIsbn.Type.B) && isbn.isTypeB()) {
                return false;
            }
            else if (book.getBookType().equals(LibraryBookIsbn.Type.C)
                && isbn.equals(book.getId().getBookIsbn())) {
                return false;
            }
        }
        return true;
    }

    public boolean canOrderBook(LibraryBookIsbn isbn) {
        return canBorrowBook(isbn) && !isOrdered;
    }

    public boolean canReadBook() {
        return !isRead;
    }

    @SendMessage(from = "User", to = "Library")
    public void orderNewBook() {
        isOrdered = true;
    }

    @SendMessage(from = "User", to = "Library")
    public void getOrderedBook() {
        isOrdered = false;
    }

    @SendMessage(from = "User", to = "Library")
    public void readBook() {
        isRead = true;
    }

    @SendMessage(from = "User", to = "Library")
    public void restoreBook() {
        isRead = false;
    }

    public int getScore() {
        return score;
    }

    public void addScore(int score) {
        if (score > 0) {
            //限制信用分上限不超过180
            this.score = Math.min(this.score + score, 180);
        }
        else {
            //下限不小于0
            this.score = Math.max(this.score + score, 0);
        }
    }

    @SendMessage(from = "User", to = "Library")
    public void modifyScore(LocalDate lastDate, LocalDate nowDate) {
        //看借阅的书中是否有超过借阅期限的，有的话要根据上一次开馆的是否进行了处理来进一步处理
        //避免多扣信用分的情况发生
        for (Book book : ownedBooks.values()) {
            int dueDays = book.isDue(nowDate);
            int deltaDays = (int) ChronoUnit.DAYS.between(lastDate, nowDate);
            //判断是否超过期限
            if (dueDays > 0) {
                //逾期不还书的下场
                addScore(-5 * Math.min(dueDays, deltaDays));
            }
        }
    }
}
