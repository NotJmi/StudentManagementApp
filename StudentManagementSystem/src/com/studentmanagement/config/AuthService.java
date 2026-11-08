
package com.studentmanagement.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import java.io.IOException;

public class AuthService {

    public static class LoginResult {
        public boolean success;
        public String message;
        public String userId;
        public String email;
        public String role;

        public LoginResult(boolean success, String message,
                String userId, String email, String role) {
            this.success = success;
            this.message = message;
            this.userId = userId;
            this.email = email;
            this.role = role;
        }
    }

    // REGISTER NEW USER
    public static String signUp(String email, String password)
            throws IOException, InterruptedException {

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Email and password are required."
            );
        }

        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        body.addProperty("password", password);

        return SupabaseClient.request(
                "POST",
                "/auth/v1/signup",
                body.toString()
        );
    }

    // LOGIN USER
    public static LoginResult login(String email, String password)
            throws IOException, InterruptedException {

        if (email == null || email.trim().isEmpty()
                || password == null || password.isEmpty()) {
            return new LoginResult(
                    false, "Email and password are required.",
                    null, null, null
            );
        }

        JsonObject body = new JsonObject();
        body.addProperty("email", email.trim());
        body.addProperty("password", password);

        String response = SupabaseClient.request(
                "POST",
                "/auth/v1/token?grant_type=password",
                body.toString()
        );

        JsonObject auth = JsonParser.parseString(response)
                .getAsJsonObject();

        String token = auth.get("access_token").getAsString();
        String userId = auth.getAsJsonObject("user")
                .get("id").getAsString();
        String userEmail = auth.getAsJsonObject("user")
                .get("email").getAsString();

        SupabaseClient.setAccessToken(token);

        try {
            String profileResponse = SupabaseClient.get(
                    "/rest/v1/user_profiles?select=role,email&id=eq."
                    + userId
            );

            JsonArray profiles = JsonParser.parseString(profileResponse)
                    .getAsJsonArray();

            if (profiles.size() == 0) {
                logout();
                return new LoginResult(
                        false,
                        "User profile not found.",
                        null, null, null
                );
            }

            JsonObject profile = profiles.get(0).getAsJsonObject();
            String role = profile.get("role").getAsString();

            return new LoginResult(
                    true, "Login successful.",
                    userId, userEmail, role
            );

        } catch (IOException | InterruptedException | RuntimeException ex) {
            logout();
            throw ex;
        }
    }

    // LOGOUT USER
    public static void logout() {
        SupabaseClient.setAccessToken(null);
    }
}
