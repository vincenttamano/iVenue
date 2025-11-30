package iVenue;

import java.util.Scanner;

// A lightweight User subclass that can invoke admin menu
public class AdminUser extends User {
    public AdminUser(String username, String password, int userId) {
        super(username, password, userId);
    }

    public void adminMenu() {
        // reuse Admin management UI by creating Admin instance with Scanner
        Admin admin = new Admin(new Scanner(System.in));
        admin.adminMenu();
    }
}
