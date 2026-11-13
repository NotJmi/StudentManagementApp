package StudentManagementSystem;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

public class GradesPanel extends JPanel {

    private final Runnable onGradeSaved;

    private final JComboBox<GradeService.Student> studentBox = new JComboBox<>();
    private final JComboBox<String> subjectBox = new JComboBox<>();
    private final JTextField gradeField = new JTextField(6);
    private final JButton saveButton = new JButton("Save Grade");

    private final JTextField searchField = new JTextField(18);
    private final JButton searchButton = new JButton("Search");
    private final JButton refreshButton = new JButton("Refresh");

    private final JLabel summaryLabel = new JLabel(" ");
    private final JTable table = new JTable();

    private final List<GradeService.GradeRecord> records = new ArrayList<>();

    /**
     * @param onGradeSaved //CALLBACK WHEN A GRADE IS SAVED, MAY BE NULL
     */
    public GradesPanel(Runnable onGradeSaved) {
        this.onGradeSaved = onGradeSaved;
        buildUi();
        wireEvents();
    }

    //UI
    private void buildUi() {
        setLayout(new BorderLayout(0, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("Grades Management");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        JLabel subtitle = new JLabel(
            "Record grades for enrolled subjects. Passing grade is "
            + (int) GradeService.PASSING_SCORE + "."
        );

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.add(title);
        header.add(subtitle);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Enter Grade"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.WEST;

        c.gridx = 0; c.gridy = 0;
        form.add(new JLabel("Student"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        form.add(studentBox, c);

        c.gridx = 0; c.gridy = 1; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        form.add(new JLabel("Subject"), c);
        c.gridx = 1; c.fill = GridBagConstraints.HORIZONTAL; c.weightx = 1;
        form.add(subjectBox, c);

        c.gridx = 0; c.gridy = 2; c.fill = GridBagConstraints.NONE; c.weightx = 0;
        form.add(new JLabel("Grade (0-100)"), c);
        JPanel gradeRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        gradeRow.add(gradeField);
        gradeRow.add(Box.createHorizontalStrut(8));
        gradeRow.add(saveButton);
        c.gridx = 1;
        form.add(gradeRow, c);

        JPanel searchRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        searchRow.add(new JLabel("Search"));
        searchRow.add(searchField);
        searchRow.add(searchButton);
        searchRow.add(refreshButton);

        JPanel top = new JPanel(new BorderLayout(0, 6));
        top.add(header, BorderLayout.NORTH);
        top.add(form, BorderLayout.CENTER);
        top.add(searchRow, BorderLayout.SOUTH);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setModel(emptyModel());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(summaryLabel, BorderLayout.SOUTH);
    }

    private void wireEvents() {
        saveButton.addActionListener(e -> saveGrade());
        gradeField.addActionListener(e -> saveGrade());
        searchButton.addActionListener(e -> applyFilter());
        searchField.addActionListener(e -> applyFilter());
        refreshButton.addActionListener(e -> refresh());

        studentBox.addActionListener(e -> {
            fillSubjectsForSelectedStudent();
            updateSummary();
        });
        subjectBox.addActionListener(e -> showCurrentGrade());

        // When a row is selected, select the student and subject in the form.
        table.getSelectionModel().addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) {
                return;
            }
            int row = table.getSelectedRow();
            if (row < 0) {
                return;
            }
            selectStudent(Integer.parseInt(table.getValueAt(row, 0).toString()));
            subjectBox.setSelectedItem(table.getValueAt(row, 2));
        });
    }

    private static DefaultTableModel emptyModel() {
        return new DefaultTableModel(
            new String[]{
                "Student ID", "Student Name", "Subject",
                "Grade", "Equivalent", "Remarks"
            },
            0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    //LOAD
    private static final class LoadResult {
        List<GradeService.Student> students;
        List<GradeService.GradeRecord> records;
    }

    //LOAD STUDENTS AND RECORDS
    public void refresh() {
        refreshButton.setEnabled(false);
        saveButton.setEnabled(false);

        new SwingWorker<LoadResult, Void>() {
            @Override
            protected LoadResult doInBackground() throws Exception {
                LoadResult r = new LoadResult();
                r.students = GradeService.fetchStudents();
                r.records = GradeService.fetchRecords(r.students);
                return r;
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                saveButton.setEnabled(true);
                try {
                    LoadResult r = get();

                    GradeService.Student prevStudent =
                            (GradeService.Student) studentBox.getSelectedItem();
                    Object prevSubject = subjectBox.getSelectedItem();

                    records.clear();
                    records.addAll(r.records);

                    studentBox.removeAllItems();
                    for (GradeService.Student s : r.students) {
                        studentBox.addItem(s);
                    }
                    if (prevStudent != null) {
                        selectStudent(prevStudent.id);
                    }
                    fillSubjectsForSelectedStudent();
                    if (prevSubject != null) {
                        subjectBox.setSelectedItem(prevSubject);
                    }

                    applyFilter();

                } catch (Exception ex) {
                    showError("Unable to load grades", ex);
                }
            }
        }.execute();
    }

    private void selectStudent(int studentId) {
        for (int i = 0; i < studentBox.getItemCount(); i++) {
            if (studentBox.getItemAt(i).id == studentId) {
                studentBox.setSelectedIndex(i);
                return;
            }
        }
    }

    //LOAD SUBJECTS FOR SELECTED STUDENT
    private void fillSubjectsForSelectedStudent() {
        GradeService.Student student =
                (GradeService.Student) studentBox.getSelectedItem();

        subjectBox.removeAllItems();
        if (student != null) {
            for (GradeService.GradeRecord r : records) {
                if (r.studentId == student.id) {
                    subjectBox.addItem(r.subject);
                }
            }
        }
        showCurrentGrade();
    }

    //PUTS GRADE INTO FORM WHEN A ROW IS SELECTED
    private void showCurrentGrade() {
        GradeService.GradeRecord r = selectedRecord();
        gradeField.setText(r == null ? "" : r.gradeText());
    }

    private GradeService.GradeRecord selectedRecord() {
        GradeService.Student student =
                (GradeService.Student) studentBox.getSelectedItem();
        Object subject = subjectBox.getSelectedItem();
        if (student == null || subject == null) {
            return null;
        }
        for (GradeService.GradeRecord r : records) {
            if (r.studentId == student.id && r.subject.equals(subject)) {
                return r;
            }
        }
        return null;
    }

    //SEARCH
    private void applyFilter() {
        String search = searchField.getText().trim().toLowerCase();
        DefaultTableModel model = emptyModel();

        for (GradeService.GradeRecord r : records) {
            String haystack = (r.studentId + " " + r.studentName + " "
                    + r.subject + " " + r.remarks()).toLowerCase();

            if (search.isEmpty() || haystack.contains(search)) {
                model.addRow(new Object[]{
                    r.studentId,
                    r.studentName,
                    r.subject,
                    r.gradeText(),
                    r.equivalent(),
                    r.remarks()
                });
            }
        }

        table.setModel(model);
        updateSummary();
    }

    private void updateSummary() {
        GradeService.Student student =
                (GradeService.Student) studentBox.getSelectedItem();

        if (student == null) {
            summaryLabel.setText(" ");
            return;
        }

        List<GradeService.GradeRecord> mine = new ArrayList<>();
        int graded = 0;
        int failed = 0;
        for (GradeService.GradeRecord r : records) {
            if (r.studentId == student.id) {
                mine.add(r);
                if (r.isGraded()) {
                    graded++;
                    if ("Failed".equals(r.remarks())) {
                        failed++;
                    }
                }
            }
        }

        if (mine.isEmpty()) {
            summaryLabel.setText(student.fullName()
                    + ": not enrolled in any subjects.");
        } else if (graded == 0) {
            summaryLabel.setText(student.fullName() + ": "
                    + mine.size() + " subject(s), none graded yet.");
        } else {
            summaryLabel.setText(String.format(
                "%s: %d of %d graded, average %.2f, GWA %.2f, %d failed",
                student.fullName(), graded, mine.size(),
                GradeService.averageScore(mine),
                GradeService.gwa(mine),
                failed
            ));
        }
    }

    //SAVE
    private void saveGrade() {
        GradeService.Student student =
                (GradeService.Student) studentBox.getSelectedItem();
        String subject = (String) subjectBox.getSelectedItem();
        String gradeText = gradeField.getText().trim();

        if (student == null) {
            warn("Please choose a student.", "Missing Information");
            return;
        }
        if (subject == null) {
            warn(student.fullName() + " is not enrolled in any subjects.\n"
                + "Enroll them first, then record a grade.",
                "No Enrolled Subjects");
            return;
        }
        if (gradeText.isEmpty()) {
            warn("Please enter a grade.", "Missing Information");
            return;
        }

        double grade;
        try {
            grade = Double.parseDouble(gradeText);
            GradeService.validateScore(grade);
        } catch (IllegalArgumentException ex) {
            warn("Grade must be a number from 0 to 100.", "Invalid Grade");
            return;
        }

        saveButton.setEnabled(false);
        saveButton.setText("Saving...");

        new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return GradeService.saveGrade(student.id, subject, grade);
            }

            @Override
            protected void done() {
                saveButton.setEnabled(true);
                saveButton.setText("Save Grade");
                try {
                    boolean updated = get();
                    JOptionPane.showMessageDialog(GradesPanel.this,
                        (updated ? "Grade updated" : "Grade saved")
                        + " for " + student.fullName() + " in " + subject
                        + ":\n" + String.format("%.2f", grade)
                        + " -> " + GradeService.equivalent(grade)
                        + " (" + GradeService.remarks(grade) + ")",
                        "Success", JOptionPane.INFORMATION_MESSAGE);

                    refresh();
                    if (onGradeSaved != null) {
                        onGradeSaved.run();
                    }
                } catch (Exception ex) {
                    showError("Unable to save grade", ex);
                }
            }
        }.execute();
    }

    private void warn(String message, String title) {
        JOptionPane.showMessageDialog(this, message, title,
                JOptionPane.WARNING_MESSAGE);
    }

    private void showError(String title, Exception ex) {
        Throwable cause = ex.getCause() == null ? ex : ex.getCause();
        JOptionPane.showMessageDialog(this,
            title + ":\n" + cause.getMessage(),
            "Grades Error", JOptionPane.ERROR_MESSAGE);
    }
}