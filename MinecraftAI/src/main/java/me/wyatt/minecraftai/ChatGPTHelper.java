package me.wyatt.minecraftai;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

// this class was made by chatgpt

public class ChatGPTHelper {

    private static final String OPENAI_API_KEY = ""; // <-- api key
    private static final HttpClient client = HttpClient.newHttpClient();
    private static final Gson gson = new Gson();

    public static void askChatGPTAsync(String prompt, Consumer<String> callback) {
        new Thread(() -> {
            try {
                String response = askChatGPT(prompt);
                callback.accept(response);
            } catch (Exception e) {
                e.printStackTrace();
                callback.accept(""); // fallback on error
            }
        }).start();
    }

    private static String askChatGPT(String prompt) throws Exception {
        // Build messages array
        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        userMessage.addProperty("content", prompt);

        JsonArray messages = new JsonArray();
        messages.add(userMessage);

        // Build JSON body
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", "gpt-4"); // or gpt-3.5-turbo
        requestBody.add("messages", messages);

        // Build HTTP request
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.openai.com/v1/chat/completions"))
                .header("Authorization", "Bearer " + OPENAI_API_KEY)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody), StandardCharsets.UTF_8))
                .build();

        // Send request
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            System.out.println("ChatGPT request failed: " + response);
            return "";
        }

        // Parse JSON
        JsonObject json = gson.fromJson(response.body(), JsonObject.class);
        if (!json.has("choices")) return "";

        JsonArray choices = json.getAsJsonArray("choices");
        if (choices.size() == 0) return "";

        JsonObject firstChoice = choices.get(0).getAsJsonObject();
        if (!firstChoice.has("message")) return "";

        JsonObject message = firstChoice.getAsJsonObject("message");
        return message.has("content") ? message.get("content").getAsString().trim() : "";
    }
}
