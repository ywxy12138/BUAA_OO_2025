import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryTrace;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class Book {
    private LibraryBookId id;
    private LocalDate expireTime;
    private LibraryBookState fromShelf;
    private ArrayList<LibraryTrace> path;
    private LocalDate deadline;

    public Book(LibraryBookId id) {
        this.id = id;
        this.expireTime = LocalDate.now().plusDays(1);
        this.fromShelf = LibraryBookState.BOOKSHELF;
        this.path = new ArrayList<>();
        this.deadline = LocalDate.now().plusDays(1);
    }

    public LibraryBookId getId() {
        return id;
    }

    public LibraryBookId.Type getBookType() {
        return id.getType();
    }

    public ArrayList<LibraryTrace> getPath() {
        return path;
    }

    public LibraryBookState getShelf() { return fromShelf; }

    public void setShelf(LibraryBookState shelf) { this.fromShelf = shelf; }

    public void setExpireTime(LocalDate date) {
        this.expireTime = date;
    }

    public boolean isExpired(LocalDate nowDate) {
        return expireTime.isBefore(nowDate);
    }

    public void addPath(LibraryTrace path) {
        this.path.add(path);
    }

    public void setDeadline(LocalDate date) {
        this.deadline = date;
    }

    public int isDue(LocalDate date) {
        return (int) ChronoUnit.DAYS.between(deadline, date);
    }
}
