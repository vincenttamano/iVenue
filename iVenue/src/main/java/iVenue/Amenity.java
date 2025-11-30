package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Amenity {
    private int amenityId;
    private String name;
    private String description;
    private int quantity;
    private double price; // new price field

    public Amenity(int amenityId, String name, String description, int quantity, double price) {
        this.amenityId = amenityId;
        this.name = name;
        this.description = description;
        this.quantity = quantity;
        this.price = price;
    }

    // --- Getters and Setters ---
    public int getAmenityId() { return amenityId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; } // new getter
    public void setPrice(double price) { this.price = price; } // new setter

    // --- Display all amenities ---
    public static void displayAmenities() {
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("amenities");

        if (collection.countDocuments() == 0) {
            System.out.println("No amenities available.");
            return;
        }

        for (Document doc : collection.find()) {
            System.out.println("ID: " + doc.getInteger("amenityId"));
            System.out.println("Name: " + doc.getString("name"));
            System.out.println("Description: " + doc.getString("description"));
            System.out.println("Quantity: " + doc.getInteger("quantity"));
            System.out.println("Price: ₱" + doc.getDouble("price")); // display price
            System.out.println("---------------------------");
        }
    }

    // --- Get Amenity by ID ---
    public static Amenity getAmenity(int amenityId) {
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("amenities");
        Document doc = collection.find(new Document("amenityId", amenityId)).first();
        if (doc != null) {
            return new Amenity(
                    doc.getInteger("amenityId"),
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getInteger("quantity"),
                    doc.getDouble("price") // fetch price
            );
        }
        return null;
    }
}
