import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;

public class User {
    private String userId;
    private HashMap<LibraryBookId, Book> ownedBooks;
    private boolean isOrdered;

    public User(String userId) {
        this.userId = userId;
        ownedBooks = new HashMap<>();
        isOrdered = false;
    }

    public String getId() {
        return userId;
    }

    public void addBook(Book book) {
        ownedBooks.put(book.getId(), book);
    }

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

    public void addAppointment() {
        isOrdered = true;
    }

    public void cancelAppointment() {
        isOrdered = false;
    }
}
