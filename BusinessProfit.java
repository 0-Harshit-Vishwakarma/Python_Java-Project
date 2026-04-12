import java.util.Scanner;

public class BusinessProfit {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        // Input
        System.out.print("Enter total revenue: ");
        double revenue = scanner.nextDouble();
        System.out.print("Enter cost of goods sold: ");
        double costOfGoodsSold = scanner.nextDouble();
        System.out.print("Enter operating expenses: ");
        double operatingExpenses = scanner.nextDouble();
        System.out.print("Enter other income: ");
        double otherIncome = scanner.nextDouble();
        System.out.print("Enter other expenses: ");
        double otherExpenses = scanner.nextDouble();
        // Calculations
        double grossProfit = revenue - costOfGoodsSold;
        double netProfit = grossProfit - operatingExpenses + otherIncome - otherExpenses;
        // Output
        System.out.println("Gross Profit: " + grossProfit);
        System.out.println("Net Profit: " + netProfit);
        // Profit status
        if (netProfit > 0) {
            System.out.println("Business is profitable");
        } else if (netProfit < 0) {
            System.out.println("Business is running at a loss");
        } else {
            System.out.println("Business is breaking even");
        }
        scanner.close();
    }
}