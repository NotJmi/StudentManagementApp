
package com.studentmanagement.config;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class SupabaseClient {

    // Your Supabase project URL
    private static final String SUPABASE_URL =
            "https://iohhxyxsjgojyharvtne.supabase.co";

    // Your Supabase publishable key (or legacy anon key)
    private static final String SUPABASE_KEY =
            "sb_publishable_wmUfcQu9V42q-Ih8VRHJKg_YKjjthjz";

    private static final HttpClient CLIENT =
            HttpClient.newHttpClient();

    private static String accessToken;

    // Store authenticated user's access token
    public static void setAccessToken(String token) {
        accessToken = token;
    }

    // Send HTTP requests to Supabase
    public static String request(
            String method,
            String endpoint,
            String body
    ) throws IOException, InterruptedException {

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(SUPABASE_URL + endpoint))
                .header("apikey", SUPABASE_KEY)
                .header("Content-Type", "application/json")
                .header(
                        "Authorization",
                        "Bearer " + (accessToken == null
                                ? SUPABASE_KEY : accessToken)
                );

        switch (method.toUpperCase()) {

            case "GET":
                builder.GET();
                break;

            case "POST":
                builder.header("Prefer", "return=representation");
                builder.POST(
                        HttpRequest.BodyPublishers.ofString(
                                body == null ? "" : body
                        )
                );
                break;

            case "PATCH":
                builder.header("Prefer", "return=representation");
                builder.method(
                        "PATCH",
                        HttpRequest.BodyPublishers.ofString(
                                body == null ? "" : body
                        )
                );
                break;

            case "DELETE":
                builder.DELETE();
                break;

            default:
                throw new IllegalArgumentException(
                        "Unsupported HTTP method: " + method
                );
        }

        HttpResponse<String> response = CLIENT.send(
                builder.build(),
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {

            throw new IOException(
                    "HTTP " + response.statusCode()
                    + ": " + response.body()
            );
        }

        return response.body();
    }

    // GET request
    public static String get(String endpoint)
            throws IOException, InterruptedException {
        return request("GET", endpoint, null);
    }

    // POST request
    public static String post(
            String endpoint,
            JsonObject body
    ) throws IOException, InterruptedException {
        return request("POST", endpoint, body.toString());
    }

    // PATCH request
    public static String patch(
            String endpoint,
            JsonObject body
    ) throws IOException, InterruptedException {
        return request("PATCH", endpoint, body.toString());
    }

    // DELETE request
    public static String delete(String endpoint)
            throws IOException, InterruptedException {
        return request("DELETE", endpoint, null);
    }
}
