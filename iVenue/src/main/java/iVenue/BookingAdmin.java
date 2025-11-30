package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.Date;
import java.util.LinkedList;
import java.util.Scanner;

public class BookingAdmin implements AdminManagement<Booking> {

    private final MongoCollection<Document> collection;

    public BookingAdmin() {
        MongoDatabase database = MongoDb.getDatabase();
        this.collection = database.getCollection("bookings");
    }


    public void create(Scanner input) {
        MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("bookings");

        System.out.println("----CREATE NEW BOOKING----");

        // Display available venues
        Venue.displayAvailableVenues();
        System.out.print("Enter Venue ID: ");
        int venueId = Integer.parseInt(input.nextLine());
        Venue selectedVenue = Venue.getVenue(venueId);
        if (selectedVenue == null) {
            System.out.println("Invalid Venue ID. Booking cancelled.");
            return;
        }

        // Mark venue as unavailable
        selectedVenue.setAvailability(false);
        MongoDb.getDatabase().getCollection("venues")
                .updateOne(new Document("venueId", venueId),
                        new Document("$set", new Document("availability", false)));

        // Select amenities
        Amenity.displayAmenities();
        LinkedList<Amenity> selectedAmenityObjects = new LinkedList<>();
        LinkedList<Document> selectedAmenityDocs = new LinkedList<>(); // <-- use LinkedList here

        while (true) {
            System.out.print("Enter Amenity ID to add (0 to stop): ");
            int aid = Integer.parseInt(input.nextLine().trim());
            if (aid == 0) break;

            Amenity am = Amenity.getAmenity(aid);
            if (am != null) {
                int maxQty = am.getQuantity();
                int qty = 0;

                // Ask user for quantity
                while (true) {
                    System.out.print("Enter quantity for " + am.getName() + " (max " + maxQty + "): ");
                    qty = Integer.parseInt(input.nextLine().trim());
                    if (qty <= 0 || qty > maxQty) {
                        System.out.println("Invalid quantity. Must be between 1 and " + maxQty);
                    } else {
                        break;
                    }
                }

                selectedAmenityObjects.add(am);  // store Amenity object
                selectedAmenityDocs.add(new Document("amenityId", am.getAmenityId())
                        .append("quantity", qty)
                        .append("price", am.getPrice() * qty)); // store total price per amenity
                System.out.println("Added " + qty + " x " + am.getName());
            } else {
                System.out.println("Amenity not found.");
            }
        }

        System.out.print("Enter Purpose: ");
        String purpose = input.nextLine();

        // Generate Booking ID
        int maxId = 0;
        Document lastBooking = collection.find().sort(new Document("bookingId", -1)).first();
        if (lastBooking != null) maxId = lastBooking.getInteger("bookingId");

        // Create booking object
        Date bookingDate = new Date();
        Booking newBooking = new Booking(
                maxId + 1,
                selectedVenue,
                bookingDate,
                PaymentStatus.Pending,
                BookingStatus.Pending,
                purpose,  "N/A"

        );
        newBooking.getAmenities().addAll(selectedAmenityObjects);

        // Insert booking into MongoDB
        Document doc = new Document("bookingId", newBooking.getBookingId())
                .append("venueName", selectedVenue.getName())
                .append("venueId", selectedVenue.getVenueId())
                .append("date", bookingDate.toString())
                .append("paymentStatus", newBooking.getPaymentStatus().name())
                .append("bookingStatus", newBooking.getBookingStatus().name())
                .append("purpose", purpose)
                .append("amenities", selectedAmenityDocs) // <-- LinkedList used
                .append("price", selectedVenue.getPrice())
                .append("isFree", selectedVenue.isFree());

        collection.insertOne(doc);

        System.out.println("Booking successfully created. Booking ID: " + newBooking.getBookingId());
        System.out.println("Venue Price: " + selectedVenue.getPriceLabel());
    }


    // --- UPDATE BOOKING (user input only) ---
    @Override
    public void update(Scanner input) {
        System.out.println("----UPDATE BOOKING STATUS----");
        System.out.print("Enter Booking ID: ");
        int id = Integer.parseInt(input.nextLine());

        Document doc = collection.find(new Document("bookingId", id)).first();
        if (doc == null) {
            System.out.println("Booking not found.");
            return;
        }

        // Display enum options
        System.out.println("Select new Booking Status:");
        BookingStatus[] statuses = BookingStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            System.out.println((i + 1) + ". " + statuses[i]);
        }

        int choice = 0;
        while (true) {
            System.out.print("Enter choice (1-" + statuses.length + "): ");
            try {
                choice = Integer.parseInt(input.nextLine());
                if (choice >= 1 && choice <= statuses.length) break;
            } catch (NumberFormatException e) {
                // ignore invalid input
            }
            System.out.println("Invalid choice. Try again.");
        }

        BookingStatus newStatus = statuses[choice - 1];

        // Update in MongoDB
        collection.updateOne(
                new Document("bookingId", id),
                new Document("$set", new Document("bookingStatus", newStatus.name()))
        );

        System.out.println("Booking updated successfully.");

        // If marking as finished, add to finished history
        if (newStatus == BookingStatus.Finished) {
            String username = "N/A";
            if (doc.containsKey("bookedBy")) {
                Document bookedBy = (Document) doc.get("bookedBy");
                username = bookedBy.getString("username") == null ? "N/A" : bookedBy.getString("username");
            }
            Booking snapshot = new Booking(
                    id,
                    null,
                    null,
                    PaymentStatus.valueOf(doc.getString("paymentStatus")),
                    newStatus,
                    doc.getString("purpose"),
                    username
            );
            BookingHistory.addFinished(snapshot);
            System.out.println("Booking recorded in finished history.");
        }
    }

    @Override
    public void delete(Scanner input) {
        System.out.println("----DELETE BOOKING-----");
        System.out.print("Enter Booking ID: ");
        int id = Integer.parseInt(input.nextLine());

        Document doc = collection.find(new Document("bookingId", id)).first();
        if (doc == null) {
            System.out.println("Booking not found.");
            return;
        }

        String venueName = doc.getString("venueName");
        MongoDb.getDatabase().getCollection("venues")
            .updateOne(new Document("name", venueName),
                new Document("$set", new Document("availability", true)));

        // Extract username from bookedBy
        String username = "N/A";
        if (doc.containsKey("bookedBy")) {
            Document bookedBy = (Document) doc.get("bookedBy");
            username = bookedBy.getString("username") == null ? "N/A" : bookedBy.getString("username");
        }

        // Add a snapshot of the deleted booking to history before removing from DB
        Booking deletedSnapshot = new Booking(id, null, null, PaymentStatus.valueOf(doc.getString("paymentStatus")), BookingStatus.valueOf(doc.getString("bookingStatus")), doc.getString("purpose"), username);
        BookingHistory.addDeleted(deletedSnapshot);

        collection.deleteOne(new Document("bookingId", id));
        System.out.println("Booking deleted successfully. Venue is now available and booking recorded in deleted history.");
    }

    @Override
        public void displayAll() {
            System.out.println("----ALL BOOKINGS----");
            for (Document doc : collection.find()) {
                System.out.println("Booking ID: " + doc.getInteger("bookingId"));
                System.out.println("Venue: " + doc.getString("venueName"));
                // show who booked
                if (doc.containsKey("bookedBy")) {
                    Document by = (Document) doc.get("bookedBy");
                    System.out.println("Booked By: " + by.getString("username") + " (UserID: " + by.getInteger("userId") + ")");
                    if (by.containsKey("firstName") || by.containsKey("lastName")) {
                        System.out.println("  Name: " + (by.getString("firstName") == null ? "" : by.getString("firstName")) + (by.getString("lastName") == null ? "" : " " + by.getString("lastName")));
                    }
                    if (by.containsKey("contactNumber")) System.out.println("  Contact: " + by.getString("contactNumber"));
                    if (by.containsKey("email")) System.out.println("  Email: " + by.getString("email"));
                }
                System.out.println("Purpose: " + doc.getString("purpose"));
                System.out.println("Status: " + doc.getString("bookingStatus"));
                System.out.println("Price: ₱" + doc.getDouble("price"));
                System.out.println("Free?: " + ((doc.getBoolean("isFree")) ? "YES" : "NO"));
                System.out.print("Amenities: ");
                if (doc.containsKey("amenities")) {
                    for (Object a : doc.getList("amenities", Object.class)) System.out.print(a + " ");
                } else {
                    System.out.print("None");
                }
                System.out.println("\n-----------------------------");
            }
        }

    /**
     * Display booking history: finished and deleted bookings in queue format.
     */
    public void displayHistory(Scanner input) {
        int choice;
        do {
            System.out.println("\n--- BOOKING HISTORY ---");
            System.out.println("1. View Finished Bookings Queue");
            System.out.println("2. View Deleted Bookings Queue");
            System.out.println("3. Back");
            System.out.print("Enter choice: ");
            choice = Integer.parseInt(input.nextLine());

            switch (choice) {
                case 1:
                    System.out.println("\n----FINISHED BOOKINGS----");
                    java.util.List<Booking> finished = BookingHistory.listFinished();
                    if (finished.isEmpty()) {
                        System.out.println("No finished bookings in history.");
                    } else {
                        int index = 1;
                        for (Booking b : finished) {
                            Document full = MongoDb.getDatabase().getCollection("bookings").find(new Document("bookingId", b.getBookingId())).first();
                            System.out.println("[" + index + "]");
                            if (full != null) {
                                System.out.println("Booking ID: " + full.getInteger("bookingId"));
                                System.out.println("Venue: " + full.getString("venueName"));
                                System.out.println("Purpose: " + full.getString("purpose"));
                                System.out.println("Status: " + full.getString("bookingStatus"));
                                System.out.println("Payment Status: " + full.getString("paymentStatus"));
                                System.out.println("User: " + (b.getUsername() == null ? "N/A" : b.getUsername()));
                                Object amenities = full.get("amenities");
                                System.out.print("Amenities: ");
                                if (amenities != null) System.out.println(amenities); else System.out.println("None");
                                Double price = full.getDouble("price");
                                if (price != null) System.out.println("Price: ₱" + price);
                            } else {
                                System.out.println("Booking ID: " + b.getBookingId());
                                System.out.println("  Payment: " + (b.getPaymentStatus() == null ? "N/A" : b.getPaymentStatus()));
                                System.out.println("  Status: " + (b.getBookingStatus() == null ? "N/A" : b.getBookingStatus()));
                                System.out.println("  Purpose: " + (b.getPurpose() == null ? "N/A" : b.getPurpose()));
                                System.out.println("  User: " + (b.getUsername() == null ? "N/A" : b.getUsername()));
                            }
                            System.out.println("-----------------------------");
                            index++;
                        }
                    }
                    System.out.println("Total finished: " + BookingHistory.finishedCount());

                    // Allow admin to remove entries from finished history (submenu)
                    int subChoice;
                    do {
                        System.out.println("\n--- Finished History Options ---");
                        System.out.println("1. Delete one entry");
                        System.out.println("2. Delete all entries");
                        System.out.println("3. Back");
                        System.out.print("Enter choice: ");
                        subChoice = Integer.parseInt(input.nextLine());
                        switch (subChoice) {
                            case 1:
                                System.out.print("Enter Booking ID to remove from finished history: ");
                                int remId = Integer.parseInt(input.nextLine());
                                if (BookingHistory.removeFinishedById(remId)) {
                                    System.out.println("Removed booking " + remId + " from finished history.");
                                } else {
                                    System.out.println("Booking ID not found in finished history.");
                                }
                                break;
                            case 2:
                                System.out.print("Are you sure you want to delete ALL finished history? (y/n): ");
                                String conf = input.nextLine();
                                if (conf.equalsIgnoreCase("y")) {
                                    BookingHistory.clearFinished();
                                    System.out.println("Finished history cleared.");
                                } else {
                                    System.out.println("Cancelled.");
                                }
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Invalid option, please try again!");
                        }
                    } while (subChoice != 3);
                    break;
                case 2:
                    System.out.println("\n----DELETED BOOKINGS----");
                    java.util.List<Booking> deleted = BookingHistory.listDeleted();
                    if (deleted.isEmpty()) {
                        System.out.println("No deleted bookings in history.");
                    } else {
                        int index = 1;
                        for (Booking b : deleted) {
                            Document full = MongoDb.getDatabase().getCollection("bookings").find(new Document("bookingId", b.getBookingId())).first();
                            System.out.println("[" + index + "]");
                            if (full != null) {
                                System.out.println("Booking ID: " + full.getInteger("bookingId"));
                                System.out.println("Venue: " + full.getString("venueName"));
                                System.out.println("Purpose: " + full.getString("purpose"));
                                System.out.println("Status: " + full.getString("bookingStatus"));
                                System.out.println("Payment Status: " + full.getString("paymentStatus"));
                                System.out.println("User: " + (b.getUsername() == null ? "N/A" : b.getUsername()));
                                Object amenities = full.get("amenities");
                                System.out.print("Amenities: ");
                                if (amenities != null) System.out.println(amenities); else System.out.println("None");
                                Double price = full.getDouble("price");
                                if (price != null) System.out.println("Price: ₱" + price);
                            } else {
                                System.out.println("Booking ID: " + b.getBookingId());
                                System.out.println("  Payment: " + (b.getPaymentStatus() == null ? "N/A" : b.getPaymentStatus()));
                                System.out.println("  Status: " + (b.getBookingStatus() == null ? "N/A" : b.getBookingStatus()));
                                System.out.println("  Purpose: " + (b.getPurpose() == null ? "N/A" : b.getPurpose()));
                                System.out.println("  User: " + (b.getUsername() == null ? "N/A" : b.getUsername()));
                            }
                            System.out.println("-----------------------------");
                            index++;
                        }
                    }
                    System.out.println("Total deleted: " + BookingHistory.deletedCount());

                    // Allow admin to remove entries from deleted history (submenu)
                    int subChoice2;
                    do {
                        System.out.println("\n--- Deleted History Options ---");
                        System.out.println("1. Delete one entry");
                        System.out.println("2. Delete all entries");
                        System.out.println("3. Back");
                        System.out.print("Enter choice: ");
                        subChoice2 = Integer.parseInt(input.nextLine());
                        switch (subChoice2) {
                            case 1:
                                System.out.print("Enter Booking ID to remove from deleted history: ");
                                int remId2 = Integer.parseInt(input.nextLine());
                                if (BookingHistory.removeDeletedById(remId2)) {
                                    System.out.println("Removed booking " + remId2 + " from deleted history.");
                                } else {
                                    System.out.println("Booking ID not found in deleted history.");
                                }
                                break;
                            case 2:
                                System.out.print("Are you sure you want to delete ALL deleted history? (y/n): ");
                                String conf2 = input.nextLine();
                                if (conf2.equalsIgnoreCase("y")) {
                                    BookingHistory.clearDeleted();
                                    System.out.println("Deleted history cleared.");
                                } else {
                                    System.out.println("Cancelled.");
                                }
                                break;
                            case 3:
                                break;
                            default:
                                System.out.println("Invalid option, please try again!");
                        }
                    } while (subChoice2 != 3);
                    break;
                case 3:
                    System.out.println("Returning to Booking Management.");
                    break;
                default:
                    System.out.println("Invalid option, please try again!");
            }
        } while (choice != 3);
    }
}

