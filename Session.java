import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

class Session {
    String sessionId;
    LocalDate date;
    List<AttendanceRecord> attendanceRecords = new ArrayList<>();
    Session(String sessionId, LocalDate date) {
        this.sessionId = sessionId; this.date = date;
    }
}