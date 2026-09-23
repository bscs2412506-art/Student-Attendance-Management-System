import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

class AttendanceRecord {
    Student student;
    AttendanceStatus status;
    LocalTime checkInTime, checkOutTime;
    String remarks;
    AttendanceRecord(Student student, AttendanceStatus status) {
        this.student = student; this.status = status;
    }
}