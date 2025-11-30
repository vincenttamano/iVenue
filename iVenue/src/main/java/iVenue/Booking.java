package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class Booking {
    private int bookingId;
    private Venue venue;
    private Date date;
    private PaymentStatus paymentStatus; // enum
    private BookingStatus bookingStatus; // enum
    private String purpose;
    private LinkedList<Amenity> amenities; // keep Amenity objects
    private String username;

    private static LinkedList<Booking> bookings = new LinkedList<>();

    public Booking(int bookingId, Venue venue, Date date,
                   PaymentStatus paymentStatus, BookingStatus bookingStatus,
                   String purpose, String username) {
        this.bookingId = bookingId;
        this.venue = venue;
        this.date = date;
        this.paymentStatus = paymentStatus;
        this.bookingStatus = bookingStatus;
        this.purpose = purpose;
        this.amenities = new LinkedList<>();
        this.username = username;
    }



    public int getBookingId() { return bookingId; }
    public void setBookingId(int bookingId) { this.bookingId = bookingId; }

    public Venue getVenue() { return venue; }
    public void setVenue(Venue venue) { this.venue = venue; }

    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }

    public PaymentStatus getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(PaymentStatus paymentStatus) { this.paymentStatus = paymentStatus; }

    public BookingStatus getBookingStatus() { return bookingStatus; }
    public void setBookingStatus(BookingStatus bookingStatus) { this.bookingStatus = bookingStatus; }

    public String getPurpose() { return purpose; }
    public void setPurpose(String purpose) { this.purpose = purpose; }

    public LinkedList<Amenity> getAmenities() { return amenities; }
    public void setAmenities(LinkedList<Amenity> amenities) { this.amenities = amenities; }

    public static LinkedList<Booking> getBookings() { return bookings; }
    public static void setBookings(LinkedList<Booking> bookings) { Booking.bookings = bookings; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }




    // CREATE BOOKING
    public static void createBooking(Customer customer) {
        Scanner sc = new Scanner(System.in);

        Venue.displayAvailableVenues();
        System.out.print("Choose venue ID: ");
        int vid = Integer.parseInt(sc.nextLine().trim());
        Venue chosen = Venue.getVenue(vid);
        if (chosen == null) { System.out.println("Invalid venue."); return; }

        Amenity.displayAmenities();
        LinkedList<Document> selectedAmenities = new LinkedList<>();
        while (true) {
            System.out.print("Enter Amenity ID to add (0 to stop): ");
            int aid = Integer.parseInt(sc.nextLine().trim());
            if (aid == 0) break;
            Amenity am = Amenity.getAmenity(aid);
            if (am != null) {
                System.out.print("Enter quantity for " + am.getName() + " (max " + am.getQuantity() + "): ");
                int qty = Integer.parseInt(sc.nextLine().trim());
                selectedAmenities.add(new Document("amenityId", aid).append("quantity", qty).append("price", am.getPrice() * qty));
                System.out.println("Added " + qty + " x " + am.getName());
            } else System.out.println("Amenity not found.");
        }

        System.out.print("Purpose: ");
        String purpose = sc.nextLine().trim();

        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        int maxId = 0;
        Document last = collection.find().sort(new Document("bookingId", -1)).first();
        if (last != null) maxId = last.getInteger("bookingId");

        int userId = customer.getUserId();
        Document userDoc = MongoDb.getDatabase().getCollection("users").find(new Document("userId", userId)).first();
        Document bookedBy = new Document("userId", userId)
                .append("username", customer.getUsername())
                .append("firstName", userDoc != null ? userDoc.getString("firstName") : customer.getFirstName())
                .append("lastName", userDoc != null ? userDoc.getString("lastName") : customer.getLastName())
                .append("contactNumber", userDoc != null ? userDoc.getString("contactNumber") : customer.getContactNumber())
                .append("email", userDoc != null ? userDoc.getString("email") : customer.getEmail());

        collection.insertOne(new Document("bookingId", maxId + 1)
                .append("venueId", chosen.getVenueId())
                .append("venueName", chosen.getName())
                .append("userId", userId)
                .append("bookedBy", bookedBy)
                .append("date", new Date().toString())
                .append("paymentStatus", PaymentStatus.Pending.name())
                .append("bookingStatus", BookingStatus.Pending.name())
                .append("purpose", purpose)
                .append("amenities", selectedAmenities)
                .append("price", chosen.getPrice())
                .append("isFree", chosen.isFree()));

        MongoDb.getDatabase().getCollection("venues")
                .updateOne(new Document("venueId", chosen.getVenueId()), new Document("$set", new Document("availability", false)));

        System.out.println("Booking created with ID: " + (maxId + 1));
    }



    // CANCEL BOOKING
    public static void cancelBooking(Customer customer) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Booking ID to cancel: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");

        Document doc = collection.find(
                        new Document("bookingId", id).append("userId", customer.getUserId()))
                .first();

        if (doc == null) {
            System.out.println("Booking not found or not your booking.");
            return;
        }

        // Update both bookingStatus and paymentStatus to Cancelled
        collection.updateOne(
                new Document("bookingId", id),
                new Document("$set", new Document("bookingStatus", BookingStatus.Cancelled.name())
                        .append("paymentStatus", PaymentStatus.Cancelled.name()))
        );

        int venueId = doc.getInteger("venueId");

        MongoDb.getDatabase().getCollection("venues")
                .updateOne(new Document("venueId", venueId),
                        new Document("$set", new Document("availability", true)));
        String username = "N/A";
        if (doc.containsKey("bookedBy")) {
            Document bookedBy = (Document) doc.get("bookedBy");
            username = bookedBy.getString("username") == null ? "N/A" : bookedBy.getString("username");
        }

        System.out.println("Booking cancelled and payment status set to Cancelled.");
    }


    // VIEW OWN BOOKINGS
    // VIEW OWN BOOKINGS
    public static void viewBookingDetails(Customer customer) {
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");
        boolean found = false;

        for (Document doc : collection.find(new Document("userId", customer.getUserId()))) {
            found = true;
            System.out.println("Booking ID: " + doc.getInteger("bookingId"));
            System.out.println("Venue: " + doc.getString("venueName"));
            System.out.println("Purpose: " + doc.getString("purpose"));
            System.out.println("Status: " + doc.getString("bookingStatus"));
            System.out.println("Price: " + doc.getDouble("price"));

            // Display amenities nicely
            if (doc.containsKey("amenities")) {
                System.out.println("Amenities selected:");
                for (Object obj : doc.getList("amenities", Object.class)) {
                    if (obj instanceof Document aDoc) {
                        int aid = aDoc.getInteger("amenityId");
                        int qty = aDoc.getInteger("quantity");
                        double price = aDoc.getDouble("price");

                        Amenity a = Amenity.getAmenity(aid);
                        String name = (a != null) ? a.getName() : "Unknown Amenity";

                        System.out.println(" - " + name + " x" + qty + " (₱" + price + ")");
                    }
                }
            } else {
                System.out.println("Amenities: None");
            }

            System.out.println("---------------------------");
        }

        if (!found)
            System.out.println("You have no bookings.");
    }


    // CHECK STATUS
    public static void checkStatus(Customer customer) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Booking ID to check status: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");

        Document doc = collection.find(
                        new Document("bookingId", id).append("userId", customer.getUserId()))
                .first();

        if (doc == null) {
            System.out.println("Booking not found or not your booking.");
            return;
        }

        System.out.println("Status: " + doc.getString("bookingStatus"));
    }

    // PAY BOOKING
    public static void payBooking(Customer customer) {
        Scanner sc = new Scanner(System.in);
        System.out.print("Booking ID to pay: ");
        int id = Integer.parseInt(sc.nextLine().trim());

        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");

        Document doc = collection.find(
                        new Document("bookingId", id).append("userId", customer.getUserId()))
                .first();

        if (doc == null) {
            System.out.println("Booking not found or not your booking.");
            return;
        }

        if (doc.getString("bookingStatus").equalsIgnoreCase(BookingStatus.Booked.name())) {
            System.out.println("Already booked/paid.");
            return;
        }

        double total = 0;

        // Venue price
        Integer venueId = doc.getInteger("venueId");
        if (venueId != null) {
            Venue v = Venue.getVenue(venueId);
            if (v != null) total += v.getPrice();
        }

        // Amenities price
        if (doc.containsKey("amenities")) {
            @SuppressWarnings("unchecked")
            java.util.List<Document> amenitiesList = (java.util.List<Document>) doc.get("amenities");
            System.out.println("Amenities selected:");
            for (Document aDoc : amenitiesList) {
                int quantity = aDoc.getInteger("quantity", 0);
                double price = 0;
                Object priceObj = aDoc.get("price");
                if (priceObj instanceof Number) price = ((Number) priceObj).doubleValue();
                total += price;
                System.out.println(" - " + aDoc.getInteger("amenityId") + " x " + quantity + " (₱" + price + ")");
            }
        }

        System.out.println("Venue price: ₱" + (venueId != null ? Venue.getVenue(venueId).getPrice() : 0));
        System.out.println("Total amount to pay: ₱" + total);

        System.out.print("Enter any input to simulate payment: ");
        sc.nextLine();

        collection.updateOne(new Document("bookingId", id),
                new Document("$set",
                        new Document("paymentStatus", PaymentStatus.Paid.name())
                                .append("bookingStatus", BookingStatus.Booked.name())
                                .append("total", total)));

        System.out.println("Payment accepted. Booking confirmed.");

        String username = customer.getUsername();
        Booking finishedSnapshot = new Booking(id, null, null, PaymentStatus.Paid, BookingStatus.Booked, doc.getString("purpose"), username);
        BookingHistory.addFinished(finishedSnapshot);
        System.out.println("Payment accepted. Booking confirmed and recorded in finished history.");
    }
}


