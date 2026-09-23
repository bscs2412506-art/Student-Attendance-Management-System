import java.util.ArrayList;
import java.util.List;


public class Course {
    String courseId;
    String courseName;
    User teacher; // The teacher assigned to the course
    List<Student> students = new ArrayList<>();
    List<Session> sessions = new ArrayList<>();


    public Course(String courseId, String courseName, User teacher) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.teacher = teacher;
    }
}