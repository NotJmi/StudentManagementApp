package com.studentmanagement.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.io.IOException;

public class AuthService {

public static String signUp(String email, String password)
        throws IOException, InterruptedException {

    if (email == null || email.trim().isEmpty()
            || password == null || password.isEmpty()) {

        throw new IllegalArgumentException(
                "Email and password are required."
        );
    }

    JsonObject requestBody = new JsonObject();
    requestBody.addProperty("email", email.trim());
    requestBody.addProperty("password", password);

    return SupabaseClient.request(
            "POST",
            "/auth/v1/signup",
            requestBody.toString()
    );
}
}
