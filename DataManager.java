import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;


public class DataManager {

    static List<User> loadUsers() throws IOException {
        File file = new File("src/users.txt");
        List<User> users = new ArrayList<>();
        if (!file.exists()) {
            file.createNewFile();
            return users;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6) continue;
                String type = parts[0].trim();
                String id = parts[1].trim();
                String name = parts[2].trim();
                String email = parts[3].trim();
                String phone = parts[4].trim();
                String password = parts[5].trim();
                if (type.equals("student") && parts.length >= 7) {
                    String classSection = parts[6].trim();
                    users.add(new Student(id, name, email, phone, password, classSection));
                } else if (type.equals("teacher")) {
                    users.add(new Teacher(id, name, email, phone, password));
                } else if (type.equals("admin")) {
                    users.add(new Administrator(id, name, email, phone, password));
                }
            }
        }
        return users;
    }


    static List<Course> loadCourses(List<User> users) throws IOException {
        File file = new File("src/courses.txt");
        List<Course> courses = new ArrayList<>();
        if (!file.exists()) {
            file.createNewFile();
            return courses;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4) {
                    continue;
                }
                String type = parts[0].trim();
                if (!type.equals("course")) {
                    continue;
                }
                String courseId = parts[1].trim();
                String courseName = parts[2].trim();
                String teacherId = parts[3].trim();
                User teacher = users.stream()
                        .filter(u -> u.id.equals(teacherId) && u instanceof Teacher)
                        .findFirst().orElse(null);
                if (teacher == null) {
                    continue;
                }
                Course course = new Course(courseId, courseName, teacher);
                // Add students
                for (int i = 4; i < parts.length; i++) {
                    String studentId = parts[i].trim();
                    User student = users.stream()
                            .filter(u -> u.id.equals(studentId) && u instanceof Student)
                            .findFirst().orElse(null);
                    if (student != null) {
                        course.students.add((Student) student);
                    }
                }
                courses.add(course);
            }
        }
        return courses;
    }


    static void loadSessions(List<Course> courses) throws IOException {
        File file = new File("src/sessions.txt");
        if (!file.exists()) {
            file.createNewFile();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length != 4 || !parts[0].trim().equals("session")) continue;
                String courseId = parts[1].trim();
                String sessionId = parts[2].trim();
                String dateStr = parts[3].trim();
                Course course = courses.stream()
                        .filter(c -> c.courseId.equals(courseId))
                        .findFirst().orElse(null);
                if (course != null) {
                    try {
                        LocalDate date = LocalDate.parse(dateStr);
                        course.sessions.add(new Session(sessionId, date));
                    } catch (DateTimeParseException e) {
                    }
                }
            }
        }
    }


    static void loadAttendance(List<Course> courses, List<User> users) throws IOException {
        File file = new File("src/attendance.txt");
        if (!file.exists()) {
            file.createNewFile();
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 4 || !parts[0].trim().equals("attendance")) continue;
                String sessionId = parts[1].trim();
                String studentId = parts[2].trim();
                String statusStr = parts[3].trim();
                String checkIn = parts.length > 4 ? parts[4].trim() : "";
                String checkOut = parts.length > 5 ? parts[5].trim() : "";
                String remarks = parts.length > 6 ? parts[6].trim() : "";
                Student student = (Student) users.stream()
                        .filter(u -> u.id.equals(studentId) && u instanceof Student)
                        .findFirst().orElse(null);
                if (student == null) {
                    continue;
                }
                for (Course c : courses) {
                    for (Session s : c.sessions) {
                        if (s.sessionId.equals(sessionId)) {
                            try {
                                AttendanceRecord ar = new AttendanceRecord(student, AttendanceStatus.valueOf(statusStr.toUpperCase()));
                                if (!checkIn.isEmpty()) ar.checkInTime = LocalTime.parse(checkIn);
                                if (!checkOut.isEmpty()) ar.checkOutTime = LocalTime.parse(checkOut);
                                if (!remarks.isEmpty()) ar.remarks = remarks;
                                s.attendanceRecords.add(ar);
                            } catch (IllegalArgumentException e) {
                            }
                        }
                    }
                }
            }
        }
    }


    static List<Request> loadRequests(List<User> users, List<Course> courses) throws IOException {
        File file = new File("src/requests.txt");
        List<Request> requests = new ArrayList<>();
        if (!file.exists()) {
            file.createNewFile();
            return requests;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length < 6 || !parts[0].trim().equals("request")) continue;
                String requestId = parts[1].trim();
                String studentId = parts[2].trim();
                String sessionId = parts[3].trim();
                String requestedStatus = parts[4].trim();
                String reason = parts[5].trim();
                Student student = (Student) users.stream()
                        .filter(u -> u.id.equals(studentId) && u instanceof Student)
                        .findFirst().orElse(null);
                if (student == null) continue;
                Session session = null;
                for (Course c : courses) {
                    for (Session s : c.sessions) {
                        if (s.sessionId.equals(sessionId)) {
                            session = s;
                            break;
                        }
                    }
                    if (session != null) break;
                }
                if (session == null) continue;
                try {
                    requests.add(new Request(requestId, student, session, AttendanceStatus.valueOf(requestedStatus.toUpperCase()), reason));
                } catch (IllegalArgumentException e) {
                }
            }
        }
        return requests;
    }


    static void saveUsers(List<User> users) throws IOException {
        File file = new File("src/users.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (User u : users) {
                if (u instanceof Student) {
                    Student s = (Student) u;
                    bw.write(String.format("student,%s,%s,%s,%s,%s,%s%n",
                            s.id, s.name, s.email, s.phone, s.password, s.classSection));
                } else if (u instanceof Teacher) {
                    bw.write(String.format("teacher,%s,%s,%s,%s,%s%n",
                            u.id, u.name, u.email, u.phone, u.password));
                } else if (u instanceof Administrator) {
                    bw.write(String.format("admin,%s,%s,%s,%s,%s%n",
                            u.id, u.name, u.email, u.phone, u.password));
                }
            }
        }
    }


    static void saveCourses(List<Course> courses) throws IOException {
        File file = new File("src/courses.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Course c : courses) {
                StringBuilder line = new StringBuilder(String.format("course,%s,%s,%s",
                        c.courseId, c.courseName, c.teacher != null ? c.teacher.id : ""));
                for (Student s : c.students) {
                    line.append(",").append(s.id);
                }
                bw.write(line + "\n");
            }
        }
    }


    static void saveSessions(List<Course> courses) throws IOException {
        File file = new File("src/sessions.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Course c : courses) {
                for (Session s : c.sessions) {
                    bw.write(String.format("session,%s,%s,%s%n",
                            c.courseId, s.sessionId, s.date));
                }
            }
        }
    }


    static void saveAttendance(List<Course> courses) throws IOException {
        File file = new File("src/attendance.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Course c : courses) {
                for (Session s : c.sessions) {
                    for (AttendanceRecord ar : s.attendanceRecords) {
                        bw.write(String.format("attendance,%s,%s,%s,%s,%s,%s%n",
                                s.sessionId, ar.student.id, ar.status,
                                ar.checkInTime != null ? ar.checkInTime : "",
                                ar.checkOutTime != null ? ar.checkOutTime : "",
                                ar.remarks != null ? ar.remarks : ""));
                    }
                }
            }
        }
    }


    static void saveRequests(List<Request> requests) throws IOException {
        File file = new File("src/requests.txt");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            for (Request r : requests) {
                bw.write(String.format("request,%s,%s,%s,%s,%s%n",
                        r.requestId, r.student.id, r.session.sessionId, r.requestedStatus, r.reason));
            }
        }
    }
}