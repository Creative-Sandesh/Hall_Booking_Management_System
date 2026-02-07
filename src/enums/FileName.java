package enums;


public enum FileName {
    // pointing all txt file of data folder

    USERS("data\\users.txt"),
    HALLS("D:\\University_Assignments\\3n Sem\\Oop With Java\\Hall Booking Management System\\Hall Booking Management System\\data\\halls.txt"),
    BOOKINGS("D:\\University_Assignments\\3n Sem\\Oop With Java\\Hall Booking Management System\\Hall Booking Management System\\data\\bookings.txt"),
    SCHEDULES("data\\schedules.txt"),
    ISSUES("data\\issues.txt");
    private final String filename;

    // constructor
    FileName(String filename){
        this.filename = filename;
    }

    // getter file
    public String getFilename(){
        return filename;
    }

}
