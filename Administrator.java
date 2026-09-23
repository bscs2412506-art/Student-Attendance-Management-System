import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

enum AttendanceStatus { PRESENT, ABSENT, LATE, EXCUSED }
enum RequestStatus { PENDING, APPROVED, DENIED }

class Administrator extends User {
    Administrator(String id, String name, String email, String phone, String password) {
        super(id, name, email, phone, password);
    }
}