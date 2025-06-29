public class MainClass {
    public static void main(String[] args) {
        Bookshelf bookshelf = Bookshelf.getInstance();
        BorrowReturnOffice borrowOffice = new BorrowReturnOffice();
        AppointmentOffice appointmentOffice = new AppointmentOffice();
        Library library = new Library(bookshelf, borrowOffice, appointmentOffice);
        library.run();
    }
}
