/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package StudentManagementSystem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.BorderLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import java.awt.CardLayout;
import com.studentmanagement.config.AuthService;
import com.studentmanagement.config.SupabaseClient;
import java.util.ArrayList;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedWriter;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import javax.swing.JFileChooser;
import java.net.URLEncoder;
/**
 *
 * @author jamesandrewwalker
 */
public class StudentManagementSystem extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StudentManagementSystem.class.getName());

    private final List<String> subjectNames = new ArrayList<>();
    // Stores the grade records loaded from Supabase.
    private final List<Object[]> gradeRows = new ArrayList<>();

// Prevents unnecessary dropdown events while loading students.
    private boolean loadingGradeStudents = false;
    
    // Stores enrollment records currently loaded from Supabase.
    private final List<Object[]> enrollmentRows = new ArrayList<>();

    // Stores the generated report information.
    private static class ReportData {
    
    int totalStudents;
    int totalEnrollments;
    int totalGrades;
    int pendingGrades;

    List<Object[]> rows = new ArrayList<>();
}
    

    public StudentManagementSystem() {
    initComponents();
    
    
    jTable4.getSelectionModel().addListSelectionListener(e -> {

    if (!e.getValueIsAdjusting()) {
        loadSelectedEnrollmentIntoForm();
    }

});
    
    
    initializeEnrollmentControls();
    jComboBox2.addActionListener(e -> {
    if (!loadingGradeStudents) {
        loadGradeSubjects();
    }
});


 

    CardLayout cl = (CardLayout) getContentPane().getLayout();
    cl.first(getContentPane());

    setLocationRelativeTo(null);
}
    // Selects an existing item in a ComboBox.
// Adds the item if it does not exist.
private void selectComboItem(
        javax.swing.JComboBox<String> combo,
        String value) {

    for (int i = 0; i < combo.getItemCount(); i++) {

        if (combo.getItemAt(i).equalsIgnoreCase(value)) {
            combo.setSelectedIndex(i);
            return;
        }
    }

    combo.addItem(value);
    combo.setSelectedItem(value);
}


// Loads the selected JTable record into the enrollment form.
private void loadSelectedEnrollmentIntoForm() {

    int selectedRow = jTable4.getSelectedRow();

    if (selectedRow == -1) {
        return;
    }

    int row = jTable4.convertRowIndexToModel(selectedRow);

    String studentId =
            jTable4.getValueAt(row, 1).toString();

    String studentName =
            jTable4.getValueAt(row, 2).toString();

    String subject =
            jTable4.getValueAt(row, 3).toString();

    String academicYear =
            jTable4.getValueAt(row, 4).toString();

    String semester =
            jTable4.getValueAt(row, 5).toString();

    String enrollmentDate =
            jTable4.getValueAt(row, 6).toString();

    String status =
            jTable4.getValueAt(row, 7).toString();

    // Populate Student.
    selectComboItem(
        cmbStudent,
        studentId + " - " + studentName
    );

    // Populate Subject.
    selectComboItem(cmbSubject, subject);

    // Populate Academic Year.
    selectComboItem(cmbAcademicYear, academicYear);

    // Populate Semester.
    selectComboItem(cmbSemester, semester);

    // Populate Status.
    selectComboItem(cmbStatus, status);

    // Convert the database date into MM/DD/YYYY.
    try {

        LocalDate date = LocalDate.parse(
            enrollmentDate.substring(0, 10)
        );

        txtEnrollmentDate.setText(
            date.format(
                DateTimeFormatter.ofPattern("MM/dd/yyyy")
            )
        );

    } catch (Exception ex) {

        txtEnrollmentDate.setText(enrollmentDate);
    }
}
    // Sets the default options of the enrollment form.
private void initializeEnrollmentControls() {

    // Get the current year.
    int currentYear = LocalDate.now().getYear();

    // Academic year options.
    cmbAcademicYear.removeAllItems();

    for (int i = 0; i < 3; i++) {
        int startYear = currentYear + i;

        cmbAcademicYear.addItem(
            startYear + "-" + (startYear + 1)
        );
    }

    // Semester options.
    cmbSemester.removeAllItems();
    cmbSemester.addItem("First Semester");
    cmbSemester.addItem("Second Semester");
    cmbSemester.addItem("Summer");

    // Enrollment status options.
    cmbStatus.removeAllItems();
    cmbStatus.addItem("Pending");
    cmbStatus.addItem("Enrolled");
    cmbStatus.addItem("Dropped");

    // Default enrollment date.
    txtEnrollmentDate.setText(
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        )
    );

    // Remove the sample combo box entries.
    cmbStudent.removeAllItems();
    cmbSubject.removeAllItems();

    // Clear the search field.
    txtSearchEnrollment.setText("");
}
    private void showMainContent(String cardName) {

    CardLayout cl =
            (CardLayout) MainContentPanel.getLayout();

    cl.show(MainContentPanel, cardName);
}
    private void updateDashboardCounts() {

    SwingWorker<int[], Void> worker =
            new SwingWorker<int[], Void>() {

        @Override
        protected int[] doInBackground() throws Exception {

            JsonArray students = JsonParser.parseString(
                SupabaseClient.get(
                    "/rest/v1/students?select=id"
                )
            ).getAsJsonArray();

            JsonArray subjects = JsonParser.parseString(
                SupabaseClient.get(
                    "/rest/v1/subjects?select=name"
                )
            ).getAsJsonArray();

            JsonArray enrollments = JsonParser.parseString(
                SupabaseClient.get(
                    "/rest/v1/enrollments?select=student_id"
                )
            ).getAsJsonArray();

            JsonArray grades = JsonParser.parseString(
                SupabaseClient.get(
                    "/rest/v1/grades?select=student_id"
                )
            ).getAsJsonArray();

            int totalStudents = students.size();
            int totalSubjects = subjects.size();
            int totalEnrollments = enrollments.size();
            int pendingGrades = Math.max(
                0, totalEnrollments - grades.size()
            );

            return new int[]{
                totalStudents,
                totalSubjects,
                totalEnrollments,
                pendingGrades
            };
        }

        @Override
        protected void done() {
            try {
                int[] counts = get();

                jLabel5.setText(
                    counts[0] + " - Total Students"
                );

                jLabel6.setText(
                    counts[1] + " - Total Subjects"
                );

                jLabel7.setText(
                    counts[2] + " - Total Enrollments"
                );

                jLabel8.setText(
                    counts[3] + " - Pending Grades"
                );

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to retrieve Dashboard counts:\n"
                    + ex.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
    // Loads all students from Supabase into jTable2.
// If a search term is provided, filters the displayed records.
private void loadStudents(String searchText) {

    SwingWorker<List<Object[]>, Void> worker =
            new SwingWorker<List<Object[]>, Void>() {

        @Override
        protected List<Object[]> doInBackground() throws Exception {

            String response = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name,course,year_level&order=id.asc"
            );

            JsonArray students =
                    JsonParser.parseString(response).getAsJsonArray();

            List<Object[]> rows = new ArrayList<>();

            String search = searchText == null
                    ? ""
                    : searchText.trim().toLowerCase();

            for (int i = 0; i < students.size(); i++) {

                JsonObject student = students.get(i).getAsJsonObject();

                String id = student.get("id").getAsString();
                String firstName = student.get("first_name").getAsString();
                String lastName = student.get("last_name").getAsString();
                String course = student.get("course").getAsString();
                String yearLevel = student.get("year_level").getAsString();

                String searchableText = (
                    id + " " + firstName + " " + lastName
                    + " " + course + " " + yearLevel
                ).toLowerCase();

                if (search.isEmpty() || searchableText.contains(search)) {

                    rows.add(new Object[]{
                        id,
                        firstName,
                        lastName,
                        course,
                        yearLevel
                    });
                }
            }

            return rows;
        }

        @Override
        protected void done() {

            try {
                List<Object[]> rows = get();

                DefaultTableModel model =
                        new DefaultTableModel(
                            new String[]{
                                "Student ID",
                                "First Name",
                                "Last Name",
                                "Course",
                                "Year Level"
                            },
                            0
                        ) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };

                for (Object[] row : rows) {
                    model.addRow(row);
                }

                jTable2.setModel(model);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load student records:\n" + ex.getMessage(),
                    "Students Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}


// Loads the five most recently added students into jTable1.
private void loadRecentStudents() {

    SwingWorker<List<Object[]>, Void> worker =
            new SwingWorker<List<Object[]>, Void>() {

        @Override
        protected List<Object[]> doInBackground() throws Exception {

            String response = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name,course,year_level&order=id.desc&limit=5"
            );

            JsonArray students =
                    JsonParser.parseString(response).getAsJsonArray();

            List<Object[]> rows = new ArrayList<>();

            for (int i = 0; i < students.size(); i++) {

                JsonObject student = students.get(i).getAsJsonObject();

                rows.add(new Object[]{
                    student.get("id").getAsString(),
                    student.get("first_name").getAsString(),
                    student.get("last_name").getAsString(),
                    student.get("course").getAsString(),
                    student.get("year_level").getAsString()
                });
            }

            return rows;
        }

        @Override
        protected void done() {

            try {
                List<Object[]> rows = get();

                DefaultTableModel model =
                        new DefaultTableModel(
                            new String[]{
                                "Student ID",
                                "First Name",
                                "Last Name",
                                "Course",
                                "Year Level"
                            },
                            0
                        ) {
                            @Override
                            public boolean isCellEditable(int row, int column) {
                                return false;
                            }
                        };

                for (Object[] row : rows) {
                    model.addRow(row);
                }

                jTable1.setModel(model);

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load recent students:\n" + ex.getMessage(),
                    "Dashboard Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// ==========================================
// LOAD SUBJECTS FROM SUPABASE
// ==========================================

private void loadSubjects() {

    jButton7.setEnabled(false);

    SwingWorker<List<String>, Void> worker =
            new SwingWorker<List<String>, Void>() {

        @Override
        protected List<String> doInBackground() throws Exception {

            String response = SupabaseClient.get(
                "/rest/v1/subjects?select=name&order=name.asc"
            );

            JsonArray subjects =
                    JsonParser.parseString(response).getAsJsonArray();

            List<String> names = new ArrayList<>();

            for (int i = 0; i < subjects.size(); i++) {

                JsonObject subject =
                        subjects.get(i).getAsJsonObject();

                names.add(subject.get("name").getAsString());
            }

            return names;
        }

        @Override
        protected void done() {

            jButton7.setEnabled(true);

            try {
                subjectNames.clear();
                subjectNames.addAll(get());

                filterSubjects();

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load subjects:\n"
                    + cause.getMessage(),
                    "Subjects Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}


// ==========================================
// SEARCH SUBJECTS
// ==========================================

private void filterSubjects() {

    String search = jTextField8.getText()
            .trim()
            .toLowerCase();

    DefaultTableModel model = new DefaultTableModel(
        new String[]{"No.", "Subject Name"},
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    int number = 1;

    for (String subject : subjectNames) {

        if (subject.toLowerCase().contains(search)) {

            model.addRow(new Object[]{
                number++,
                subject
            });
        }
    }

    jTable3.setModel(model);
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        LoginPage = new javax.swing.JPanel();
        jTextField2 = new javax.swing.JTextField();
        jPasswordField2 = new javax.swing.JPasswordField();
        jButton2 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        SignUpPage = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jPasswordField3 = new javax.swing.JPasswordField();
        jButton3 = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        Dashboard = new javax.swing.JPanel();
        jButton5 = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jButton7 = new javax.swing.JButton();
        jButton8 = new javax.swing.JButton();
        jButton9 = new javax.swing.JButton();
        jButton10 = new javax.swing.JButton();
        jButton11 = new javax.swing.JButton();
        MainContentPanel = new javax.swing.JPanel();
        SideDashboard = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jButton12 = new javax.swing.JButton();
        jButton13 = new javax.swing.JButton();
        jButton14 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        Students = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton15 = new javax.swing.JButton();
        jButton16 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();
        Subjects = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        jButton19 = new javax.swing.JButton();
        jTextField8 = new javax.swing.JTextField();
        jButton20 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTable3 = new javax.swing.JTable();
        Enrollments = new javax.swing.JPanel();
        jLabel23 = new javax.swing.JLabel();
        jLabel24 = new javax.swing.JLabel();
        jLabel25 = new javax.swing.JLabel();
        jLabel26 = new javax.swing.JLabel();
        cmbSubject = new javax.swing.JComboBox<>();
        jLabel27 = new javax.swing.JLabel();
        cmbStudent = new javax.swing.JComboBox<>();
        jLabel28 = new javax.swing.JLabel();
        jLabel29 = new javax.swing.JLabel();
        cmbSemester = new javax.swing.JComboBox<>();
        cmbAcademicYear = new javax.swing.JComboBox<>();
        jLabel30 = new javax.swing.JLabel();
        jLabel31 = new javax.swing.JLabel();
        cmbStatus = new javax.swing.JComboBox<>();
        txtEnrollmentDate = new javax.swing.JTextField();
        btnAddEnrollment = new javax.swing.JButton();
        btnClearEnrollment = new javax.swing.JButton();
        jLabel32 = new javax.swing.JLabel();
        txtSearchEnrollment = new javax.swing.JTextField();
        btnSearchEnrollment = new javax.swing.JButton();
        tblEnrollments = new javax.swing.JScrollPane();
        jTable4 = new javax.swing.JTable();
        btnUpdateEnrollment = new javax.swing.JButton();
        Grades = new javax.swing.JPanel();
        jLabel40 = new javax.swing.JLabel();
        jLabel41 = new javax.swing.JLabel();
        jLabel42 = new javax.swing.JLabel();
        jLabel43 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jLabel44 = new javax.swing.JLabel();
        jComboBox3 = new javax.swing.JComboBox<>();
        jLabel45 = new javax.swing.JLabel();
        jTextField11 = new javax.swing.JTextField();
        jButton25 = new javax.swing.JButton();
        jLabel46 = new javax.swing.JLabel();
        jTextField12 = new javax.swing.JTextField();
        jButton26 = new javax.swing.JButton();
        jButton27 = new javax.swing.JButton();
        jScrollPane5 = new javax.swing.JScrollPane();
        jTable6 = new javax.swing.JTable();
        Reports = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jLabel34 = new javax.swing.JLabel();
        jLabel35 = new javax.swing.JLabel();
        jLabel36 = new javax.swing.JLabel();
        jLabel37 = new javax.swing.JLabel();
        jLabel38 = new javax.swing.JLabel();
        jLabel39 = new javax.swing.JLabel();
        jTextField10 = new javax.swing.JTextField();
        jButton23 = new javax.swing.JButton();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTable5 = new javax.swing.JTable();
        jButton24 = new javax.swing.JButton();
        AddStudentForm = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jTextField5 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel16 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        jButton17 = new javax.swing.JButton();
        jButton18 = new javax.swing.JButton();
        AddNewSubjectForm = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        jLabel20 = new javax.swing.JLabel();
        jLabel21 = new javax.swing.JLabel();
        jTextField9 = new javax.swing.JTextField();
        jButton21 = new javax.swing.JButton();
        jButton22 = new javax.swing.JButton();
        jLabel22 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new java.awt.CardLayout());

        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jButton2.setText("Login");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        jButton1.setText("SIgn Up");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Don't have an account?");

        javax.swing.GroupLayout LoginPageLayout = new javax.swing.GroupLayout(LoginPage);
        LoginPage.setLayout(LoginPageLayout);
        LoginPageLayout.setHorizontalGroup(
            LoginPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(LoginPageLayout.createSequentialGroup()
                .addGroup(LoginPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(LoginPageLayout.createSequentialGroup()
                        .addGap(347, 347, 347)
                        .addGroup(LoginPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPasswordField2)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 149, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(LoginPageLayout.createSequentialGroup()
                        .addGap(295, 295, 295)
                        .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 241, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(LoginPageLayout.createSequentialGroup()
                        .addGap(382, 382, 382)
                        .addComponent(jButton1)))
                .addContainerGap(322, Short.MAX_VALUE))
        );
        LoginPageLayout.setVerticalGroup(
            LoginPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, LoginPageLayout.createSequentialGroup()
                .addContainerGap(213, Short.MAX_VALUE)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPasswordField2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(23, 23, 23)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(101, 101, 101))
        );

        getContentPane().add(LoginPage, "card2");

        jButton3.setText("Sign Up");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Already have an account?");

        jButton4.setText("Login");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        javax.swing.GroupLayout SignUpPageLayout = new javax.swing.GroupLayout(SignUpPage);
        SignUpPage.setLayout(SignUpPageLayout);
        SignUpPageLayout.setHorizontalGroup(
            SignUpPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SignUpPageLayout.createSequentialGroup()
                .addGap(347, 347, 347)
                .addGroup(SignUpPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPasswordField3)
                    .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 149, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SignUpPageLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(SignUpPageLayout.createSequentialGroup()
                .addGap(367, 367, 367)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 108, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(383, Short.MAX_VALUE))
        );
        SignUpPageLayout.setVerticalGroup(
            SignUpPageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, SignUpPageLayout.createSequentialGroup()
                .addContainerGap(213, Short.MAX_VALUE)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jPasswordField3, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(34, 34, 34)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton4, javax.swing.GroupLayout.PREFERRED_SIZE, 34, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(72, 72, 72))
        );

        getContentPane().add(SignUpPage, "card3");

        jButton5.setText("Dashboard");
        jButton5.addActionListener(this::jButton5ActionPerformed);

        jButton6.setText("Students");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jButton7.setText("Subjects");
        jButton7.addActionListener(this::jButton7ActionPerformed);

        jButton8.setText("Enrollments");
        jButton8.addActionListener(this::jButton8ActionPerformed);

        jButton9.setText("Grades");
        jButton9.addActionListener(this::jButton9ActionPerformed);

        jButton10.setText("Reports");
        jButton10.addActionListener(this::jButton10ActionPerformed);

        jButton11.setText("Logout");
        jButton11.addActionListener(this::jButton11ActionPerformed);

        MainContentPanel.setLayout(new java.awt.CardLayout());

        jLabel3.setText("Dashboard Overview");

        jLabel4.setText("Welcome back! Here's your academic summary.");

        jLabel5.setText(" – Total Students");

        jLabel6.setText(" – Total Subjects");

        jLabel7.setText(" – Total Enrollments");

        jLabel8.setText(" – Pending Grades");

        jButton12.setText("Add Students");
        jButton12.addActionListener(this::jButton12ActionPerformed);

        jButton13.setText("Add Subjects");
        jButton13.addActionListener(this::jButton13ActionPerformed);

        jButton14.setText("Reports");
        jButton14.addActionListener(this::jButton14ActionPerformed);

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        jScrollPane1.setViewportView(jTable1);

        javax.swing.GroupLayout SideDashboardLayout = new javax.swing.GroupLayout(SideDashboard);
        SideDashboard.setLayout(SideDashboardLayout);
        SideDashboardLayout.setHorizontalGroup(
            SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideDashboardLayout.createSequentialGroup()
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SideDashboardLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel4)
                            .addComponent(jLabel3)))
                    .addGroup(SideDashboardLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(SideDashboardLayout.createSequentialGroup()
                                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel7))
                                .addGap(104, 104, 104)
                                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel8)))
                            .addGroup(SideDashboardLayout.createSequentialGroup()
                                .addComponent(jButton12)
                                .addGap(60, 60, 60)
                                .addComponent(jButton13)
                                .addGap(65, 65, 65)
                                .addComponent(jButton14))))
                    .addGroup(SideDashboardLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 585, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        SideDashboardLayout.setVerticalGroup(
            SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideDashboardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(70, 70, 70)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(29, 29, 29)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12)
                    .addComponent(jButton13)
                    .addComponent(jButton14))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(24, Short.MAX_VALUE))
        );

        MainContentPanel.add(SideDashboard, "card7");

        jLabel9.setText("Students Management");

        jLabel10.setText("Manage student information and academic records.");

        jTextField1.addActionListener(this::jTextField1ActionPerformed);

        jButton15.setText("+Add Students");
        jButton15.addActionListener(this::jButton15ActionPerformed);

        jButton16.setText("Search");
        jButton16.addActionListener(this::jButton16ActionPerformed);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null},
                {null, null, null, null, null}
            },
            new String [] {
                "Student ID", "First Name", "Last Name", "Course", "Year Level"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout StudentsLayout = new javax.swing.GroupLayout(Students);
        Students.setLayout(StudentsLayout);
        StudentsLayout.setHorizontalGroup(
            StudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StudentsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(StudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(StudentsLayout.createSequentialGroup()
                        .addGroup(StudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel10)
                            .addComponent(jLabel9)
                            .addGroup(StudentsLayout.createSequentialGroup()
                                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 310, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 118, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton15)))
                        .addGap(0, 66, Short.MAX_VALUE)))
                .addContainerGap())
        );
        StudentsLayout.setVerticalGroup(
            StudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(StudentsLayout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jLabel9)
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(StudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(StudentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButton16, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jButton15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 31, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(53, Short.MAX_VALUE))
        );

        MainContentPanel.add(Students, "card2");

        jLabel17.setText("Subjects Management");

        jLabel18.setText("Manage the academic subjects offered by your institution.");

        jButton19.setText("+ Add Subject");
        jButton19.addActionListener(this::jButton19ActionPerformed);

        jTextField8.addActionListener(this::jTextField8ActionPerformed);

        jButton20.setText("Search");
        jButton20.addActionListener(this::jButton20ActionPerformed);

        jTable3.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "No.", "Subject Name"
            }
        ));
        jScrollPane3.setViewportView(jTable3);

        javax.swing.GroupLayout SubjectsLayout = new javax.swing.GroupLayout(Subjects);
        Subjects.setLayout(SubjectsLayout);
        SubjectsLayout.setHorizontalGroup(
            SubjectsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SubjectsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(SubjectsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(SubjectsLayout.createSequentialGroup()
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton19))
                    .addComponent(jLabel17)
                    .addComponent(jLabel18)
                    .addComponent(jScrollPane3))
                .addContainerGap(76, Short.MAX_VALUE))
        );
        SubjectsLayout.setVerticalGroup(
            SubjectsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SubjectsLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jLabel17)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel18)
                .addGap(18, 18, 18)
                .addGroup(SubjectsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField8)
                    .addComponent(jButton19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                .addContainerGap())
        );

        MainContentPanel.add(Subjects, "card3");

        jLabel23.setFont(new java.awt.Font("Segoe UI", 0, 24)); // NOI18N
        jLabel23.setText("Enrollment Management");

        jLabel24.setText("Manage student subject registrations and enrollment records.");

        jLabel25.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel25.setText("New Enrollment");

        jLabel26.setText("Student");

        cmbSubject.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSubject.addActionListener(this::cmbSubjectActionPerformed);

        jLabel27.setText("Subject");

        cmbStudent.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbStudent.addActionListener(this::cmbStudentActionPerformed);

        jLabel28.setText("Academic year");

        jLabel29.setText("Semester");

        cmbSemester.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbSemester.addActionListener(this::cmbSemesterActionPerformed);

        cmbAcademicYear.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbAcademicYear.addActionListener(this::cmbAcademicYearActionPerformed);

        jLabel30.setText("Enrollment Date");

        jLabel31.setText("Status");

        cmbStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cmbStatus.addActionListener(this::cmbStatusActionPerformed);

        txtEnrollmentDate.setText("MM/DD/YY");
        txtEnrollmentDate.addActionListener(this::txtEnrollmentDateActionPerformed);

        btnAddEnrollment.setText("+ Add Enrollment");
        btnAddEnrollment.addActionListener(this::btnAddEnrollmentActionPerformed);

        btnClearEnrollment.setText("Clear");
        btnClearEnrollment.addActionListener(this::btnClearEnrollmentActionPerformed);

        jLabel32.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jLabel32.setText("Enrollment Records");

        txtSearchEnrollment.setText("Search Enrollments");
        txtSearchEnrollment.addActionListener(this::txtSearchEnrollmentActionPerformed);

        btnSearchEnrollment.setFont(new java.awt.Font("Segoe UI", 0, 10)); // NOI18N
        btnSearchEnrollment.setText("Search");
        btnSearchEnrollment.addActionListener(this::btnSearchEnrollmentActionPerformed);

        tblEnrollments.setFont(new java.awt.Font("Segoe UI", 0, 8)); // NOI18N

        jTable4.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null}
            },
            new String [] {
                "Enrollment ID", "Student ID", "Student Name", "Subject", "Academic Year", "Semester", "Enrollment Date", "Status"
            }
        ));
        jTable4.setName(""); // NOI18N
        tblEnrollments.setViewportView(jTable4);

        btnUpdateEnrollment.setText("Update");
        btnUpdateEnrollment.addActionListener(this::btnUpdateEnrollmentActionPerformed);

        javax.swing.GroupLayout EnrollmentsLayout = new javax.swing.GroupLayout(Enrollments);
        Enrollments.setLayout(EnrollmentsLayout);
        EnrollmentsLayout.setHorizontalGroup(
            EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EnrollmentsLayout.createSequentialGroup()
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EnrollmentsLayout.createSequentialGroup()
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(EnrollmentsLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(cmbStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel26)
                                    .addComponent(jLabel28)))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EnrollmentsLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(cmbAcademicYear, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel27)
                            .addComponent(cmbSubject, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel29)
                            .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel31)))
                    .addGroup(EnrollmentsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 296, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel25)
                            .addComponent(jLabel24)))
                    .addGroup(EnrollmentsLayout.createSequentialGroup()
                        .addGap(21, 21, 21)
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(EnrollmentsLayout.createSequentialGroup()
                                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addGroup(EnrollmentsLayout.createSequentialGroup()
                                        .addComponent(jLabel32)
                                        .addGap(28, 28, 28)
                                        .addComponent(btnUpdateEnrollment)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(btnClearEnrollment))
                                    .addComponent(btnSearchEnrollment))
                                .addGap(18, 18, 18)
                                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(btnAddEnrollment)
                                    .addComponent(txtSearchEnrollment, javax.swing.GroupLayout.PREFERRED_SIZE, 131, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jLabel30)
                            .addGroup(EnrollmentsLayout.createSequentialGroup()
                                .addComponent(txtEnrollmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, EnrollmentsLayout.createSequentialGroup()
                .addGap(0, 22, Short.MAX_VALUE)
                .addComponent(tblEnrollments, javax.swing.GroupLayout.PREFERRED_SIZE, 591, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(23, Short.MAX_VALUE))
        );
        EnrollmentsLayout.setVerticalGroup(
            EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(EnrollmentsLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel23, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel24)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel25)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel26)
                    .addComponent(jLabel27))
                .addGap(3, 3, 3)
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbStudent, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSubject, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel28)
                    .addComponent(jLabel29))
                .addGap(6, 6, 6)
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbAcademicYear, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(cmbSemester, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel30)
                    .addComponent(jLabel31))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(cmbStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(txtEnrollmentDate, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(EnrollmentsLayout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(btnAddEnrollment)
                            .addComponent(btnClearEnrollment))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtSearchEnrollment, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(btnSearchEnrollment, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(EnrollmentsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel32)
                            .addComponent(btnUpdateEnrollment, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tblEnrollments, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(15, Short.MAX_VALUE))
        );

        MainContentPanel.add(Enrollments, "card6");

        jLabel40.setText("Grades Management");

        jLabel41.setText("Record grades for enrolled subjects. Passing grade is 75.");

        jLabel42.setText("Enter Grade");

        jLabel43.setText("Student");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel44.setText("Subject");

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel45.setText("Grade (0–100)");

        jTextField11.addActionListener(this::jTextField11ActionPerformed);

        jButton25.setText("Save Grade");
        jButton25.addActionListener(this::jButton25ActionPerformed);

        jLabel46.setText("Search");

        jTextField12.addActionListener(this::jTextField12ActionPerformed);

        jButton26.setText("Search");
        jButton26.addActionListener(this::jButton26ActionPerformed);

        jButton27.setText("Refresh");
        jButton27.addActionListener(this::jButton27ActionPerformed);

        jTable6.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null},
                {null, null, null, null, null, null}
            },
            new String [] {
                "Student ID", "Student Name", "Subject", "Grade", "Equivalent", "Remarks"
            }
        ));
        jScrollPane5.setViewportView(jTable6);

        javax.swing.GroupLayout GradesLayout = new javax.swing.GroupLayout(Grades);
        Grades.setLayout(GradesLayout);
        GradesLayout.setHorizontalGroup(
            GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GradesLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel40)
                    .addComponent(jLabel41)
                    .addGroup(GradesLayout.createSequentialGroup()
                        .addGap(25, 25, 25)
                        .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(GradesLayout.createSequentialGroup()
                                .addComponent(jLabel44)
                                .addGap(49, 49, 49)
                                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(jComboBox2, 0, 298, Short.MAX_VALUE)
                                    .addComponent(jComboBox3, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 580, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel43)
                            .addComponent(jLabel42)
                            .addGroup(GradesLayout.createSequentialGroup()
                                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addGroup(GradesLayout.createSequentialGroup()
                                        .addComponent(jLabel45)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField11)
                                        .addGap(18, 18, 18)
                                        .addComponent(jButton25))
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, GradesLayout.createSequentialGroup()
                                        .addComponent(jLabel46)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, 180, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton26)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButton27)))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        GradesLayout.setVerticalGroup(
            GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(GradesLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel40)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel41)
                .addGap(18, 18, 18)
                .addComponent(jLabel42)
                .addGap(18, 18, 18)
                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel43)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21)
                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel44)
                    .addComponent(jComboBox3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel45)
                    .addComponent(jTextField11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton25))
                .addGap(36, 36, 36)
                .addGroup(GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel46)
                    .addComponent(jTextField12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton26)
                    .addComponent(jButton27))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane5, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(25, Short.MAX_VALUE))
        );

        MainContentPanel.add(Grades, "card4");

        jLabel33.setText("Academic Reports");

        jLabel34.setText("View student performance and academic summaries.");

        jLabel35.setText(" -Total Students");

        jLabel36.setText(" -Enrollments");

        jLabel37.setText(" -Grades Recorded");

        jLabel38.setText(" -Pending Grades");

        jLabel39.setText("Student Performance");

        jTextField10.addActionListener(this::jTextField10ActionPerformed);

        jButton23.setText("Generate");
        jButton23.addActionListener(this::jButton23ActionPerformed);

        jTable5.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null}
            },
            new String [] {
                "Student ID", "Student Name", "Course", "Year", "Enrollments", "Grades Recorded", "Average Grade"
            }
        ));
        jScrollPane4.setViewportView(jTable5);

        jButton24.setText("Export CSV");
        jButton24.addActionListener(this::jButton24ActionPerformed);

        javax.swing.GroupLayout ReportsLayout = new javax.swing.GroupLayout(Reports);
        Reports.setLayout(ReportsLayout);
        ReportsLayout.setHorizontalGroup(
            ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReportsLayout.createSequentialGroup()
                .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(ReportsLayout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ReportsLayout.createSequentialGroup()
                                .addComponent(jLabel39)
                                .addGap(18, 18, 18)
                                .addComponent(jTextField10))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, ReportsLayout.createSequentialGroup()
                                .addGap(35, 35, 35)
                                .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(ReportsLayout.createSequentialGroup()
                                        .addComponent(jLabel35)
                                        .addGap(258, 258, 258)
                                        .addComponent(jLabel36))
                                    .addGroup(ReportsLayout.createSequentialGroup()
                                        .addComponent(jLabel37)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(jLabel38)))))
                        .addGap(18, 18, 18)
                        .addComponent(jButton23))
                    .addGroup(ReportsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel33)
                            .addComponent(jLabel34))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(ReportsLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane4))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, ReportsLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButton24)))
                .addContainerGap())
        );
        ReportsLayout.setVerticalGroup(
            ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(ReportsLayout.createSequentialGroup()
                .addGap(5, 5, 5)
                .addComponent(jLabel33)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel34)
                .addGap(26, 26, 26)
                .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel35)
                    .addComponent(jLabel36))
                .addGap(36, 36, 36)
                .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel37)
                    .addComponent(jLabel38))
                .addGap(45, 45, 45)
                .addGroup(ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel39)
                    .addComponent(jTextField10, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton23))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jScrollPane4, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton24)
                .addContainerGap())
        );

        MainContentPanel.add(Reports, "card5");

        jLabel11.setText("Add New Student");

        jLabel12.setText("Student ID");

        jLabel13.setText("First Name");

        jLabel14.setText("Last Name");

        jLabel15.setText("Course");

        jLabel16.setText("Year");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "1", "2", "3", "4" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);

        jButton17.setText("Save Student");
        jButton17.addActionListener(this::jButton17ActionPerformed);

        jButton18.setText("Cancel");
        jButton18.addActionListener(this::jButton18ActionPerformed);

        javax.swing.GroupLayout AddStudentFormLayout = new javax.swing.GroupLayout(AddStudentForm);
        AddStudentForm.setLayout(AddStudentFormLayout);
        AddStudentFormLayout.setHorizontalGroup(
            AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddStudentFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel11)
                    .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addGroup(AddStudentFormLayout.createSequentialGroup()
                            .addGap(14, 14, 14)
                            .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel15)
                                .addComponent(jLabel16))
                            .addGap(20, 20, 20)
                            .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(AddStudentFormLayout.createSequentialGroup()
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 93, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(126, 135, Short.MAX_VALUE))
                                .addComponent(jTextField7)
                                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AddStudentFormLayout.createSequentialGroup()
                                    .addGap(0, 0, Short.MAX_VALUE)
                                    .addComponent(jButton17))))
                        .addGroup(AddStudentFormLayout.createSequentialGroup()
                            .addComponent(jLabel14)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(jTextField6))
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, AddStudentFormLayout.createSequentialGroup()
                            .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jLabel12)
                                .addComponent(jLabel13))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                            .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                .addComponent(jTextField4, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
                                .addComponent(jTextField5)))
                        .addComponent(jButton18)))
                .addContainerGap(327, Short.MAX_VALUE))
        );
        AddStudentFormLayout.setVerticalGroup(
            AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddStudentFormLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel11)
                .addGap(18, 18, 18)
                .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel12)
                    .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel13)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15))
                .addGap(18, 18, 18)
                .addGroup(AddStudentFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel16)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 185, Short.MAX_VALUE)
                .addComponent(jButton17)
                .addGap(18, 18, 18)
                .addComponent(jButton18)
                .addGap(23, 23, 23))
        );

        MainContentPanel.add(AddStudentForm, "card8");

        jLabel19.setText("Add New Subject");

        jLabel20.setText("Enter the details of the new academic subject.");

        jLabel21.setText("Subject Name");

        jTextField9.addActionListener(this::jTextField9ActionPerformed);

        jButton21.setText("Cancel");
        jButton21.addActionListener(this::jButton21ActionPerformed);

        jButton22.setText("Save Subject");
        jButton22.addActionListener(this::jButton22ActionPerformed);

        jLabel22.setText("Enter the complete subject name.");

        javax.swing.GroupLayout AddNewSubjectFormLayout = new javax.swing.GroupLayout(AddNewSubjectForm);
        AddNewSubjectForm.setLayout(AddNewSubjectFormLayout);
        AddNewSubjectFormLayout.setHorizontalGroup(
            AddNewSubjectFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddNewSubjectFormLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(AddNewSubjectFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(AddNewSubjectFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jLabel19)
                        .addComponent(jLabel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel21)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, AddNewSubjectFormLayout.createSequentialGroup()
                            .addComponent(jButton21)
                            .addGap(18, 18, 18)
                            .addComponent(jButton22))
                        .addComponent(jTextField9))
                    .addComponent(jLabel22))
                .addContainerGap(358, Short.MAX_VALUE))
        );
        AddNewSubjectFormLayout.setVerticalGroup(
            AddNewSubjectFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(AddNewSubjectFormLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel19)
                .addGap(18, 18, 18)
                .addComponent(jLabel20)
                .addGap(44, 44, 44)
                .addComponent(jLabel21)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField9, javax.swing.GroupLayout.PREFERRED_SIZE, 39, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel22)
                .addGap(40, 40, 40)
                .addGroup(AddNewSubjectFormLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton21, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton22, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(229, Short.MAX_VALUE))
        );

        MainContentPanel.add(AddNewSubjectForm, "card9");

        javax.swing.GroupLayout DashboardLayout = new javax.swing.GroupLayout(Dashboard);
        Dashboard.setLayout(DashboardLayout);
        DashboardLayout.setHorizontalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jButton9)
                        .addComponent(jButton11)
                        .addComponent(jButton5)
                        .addComponent(jButton7)
                        .addComponent(jButton10)
                        .addComponent(jButton8))
                    .addGroup(DashboardLayout.createSequentialGroup()
                        .addComponent(jButton6)
                        .addGap(17, 17, 17)))
                .addContainerGap(711, Short.MAX_VALUE))
            .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, DashboardLayout.createSequentialGroup()
                    .addContainerGap(216, Short.MAX_VALUE)
                    .addComponent(MainContentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 636, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap()))
        );
        DashboardLayout.setVerticalGroup(
            DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(DashboardLayout.createSequentialGroup()
                .addGap(71, 71, 71)
                .addComponent(jButton5)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 43, Short.MAX_VALUE)
                .addComponent(jButton6)
                .addGap(31, 31, 31)
                .addComponent(jButton7)
                .addGap(38, 38, 38)
                .addComponent(jButton8)
                .addGap(48, 48, 48)
                .addComponent(jButton9)
                .addGap(32, 32, 32)
                .addComponent(jButton10)
                .addGap(38, 38, 38)
                .addComponent(jButton11)
                .addGap(41, 41, 41))
            .addGroup(DashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(DashboardLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(MainContentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 491, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );

        getContentPane().add(Dashboard, "card4");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        String email = jTextField2.getText().trim();
    char[] passwordChars = jPasswordField2.getPassword();
    String password = new String(passwordChars);

    java.util.Arrays.fill(passwordChars, '\0');

    if (email.isEmpty() || password.isEmpty()) {
        JOptionPane.showMessageDialog(
                this,
                "Please enter your email and password.",
                "Login Required",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    jButton2.setEnabled(false);
    jButton2.setText("Logging in...");

    SwingWorker<AuthService.LoginResult, Void> worker =
            new SwingWorker<AuthService.LoginResult, Void>() {

        @Override
        protected AuthService.LoginResult doInBackground()
                throws Exception {
            return AuthService.login(email, password);
        }

        @Override
        protected void done() {

            jButton2.setEnabled(true);
            jButton2.setText("Login");
            jPasswordField2.setText("");

            try {
                AuthService.LoginResult result = get();

                if (result.success) {

                    JOptionPane.showMessageDialog(
                            StudentManagementSystem.this,
                            "Welcome, " + result.email
                            + "\nRole: " + result.role,
                            "Login Successful",
                            JOptionPane.INFORMATION_MESSAGE
                    );

                    showDashboard(result.email, result.role);

                } else {
                    AuthService.logout();

                    JOptionPane.showMessageDialog(
                            StudentManagementSystem.this,
                            result.message,
                            "Login Failed",
                            JOptionPane.ERROR_MESSAGE
                    );
                }

            } catch (java.util.concurrent.ExecutionException ex) {

                AuthService.logout();

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                        StudentManagementSystem.this,
                        cause.getMessage(),
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );

            } catch (InterruptedException ex) {

                Thread.currentThread().interrupt();

                JOptionPane.showMessageDialog(
                        StudentManagementSystem.this,
                        "Login was interrupted.",
                        "Login Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
         CardLayout cl = (CardLayout) getContentPane().getLayout();
    cl.show(getContentPane(), "card3");
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
    CardLayout cl = (CardLayout) getContentPane().getLayout();
    cl.show(getContentPane(), "card2");
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        String email = jTextField3.getText().trim();

char[] passwordChars = jPasswordField3.getPassword();
String password = new String(passwordChars);

java.util.Arrays.fill(passwordChars, '\0');

if (email.isEmpty() || password.isEmpty()) {
    JOptionPane.showMessageDialog(
            this,
            "Please enter your email and password.",
            "Registration Required",
            JOptionPane.WARNING_MESSAGE
    );
    return;
}

jButton3.setEnabled(false);
jButton3.setText("Creating Account...");

SwingWorker<String, Void> worker =
        new SwingWorker<String, Void>() {

    @Override
    protected String doInBackground() throws Exception {
        return AuthService.signUp(email, password);
    }

    @Override
    protected void done() {

        jButton3.setEnabled(true);
        jButton3.setText("Sign Up");
        jPasswordField3.setText("");

        try {
            String response = get();

            JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Registration request submitted!\n"
                    + "Check your email for a confirmation link "
                    + "if email confirmation is enabled.",
                    "Registration Successful",
                    JOptionPane.INFORMATION_MESSAGE
            );

            CardLayout cl =
                    (CardLayout) getContentPane().getLayout();

            cl.show(getContentPane(), "card2");

        } catch (java.util.concurrent.ExecutionException ex) {

            Throwable cause = ex.getCause() == null
                    ? ex : ex.getCause();

            JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    cause.getMessage(),
                    "Registration Failed",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (InterruptedException ex) {

            Thread.currentThread().interrupt();

            JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Registration was interrupted.",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
};

worker.execute();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:   
        showMainContent("card7");
        updateDashboardCounts();
        loadRecentStudents();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton7ActionPerformed
        // TODO add your handling code here:
            showMainContent("card3");
    loadSubjects();
    }//GEN-LAST:event_jButton7ActionPerformed

    private void jButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton11ActionPerformed
        // TODO add your handling code here:
       
    int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
    );

    if (choice == JOptionPane.YES_OPTION) {

        AuthService.logout();

        jTextField2.setText("");
        jPasswordField2.setText("");

        CardLayout cl =
                (CardLayout) getContentPane().getLayout();

        cl.show(getContentPane(), "card2");
    }
    }//GEN-LAST:event_jButton11ActionPerformed

    private void jButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton10ActionPerformed
        // TODO add your handling code here:
            showMainContent("card5");
                generateReports();

    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
            showMainContent("card4");
            loadGradeStudents();
            loadGrades();
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
            showMainContent("card6");
            loadEnrollmentOptions();
            loadEnrollments();
    }//GEN-LAST:event_jButton8ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
    showMainContent("card2");
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton16ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton16ActionPerformed
        // TODO add your handling code here:
    String search = jTextField1.getText().trim();
    loadStudents(search);
    }//GEN-LAST:event_jButton16ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
            loadStudents(jTextField1.getText().trim());
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton15ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton15ActionPerformed
        // TODO add your handling code here:
            showMainContent("card8");
    }//GEN-LAST:event_jButton15ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jButton17ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton17ActionPerformed
        // TODO add your handling code here:
        // Get form values
    String studentIdText = jTextField4.getText().trim();
    String firstName = jTextField5.getText().trim();
    String lastName = jTextField6.getText().trim();
    String course = jTextField7.getText().trim();
    String yearText = jComboBox1.getSelectedItem().toString();

    // Validate empty fields
    if (studentIdText.isEmpty()
            || firstName.isEmpty()
            || lastName.isEmpty()
            || course.isEmpty()) {

        JOptionPane.showMessageDialog(
                this,
                "Please complete all student information.",
                "Missing Information",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // Validate numeric Student ID
    int studentId;
    int yearLevel;

    try {
        studentId = Integer.parseInt(studentIdText);
        yearLevel = Integer.parseInt(yearText);

        if (studentId <= 0) {
            throw new NumberFormatException();
        }

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(
                this,
                "Student ID must be a positive whole number.",
                "Invalid Student ID",
                JOptionPane.WARNING_MESSAGE
        );
        return;
    }

    // Create JSON request body
    JsonObject student = new JsonObject();

    student.addProperty("id", studentId);
    student.addProperty("first_name", firstName);
    student.addProperty("last_name", lastName);
    student.addProperty("course", course);
    student.addProperty("year_level", yearLevel);

    // Disable button while saving
    jButton17.setEnabled(false);
    jButton17.setText("Saving...");

    SwingWorker<String, Void> worker =
            new SwingWorker<String, Void>() {

        @Override
        protected String doInBackground() throws Exception {

            return SupabaseClient.post(
                    "/rest/v1/students",
                    student
            );
        }

        @Override
        protected void done() {

            jButton17.setEnabled(true);
            jButton17.setText("Save Student");

            try {
                get();

                JOptionPane.showMessageDialog(
                        StudentManagementSystem.this,
                        "Student successfully saved!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE
                );

                // Clear the form
                jTextField4.setText("");
                jTextField5.setText("");
                jTextField6.setText("");
                jTextField7.setText("");
                jComboBox1.setSelectedIndex(0);

                // Refresh both tables and dashboard counts
                loadStudents("");
                loadRecentStudents();
                updateDashboardCounts();

                // Return to Students page
                showMainContent("card2");

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                        StudentManagementSystem.this,
                        "Unable to save student:\n"
                        + cause.getMessage(),
                        "Save Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();

    }//GEN-LAST:event_jButton17ActionPerformed

    private void jButton18ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton18ActionPerformed
        // TODO add your handling code here:
            showMainContent("card2");
    }//GEN-LAST:event_jButton18ActionPerformed

    private void jButton13ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton13ActionPerformed
        // TODO add your handling code here:
         showMainContent("card3");
         loadSubjects();
    }//GEN-LAST:event_jButton13ActionPerformed

    private void jButton14ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton14ActionPerformed
        // TODO add your handling code here:
        showMainContent("card5");
        generateReports();
    }//GEN-LAST:event_jButton14ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
            filterSubjects();
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jButton20ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton20ActionPerformed
        // TODO add your handling code here:
            filterSubjects();
    }//GEN-LAST:event_jButton20ActionPerformed

    private void jButton19ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton19ActionPerformed
        // TODO add your handling code here:
         jTextField9.setText("");

    showMainContent("card9");

    jTextField9.requestFocusInWindow();
    }//GEN-LAST:event_jButton19ActionPerformed

    private void jButton21ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton21ActionPerformed
        // TODO add your handling code here:
        jTextField9.setText("");

    showMainContent("card3");
    }//GEN-LAST:event_jButton21ActionPerformed

    private void jButton22ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton22ActionPerformed
        // TODO add your handling code here:
        String subjectName = jTextField9.getText().trim();

    // Validate input
    if (subjectName.isEmpty()) {

        JOptionPane.showMessageDialog(
            this,
            "Please enter a subject name.",
            "Missing Information",
            JOptionPane.WARNING_MESSAGE
        );

        jTextField9.requestFocus();
        return;
    }

    // Check duplicate names in the currently loaded list
    for (String existing : subjectNames) {

        if (existing.equalsIgnoreCase(subjectName)) {

            JOptionPane.showMessageDialog(
                this,
                "This subject already exists.",
                "Duplicate Subject",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }
    }

    // Create JSON body
    JsonObject subject = new JsonObject();
    subject.addProperty("name", subjectName);

    jButton22.setEnabled(false);
    jButton22.setText("Saving...");

    SwingWorker<String, Void> worker =
            new SwingWorker<String, Void>() {

        @Override
        protected String doInBackground() throws Exception {

            return SupabaseClient.post(
                "/rest/v1/subjects",
                subject
            );
        }

        @Override
        protected void done() {

            jButton22.setEnabled(true);
            jButton22.setText("Save Subject");

            try {

                get();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Subject successfully added!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                jTextField9.setText("");

                showMainContent("card3");

                loadSubjects();
                updateDashboardCounts();

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to save subject:\n"
                    + cause.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
    }//GEN-LAST:event_jButton22ActionPerformed

    private void jTextField9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField9ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField9ActionPerformed

    private void jButton12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton12ActionPerformed
        // TODO add your handling code here:
        showMainContent("card8");
    }//GEN-LAST:event_jButton12ActionPerformed

    private void cmbSubjectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSubjectActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSubjectActionPerformed

    private void cmbStudentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStudentActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbStudentActionPerformed

    private void cmbSemesterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbSemesterActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbSemesterActionPerformed

    private void cmbAcademicYearActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbAcademicYearActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_cmbAcademicYearActionPerformed

    private void cmbStatusActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cmbStatusActionPerformed
        // TODO add your handling code here:
        cmbStatus.requestFocusInWindow();
    }//GEN-LAST:event_cmbStatusActionPerformed

    private void txtEnrollmentDateActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtEnrollmentDateActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtEnrollmentDateActionPerformed

    private void btnAddEnrollmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnAddEnrollmentActionPerformed
// Get selected student.
    if (cmbStudent.getSelectedItem() == null
            || cmbSubject.getSelectedItem() == null
            || cmbAcademicYear.getSelectedItem() == null
            || cmbSemester.getSelectedItem() == null
            || cmbStatus.getSelectedItem() == null) {

        JOptionPane.showMessageDialog(
            this,
            "Please load and select all enrollment information.",
            "Missing Information",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String selectedStudent =
            cmbStudent.getSelectedItem().toString();

    // Extract the Student ID from the ComboBox.
    int separator = selectedStudent.indexOf(" - ");

    if (separator < 1) {

        JOptionPane.showMessageDialog(
            this,
            "Invalid student selection.",
            "Enrollment Error",
            JOptionPane.ERROR_MESSAGE
        );

        return;
    }

    String studentIdText =
            selectedStudent.substring(0, separator);

    String subject =
            cmbSubject.getSelectedItem().toString();

    String academicYear =
            cmbAcademicYear.getSelectedItem().toString();

    String semester =
            cmbSemester.getSelectedItem().toString();

    String status =
            cmbStatus.getSelectedItem().toString();

    String dateText =
            txtEnrollmentDate.getText().trim();

    // Validate the date.
    LocalDate enrollmentDate;

    try {

        enrollmentDate = LocalDate.parse(
            dateText,
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            "Invalid date format.\nPlease use MM/DD/YYYY.",
            "Invalid Date",
            JOptionPane.WARNING_MESSAGE
        );

        txtEnrollmentDate.requestFocus();
        return;
    }

    // Prevent duplicate enrollment for the same subject and semester.
    for (Object[] row : enrollmentRows) {

        boolean sameStudent =
                String.valueOf(row[1]).equals(studentIdText);

        boolean sameSubject =
                String.valueOf(row[3]).equalsIgnoreCase(subject);

        boolean sameYear =
                String.valueOf(row[4]).equals(academicYear);

        boolean sameSemester =
                String.valueOf(row[5]).equals(semester);

        boolean notDropped =
                !String.valueOf(row[7]).equalsIgnoreCase("Dropped");

        if (sameStudent && sameSubject
                && sameYear && sameSemester && notDropped) {

            JOptionPane.showMessageDialog(
                this,
                "This student is already enrolled in this subject "
                + "for the selected academic year and semester.",
                "Duplicate Enrollment",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }
    }

    // Create JSON request body.
    JsonObject enrollment = new JsonObject();

    try {
        enrollment.addProperty(
            "student_id",
            Integer.parseInt(studentIdText)
        );
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(
            this,
            "Invalid Student ID.",
            "Enrollment Error",
            JOptionPane.ERROR_MESSAGE
        );
        return;
    }

    enrollment.addProperty("subject", subject);
    enrollment.addProperty("academic_year", academicYear);
    enrollment.addProperty("semester", semester);
    enrollment.addProperty(
        "enrollment_date",
        enrollmentDate.toString()
    );
    enrollment.addProperty("status", status);

    // Disable button while saving.
    btnAddEnrollment.setEnabled(false);
    btnAddEnrollment.setText("Saving...");

    SwingWorker<String, Void> worker =
            new SwingWorker<String, Void>() {

        @Override
        protected String doInBackground() throws Exception {

            return SupabaseClient.post(
                "/rest/v1/enrollments",
                enrollment
            );
        }

        @Override
        protected void done() {

            btnAddEnrollment.setEnabled(true);
            btnAddEnrollment.setText("+ Add Enrollment");

            try {

                get();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Enrollment successfully added!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Clear the form.
                clearEnrollmentForm();

                // Refresh enrollment records.
                loadEnrollments();

                // Update dashboard counts.
                updateDashboardCounts();

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to save enrollment:\n"
                    + cause.getMessage(),
                    "Save Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();        // TODO add your handling code here:
    }//GEN-LAST:event_btnAddEnrollmentActionPerformed

    private void btnClearEnrollmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnClearEnrollmentActionPerformed
    clearEnrollmentForm();
    }//GEN-LAST:event_btnClearEnrollmentActionPerformed

    private void btnSearchEnrollmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSearchEnrollmentActionPerformed
    filterEnrollmentRecords();
    }//GEN-LAST:event_btnSearchEnrollmentActionPerformed

    private void txtSearchEnrollmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtSearchEnrollmentActionPerformed
    filterEnrollmentRecords();
    }//GEN-LAST:event_txtSearchEnrollmentActionPerformed

    private void btnUpdateEnrollmentActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpdateEnrollmentActionPerformed

    // Check whether a record is selected.
    int selectedRow = jTable4.getSelectedRow();

    if (selectedRow == -1) {

        JOptionPane.showMessageDialog(
            this,
            "Please select an enrollment record from the table.",
            "No Record Selected",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    // Get the actual database ID.
    int modelRow = jTable4.convertRowIndexToModel(selectedRow);

    long enrollmentId = Long.parseLong(
        jTable4.getModel().getValueAt(modelRow, 0).toString()
    );

    // Get values from the form.
    String selectedStudent =
            cmbStudent.getSelectedItem().toString();

    String studentIdText =
            selectedStudent.substring(
                0, selectedStudent.indexOf(" - ")
            );

    String subject = cmbSubject.getSelectedItem().toString();

    String academicYear =
            cmbAcademicYear.getSelectedItem().toString();

    String semester =
            cmbSemester.getSelectedItem().toString();

    String status = cmbStatus.getSelectedItem().toString();

    String dateText = txtEnrollmentDate.getText().trim();

    // Validate Student ID.
    int studentId;

    try {

        studentId = Integer.parseInt(studentIdText);

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(
            this,
            "Invalid Student ID.",
            "Validation Error",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    // Validate enrollment date.
    LocalDate enrollmentDate;

    try {

        enrollmentDate = LocalDate.parse(
            dateText,
            DateTimeFormatter.ofPattern("MM/dd/uuuu")
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            "Please enter a valid date using MM/DD/YYYY.",
            "Invalid Date",
            JOptionPane.WARNING_MESSAGE
        );

        txtEnrollmentDate.requestFocus();
        return;
    }

    // Create JSON request for the Supabase function.
    JsonObject update = new JsonObject();

    update.addProperty("p_enrollment_id", enrollmentId);
    update.addProperty("p_student_id", studentId);
    update.addProperty("p_subject", subject);
    update.addProperty("p_academic_year", academicYear);
    update.addProperty("p_semester", semester);
    update.addProperty(
        "p_enrollment_date",
        enrollmentDate.toString()
    );
    update.addProperty("p_status", status);

    // Disable the button while updating.
    btnUpdateEnrollment.setEnabled(false);
    btnUpdateEnrollment.setText("Updating...");

    SwingWorker<String, Void> worker =
            new SwingWorker<String, Void>() {

        @Override
        protected String doInBackground() throws Exception {

            return SupabaseClient.post(
                "/rest/v1/rpc/update_enrollment",
                update
            );
        }

        @Override
        protected void done() {

            btnUpdateEnrollment.setEnabled(true);
            btnUpdateEnrollment.setText("Update");

            try {

                get();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Enrollment record successfully updated!",
                    "Update Successful",
                    JOptionPane.INFORMATION_MESSAGE
                );

                // Clear the form.
                clearEnrollmentForm();

                // Remove the selected row.
                jTable4.clearSelection();

                // Reload the enrollment records.
                loadEnrollments();

                // Refresh dashboard totals.
                updateDashboardCounts();

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to update enrollment:\n"
                    + cause.getMessage(),
                    "Update Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
    }//GEN-LAST:event_btnUpdateEnrollmentActionPerformed
    private void jButton24ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton24ActionPerformed
        // TODO add your handling code here:
    exportReportsCSV();
    }//GEN-LAST:event_jButton24ActionPerformed

    private void jButton23ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton23ActionPerformed
        // TODO add your handling code here:
            generateReports();
    }//GEN-LAST:event_jButton23ActionPerformed

    private void jTextField10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField10ActionPerformed
        // TODO add your handling code here:
            generateReports();
    }//GEN-LAST:event_jTextField10ActionPerformed

    private void jTextField11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField11ActionPerformed
        // TODO add your handling code here:
            saveGrade();
    }//GEN-LAST:event_jTextField11ActionPerformed

    private void jTextField12ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField12ActionPerformed
        // TODO add your handling code here:
            filterGrades();
    }//GEN-LAST:event_jTextField12ActionPerformed

    private void jButton26ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton26ActionPerformed
        // TODO add your handling code here:
            filterGrades();
    }//GEN-LAST:event_jButton26ActionPerformed

    private void jButton27ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton27ActionPerformed
        // TODO add your handling code here:
        jTextField12.setText("");
    loadGradeStudents();
    loadGrades();
    }//GEN-LAST:event_jButton27ActionPerformed

    private void jButton25ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton25ActionPerformed
        // TODO add your handling code here:
            saveGrade();
    }//GEN-LAST:event_jButton25ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new StudentManagementSystem().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel AddNewSubjectForm;
    private javax.swing.JPanel AddStudentForm;
    private javax.swing.JPanel Dashboard;
    private javax.swing.JPanel Enrollments;
    private javax.swing.JPanel Grades;
    private javax.swing.JPanel LoginPage;
    private javax.swing.JPanel MainContentPanel;
    private javax.swing.JPanel Reports;
    private javax.swing.JPanel SideDashboard;
    private javax.swing.JPanel SignUpPage;
    private javax.swing.JPanel Students;
    private javax.swing.JPanel Subjects;
    private javax.swing.JButton btnAddEnrollment;
    private javax.swing.JButton btnClearEnrollment;
    private javax.swing.JButton btnSearchEnrollment;
    private javax.swing.JButton btnUpdateEnrollment;
    private javax.swing.JComboBox<String> cmbAcademicYear;
    private javax.swing.JComboBox<String> cmbSemester;
    private javax.swing.JComboBox<String> cmbStatus;
    private javax.swing.JComboBox<String> cmbStudent;
    private javax.swing.JComboBox<String> cmbSubject;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton10;
    private javax.swing.JButton jButton11;
    private javax.swing.JButton jButton12;
    private javax.swing.JButton jButton13;
    private javax.swing.JButton jButton14;
    private javax.swing.JButton jButton15;
    private javax.swing.JButton jButton16;
    private javax.swing.JButton jButton17;
    private javax.swing.JButton jButton18;
    private javax.swing.JButton jButton19;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton20;
    private javax.swing.JButton jButton21;
    private javax.swing.JButton jButton22;
    private javax.swing.JButton jButton23;
    private javax.swing.JButton jButton24;
    private javax.swing.JButton jButton25;
    private javax.swing.JButton jButton26;
    private javax.swing.JButton jButton27;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel23;
    private javax.swing.JLabel jLabel24;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel36;
    private javax.swing.JLabel jLabel37;
    private javax.swing.JLabel jLabel38;
    private javax.swing.JLabel jLabel39;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel40;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel42;
    private javax.swing.JLabel jLabel43;
    private javax.swing.JLabel jLabel44;
    private javax.swing.JLabel jLabel45;
    private javax.swing.JLabel jLabel46;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPasswordField jPasswordField2;
    private javax.swing.JPasswordField jPasswordField3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTable jTable4;
    private javax.swing.JTable jTable5;
    private javax.swing.JTable jTable6;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField11;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
    private javax.swing.JScrollPane tblEnrollments;
    private javax.swing.JTextField txtEnrollmentDate;
    private javax.swing.JTextField txtSearchEnrollment;
    // End of variables declaration//GEN-END:variables
private void showDashboard(String email, String role) {

    setTitle("Student Management System - " + email);

    CardLayout cl =
            (CardLayout) getContentPane().getLayout();

    cl.show(getContentPane(), "card4");

    showMainContent("card7");

    updateDashboardCounts();

    loadRecentStudents();

    loadStudents("");
}
// Loads students and subjects into the enrollment form.
private void loadEnrollmentOptions() {

    SwingWorker<String[][], Void> worker =
            new SwingWorker<String[][], Void>() {

        @Override
        protected String[][] doInBackground() throws Exception {

            // Load students.
            String studentResponse = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name&order=id.asc"
            );

            JsonArray students =
                    JsonParser.parseString(studentResponse).getAsJsonArray();

            List<String> studentOptions = new ArrayList<>();

            for (int i = 0; i < students.size(); i++) {

                JsonObject student = students.get(i).getAsJsonObject();

                String id = student.get("id").getAsString();
                String firstName = student.get("first_name").getAsString();
                String lastName = student.get("last_name").getAsString();

                String displayName =
                        id + " - " + firstName + " " + lastName;

                studentOptions.add(displayName);
            }

            // Load subjects.
            String subjectResponse = SupabaseClient.get(
                "/rest/v1/subjects?select=name&order=name.asc"
            );

            JsonArray subjects =
                    JsonParser.parseString(subjectResponse).getAsJsonArray();

            List<String> subjectOptions = new ArrayList<>();

            for (int i = 0; i < subjects.size(); i++) {

                JsonObject subject = subjects.get(i).getAsJsonObject();

                subjectOptions.add(
                    subject.get("name").getAsString()
                );
            }

            return new String[][] {
                studentOptions.toArray(new String[0]),
                subjectOptions.toArray(new String[0])
            };
        }

        @Override
        protected void done() {

            try {

                String[][] options = get();

                // Populate student ComboBox.
                cmbStudent.removeAllItems();

                for (String student : options[0]) {
                    cmbStudent.addItem(student);
                }

                // Populate subject ComboBox.
                cmbSubject.removeAllItems();

                for (String subject : options[1]) {
                    cmbSubject.addItem(subject);
                }

                if (cmbStudent.getItemCount() > 0) {
                    cmbStudent.setSelectedIndex(0);
                }

                if (cmbSubject.getItemCount() > 0) {
                    cmbSubject.setSelectedIndex(0);
                }

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load enrollment options:\n"
                    + cause.getMessage(),
                    "Enrollment Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// Loads enrollment records from Supabase into jTable4.
private void loadEnrollments() {

    SwingWorker<List<Object[]>, Void> worker =
            new SwingWorker<List<Object[]>, Void>() {

        @Override
        protected List<Object[]> doInBackground() throws Exception {

            // Load student names for displaying alongside student IDs.
            String studentResponse = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name"
            );

            JsonArray students =
                    JsonParser.parseString(studentResponse).getAsJsonArray();

            Map<String, String> studentNames = new HashMap<>();

            for (int i = 0; i < students.size(); i++) {

                JsonObject student = students.get(i).getAsJsonObject();

                String id = student.get("id").getAsString();

                String fullName =
                        student.get("first_name").getAsString()
                        + " "
                        + student.get("last_name").getAsString();

                studentNames.put(id, fullName);
            }

            // Retrieve enrollment records.
            String response = SupabaseClient.get(
    "/rest/v1/enrollments?select=id,student_id,subject,academic_year,semester,enrollment_date,status&order=id.asc"
);

            JsonArray enrollments =
                    JsonParser.parseString(response).getAsJsonArray();

            List<Object[]> rows = new ArrayList<>();

            for (int i = 0; i < enrollments.size(); i++) {

                JsonObject enrollment =
                        enrollments.get(i).getAsJsonObject();

                String studentId =
                        enrollment.get("student_id").getAsString();

                String studentName =
                        studentNames.getOrDefault(studentId, "Unknown Student");

                rows.add(new Object[]{

    enrollment.get("id").getAsLong(),

    studentId,

    studentName,

    enrollment.get("subject").getAsString(),

    enrollment.get("academic_year").getAsString(),

    enrollment.get("semester").getAsString(),

    enrollment.get("enrollment_date").getAsString(),

    enrollment.get("status").getAsString()

});
            }

            return rows;
        }

        @Override
        protected void done() {

            try {

                enrollmentRows.clear();
                enrollmentRows.addAll(get());

                filterEnrollmentRecords();

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load enrollment records:\n"
                    + cause.getMessage(),
                    "Enrollment Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// Filters enrollment records displayed in jTable4.
private void filterEnrollmentRecords() {

    String search = txtSearchEnrollment.getText()
            .trim()
            .toLowerCase();

    DefaultTableModel model = new DefaultTableModel(
        new String[]{
            "Enrollment ID",
            "Student ID",
            "Student Name",
            "Subject",
            "Academic Year",
            "Semester",
            "Enrollment Date",
            "Status"
        },
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    for (Object[] row : enrollmentRows) {

        String searchableText = "";

        for (Object value : row) {
            searchableText += String.valueOf(value) + " ";
        }

        if (search.isEmpty()
                || searchableText.toLowerCase().contains(search)) {

            model.addRow(row);
        }
    }

    jTable4.setModel(model);
}
// Clears enrollment form fields and restores defaults.
private void clearEnrollmentForm() {

    if (cmbStudent.getItemCount() > 0) {
        cmbStudent.setSelectedIndex(0);
    }

    if (cmbSubject.getItemCount() > 0) {
        cmbSubject.setSelectedIndex(0);
    }

    if (cmbAcademicYear.getItemCount() > 0) {
        cmbAcademicYear.setSelectedIndex(0);
    }

    if (cmbSemester.getItemCount() > 0) {
        cmbSemester.setSelectedIndex(0);
    }

    if (cmbStatus.getItemCount() > 0) {
        cmbStatus.setSelectedIndex(0);
    }

    txtEnrollmentDate.setText(
        LocalDate.now().format(
            DateTimeFormatter.ofPattern("MM/dd/yyyy")
        )
    );

    txtSearchEnrollment.setText("");
}
// ==========================================
// LOAD STUDENTS FOR GRADES
// ==========================================

private void loadGradeStudents() {

    loadingGradeStudents = true;
    jComboBox2.removeAllItems();
    jComboBox3.removeAllItems();

    SwingWorker<Void, Void> worker =
            new SwingWorker<Void, Void>() {

        @Override
        protected Void doInBackground() throws Exception {

            String studentResponse = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name&order=id.asc"
            );

            JsonArray students =
                    JsonParser.parseString(studentResponse).getAsJsonArray();

            Map<Integer, String> studentNames = new HashMap<>();

            for (int i = 0; i < students.size(); i++) {

                JsonObject student = students.get(i).getAsJsonObject();

                int id = student.get("id").getAsInt();

                String name =
                        student.get("first_name").getAsString()
                        + " "
                        + student.get("last_name").getAsString();

                studentNames.put(id, name);
            }

            String enrollmentResponse = SupabaseClient.get(
                "/rest/v1/enrollments?select=student_id,status"
            );

            JsonArray enrollments =
                    JsonParser.parseString(enrollmentResponse).getAsJsonArray();

            List<String> enrolledStudents = new ArrayList<>();

            for (int i = 0; i < enrollments.size(); i++) {

                JsonObject enrollment =
                        enrollments.get(i).getAsJsonObject();

                if (!enrollment.get("status").getAsString()
                        .equalsIgnoreCase("Enrolled")) {
                    continue;
                }

                int id = enrollment.get("student_id").getAsInt();

                String name = studentNames.get(id);

                if (name != null) {

                    String item = id + " - " + name;

                    if (!enrolledStudents.contains(item)) {
                        enrolledStudents.add(item);
                    }
                }
            }

            javax.swing.SwingUtilities.invokeLater(() -> {

                for (String student : enrolledStudents) {
                    jComboBox2.addItem(student);
                }

                loadingGradeStudents = false;

                if (jComboBox2.getItemCount() > 0) {
                    loadGradeSubjects();
                }
            });

            return null;
        }

        @Override
        protected void done() {

            try {
                get();

            } catch (Exception ex) {

                loadingGradeStudents = false;

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load enrolled students:\n"
                    + ex.getMessage(),
                    "Grades Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// ==========================================
// GENERATE ACADEMIC REPORTS
// ==========================================

private void generateReports() {

    String search = jTextField10.getText()
            .trim()
            .toLowerCase();

    jButton23.setEnabled(false);
    jButton23.setText("Generating...");

    SwingWorker<ReportData, Void> worker =
            new SwingWorker<ReportData, Void>() {

        @Override
        protected ReportData doInBackground() throws Exception {

            ReportData report = new ReportData();

            // LOAD STUDENTS
            String studentResponse = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name,course,year_level&order=id.asc"
            );

            JsonArray students =
                    JsonParser.parseString(studentResponse)
                            .getAsJsonArray();

            // LOAD ENROLLMENTS
            String enrollmentResponse = SupabaseClient.get(
                "/rest/v1/enrollments?select=student_id,status"
            );

            JsonArray enrollments =
                    JsonParser.parseString(enrollmentResponse)
                            .getAsJsonArray();

            // LOAD GRADES
            String gradeResponse = SupabaseClient.get(
                "/rest/v1/grades?select=student_id,subject,grade"
            );

            JsonArray grades =
                    JsonParser.parseString(gradeResponse)
                            .getAsJsonArray();


            // SUMMARY COUNTS
            report.totalStudents = students.size();
            report.totalEnrollments = enrollments.size();
            report.totalGrades = grades.size();

            report.pendingGrades = Math.max(
                0,
                report.totalEnrollments - report.totalGrades
            );


            // COUNT ENROLLMENTS PER STUDENT
            Map<Integer, Integer> enrollmentCounts =
                    new HashMap<>();

            for (int i = 0; i < enrollments.size(); i++) {

                JsonObject enrollment =
                        enrollments.get(i).getAsJsonObject();

                int studentId =
                        enrollment.get("student_id").getAsInt();

                enrollmentCounts.put(
                    studentId,
                    enrollmentCounts.getOrDefault(studentId, 0) + 1
                );
            }


            // COLLECT GRADES PER STUDENT
            Map<Integer, List<Double>> studentGrades =
                    new HashMap<>();

            for (int i = 0; i < grades.size(); i++) {

                JsonObject grade =
                        grades.get(i).getAsJsonObject();

                int studentId =
                        grade.get("student_id").getAsInt();

                double gradeValue =
                        grade.get("grade").getAsDouble();

                studentGrades
                    .computeIfAbsent(
                        studentId,
                        key -> new ArrayList<>()
                    )
                    .add(gradeValue);
            }


            // GENERATE ONE REPORT ROW PER STUDENT
            for (int i = 0; i < students.size(); i++) {

                JsonObject student =
                        students.get(i).getAsJsonObject();

                int studentId =
                        student.get("id").getAsInt();

                String firstName =
                        student.get("first_name").getAsString();

                String lastName =
                        student.get("last_name").getAsString();

                String studentName =
                        firstName + " " + lastName;

                String course =
                        student.get("course").getAsString();

                String yearLevel =
                        student.get("year_level").getAsString();


                // Get enrollment count
                int enrollmentCount =
                        enrollmentCounts.getOrDefault(studentId, 0);


                // Get student's grades
                List<Double> gradesList =
                        studentGrades.getOrDefault(
                            studentId,
                            new ArrayList<>()
                        );

                int gradesRecorded = gradesList.size();


                // Calculate average grade
                double average = 0;

                if (gradesRecorded > 0) {

                    double total = 0;

                    for (double value : gradesList) {
                        total += value;
                    }

                    average = total / gradesRecorded;
                }


                String averageText =
                        gradesRecorded == 0
                        ? "N/A"
                        : String.format(
                            java.util.Locale.US,
                            "%.2f",
                            average
                        );


                // Prepare row for the JTable
                report.rows.add(new Object[]{

                    studentId,
                    studentName,
                    course,
                    yearLevel,
                    enrollmentCount,
                    gradesRecorded,
                    averageText

                });
            }

            return report;
        }


        @Override
        protected void done() {

            jButton23.setEnabled(true);
            jButton23.setText("Generate");

            try {

                ReportData report = get();


                // UPDATE SUMMARY LABELS
                jLabel35.setText(
                    "Total Students: " + report.totalStudents
                );

                jLabel36.setText(
                    "Enrollments: " + report.totalEnrollments
                );

                jLabel37.setText(
                    "Grades Recorded: " + report.totalGrades
                );

                jLabel38.setText(
                    "Pending Grades: " + report.pendingGrades
                );


                // CREATE REPORT TABLE MODEL
                DefaultTableModel model =
                        new DefaultTableModel(
                            new String[]{

                                "Student ID",
                                "Student Name",
                                "Course",
                                "Year",
                                "Enrollments",
                                "Grades Recorded",
                                "Average Grade"

                            },
                            0
                        ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column
                    ) {
                        return false;
                    }
                };


                // APPLY SEARCH FILTER
                for (Object[] row : report.rows) {

                    String searchableText = "";

                    for (Object value : row) {
                        searchableText +=
                                String.valueOf(value) + " ";
                    }

                    if (search.isEmpty()
                            || searchableText.toLowerCase()
                                    .contains(search)) {

                        model.addRow(row);
                    }
                }


                // DISPLAY RESULTS
                jTable5.setModel(model);

                jTable5.setRowHeight(28);

                jTable5.setAutoResizeMode(
                    javax.swing.JTable.AUTO_RESIZE_ALL_COLUMNS
                );


            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to generate reports:\n"
                    + cause.getMessage(),
                    "Reports Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// ==========================================
// LOAD ENROLLED SUBJECTS FOR SELECTED STUDENT
// ==========================================

private void loadGradeSubjects() {

    if (jComboBox2.getSelectedItem() == null) {
        return;
    }

    String selectedStudent =
            jComboBox2.getSelectedItem().toString();

    int separator = selectedStudent.indexOf(" - ");

    if (separator < 1) {
        return;
    }

    int studentId = Integer.parseInt(
        selectedStudent.substring(0, separator)
    );

    SwingWorker<List<String>, Void> worker =
            new SwingWorker<List<String>, Void>() {

        @Override
        protected List<String> doInBackground() throws Exception {

            String response = SupabaseClient.get(
                "/rest/v1/enrollments?select=subject,status"
                + "&student_id=eq." + studentId
            );

            JsonArray enrollments =
                    JsonParser.parseString(response).getAsJsonArray();

            List<String> subjects = new ArrayList<>();

            for (int i = 0; i < enrollments.size(); i++) {

                JsonObject enrollment =
                        enrollments.get(i).getAsJsonObject();

                if (!enrollment.get("status").getAsString()
                        .equalsIgnoreCase("Enrolled")) {
                    continue;
                }

                String subject =
                        enrollment.get("subject").getAsString();

                if (!subjects.contains(subject)) {
                    subjects.add(subject);
                }
            }

            return subjects;
        }

        @Override
        protected void done() {

            try {

                List<String> subjects = get();

                jComboBox3.removeAllItems();

                for (String subject : subjects) {
                    jComboBox3.addItem(subject);
                }

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load enrolled subjects:\n"
                    + ex.getMessage(),
                    "Grades Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}// ==========================================
// LOAD GRADES FROM SUPABASE
// ==========================================

private void loadGrades() {

    SwingWorker<List<Object[]>, Void> worker =
            new SwingWorker<List<Object[]>, Void>() {

        @Override
        protected List<Object[]> doInBackground() throws Exception {

            String studentResponse = SupabaseClient.get(
                "/rest/v1/students?select=id,first_name,last_name"
            );

            JsonArray students =
                    JsonParser.parseString(studentResponse).getAsJsonArray();

            Map<Integer, String> studentNames = new HashMap<>();

            for (int i = 0; i < students.size(); i++) {

                JsonObject student = students.get(i).getAsJsonObject();

                int id = student.get("id").getAsInt();

                String name =
                        student.get("first_name").getAsString()
                        + " "
                        + student.get("last_name").getAsString();

                studentNames.put(id, name);
            }

            String gradeResponse = SupabaseClient.get(
                "/rest/v1/grades?select=student_id,subject,grade"
            );

            JsonArray grades =
                    JsonParser.parseString(gradeResponse).getAsJsonArray();

            List<Object[]> rows = new ArrayList<>();

            for (int i = 0; i < grades.size(); i++) {

                JsonObject grade = grades.get(i).getAsJsonObject();

                int studentId =
                        grade.get("student_id").getAsInt();

                String studentName =
                        studentNames.getOrDefault(
                            studentId,
                            "Unknown Student"
                        );

                String subject =
                        grade.get("subject").getAsString();

                double gradeValue =
                        grade.get("grade").getAsDouble();

                String remarks =
                        gradeValue >= 75 ? "Passed" : "Failed";

                rows.add(new Object[]{
                    studentId,
                    studentName,
                    subject,
                    String.format(java.util.Locale.US, "%.2f", gradeValue),
                    "—",
                    remarks
                });
            }

            return rows;
        }

        @Override
        protected void done() {

            try {

                gradeRows.clear();
                gradeRows.addAll(get());

                filterGrades();

            } catch (Exception ex) {

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to load grades:\n"
                    + ex.getMessage(),
                    "Grades Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// ==========================================
// SEARCH GRADES
// ==========================================

private void filterGrades() {

    String search = jTextField12.getText()
            .trim()
            .toLowerCase();

    DefaultTableModel model = new DefaultTableModel(
        new String[]{
            "Student ID",
            "Student Name",
            "Subject",
            "Grade",
            "Equivalent",
            "Remarks"
        },
        0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };

    for (Object[] row : gradeRows) {

        String searchableText = "";

        for (Object value : row) {
            searchableText += String.valueOf(value) + " ";
        }

        if (search.isEmpty()
                || searchableText.toLowerCase().contains(search)) {

            model.addRow(row);
        }
    }

    jTable6.setModel(model);
    jTable6.setRowHeight(28);
}
// ==========================================
// SAVE GRADE TO SUPABASE
// ==========================================

private void saveGrade() {

    if (jComboBox2.getSelectedItem() == null
            || jComboBox3.getSelectedItem() == null) {

        JOptionPane.showMessageDialog(
            this,
            "Please select a student and subject.",
            "Missing Information",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    String selectedStudent =
            jComboBox2.getSelectedItem().toString();

    int separator = selectedStudent.indexOf(" - ");

    if (separator < 1) {
        return;
    }

    int studentId = Integer.parseInt(
        selectedStudent.substring(0, separator)
    );

    String subject =
            jComboBox3.getSelectedItem().toString();

    double gradeValue;

    try {

        gradeValue = Double.parseDouble(
            jTextField11.getText().trim()
        );

        if (gradeValue < 0 || gradeValue > 100) {
            throw new NumberFormatException();
        }

    } catch (NumberFormatException ex) {

        JOptionPane.showMessageDialog(
            this,
            "Please enter a valid grade between 0 and 100.",
            "Invalid Grade",
            JOptionPane.WARNING_MESSAGE
        );

        jTextField11.requestFocus();
        return;
    }

    JsonObject grade = new JsonObject();

    grade.addProperty("student_id", studentId);
    grade.addProperty("subject", subject);
    grade.addProperty("grade", gradeValue);

    jButton25.setEnabled(false);
    jButton25.setText("Saving...");

    SwingWorker<Void, Void> worker =
            new SwingWorker<Void, Void>() {

        @Override
        protected Void doInBackground() throws Exception {

            String encodedSubject =
                    URLEncoder.encode(subject, "UTF-8")
                    .replace("+", "%20");

            String endpoint =
                    "/rest/v1/grades"
                    + "?student_id=eq." + studentId
                    + "&subject=eq." + encodedSubject
                    + "&select=student_id";

            String existingResponse =
                    SupabaseClient.get(endpoint);

            JsonArray existingGrades =
                    JsonParser.parseString(existingResponse).getAsJsonArray();

            if (existingGrades.size() > 0) {

                // Update existing grade.
                SupabaseClient.patch(
                    "/rest/v1/grades"
                    + "?student_id=eq." + studentId
                    + "&subject=eq." + encodedSubject,
                    grade
                );

            } else {

                // Insert new grade.
                SupabaseClient.post(
                    "/rest/v1/grades",
                    grade
                );
            }

            return null;
        }

        @Override
        protected void done() {

            jButton25.setEnabled(true);
            jButton25.setText("Save Grade");

            try {

                get();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Grade successfully saved!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );

                jTextField11.setText("");

                loadGrades();
                updateDashboardCounts();

            } catch (Exception ex) {

                Throwable cause = ex.getCause() == null
                        ? ex : ex.getCause();

                JOptionPane.showMessageDialog(
                    StudentManagementSystem.this,
                    "Unable to save grade:\n"
                    + cause.getMessage(),
                    "Save Grade Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    };

    worker.execute();
}
// ==========================================
// EXPORT REPORTS TO CSV
// ==========================================

private void exportReportsCSV() {

    if (jTable5.getRowCount() == 0) {

        JOptionPane.showMessageDialog(
            this,
            "There are no report records to export.",
            "Export Reports",
            JOptionPane.WARNING_MESSAGE
        );

        return;
    }

    JFileChooser fileChooser = new JFileChooser();

    fileChooser.setDialogTitle("Save Academic Report");

    fileChooser.setSelectedFile(
        new File("Academic_Report.csv")
    );

    int result = fileChooser.showSaveDialog(this);

    if (result != JFileChooser.APPROVE_OPTION) {
        return;
    }

    File file = fileChooser.getSelectedFile();

    // Automatically add .csv extension
    if (!file.getName().toLowerCase().endsWith(".csv")) {

        file = new File(
            file.getParentFile(),
            file.getName() + ".csv"
        );
    }

    // Confirm overwrite
    if (file.exists()) {

        int overwrite = JOptionPane.showConfirmDialog(
            this,
            "This file already exists. Replace it?",
            "Confirm Export",
            JOptionPane.YES_NO_OPTION
        );

        if (overwrite != JOptionPane.YES_OPTION) {
            return;
        }
    }

    try (BufferedWriter writer =
            Files.newBufferedWriter(
                file.toPath(),
                StandardCharsets.UTF_8
            )) {

        // UTF-8 BOM for spreadsheet compatibility
        writer.write('\uFEFF');

        int columns = jTable5.getColumnCount();
        int rows = jTable5.getRowCount();


        // WRITE HEADERS
        for (int col = 0; col < columns; col++) {

            writer.write(
                escapeCSV(jTable5.getColumnName(col))
            );

            if (col < columns - 1) {
                writer.write(",");
            }
        }

        writer.newLine();


        // WRITE REPORT DATA
        for (int row = 0; row < rows; row++) {

            for (int col = 0; col < columns; col++) {

                Object value =
                        jTable5.getValueAt(row, col);

                writer.write(
                    escapeCSV(value)
                );

                if (col < columns - 1) {
                    writer.write(",");
                }
            }

            writer.newLine();
        }


        JOptionPane.showMessageDialog(
            this,
            "Report successfully exported!\n"
            + file.getAbsolutePath(),
            "Export Successful",
            JOptionPane.INFORMATION_MESSAGE
        );

    } catch (Exception ex) {

        JOptionPane.showMessageDialog(
            this,
            "Unable to export report:\n"
            + ex.getMessage(),
            "Export Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
}


// Escape commas, quotes, and newlines for CSV
private String escapeCSV(Object value) {

    String text = String.valueOf(value);

    text = text.replace("\"", "\"\"");

    return "\"" + text + "\"";
}
}