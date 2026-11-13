package StudentManagementSystem;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.studentmanagement.config.SupabaseClient;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class GradeService {

    public static final double PASSING_SCORE = 75.0;

    static List<GradeRecord> fetchRecords(List<Student> students) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private GradeService(){
    }

    public static final class Student {
        public final int id;
        public final String firstName;
        public final String lastName;

        public Student(int id, String firstName, String lastName) {
            this.id = id;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String fullName() {
            return firstName + " " + lastName;
        }

        @Override
        public String toString() {
            return id + " - " + fullName();
        }
    }

    //ENROLLMENT WITH GRADE
    public static final class GradeRecord {
        public final int studentId;
        public final String studentName;
        public final String subject;
        /** null when the enrollment has no grade yet. */
        public final Double grade;

        public GradeRecord(int studentId, String studentName,
                           String subject, Double grade) {
            this.studentId = studentId;
            this.studentName = studentName;
            this.subject = subject;
            this.grade = grade;
        }

        public boolean isGraded() {
            return grade != null;
        }

        public String gradeText() {
            return isGraded() ? String.format("%.2f", grade) : "";
        }

        public String equivalent() {
            return isGraded() ? GradeService.equivalent(grade) : "-";
        }

        public String remarks() {
            return isGraded() ? GradeService.remarks(grade) : "Pending";
        }
    }


    //READS

    public static List<Student> fetchStudents() throws Exception {
        JsonArray rows = getArray(
            "/rest/v1/students?select=id,first_name,last_name&order=id.asc"
        );

        List<Student> students = new ArrayList<>();
        for (JsonElement el : rows) {
            JsonObject o = el.getAsJsonObject();
            students.add(new Student(
                o.get("id").getAsInt(),
                text(o, "first_name"),
                text(o, "last_name")
            ));
        }
        return students;
    }


    //SAVES GRADE, UPDATE EXISTING ROW
    public static boolean saveGrade(int studentId, String subject,
                                    double grade) throws Exception {

        validateScore(grade);

        String filter = "student_id=eq." + studentId
                + "&subject=eq." + encode(subject);

        JsonArray existing = getArray(
            "/rest/v1/grades?select=student_id&" + filter
        );

        JsonObject body = new JsonObject();
        body.addProperty("grade", grade);

        if (existing.size() > 0) {
            SupabaseClient.patch("/rest/v1/grades?" + filter, body);
            return true;
        }

        body.addProperty("student_id", studentId);
        body.addProperty("subject", subject);
        SupabaseClient.post("/rest/v1/grades", body);
        return false;
    }

    //GRADE COMPUTATION
    public static void validateScore(double score) {
        if (Double.isNaN(score) || score < 0 || score > 100) {
            throw new IllegalArgumentException(
                "Grade must be between 0 and 100."
            );
        }
    }

     public static String equivalent(double score) {
        long s = Math.round(score);
        if (s >= 97) return "1.00";
        if (s >= 94) return "1.25";
        if (s >= 91) return "1.50";
        if (s >= 88) return "1.75";
        if (s >= 85) return "2.00";
        if (s >= 82) return "2.25";
        if (s >= 79) return "2.50";
        if (s >= 76) return "2.75";
        if (s >= 75) return "3.00";
        return "5.00";
    }

    public static String remarks(double score) {
        return Math.round(score) >= PASSING_SCORE ? "Passed" : "Failed";
    }

    // AVERAGE 0-100
    public static double averageScore(List<GradeRecord> records) {
        double total = 0;
        int count = 0;
        for (GradeRecord r : records) {
            if (r.isGraded()) {
                total += r.grade;
                count++;
            }
        }
        return count == 0 ? Double.NaN : total / count;
    }

    public static double gwa(List<GradeRecord> records) {
        double total = 0;
        int count = 0;
        for (GradeRecord r : records) {
            if (r.isGraded()) {
                total += Double.parseDouble(r.equivalent());
                count++;
            }
        }
        return count == 0 ? Double.NaN : total / count;
    }

    private static JsonArray getArray(String path) throws Exception {
        return JsonParser.parseString(SupabaseClient.get(path)).getAsJsonArray();
    }

    private static String key(int studentId, String subject) {
        return studentId + "\u0000" + subject;
    }

    private static String text(JsonObject o, String key) {
        return !o.has(key) || o.get(key).isJsonNull()
                ? "" : o.get(key).getAsString();
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }
}