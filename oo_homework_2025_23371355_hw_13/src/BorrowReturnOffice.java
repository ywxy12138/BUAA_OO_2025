import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;

public class BorrowReturnOffice {
    private ArrayList<Book> books;

    public BorrowReturnOffice() {
        books = new ArrayList<>();
    }

    public void handle4Returned(Book book) {
        books.add(book);
    }

    public ArrayList<LibraryMoveInfo> arrange4Return(LocalDate nowDate) {
        //把借还处的书全部返回书架
        ArrayList<LibraryMoveInfo> infos = new ArrayList<>();
        Bookshelf bookshelf = Bookshelf.getInstance();
        for (Book book : books) {
            bookshelf.addBook(book);
            //增加移动信息
            infos.add(new LibraryMoveInfo(book.getId(),
                LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
            //书新增一条从借还处到书架的路径
            book.addPath(new LibraryTrace(nowDate,
                LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
        }
        books.clear();
        return infos;
    }
}
