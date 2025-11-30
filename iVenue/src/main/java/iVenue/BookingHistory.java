package iVenue;

import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import com.mongodb.client.MongoCollection;
import org.bson.Document;


public class BookingHistory {

	private static final Queue<Booking> finishedQueue = new LinkedList<>();
	private static final Queue<Booking> deletedQueue = new LinkedList<>();
	private static final MongoCollection<Document> collection = MongoDb.getDatabase().getCollection("booking_history");

	static {
		loadHistory();
	}

    private static void loadHistory() {
        synchronized (finishedQueue) {
            for (Document doc : collection.find(new Document("type", "finished"))) {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(doc.getString("paymentStatus"));

                // Normalize bookingStatus string from DB
                String statusStr = doc.getString("bookingStatus");
                BookingStatus bookingStatus = BookingStatus.valueOf(
                        statusStr.substring(0,1).toUpperCase() + statusStr.substring(1).toLowerCase()
                );

                Booking b = new Booking(
                        doc.getInteger("bookingId"),
                        null,
                        null,
                        paymentStatus,
                        bookingStatus,
                        doc.getString("purpose"),
                        doc.getString("username")
                );
                finishedQueue.offer(b);
            }
        }

        synchronized (deletedQueue) {
            for (Document doc : collection.find(new Document("type", "deleted"))) {
                PaymentStatus paymentStatus = PaymentStatus.valueOf(doc.getString("paymentStatus"));

                String statusStr = doc.getString("bookingStatus");
                BookingStatus bookingStatus = BookingStatus.valueOf(
                        statusStr.substring(0,1).toUpperCase() + statusStr.substring(1).toLowerCase()
                );

                Booking b = new Booking(
                        doc.getInteger("bookingId"),
                        null,
                        null,
                        paymentStatus,
                        bookingStatus,
                        doc.getString("purpose"),
                        doc.getString("username")
                );
                deletedQueue.offer(b);
            }
        }
    }


    private static void writeHistory(Booking booking, String type) {
		if (booking == null) return;
		Document doc = new Document("bookingId", booking.getBookingId())
			.append("type", type)
			.append("paymentStatus", booking.getPaymentStatus())
			.append("bookingStatus", booking.getBookingStatus())
			.append("purpose", booking.getPurpose())
			.append("username", booking.getUsername())
			.append("timestamp", new java.util.Date());
		collection.insertOne(doc);
	}

	// --- Finished bookings ---

	public static synchronized void addFinished(Booking booking) {
		if (booking == null) return;
		finishedQueue.offer(booking);
		writeHistory(booking, "finished");
	}

	public static synchronized Booking peekFinished() {
		return finishedQueue.peek();
	}

	public static synchronized Booking pollFinished() {
		return finishedQueue.poll();
	}

	public static synchronized List<Booking> listFinished() {
		return new ArrayList<>(finishedQueue);
	}

	public static synchronized int finishedCount() {
		return finishedQueue.size();
	}

	public static synchronized void clearFinished() {
		finishedQueue.clear();
		collection.deleteMany(new Document("type", "finished"));
	}

	// --- Deleted bookings ---

	public static synchronized void addDeleted(Booking booking) {
		if (booking == null) return;
		deletedQueue.offer(booking);
		writeHistory(booking, "deleted");
	}

	public static synchronized Booking peekDeleted() {
		return deletedQueue.peek();
	}

	public static synchronized Booking pollDeleted() {
		return deletedQueue.poll();
	}

	public static synchronized List<Booking> listDeleted() {
		return new ArrayList<>(deletedQueue);
	}

	public static synchronized int deletedCount() {
		return deletedQueue.size();
	}

	public static synchronized void clearDeleted() {
		deletedQueue.clear();
		collection.deleteMany(new Document("type", "deleted"));
	}

	public static synchronized boolean moveDeletedToFinished() {
		Booking b = deletedQueue.poll();
		if (b == null) return false;
		finishedQueue.offer(b);
		collection.deleteOne(new Document("bookingId", b.getBookingId()).append("type", "deleted"));
		writeHistory(b, "finished");
		return true;
	}

	// Convenience lookup by booking id
	public static synchronized Booking findFinishedById(int bookingId) {
		for (Booking b : finishedQueue) {
			if (b.getBookingId() == bookingId) return b;
		}
		return null;
	}

	public static synchronized Booking findDeletedById(int bookingId) {
		for (Booking b : deletedQueue) {
			if (b.getBookingId() == bookingId) return b;
		}
		return null;
	}

	public static synchronized boolean removeFinishedById(int bookingId) {
		java.util.Iterator<Booking> it = finishedQueue.iterator();
		while (it.hasNext()) {
			Booking b = it.next();
			if (b.getBookingId() == bookingId) {
				it.remove();
				collection.deleteOne(new Document("bookingId", bookingId).append("type", "finished"));
				return true;
			}
		}
		return false;
	}

	public static synchronized boolean removeDeletedById(int bookingId) {
		java.util.Iterator<Booking> it = deletedQueue.iterator();
		while (it.hasNext()) {
			Booking b = it.next();
			if (b.getBookingId() == bookingId) {
				it.remove();
				collection.deleteOne(new Document("bookingId", bookingId).append("type", "deleted"));
				return true;
			}
		}
		return false;
	}

}

