package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class Venue {
    private int venueId; // remove static counter
    private String name;
    private String description;
    private int capacity;
    private boolean availability;
    private String location;
    private double price;
    private boolean isFree;

    public Venue(int venueId, String name, String description, int capacity,boolean availability, String location, double price) {
        this.venueId = venueId;
        this.name = name;
        this.description = description;
        this.capacity = capacity;
        this.availability = availability;
        this.location = location;
        this.price = Math.max(price, 0);
        this.isFree = (this.price == 0);
    }

    public int getVenueId() {
        return venueId;
    }

    public void setVenueId(int venueId) {
        this.venueId = venueId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isAvailability() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = Math.max(price, 0);
        this.isFree = (this.price == 0);
    }
    public void setFree(boolean free) {
        isFree = free;
    }

    public boolean isFree() {
        return this.isFree;
    }


    public String getPriceLabel() {
        return (price == 0) ? "FREE" : "₱" + price;
    }


    // -------- DISPLAY METHODS --------

    public static void displayAvailableVenues() {
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("venues");

        System.out.println("Available Venues:");
        for (Document doc : collection.find(new Document("availability", true))) {

            double price = doc.getDouble("price");
            String priceLabel = (price == 0) ? "FREE" : "₱" + price;

            System.out.println("ID: " + doc.getInteger("venueId"));
            System.out.println("Name: " + doc.getString("name"));
            System.out.println("Description: " + doc.getString("description"));
            System.out.println("Capacity: " + doc.getInteger("capacity"));
            System.out.println("Location: " + doc.getString("location"));
            System.out.println("Price: " + priceLabel);
            System.out.println("---------------------------");
        }
    }

    public static void displayAllVenues() {
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("venues");

        System.out.println("ALL VENUES");

        // AVAILABLE
        System.out.println("\nAvailable Venues:");
        for (Document doc : collection.find(new Document("availability", true))) {

            double price = doc.getDouble("price");
            String priceLabel = (price == 0) ? "FREE" : "₱" + price;

            System.out.println("ID: " + doc.getInteger("venueId"));
            System.out.println("Name: " + doc.getString("name"));
            System.out.println("Description: " + doc.getString("description"));
            System.out.println("Capacity: " + doc.getInteger("capacity"));
            System.out.println("Availability: Available");
            System.out.println("Location: " + doc.getString("location"));
            System.out.println("Price: " + priceLabel);
            System.out.println("---------------------------");
        }

        // BOOKED
        System.out.println("\nBooked Venues:");
        for (Document doc : collection.find(new Document("availability", false))) {

            double price = doc.getDouble("price");
            String priceLabel = (price == 0) ? "FREE" : "₱" + price;

            System.out.println("ID: " + doc.getInteger("venueId"));
            System.out.println("Name: " + doc.getString("name"));
            System.out.println("Description: " + doc.getString("description"));
            System.out.println("Capacity: " + doc.getInteger("capacity"));
            System.out.println("Availability: Booked");
            System.out.println("Location: " + doc.getString("location"));
            System.out.println("Price: " + priceLabel);
            System.out.println("---------------------------");
        }
    }

    // -------- Get Venue by ID --------

    public static Venue getVenue(int venueId) {
        MongoDatabase database = MongoDb.getDatabase();
        MongoCollection<Document> collection = database.getCollection("venues");

        Document doc = collection.find(new Document("venueId", venueId)).first();

        if (doc != null) {
            return new Venue(
                    doc.getInteger("venueId"),
                    doc.getString("name"),
                    doc.getString("description"),
                    doc.getInteger("capacity"),
                    doc.getBoolean("availability"),
                    doc.getString("location"),
                    doc.getDouble("price")
            );
        }

        System.out.println("Venue not found!");
        return null;
    }

}