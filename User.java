import java.time.LocalTime;
import java.time.LocalDate;
import java.util.*;
import java.io.*;

abstract class User {
    String id, name, email, phone, password;
    User(String id, String name, String email, String phone, String password) {
        this.id = id; this.name = name; this.email = email; this.phone = phone; this.password = password;
    }
}