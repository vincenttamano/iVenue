package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Scanner;

public class AmenityAdmin implements AdminManagement<Amenity> {

    private final MongoCollection<Document> collection;

    public AmenityAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("amenities");
    }

    public void create(Scanner input) {
        System.out.println("\n----ADD NEW AMENITY----");

        System.out.print("Enter Name: ");
        String name = input.nextLine();

        System.out.print("Enter Description: ");
        String description = input.nextLine();

        System.out.print("Enter Quantity: ");
        int quantity = Integer.parseInt(input.nextLine());

        System.out.print("Enter Price: ");
        double price = Double.parseDouble(input.nextLine());

        // --- Get max amenityId from MongoDB ---
        int maxId = 0;
        Document lastAmenity = collection.find().sort(new Document("amenityId", -1)).first();
        if (lastAmenity != null) {
            maxId = lastAmenity.getInteger("amenityId");
        }

        Amenity newAmenity = new Amenity(maxId + 1, name, description, quantity, price);

        // Insert into DB
        Document doc = new Document("amenityId", newAmenity.getAmenityId())
                .append("name", newAmenity.getName())
                .append("description", newAmenity.getDescription())
                .append("quantity", newAmenity.getQuantity())
                .append("price", newAmenity.getPrice());

        collection.insertOne(doc);
        System.out.println("Amenity added successfully. ID: " + newAmenity.getAmenityId());
    }

    @Override
    public void update(Scanner input) {
        System.out.println("----UPDATE AMENITY----");
        System.out.print("Enter Amenity ID: ");
        int id = Integer.parseInt(input.nextLine());

        Document doc = collection.find(new Document("amenityId", id)).first();
        if (doc == null) {
            System.out.println("Amenity not found.");
            return;
        }

        System.out.println("Leave field blank to keep current value.");

        System.out.print("Enter new Name (" + doc.getString("name") + "): ");
        String name = input.nextLine();
        if (name.isEmpty()) name = doc.getString("name");

        System.out.print("Enter new Description (" + doc.getString("description") + "): ");
        String description = input.nextLine();
        if (description.isEmpty()) description = doc.getString("description");

        System.out.print("Enter new Quantity (" + doc.getInteger("quantity") + "): ");
        String quantityInput = input.nextLine();
        int quantity = quantityInput.isEmpty() ? doc.getInteger("quantity") : Integer.parseInt(quantityInput);

        System.out.print("Enter new Price (" + doc.getDouble("price") + "): ");
        String priceInput = input.nextLine();
        double price = priceInput.isEmpty() ? doc.getDouble("price") : Double.parseDouble(priceInput);

        Document updateFields = new Document()
                .append("name", name)
                .append("description", description)
                .append("quantity", quantity)
                .append("price", price);

        collection.updateOne(new Document("amenityId", id), new Document("$set", updateFields));
        System.out.println("Amenity updated successfully.");
    }

    @Override
    public void delete(Scanner input) {
        System.out.println("----DELETE AMENITY----");
        System.out.print("Enter Amenity ID: ");
        int id = Integer.parseInt(input.nextLine());

        Document doc = collection.find(new Document("amenityId", id)).first();
        if (doc == null) {
            System.out.println("Amenity not found.");
            return;
        }

        collection.deleteOne(new Document("amenityId", id));
        System.out.println("Amenity deleted successfully.");
    }

    @Override
    public void displayAll() {
        System.out.println("----ALL AMENITIES----");
        for (Document doc : collection.find()) {
            System.out.println("Amenity ID: " + doc.getInteger("amenityId"));
            System.out.println("Name: " + doc.getString("name"));
            System.out.println("Description: " + doc.getString("description"));
            System.out.println("Quantity: " + doc.getInteger("quantity"));
            System.out.println("Price: ₱" + doc.getDouble("price"));
            System.out.println("---------------------------");
        }
    }
}
