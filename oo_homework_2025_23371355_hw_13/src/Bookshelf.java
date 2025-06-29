import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;

public class Bookshelf {
    private HashMap<LibraryBookIsbn, ArrayList<Book>> books;
    private static Bookshelf bookshelf = null;

    public Bookshelf() {
        books = new HashMap<>();
    }

    public static Bookshelf getInstance() {
        if (bookshelf == null) {
            bookshelf = new Bookshelf();
        }
        return bookshelf;
    }

    public void addBook(Book book) {
        LibraryBookId id = book.getId();
        ArrayList<Book> bookList = new ArrayList<>();
        if (books.containsKey(id.getBookIsbn())) {
            bookList = books.get(id.getBookIsbn());
        }
        bookList.add(book);
        books.put(id.getBookIsbn(), bookList);
    }

    public Book popBook(LibraryBookIsbn isbn) {
        //从书架上取出一本书，没有此类书(压根没有或者没有余本了返回null)
        if (books.containsKey(isbn)) {
            ArrayList<Book> bookList = books.get(isbn);
            if (!bookList.isEmpty()) {
                Book book = bookList.get(bookList.size() - 1);
                bookList.remove(bookList.size() - 1);
                books.replace(isbn, bookList);
                return book;
            }
        }
        return null;
    }
}
