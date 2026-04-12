import java.util.Random;
import java.util.Scanner;

public class PasswordGenerator {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);
        Random random = new Random();

        // Characters set
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                      + "abcdefghijklmnopqrstuvwxyz"
                      + "0123456789"
                      + "!@#$%^&*()";

        System.out.print("Enter password length: ");
        int length = sc.nextInt();

        String password = "";

        for (int i = 0; i < length; i++) {
            int index = random.nextInt(chars.length());
            password += chars.charAt(index);
        }

        System.out.println("Generated Password: " + password);

        sc.close();
    }
}