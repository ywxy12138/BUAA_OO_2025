import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;

public class Book {
    private LibraryBookId id;
    private LocalDate appointTime;
    private LocalDate expireTime;
    private ArrayList<LibraryTrace> path;

    public Book(LibraryBookId id) {
        this.id = id;
        this.appointTime = LocalDate.now();
        this.expireTime = LocalDate.now().plusDays(1);
        this.path = new ArrayList<>();
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

    public void setAppointTime(LocalDate date) {
        this.appointTime = date;
    }

    public void setExpireTime(LocalDate date) {
        this.expireTime = date;
    }

    public boolean isExpired(LocalDate nowDate) {
        return expireTime.isBefore(nowDate);
    }

    public void addPath(LibraryTrace path) {
        this.path.add(path);
    }
}
