import java.time.LocalTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.io.*;


public class AttendanceManagementSystem {
    static List<User> users = new ArrayList<>();
    static List<Course> courses = new ArrayList<>();
    static List<Request> requests = new ArrayList<>();
    static double minAttendance = 75.0;
    static Scanner scanner = new Scanner(System.in);


    public static void main(String[] args) throws IOException {
        // Load all data from files
        users = DataManager.loadUsers();
        courses = DataManager.loadCourses(users);
        DataManager.loadSessions(courses);
        DataManager.loadAttendance(courses, users);
        requests = DataManager.loadRequests(users, courses);

        while (true) {
            System.out.println("Select your role (student/teacher/admin): ");
            String role = scanner.nextLine().trim().toLowerCase();
            User user = null;

            switch (role) {
                case "student":
                    user = users.stream()
                            .filter(u -> u instanceof Student)
                            .findFirst()
                            .orElse(new Student("S1", "John Doe", "Doe@example.com", "2384510934", "", "CS101"));
                    studentMenu((Student) user);
                    break;
                case "teacher":
                    user = users.stream()
                            .filter(u -> u instanceof Teacher)
                            .findFirst()
                            .orElse(new Teacher("T1", "Prof Smith", "smith@example.com", "0987654321", ""));
                    teacherMenu((Teacher) user);
                    break;
                case "admin":
                    user = new Administrator("A1", "Admin", "admin@example.com", "1112223333", "");
                    adminMenu((Administrator) user);
                    break;
                default:
                    System.out.println("Invalid role. Please enter student, teacher, or admin.");
                    continue;
            }

            System.out.println("Exit program? (yes/no): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                DataManager.saveUsers(users);
                if (!courses.isEmpty()) DataManager.saveCourses(courses);
                DataManager.saveSessions(courses);
                DataManager.saveAttendance(courses);
                DataManager.saveRequests(requests);
                break;
            }
        }
    }


    static void studentMenu(Student student) {
        checkAttendanceNotifications(student);
        while (true) {
            System.out.println("\nStudent Menu:\n1. View Attendance\n2. Request Correction\n3. Update Info\n4. View Request Status\n5. Logout");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1:
                        viewAttendance(student);
                        checkAttendanceNotifications(student);
                        break;
                    case 2: requestCorrection(student); break;
                    case 3: updatePersonalInfo(student); break;
                    case 4: viewRequestStatus(student); break;
                    case 5: return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    static void teacherMenu(Teacher teacher) {
        while (true) {
            System.out.println("\nTeacher Menu:\n1. Mark Attendance\n2. View Attendance\n3. Modify Attendance\n4. Generate Report\n5. Send Alert\n6. Logout");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1: markAttendance(teacher); break;
                    case 2: viewClassAttendance(teacher); break;
                    case 3: modifyAttendance(teacher); break;
                    case 4: generateReport(teacher); break;
                    case 5: sendAlert(teacher); break;
                    case 6: return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }

    static void adminMenu(Administrator admin) {
        while (true) {
            System.out.println("\nAdmin Menu:\n1. Manage Users\n2. View Analytics\n3. Configure Rules\n4. Handle Requests\n5. Logout");
            try {
                int choice = Integer.parseInt(scanner.nextLine().trim());
                switch (choice) {
                    case 1: manageUsers(); break;
                    case 2: viewAnalytics(); break;
                    case 3: configureRules(); break;
                    case 4: handleRequests(); break;
                    case 5: return;
                    default: System.out.println("Invalid choice.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Please enter a number.");
            }
        }
    }


    static void viewAttendance(Student student) {
        boolean hasRecords = false;
        for (Course c : courses) {
            if (c.students.contains(student)) {
                System.out.println("Course: " + c.courseName + ", Section: " + student.classSection);
                for (Session s : c.sessions) {
                    for (AttendanceRecord ar : s.attendanceRecords) {
                        if (ar.student.equals(student)) {
                            hasRecords = true;
                            System.out.printf("Date: %s, Status: %s, Check-in: %s, Check-out: %s, Remarks: %s%n",
                                    s.date, ar.status,
                                    ar.checkInTime != null ? ar.checkInTime : "N/A",
                                    ar.checkOutTime != null ? ar.checkOutTime : "N/A",
                                    ar.remarks != null ? ar.remarks : "None");
                        }
                    }
                }
            }
        }
        if (!hasRecords) {
            System.out.println("No attendance records found.");
        }
    }


    static void viewClassAttendance(Teacher teacher) {
        System.out.println("Enter Course ID: ");
        String courseId = scanner.nextLine().trim();
        Course course = courses.stream().filter(c -> c.courseId.equals(courseId) && (c.teacher != null && c.teacher.equals(teacher))).findFirst().orElse(null);
        if (course == null) {
            System.out.println("Invalid Course ID or not assigned to you.");
            return;
        }
        if (course.sessions.isEmpty()) {
            System.out.println("No sessions found for this course.");
            return;
        }
        for (Session s : course.sessions) {
            System.out.println("Session: " + s.sessionId + ", Date: " + s.date);
            for (AttendanceRecord ar : s.attendanceRecords) {
                System.out.printf("Student: %s, Status: %s, Remarks: %s%n",
                        ar.student.name, ar.status, ar.remarks != null ? ar.remarks : "None");
            }
        }
    }


    static void modifyAttendance(Teacher teacher) {
        System.out.println("Enter Course ID: ");
        String courseId = scanner.nextLine().trim();
        Course course = courses.stream().filter(c -> c.courseId.equals(courseId) && (c.teacher != null && c.teacher.equals(teacher))).findFirst().orElse(null);
        if (course == null) {
            System.out.println("Invalid Course ID or not assigned to you.");
            return;
        }
        System.out.println("Enter Session ID: ");
        String sessionId = scanner.nextLine().trim();
        Session session = course.sessions.stream().filter(s -> s.sessionId.equals(sessionId)).findFirst().orElse(null);
        if (session == null) {
            System.out.println("Invalid Session ID.");
            return;
        }
        System.out.println("Enter Student ID: ");
        String studentId = scanner.nextLine().trim();
        AttendanceRecord record = session.attendanceRecords.stream()
                .filter(ar -> ar.student.id.equals(studentId)).findFirst().orElse(null);
        if (record == null) {
            System.out.println("No record found for student.");
            return;
        }
        System.out.println("Current Status: " + record.status);
        System.out.println("Enter New Status (PRESENT/ABSENT/LATE/EXCUSED): ");
        try {
            record.status = AttendanceStatus.valueOf(scanner.nextLine().trim().toUpperCase());
            System.out.println("Enter New Remarks (or leave blank): ");
            String remarks = scanner.nextLine().trim();
            if (!remarks.isEmpty()) record.remarks = remarks;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status.");
        }
    }


    static void generateReport(Teacher teacher) {
        System.out.println("Enter Course ID: ");
        String courseId = scanner.nextLine().trim();
        Course course = courses.stream().filter(c -> c.courseId.equals(courseId) && (c.teacher != null && c.teacher.equals(teacher))).findFirst().orElse(null);
        if (course == null) {
            System.out.println("Invalid Course ID or not assigned to you.");
            return;
        }
        System.out.println("Enter Start Date (YYYY-MM-DD): ");
        LocalDate start;
        try {
            start = LocalDate.parse(scanner.nextLine().trim());
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format (use YYYY-MM-DD).");
            return;
        }
        System.out.println("Enter End Date (YYYY-MM-DD): ");
        LocalDate end;
        try {
            end = LocalDate.parse(scanner.nextLine().trim());
            if (end.isBefore(start)) {
                System.out.println("End date must be after start date.");
                return;
            }
        } catch (DateTimeParseException e) {
            System.out.println("Invalid date format (use YYYY-MM-DD).");
            return;
        }
        for (Student s : course.students) {
            int total = 0, present = 0;
            for (Session ses : course.sessions) {
                if (!ses.date.isBefore(start) && !ses.date.isAfter(end)) {
                    total++;
                    for (AttendanceRecord ar : ses.attendanceRecords) {
                        if (ar.student.equals(s) && ar.status == AttendanceStatus.PRESENT) {
                            present++;
                        }
                    }
                }
            }
            double percentage = total > 0 ? (present * 100.0 / total) : 0;
            System.out.printf("Student: %s, Attendance: %.2f%% (%d/%d)%n",
                    s.name, percentage, present, total);
        }
    }

    static void sendAlert(Teacher teacher) {
        System.out.println("Enter Course ID: ");
        String courseId = scanner.nextLine().trim();
        Course course = courses.stream().filter(c -> c.courseId.equals(courseId) && (c.teacher != null && c.teacher.equals(teacher))).findFirst().orElse(null);
        if (course == null) {
            System.out.println("Invalid Course ID or not assigned to you.");
            return;
        }
        for (Student s : course.students) {
            int total = course.sessions.size();
            int present = 0;
            for (Session ses : course.sessions) {
                for (AttendanceRecord ar : ses.attendanceRecords) {
                    if (ar.student.equals(s) && ar.status == AttendanceStatus.PRESENT) {
                        present++;
                    }
                }
            }
            double percentage = total > 0 ? (present * 100.0 / total) : 0;
            if (percentage < minAttendance) {
                System.out.printf("Alert: %s has low attendance in %s (%.2f%%).%n",
                        s.name, course.courseName, percentage);
            }
        }
    }


    static void requestCorrection(Student student) {
        System.out.println("Enter Session ID: ");
        String sessionId = scanner.nextLine().trim();
        Session session = findSession(sessionId);
        if (session == null) {
            System.out.println("Invalid Session ID.");
            return;
        }
        System.out.println("Enter Requested Status (PRESENT/ABSENT/LATE/EXCUSED): ");
        try {
            AttendanceStatus status = AttendanceStatus.valueOf(scanner.nextLine().trim().toUpperCase());
            System.out.println("Enter Reason: ");
            String reason = scanner.nextLine().trim();
            requests.add(new Request("R" + (requests.size() + 1), student, session, status, reason));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid status entered.");
        }
    }


    static void checkAttendanceNotifications(Student student) {
        for (Course c : courses) {
            if (c.students.contains(student)) {
                int totalSessions = c.sessions.size();
                int presentSessions = 0;
                for (Session s : c.sessions) {
                    for (AttendanceRecord ar : s.attendanceRecords) {
                        if (ar.student.equals(student) && ar.status == AttendanceStatus.PRESENT) {
                            presentSessions++;
                        }
                    }
                }
                double attendancePercentage = totalSessions > 0 ? (presentSessions * 100.0 / totalSessions) : 0;
                if (attendancePercentage < minAttendance) {
                    System.out.printf("Warning: Low attendance in %s (%.2f%%). Minimum required: %.2f%%%n",
                            c.courseName, attendancePercentage, minAttendance);
                }
            }
        }
    }


    static void viewRequestStatus(Student student) {
        boolean hasRequests = false;
        for (Request r : requests) {
            if (r.student.equals(student)) {
                hasRequests = true;
                System.out.printf("Request %s for Session %s: Status %s, Requested %s, Reason: %s%n",
                        r.requestId, r.session.sessionId, r.status, r.requestedStatus, r.reason);
            }
        }
        if (!hasRequests) {
            System.out.println("No correction requests found.");
        }
    }


    static void updatePersonalInfo(Student student) {
        System.out.println("New Email: ");
        String email = scanner.nextLine().trim();
        if (!email.isEmpty() && email.contains("@")) {
            student.email = email;
        } else if (!email.isEmpty()) {
            System.out.println("Invalid email format. Keeping current email.");
        }
        System.out.println("New Phone: ");
        String phone = scanner.nextLine().trim();
        if (!phone.isEmpty() && phone.matches("\\d+")) {
            student.phone = phone;
        } else if (!phone.isEmpty()) {
            System.out.println("Invalid phone number. Keeping current phone.");
        }
    }


    static void markAttendance(Teacher teacher) {
        System.out.println("Enter Course ID: ");
        String courseId = scanner.nextLine().trim();
        Course course = courses.stream()
                .filter(c -> c.courseId.equals(courseId) && (c.teacher != null && c.teacher.equals(teacher)))
                .findFirst()
                .orElse(null);
        if (course == null) {
            System.out.println("Invalid Course ID or not assigned to you.");
            return;
        }
        Session session = new Session("S" + (course.sessions.size() + 1), LocalDate.now());
        for (Student s : course.students) {
            System.out.println("Status for " + s.name + " (PRESENT/ABSENT/LATE/EXCUSED): ");
            try {
                AttendanceStatus status = AttendanceStatus.valueOf(scanner.nextLine().trim().toUpperCase());
                AttendanceRecord ar = new AttendanceRecord(s, status);
                if (status == AttendanceStatus.PRESENT || status == AttendanceStatus.LATE) {
                    System.out.println("Enter Check-in Time (HH:MM, or leave blank): ");
                    String checkIn = scanner.nextLine().trim();
                    if (!checkIn.isEmpty()) {
                        try {
                            ar.checkInTime = LocalTime.parse(checkIn);
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid time format. Skipping check-in time.");
                        }
                    }
                    System.out.println("Enter Check-out Time (HH:MM, or leave blank): ");
                    String checkOut = scanner.nextLine().trim();
                    if (!checkOut.isEmpty()) {
                        try {
                            ar.checkOutTime = LocalTime.parse(checkOut);
                        } catch (DateTimeParseException e) {
                            System.out.println("Invalid time format. Skipping check-out time.");
                        }
                    }
                }
                System.out.println("Enter Remarks (or leave blank): ");
                String remarks = scanner.nextLine().trim();
                if (!remarks.isEmpty()) ar.remarks = remarks;
                session.attendanceRecords.add(ar);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid status. Skipping student.");
            }
        }
        course.sessions.add(session);
    }


    static void manageUsers() {
        System.out.println("1. Add User\n2. Update User\n3. Delete User");
        try {
            int choice = Integer.parseInt(scanner.nextLine().trim());
            if (choice == 1) {
                System.out.println("Enter Role (student/teacher/admin): ");
                String role = scanner.nextLine().trim().toLowerCase();
                System.out.println("Enter ID: ");
                String id = scanner.nextLine().trim();
                if (users.stream().anyMatch(u -> u.id.equals(id))) {
                    System.out.println("ID already exists.");
                    return;
                }
                System.out.println("Enter Name: ");
                String name = scanner.nextLine().trim();
                System.out.println("Enter Email: ");
                String email = scanner.nextLine().trim();
                System.out.println("Enter Phone: ");
                String phone = scanner.nextLine().trim();
                if (role.equals("student")) {
                    System.out.println("Enter Class Section: ");
                    String classSection = scanner.nextLine().trim();
                    users.add(new Student(id, name, email, phone, "", classSection));
                } else if (role.equals("teacher")) {
                    users.add(new Teacher(id, name, email, phone, ""));
                } else if (role.equals("admin")) {
                    users.add(new Administrator(id, name, email, phone, ""));
                } else {
                    System.out.println("Invalid role.");
                    return;
                }
                System.out.println("User added.");
            } else if (choice == 2) {
                System.out.println("Enter User ID: ");
                String id = scanner.nextLine().trim();
                User user = users.stream().filter(u -> u.id.equals(id)).findFirst().orElse(null);
                if (user == null) {
                    System.out.println("User not found.");
                    return;
                }
                System.out.println("New Email: ");
                String email = scanner.nextLine().trim();
                if (!email.isEmpty() && email.contains("@")) {
                    user.email = email;
                }
                System.out.println("New Phone: ");
                String phone = scanner.nextLine().trim();
                if (!phone.isEmpty() && phone.matches("\\d+")) {
                    user.phone = phone;
                }
                System.out.println("User updated.");
            } else if (choice == 3) {
                System.out.println("Enter User ID: ");
                String id = scanner.nextLine().trim();
                User user = users.stream().filter(u -> u.id.equals(id)).findFirst().orElse(null);
                if (user != null) {
                    users.remove(user);
                    System.out.println("User deleted.");
                } else {
                    System.out.println("User not found.");
                }
            } else {
                System.out.println("Invalid choice.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Please enter a number.");
        }
    }


    static void viewAnalytics() {
        if (courses.isEmpty()) {
            System.out.println("No courses found.");
            return;
        }
        for (Course c : courses) {
            int totalSessions = c.sessions.size();
            int totalPresent = 0;
            for (Session s : c.sessions) {
                for (AttendanceRecord ar : s.attendanceRecords) {
                    if (ar.status == AttendanceStatus.PRESENT) {
                        totalPresent++;
                    }
                }
            }
            double avgAttendance = totalSessions > 0 ? (totalPresent * 100.0 / (totalSessions * c.students.size())) : 0;
            System.out.printf("Course: %s, Total Sessions: %d, Average Attendance: %.2f%%\n",
                    c.courseName, totalSessions, avgAttendance);
        }
    }


    static void configureRules() {
        System.out.println("Enter Minimum Attendance Percentage: ");
        try {
            double percentage = Double.parseDouble(scanner.nextLine().trim());
            if (percentage >= 0 && percentage <= 100) {
                minAttendance = percentage;
                System.out.println("Minimum attendance set to " + percentage + "%.");
            } else {
                System.out.println("Percentage must be between 0 and 100.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format.");
        }
    }


    static void handleRequests() {
        if (requests.isEmpty()) {
            System.out.println("No requests found.");
            return;
        }
        for (Request r : requests) {
            if (r.status == RequestStatus.PENDING) {
                System.out.printf("Request %s: Student %s, Session %s, Requested %s, Reason: %s\n",
                        r.requestId, r.student.name, r.session.sessionId, r.requestedStatus, r.reason);
                System.out.println("Approve? (yes/no): ");
                if (scanner.nextLine().trim().equalsIgnoreCase("yes")) {
                    r.status = RequestStatus.APPROVED;
                    // Update attendance record
                    for (Course c : courses) {
                        for (Session s : c.sessions) {
                            if (s.sessionId.equals(r.session.sessionId)) {
                                for (AttendanceRecord ar : s.attendanceRecords) {
                                    if (ar.student.equals(r.student)) {
                                        ar.status = r.requestedStatus;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    r.status = RequestStatus.DENIED;
                }
            }
        }
    }


    static Session findSession(String sessionId) {
        for (Course c : courses) {
            for (Session s : c.sessions) {
                if (s.sessionId.equals(sessionId)) {
                    return s;
                }
            }
        }
        return null;
    }
}