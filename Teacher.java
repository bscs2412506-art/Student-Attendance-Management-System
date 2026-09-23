import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

class Teacher extends User {
    List<Course> classesTaught = new ArrayList<>();
    Teacher(String id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }
}