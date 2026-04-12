import javax.swing.*;
import java.awt.*;

public class Calculator {

    public static void main(String[] args) {

        JFrame frame = new JFrame("Mini Calc");
        frame.setSize(260, 320);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(2, 2));
        frame.setLocationRelativeTo(null);

        // ===== INPUT =====
        JTextField num1 = new JTextField();
        JTextField num2 = new JTextField();

        num1.setFont(new Font("Arial", Font.PLAIN, 11));
        num2.setFont(new Font("Arial", Font.PLAIN, 11));

        JPanel input = new JPanel(new GridLayout(2, 2, 2, 2));
        input.add(new JLabel("N1"));
        input.add(num1);
        input.add(new JLabel("N2"));
        input.add(num2);

        // ===== BUTTON PANEL (VERY COMPACT) =====
        JPanel panel = new JPanel(new GridLayout(2, 3, 2, 2));

        JButton add = new JButton("+");
        JButton sub = new JButton("-");
        JButton mul = new JButton("*");
        JButton div = new JButton("/");
        JButton mod = new JButton("%");
        JButton fact = new JButton("!");

        JButton[] btns = {add, sub, mul, div, mod, fact};

        for (JButton b : btns) {
            b.setFont(new Font("Arial", Font.BOLD, 9));
            b.setMargin(new Insets(0, 0, 0, 0)); // 🔥 no padding
        }

        panel.add(add);
        panel.add(sub);
        panel.add(mul);
        panel.add(div);
        panel.add(mod);
        panel.add(fact);

        // ===== RESULT =====
        JLabel result = new JLabel("Result", SwingConstants.CENTER);
        result.setFont(new Font("Arial", Font.BOLD, 11));

        // ===== LOGIC =====
        add.addActionListener(e -> calc(num1, num2, result, "+"));
        sub.addActionListener(e -> calc(num1, num2, result, "-"));
        mul.addActionListener(e -> calc(num1, num2, result, "*"));
        div.addActionListener(e -> calc(num1, num2, result, "/"));
        mod.addActionListener(e -> calc(num1, num2, result, "%"));
        fact.addActionListener(e -> fact(num1, result));

        // ===== FRAME =====
        frame.add(input, BorderLayout.NORTH);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(result, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    static void calc(JTextField a1, JTextField a2, JLabel r, String op) {
        try {
            double a = Double.parseDouble(a1.getText());
            double b = Double.parseDouble(a2.getText());

            double res = switch (op) {
                case "+" -> a + b;
                case "-" -> a - b;
                case "*" -> a * b;
                case "/" -> (b == 0) ? Double.NaN : a / b;
                case "%" -> a % b;
                default -> 0;
            };

            r.setText("Result: " + res);

        } catch (Exception e) {
            r.setText("Error");
        }
    }

    static void fact(JTextField n1, JLabel r) {
        try {
            int n = Integer.parseInt(n1.getText());
            long f = 1;

            for (int i = 1; i <= n; i++) f *= i;

            r.setText("Result: " + f);

        } catch (Exception e) {
            r.setText("Invalid");
        }
    }
}