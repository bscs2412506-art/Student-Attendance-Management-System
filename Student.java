import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

class Student extends User {
    String classSection;
    Student(String id, String name, String email, String phone, String password, String classSection) {
        super(id, name, email, phone, password);
        this.classSection = classSection;
    }
}