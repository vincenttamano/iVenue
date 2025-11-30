package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Scanner;

public class VenueAdmin implements AdminManagement<Venue> {

    private final MongoCollection<Document> collection;

    public VenueAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("venues");
    }

    @Override
    public void create(Scanner input) {
        System.out.println("\n---- ADD NEW VENUE ----");

        System.out.print("Enter Venue Name: ");
        String name = input.nextLine();

        System.out.print("Enter Description: ");
        String description = input.nextLine();

        System.out.print("Enter Capacity: ");
        int capacity = Integer.parseInt(input.nextLine());

        System.out.print("Enter Location: ");
        String location = input.nextLine();

        System.out.print("Enter Price (0 if free): ");
        double price = Double.parseDouble(input.nextLine());

        boolean availability = true;

        // Get max venueId from MongoDB
        int maxId = 0;
        Document lastVenue = collection.find().sort(new Document("venueId", -1)).first();
        if (lastVenue != null) {
            maxId = lastVenue.getInteger("venueId");
        }

        Venue newVenue = new Venue(maxId + 1, name, description, capacity, availability, location, price);

        // Insert into DB
        Document doc = new Document("venueId", newVenue.getVenueId())
                .append("name", newVenue.getName())
                .append("description", newVenue.getDescription())
                .append("capacity", newVenue.getCapacity())
                .append("availability", newVenue.isAvailability())
                .append("location", newVenue.getLocation())
                .append("price", newVenue.getPrice())
                .append("isFree", newVenue.isFree());
        collection.insertOne(doc);
        System.out.println("Venue added successfully. Venue ID: " + newVenue.getVenueId());
    }

    @Override
    public void update(Scanner input) {
        System.out.println("---- UPDATE VENUE ----");
        System.out.print("Enter Venue ID to update: ");
        int id = Integer.parseInt(input.nextLine());

        Document doc = collection.find(new Document("venueId", id)).first();
        if (doc == null) {
            System.out.println("Venue not found!");
            return;
        }

        System.out.println("Leave field blank to keep current value.");

        System.out.print("Enter new Name (" + doc.getString("name") + "): ");
        String name = input.nextLine();
        if (name.isEmpty()) name = doc.getString("name");

        System.out.print("Enter new Description (" + doc.getString("description") + "): ");
        String description = input.nextLine();
        if (description.isEmpty()) description = doc.getString("description");

        System.out.print("Enter new Capacity (" + doc.getInteger("capacity") + "): ");
        String capacityInput = input.nextLine();
        int capacity = capacityInput.isEmpty() ? doc.getInteger("capacity") : Integer.parseInt(capacityInput);

        System.out.print("Enter new Location (" + doc.getString("location") + "): ");
        String location = input.nextLine();
        if (location.isEmpty()) location = doc.getString("location");

        System.out.print("Enter new Price (" + doc.getDouble("price") + "): ");
        String priceInput = input.nextLine();
        double price = priceInput.isEmpty() ? doc.getDouble("price") : Double.parseDouble(priceInput);

        System.out.print("Is the venue available? (Y/N, leave blank to keep current): ");
        String availInput = input.nextLine().toLowerCase();
        boolean availability = availInput.isEmpty() ? doc.getBoolean("availability") : availInput.charAt(0) == 'y';

        // Update document
        boolean isFree = (price == 0);

        // Update document fields
        Document updateFields = new Document()
                .append("name", name)
                .append("description", description)
                .append("capacity", capacity)
                .append("location", location)
                .append("availability", availability)
                .append("price", price)
                .append("isFree", isFree);


        collection.updateOne(new Document("venueId", id), new Document("$set", updateFields));
        System.out.println("Venue updated successfully!");
    }

    @Override
    public void delete(Scanner input) {
        System.out.println("---- DELETE VENUE ----");
        System.out.print("Enter Venue ID to delete: ");
        int id = Integer.parseInt(input.nextLine());

        Document doc = collection.find(new Document("venueId", id)).first();
        if (doc == null) {
            System.out.println("Venue not found!");
            return;
        }

        collection.deleteOne(new Document("venueId", id));
        System.out.println("Venue deleted successfully!");
    }

    @Override
    public void displayAll() {
        System.out.println("---- ALL VENUES ----");
        for (Document doc : collection.find()) {
            String availability = doc.getBoolean("availability") ? "Available" : "Booked";

            double price = doc.getDouble("price");
            String priceLabel = (price == 0) ? "FREE" : "₱" + price;

            System.out.println("ID: " + doc.getInteger("venueId"));
            System.out.println("Name: " + doc.getString("name"));
            System.out.println("Description: " + doc.getString("description"));
            System.out.println("Capacity: " + doc.getInteger("capacity"));
            System.out.println("Availability: " + availability);
            System.out.println("Location: " + doc.getString("location"));
            System.out.println("Price: " + priceLabel);
            System.out.println("---------------------------");
        }
    }
}
