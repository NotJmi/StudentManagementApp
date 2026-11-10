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
/**
 *
 * @author jamesandrewwalker
 */
public class StudentManagementSystem extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(StudentManagementSystem.class.getName());

    private final List<String> subjectNames = new ArrayList<>();

    public StudentManagementSystem() {
    initComponents();

    CardLayout cl = (CardLayout) getContentPane().getLayout();
    cl.first(getContentPane());

    setLocationRelativeTo(null);
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
        Grades = new javax.swing.JPanel();
        Reports = new javax.swing.JPanel();
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
                                .addGap(115, 115, 115)
                                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel8)
                                    .addComponent(jLabel6)))
                            .addGroup(SideDashboardLayout.createSequentialGroup()
                                .addComponent(jButton12)
                                .addGap(60, 60, 60)
                                .addComponent(jButton13)
                                .addGap(65, 65, 65)
                                .addComponent(jButton14))))
                    .addGroup(SideDashboardLayout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 538, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        SideDashboardLayout.setVerticalGroup(
            SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(SideDashboardLayout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel4)
                .addGap(77, 77, 77)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addGap(54, 54, 54)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(jLabel8))
                .addGap(53, 53, 53)
                .addGroup(SideDashboardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton12)
                    .addComponent(jButton13)
                    .addComponent(jButton14))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
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
                        .addGap(0, 30, Short.MAX_VALUE)))
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
                .addGap(18, 26, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 305, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(55, Short.MAX_VALUE))
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
                .addGroup(SubjectsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(SubjectsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPane3)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, SubjectsLayout.createSequentialGroup()
                            .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 322, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton20, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton19)))
                    .addComponent(jLabel17)
                    .addComponent(jLabel18))
                .addContainerGap(40, Short.MAX_VALUE))
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
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 353, Short.MAX_VALUE)
                .addContainerGap())
        );

        MainContentPanel.add(Subjects, "card3");

        javax.swing.GroupLayout EnrollmentsLayout = new javax.swing.GroupLayout(Enrollments);
        Enrollments.setLayout(EnrollmentsLayout);
        EnrollmentsLayout.setHorizontalGroup(
            EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        EnrollmentsLayout.setVerticalGroup(
            EnrollmentsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 497, Short.MAX_VALUE)
        );

        MainContentPanel.add(Enrollments, "card6");

        javax.swing.GroupLayout GradesLayout = new javax.swing.GroupLayout(Grades);
        Grades.setLayout(GradesLayout);
        GradesLayout.setHorizontalGroup(
            GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        GradesLayout.setVerticalGroup(
            GradesLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 497, Short.MAX_VALUE)
        );

        MainContentPanel.add(Grades, "card4");

        javax.swing.GroupLayout ReportsLayout = new javax.swing.GroupLayout(Reports);
        Reports.setLayout(ReportsLayout);
        ReportsLayout.setHorizontalGroup(
            ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 600, Short.MAX_VALUE)
        );
        ReportsLayout.setVerticalGroup(
            ReportsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
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
                .addContainerGap(291, Short.MAX_VALUE))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 182, Short.MAX_VALUE)
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
                .addContainerGap(322, Short.MAX_VALUE))
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
                .addContainerGap(226, Short.MAX_VALUE))
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
                    .addContainerGap(252, Short.MAX_VALUE)
                    .addComponent(MainContentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 600, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                    .addComponent(MainContentPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 497, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 6, Short.MAX_VALUE)))
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
    }//GEN-LAST:event_jButton10ActionPerformed

    private void jButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton9ActionPerformed
        // TODO add your handling code here:
            showMainContent("card4");
    }//GEN-LAST:event_jButton9ActionPerformed

    private void jButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton8ActionPerformed
        // TODO add your handling code here:
            showMainContent("card6");
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
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JButton jButton7;
    private javax.swing.JButton jButton8;
    private javax.swing.JButton jButton9;
    private javax.swing.JComboBox<String> jComboBox1;
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
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
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
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTable jTable3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextField9;
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
}
