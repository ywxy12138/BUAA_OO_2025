import com.oocourse.library2.LibraryTrace;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryMoveInfo;
import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookState;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class Bookshelf {
    private HashMap<LibraryBookIsbn, ArrayList<Book>> normalBooks;
    private HashMap<LibraryBookIsbn, ArrayList<Book>> hotBooks;
    private static Bookshelf bookshelf = null;

    public Bookshelf() {
        normalBooks = new HashMap<>();
        hotBooks = new HashMap<>();
    }

    public static Bookshelf getInstance() {
        if (bookshelf == null) {
            bookshelf = new Bookshelf();
        }
        return bookshelf;
    }

    public void addBook(Book book) {
        //addBook()方法放回的都是普通书架
        LibraryBookId id = book.getId();
        ArrayList<Book> bookList = new ArrayList<>();
        if (normalBooks.containsKey(id.getBookIsbn())) {
            bookList = normalBooks.get(id.getBookIsbn());
        }
        bookList.add(book);
        //放回普通书架了，需要更新一下fromShelf状态
        book.setShelf(LibraryBookState.BOOKSHELF);
        normalBooks.put(id.getBookIsbn(), bookList);
    }

    public Book pop(LibraryBookIsbn isbn) {
        //从书架上取出一本书，没有此类书(压根没有或者没有余本了返回null)
        if (normalBooks.containsKey(isbn)) {
            ArrayList<Book> bookList = normalBooks.get(isbn);
            if (!bookList.isEmpty()) {
                Book book = bookList.get(bookList.size() - 1);
                bookList.remove(bookList.size() - 1);
                normalBooks.replace(isbn, bookList);
                return book;
            }
        }
        else if (hotBooks.containsKey(isbn)) {
            ArrayList<Book> bookList = hotBooks.get(isbn);
            if (!bookList.isEmpty()) {
                Book book = bookList.get(bookList.size() - 1);
                bookList.remove(bookList.size() - 1);
                hotBooks.replace(isbn, bookList);
                return book;
            }
        }
        return null;
    }

    public ArrayList<LibraryMoveInfo> arrange4Open(
        HashSet<LibraryBookIsbn> hotBookList, LocalDate date) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        //先将热门书架上不在热门书清单hotBookList中的书移动到普通书架
        Iterator iterator = hotBooks.keySet().iterator();
        while (iterator.hasNext()) {
            LibraryBookIsbn isbn = (LibraryBookIsbn) iterator.next();
            if (!hotBookList.contains(isbn)) {
                //不在热门书清单上，那就得转移到普通书架上
                ArrayList<Book> bookList = hotBooks.get(isbn);
                if (!bookList.isEmpty()) {
                    for (Book book : bookList) {
                        infos.add(new LibraryMoveInfo(book.getId(), LibraryBookState.HOT_BOOKSHELF,
                            LibraryBookState.BOOKSHELF));
                        book.addPath(new LibraryTrace(date, LibraryBookState.HOT_BOOKSHELF,
                            LibraryBookState.BOOKSHELF));
                        book.setShelf(LibraryBookState.BOOKSHELF);
                    }
                    iterator.remove();
                    if (normalBooks.containsKey(isbn) && !normalBooks.get(isbn).isEmpty()) {
                        bookList.addAll(normalBooks.get(isbn));
                    }
                    normalBooks.put(isbn, bookList);
                }
            }
        }
        //再将普通书架上的热门书移动到热门书架上
        for (LibraryBookIsbn isbn : hotBookList) {
            if (normalBooks.containsKey(isbn)) {
                //这些书都要移动到热门书架上
                ArrayList<Book> bookList = normalBooks.get(isbn);
                if (!bookList.isEmpty()) {
                    for (Book book : bookList) {
                        infos.add(new LibraryMoveInfo(book.getId(), LibraryBookState.BOOKSHELF,
                            LibraryBookState.HOT_BOOKSHELF));
                        book.addPath(new LibraryTrace(date, LibraryBookState.BOOKSHELF,
                            LibraryBookState.HOT_BOOKSHELF));
                        //图书转移了需要更新状态
                        book.setShelf(LibraryBookState.HOT_BOOKSHELF);
                    }
                    //完成普通书架向热门书架书的移动
                    //需要多判断该种书是否已经在对应的在书架上
                    if (hotBooks.containsKey(isbn) && !hotBooks.get(isbn).isEmpty()) {
                        bookList.addAll(hotBooks.get(isbn));
                    }
                    hotBooks.put(isbn, bookList);
                    normalBooks.remove(isbn);
                }
            }
        }
        return infos;
    }
}
