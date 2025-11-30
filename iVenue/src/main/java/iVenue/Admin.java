package iVenue;

import java.util.Scanner;
import org.bson.Document;

public class Admin {
    private BookingAdmin bookingAdmin;
    private VenueAdmin venueAdmin;
    private AmenityAdmin amenityAdmin;
    private CustomerAdmin customerAdmin;
    private Scanner input;

    public Admin(Scanner input) {
        this.input = input;
        this.bookingAdmin = new BookingAdmin();
        this.venueAdmin = new VenueAdmin();
        this.amenityAdmin = new AmenityAdmin();
        this.customerAdmin = new CustomerAdmin();
    }

    public void adminMenu() {
        int choice;
        do {
            System.out.println("\n===== ADMIN MANAGEMENT SYSTEM =====");
            System.out.println("1. Manage Bookings");
            System.out.println("2. Manage Venues");
            System.out.println("3. Manage Amenities");
            System.out.println("4. Manage Customers");
            System.out.println("5. Manage Users");
            System.out.println("6. Exit");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    manageBookings();
                    break;
                case 2:
                    manageVenues();
                    break;
                case 3:
                    manageAmenities();
                    break;
                case 4:
                    manageCustomers();
                    break;
                case 5:
                    manageUsers();
                    break;
                case 6:
                    System.out.println("Exiting Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 6);
    }

    private void manageUsers() {
        int choice;
        do {
            System.out.println("\n--- USER MANAGEMENT ---");
            System.out.println("1. Display All Users");
            System.out.println("2. Delete User by ID");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    User.displayAllUsers();
                    break;
                case 2:
                    System.out.print("Enter User ID to delete: ");
                    int UserId = Integer.parseInt(input.nextLine());
                    User.deleteUser(UserId);
                    break;
                case 3:
                    System.out.println("Returning to Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 3);
    }

    private void manageCustomers() {
        int choice;
        do {
            System.out.println("\n--- CUSTOMER MANAGEMENT ---");
            System.out.println("1. Display All Customers");
            System.out.println("2. Update Customer by ID");
            System.out.println("3. Delete Customer by ID");
            System.out.println("4. Back");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    System.out.println("---- ALL CUSTOMERS ----");
                    User.displayAllUsers();
                    break;
                case 2:
                    customerAdmin.update(input);
                    break;
                case 3:
                    int UserId = Integer.parseInt(input.nextLine());
                    User.deleteUser(UserId);
                    break;
                case 4:
                    System.out.println("Returning to Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 4);
    }

    private void manageBookings() {
        int choice;
        do {
            System.out.println("\n--- BOOKING MANAGEMENT ---");
            System.out.println("1. Create Booking");
            System.out.println("2. Update Booking");
            System.out.println("3. Delete Booking");
            System.out.println("4. Display All Bookings");
            System.out.println("5. View Booking History");
            System.out.println("6. Back");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    bookingAdmin.create(input);
                    break;
                case 2:
                    bookingAdmin.update(input);
                    break;
                case 3:
                    bookingAdmin.delete(input);
                    break;
                case 4:
                    bookingAdmin.displayAll();
                    break;
                case 5:
                    bookingAdmin.displayHistory(input);
                    break;
                case 6:
                    System.out.println("Returning to Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 6);
    }

    private void manageVenues() {
        int choice;
        do {
            System.out.println("\n--- VENUE MANAGEMENT ---");
            System.out.println("1. Create Venue");
            System.out.println("2. Update Venue");
            System.out.println("3. Delete Venue");
            System.out.println("4. Display All Venues");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    venueAdmin.create(input);
                    break;
                case 2:
                    venueAdmin.update(input);
                    break;
                case 3:
                    venueAdmin.delete(input);
                    break;
                case 4:
                    venueAdmin.displayAll();
                    break;
                case 5:
                    System.out.println("Returning to Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 5);
    }

    private void manageAmenities() {
        int choice;
        do {
            System.out.println("\n--- AMENITY MANAGEMENT ---");
            System.out.println("1. Create Amenity");
            System.out.println("2. Update Amenity");
            System.out.println("3. Delete Amenity");
            System.out.println("4. Display All Amenities");
            System.out.println("5. Back");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    amenityAdmin.create(input);
                    break;
                case 2:
                    amenityAdmin.update(input);
                    break;
                case 3:
                    amenityAdmin.delete(input);
                    break;
                case 4:
                    amenityAdmin.displayAll();
                    break;
                case 5:
                    System.out.println("Returning to Admin Menu.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 5);
    }

    // Prompt for a customer ID, load the customer, and invoke the shared update routine.
    private void updateCustomerById() {
        System.out.print("Enter Customer User ID to update: ");
        int id;
        try {
            id = Integer.parseInt(input.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID.");
            return;
        }

        Document doc = MongoDb.getDatabase().getCollection("users").find(new Document("userId", id)).first();
        if (doc == null) {
            System.out.println("User not found.");
            return;
        }
        String type = doc.getString("userType");
        if (type == null || !type.equalsIgnoreCase("customer")) {
            System.out.println("Specified user is not a customer.");
            return;
        }

        Customer customer = new Customer(
                doc.getString("username"),
                doc.getString("password"),
                doc.getInteger("userId"),
                doc.getString("firstName"),
                doc.getString("lastName"),
                doc.getString("contactNumber"),
                doc.getString("email"),
                "customer"
        );

        Customer.updateProfile(input, customer);
    }
}
