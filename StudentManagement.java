import java.util.ArrayList;
import java.util.Scanner;
import javax.swing.JOptionPane;

class Student {
    int rollNo;
    String name;
    int marks;

    Student(int rollNo, String name, int marks) {
        this.rollNo = rollNo;
        this.name = name;
        this.marks = marks;
    }
}

public class StudentManagement {

    public static void main(String[] args) {

        ArrayList<Student> students = new ArrayList<>();
        Scanner sc = new Scanner(System.in);

        JOptionPane.showMessageDialog(null, "Welcome to Student Management System 🚀");

        while (true) {

            String menu = "1. Add Student\n2. View Students\n3. Search Student\n4. Delete Student\n5. Exit";
            String input = JOptionPane.showInputDialog(menu);

            if (input == null) {
                JOptionPane.showMessageDialog(null, "Exited!");
                break;
            }

            int choice = Integer.parseInt(input);

            switch (choice) {

                case 1:
                    int roll = Integer.parseInt(JOptionPane.showInputDialog("Enter Roll No:"));
                    String name = JOptionPane.showInputDialog("Enter Name:");
                    int marks = Integer.parseInt(JOptionPane.showInputDialog("Enter Marks:"));

                    students.add(new Student(roll, name, marks));
                    JOptionPane.showMessageDialog(null, "Student Added Successfully!");
                    break;

                case 2:
                    StringBuilder list = new StringBuilder();

                    if (students.isEmpty()) {
                        list.append("No students found.");
                    } else {
                        for (Student s : students) {
                            list.append("Roll: ").append(s.rollNo)
                                    .append(", Name: ").append(s.name)
                                    .append(", Marks: ").append(s.marks)
                                    .append("\n");
                        }
                    }

                    JOptionPane.showMessageDialog(null, list.toString());
                    break;

                case 3:
                    int searchRoll = Integer.parseInt(JOptionPane.showInputDialog("Enter Roll No to search:"));

                    boolean found = false;

                    for (Student s : students) {
                        if (s.rollNo == searchRoll) {
                            JOptionPane.showMessageDialog(null,
                                    "Found Student:\nName: " + s.name + "\nMarks: " + s.marks);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        JOptionPane.showMessageDialog(null, "Student Not Found!");
                    }
                    break;

                case 4:
                    int delRoll = Integer.parseInt(JOptionPane.showInputDialog("Enter Roll No to delete:"));

                    boolean removed = false;

                    for (int i = 0; i < students.size(); i++) {
                        if (students.get(i).rollNo == delRoll) {
                            students.remove(i);
                            JOptionPane.showMessageDialog(null, "Student Deleted!");
                            removed = true;
                            break;
                        }
                    }

                    if (!removed) {
                        JOptionPane.showMessageDialog(null, "Student Not Found!");
                    }
                    break;

                case 5:
                    JOptionPane.showMessageDialog(null, "Exiting System 👋");
                    System.exit(0);

                default:
                    JOptionPane.showMessageDialog(null, "Invalid Choice!");
            }
        }
    }
}