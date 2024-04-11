import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;

import java.io.IOException;
import java.time.Instant;

public class ApiComparator {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String[] ignoredTags = {"e2eid", "correlationid", "transactionid", "webstransid", "timestamp"};

    public static void main(String[] args) throws IOException {
        // Replace with your actual API details
        String url1 = "https://api1.example.com/endpoint";
        String user1 = "user1";
        String cookie1 = "your_cookie_1";
        String requestJson1 = "{\"data\": \"value1\"}";

        String url2 = "https://api2.example.com/endpoint";
        String user2 = "user2";
        String cookie2 = "your_cookie_2";
        String requestJson2 = "{\"data\": \"value2\"}";

        long startTime1 = Instant.now().toEpochMilli();
        JsonNode response1 = callApi(url1, user1, cookie1, requestJson1);
        long endTime1 = Instant.now().toEpochMilli();

        long startTime2 = Instant.now().toEpochMilli();
        JsonNode response2 = callApi(url2, user2, cookie2, requestJson2);
        long endTime2 = Instant.now().toEpochMilli();

        System.out.println("Time taken for API 1: " + (endTime1 - startTime1) + " ms");
        System.out.println("Time taken for API 2: " + (endTime2 - startTime2) + " ms");

        compareJson(response1, response2);
    }

    private static JsonNode callApi(String url, String user, String cookie, String requestJson) throws IOException {
        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MediaType.parse("application/json"), requestJson);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("user", user)
                .addHeader("Cookie", cookie)
                .post(body)
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("API call failed with code: " + response.code());
            }
            return mapper.readTree(response.body().string());
        }
    }

    private static void compareJson(JsonNode node1, JsonNode node2) {
        if (node1.isNull() || node2.isNull()) {
            System.out.println("One of the nodes is null");
            return;
        }

        if (node1.isObject() && node2.isObject()) {
            for (String field : node1.fieldNames()) {
                JsonNode value1 = node1.get(field);
                JsonNode value2 = node2.get(field);

                if (isIgnoredTag(field)) {
                    continue;
                }

                if (value1.isMissingNode() || value2.isMissingNode()) {
                    System.out.println("Field '" + field + "' is missing in one of the responses");
                } else if (!value1.equals(value2)) {
                    System.out.println("Field '" + field + "': Values differ -");
                    System.out.println("  - Response 1: " + value1);
                    System.out.println("  - Response 2: " + value2);
                } else {
                    compareJson(value1, value2);
                }
            }
        } else if (node1.isArray() && node2.isArray()) {
            // Implement logic for array comparison (optional)
            System.out.println("Arrays are not currently supported for comparison");
        } else {
            if (!isIgnoredTag(node1.asText())) {
                System.out.println("Values differ for root node:");
                System.out.println("  - Response 1: " + node1);
                System.out.println("  - Response 2: " + node2);
            }
        }
    }

    private static boolean isIgnoredTag(String tag) {
        for (String ignored : ignoredTags) {
            if (ignored.equalsIgnoreCase(tag)) {
                return true;
            }
        }
        return false;
