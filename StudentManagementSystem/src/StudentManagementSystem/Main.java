package StudentManagementSystem;

import com.studentmanagement.config.AuthService;
import com.studentmanagement.config.SupabaseClient;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Supabase Connection Test ===");

        System.out.print("Email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            // Step 1: Authenticate user
            System.out.println("\nAuthenticating...");

            AuthService.LoginResult result =
                    AuthService.login(email, password);

            if (!result.success) {
                System.out.println("Login failed: " + result.message);
                return;
            }

            System.out.println("Login successful!");
            System.out.println("Email: " + result.email);
            System.out.println("Role: " + result.role);

            // Step 2: Request subjects using authenticated token
            System.out.println("\nFetching subjects...");

            String response = SupabaseClient.get(
                    "/rest/v1/subjects?select=name"
            );

            System.out.println("Request successful!");
            System.out.println("Subjects from Supabase:");
            System.out.println(response);

        } catch (Exception e) {

            System.out.println("Supabase test failed.");
            System.out.println(e.getMessage());

        } finally {

            // Clear the authenticated session
            AuthService.logout();
        }
    }
}