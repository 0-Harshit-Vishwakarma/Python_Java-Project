import javax.swing.*;
import java.awt.*;

public class SimpleCalendar {
    public static void main(String[] args) {

        JFrame f = new JFrame("Calendar");

        JLabel l1 = new JLabel("Enter Month (1-12):");
        JTextField t1 = new JTextField();
        JButton b = new JButton("Show");
        JTextArea area = new JTextArea();

        l1.setBounds(20, 20, 150, 30);
        t1.setBounds(180, 20, 100, 30);
        b.setBounds(120, 60, 80, 25);
        area.setBounds(20, 100, 260, 150);

        f.add(l1);
        f.add(t1);
        f.add(b);
        f.add(area);

        f.setSize(320, 320);
        f.setLayout(null);
        f.setVisible(true);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // ✅ INPUT BLOCK (1-12 only)
        t1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent e) {

                char c = e.getKeyChar();

                if (!Character.isDigit(c)) {
                    e.consume();
                    return;
                }

                String text = t1.getText() + c;

                try {
                    int val = Integer.parseInt(text);

                    if (val > 12) {
                        e.consume();
                        Toolkit.getDefaultToolkit().beep();
                        JOptionPane.showMessageDialog(f,
                                "Only 1 to 12 allowed!",
                                "Invalid Input",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    e.consume();
                }
            }
        });

        b.addActionListener(e -> {
            try {
                int month = Integer.parseInt(t1.getText().trim());

                if (month < 1 || month > 12) {
                    throw new Exception("Month must be 1 to 12 only!");
                }

                String[] months = {
                        "January","February","March","April",
                        "May","June","July","August",
                        "September","October","November","December"
                };

                int[] days = {
                        31,28,31,30,
                        31,30,31,31,
                        30,31,30,31
                };

                area.setText("Month: " + months[month - 1]
                        + "\nDays: " + days[month - 1]);

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(f,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}