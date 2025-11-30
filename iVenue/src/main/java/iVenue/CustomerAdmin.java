package iVenue;

import org.bson.Document;

public class CustomerAdmin implements AdminManagement<Customer> {
    @Override
    public void update(java.util.Scanner input) {
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
    
    @Override
    public void create(java.util.Scanner input) {
        System.out.println("Create customer not implemented.");
    }

    @Override
    public void delete(java.util.Scanner input) {
        System.out.println("Delete customer not implemented.");
    }

    @Override
    public void displayAll() {
        System.out.println("Display all customers not implemented.");
    }
    
}
