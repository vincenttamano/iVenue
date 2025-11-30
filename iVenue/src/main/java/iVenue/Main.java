package iVenue;

import com.mongodb.client.MongoDatabase;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);
        int choice;

        System.out.println("Connecting to MongoDB...");
        MongoDatabase db = MongoDb.getDatabase();
        System.out.println("MongoDB connected successfully.\n");
        // Ensure there is a single admin account present
        UserStore.ensureAdminExists();

        do {
            System.out.println("\n===== VENYO BOOKING SYSTEM =====");
            System.out.println("1. Login");
            System.out.println("2. Register customer");
            System.out.println("3. Exit");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    new User("","",0).login();
                    break;
                case 2:
                    Customer.registerCustomer(input);
                    break;
                case 3:
                    System.out.println("Exiting system.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 3);
    }
}