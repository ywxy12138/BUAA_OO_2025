import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryTrace;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.annotation.SendMessage;
import com.oocourse.library3.annotation.Trigger;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;

public class ReadingRoom {
    private HashMap<LibraryBookId, Book> books;

    public ReadingRoom() {
        books = new HashMap<>();
    }

    @SendMessage(from = "ReadingRoom", to = "Library")
    public void handle4Read(Book book) {
        books.put(book.getId(), book);
    }

    @SendMessage(from = "ReadingRoom", to = "Library")
    public Book handle4Restored(LibraryBookId id) {
        return books.remove(id);
    }

    @Trigger(from = "onRr", to = "onBs")
    @SendMessage(from = "ReadingRoom", to = "Bookshelf")
    @SendMessage(from = "ReadingRoom", to = "Library")
    public ArrayList<LibraryMoveInfo> arrange4Return(LocalDate nowDate) {
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        Bookshelf bookshelf = Bookshelf.getInstance();
        for (LibraryBookId id : books.keySet()) {
            Book book = books.get(id);
            bookshelf.addBook(book);
            infos.add(new LibraryMoveInfo(id,
                LibraryBookState.READING_ROOM, LibraryBookState.BOOKSHELF));
            book.addPath(new LibraryTrace(nowDate,
                LibraryBookState.READING_ROOM, LibraryBookState.BOOKSHELF));
        }
        books.clear();
        return infos;
    }
}
