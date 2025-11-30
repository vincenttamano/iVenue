package iVenue;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class UserStore {
    private static final MongoDatabase DATABASE = MongoDb.getDatabase();
    private static final MongoCollection<Document> COLLECTION = DATABASE.getCollection("users");

    public static void ensureAdminExists() {
        Document adminDoc = COLLECTION.find(new Document("userType", "admin")).first();
        if (adminDoc == null) {
            int maxId = 0;
            Document last = COLLECTION.find().sort(new Document("userId", -1)).first();
            if (last != null) maxId = last.getInteger("userId");

            Document doc = new Document("userId", maxId + 1)
                    .append("username", "admin")
                    .append("password", "admin123")
                    .append("userType", "admin")
                    .append("firstName", "System")
                    .append("lastName", "Administrator");
            COLLECTION.insertOne(doc);
            System.out.println("Seeded default admin (username: admin, password: admin123)");
        }
    }

    public static Customer registerCustomer(String username, String password, String firstName, String lastName, String contactNumber, String email) {
        // ensure unique username
        Document existing = COLLECTION.find(new Document("username", username)).first();
        if (existing != null) return null;

        int maxId = 0;
        Document last = COLLECTION.find().sort(new Document("userId", -1)).first();
        if (last != null) maxId = last.getInteger("userId");

        Document doc = new Document("userId", maxId + 1)
                .append("username", username)
                .append("password", password)
                .append("userType", "customer")
                .append("firstName", firstName)
                .append("lastName", lastName)
                .append("contactNumber", contactNumber)
                .append("email", email);

        COLLECTION.insertOne(doc);

        return new Customer(username, password, maxId + 1, firstName, lastName, contactNumber, email, "customer");
    }

    public static User findByCredentials(String username, String password) {
        Document doc = COLLECTION.find(new Document("username", username).append("password", password)).first();
        if (doc == null) return null;
        String type = doc.getString("userType");
        int id = doc.getInteger("userId");
        if ("admin".equalsIgnoreCase(type)) {
            return new AdminUser(username, password, id);
        } else if ("customer".equalsIgnoreCase(type)) {
            return new Customer(
                    username,
                    password,
                    id,
                    doc.getString("firstName"),
                    doc.getString("lastName"),
                    doc.getString("contactNumber"),
                    doc.getString("email"),
                    type
            );
        } else {
            return new User(username, password, id);
        }
    }

    public static List<User> getAll() {
        List<User> out = new ArrayList<>();
        for (Document doc : COLLECTION.find()) {
            String username = doc.getString("username");
            String password = doc.getString("password");
            String type = doc.getString("userType");
            int id = doc.getInteger("userId");
            if ("admin".equalsIgnoreCase(type)) out.add(new AdminUser(username, password, id));
            else if ("customer".equalsIgnoreCase(type)) out.add(new Customer(username, password, id, doc.getString("firstName"), doc.getString("lastName"), doc.getString("contactNumber"), doc.getString("email"), type));
            else out.add(new User(username, password, id));
        }
        return out;
    }
}