package iVenue;

import java.util.Scanner;

public class User {
    private String username;
    private String password;
    private int UserID;

    public User(String username, String password, int UserID) {
        this.username = username;
        this.password = password;
        this.UserID = UserID;
    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public int getUserId() {
        return UserID;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUserId(int UserID) {
        this.UserID = UserID;
    }

    //Prompt for username and password, authenticate against the
    //`UserStore`, and dispatch to the appropriate menu on success.
    public void login() {
        Scanner sc = new Scanner(System.in);
        System.out.print("Username: ");
        String u = sc.nextLine().trim();
        System.out.print("Password: ");
        String p = sc.nextLine().trim();
        User found = UserStore.findByCredentials(u, p);
        if (found == null) {
            System.out.println("Login failed: invalid username or password.");
            return;
        }
        System.out.println("Login successful. Welcome, " + found.getUsername());
        if (found instanceof AdminUser) {
            ((AdminUser) found).adminMenu();
        } else if (found instanceof Customer) {
            ((Customer) found).userMenu();
        } else {
            System.out.println("Logged in as a basic user. No menu available.");
        }
    }

    public static void displayAllUsers() {
        System.out.println("----ALL USERS----");
        for (User u : UserStore.getAll()) {
            org.bson.Document doc = MongoDb.getDatabase().getCollection("users").find(new org.bson.Document("userId", u.getUserId())).first();
            String username = u.getUsername();
            String type = (u instanceof AdminUser) ? "admin" : (u instanceof Customer ? "customer" : "user");
            String first = doc != null ? doc.getString("firstName") : null;
            String last = doc != null ? doc.getString("lastName") : null;
            String contact = doc != null ? doc.getString("contactNumber") : null;
            String email = doc != null ? doc.getString("email") : null;
            System.out.println("UserID: " + u.getUserId());
            System.out.println("  username: " + username);
            System.out.println("  type: " + type);
            if (first != null || last != null) System.out.println("  name: " + (first == null ? "" : first) + (last == null ? "" : " " + last));
            if (contact != null) System.out.println("  contact: " + contact);
            if (email != null) System.out.println("  email: " + email);
            System.out.println("---------------------------");
        }
    }

    public static void deleteUser(int UserID) {
        System.out.print("Enter User ID to delete: ");
        org.bson.Document doc = MongoDb.getDatabase().getCollection("users").find(new org.bson.Document("userId", UserID)).first();
        if (doc == null) {
            System.out.println("User not found.");
            return;
        }
        String t = doc.getString("userType");
        if ("admin".equalsIgnoreCase(t)) {
            System.out.println("Cannot delete admin users.");
            return;
        }
        MongoDb.getDatabase().getCollection("users").deleteOne(new org.bson.Document("userId", UserID));
        System.out.println("User deleted (id=" + UserID + ").");
    }
}
