package com.catmate;

import static org.assertj.core.api.Assertions.assertThat;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthApiIntegrationTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();

    @Test void adminEndpointRejectsUserAndAcceptsAdmin() throws Exception {
        String userToken = login("user", "123456");
        String adminToken = login("admin", "admin123");
        assertThat(getAdminMetrics(userToken).statusCode()).isEqualTo(403);
        HttpResponse<String> adminResponse = getAdminMetrics(adminToken);
        assertThat(adminResponse.statusCode()).isEqualTo(200);
        assertThat(adminResponse.body()).contains("catCount", "userCount");
    }

    @Test void registrationCreatesOnlyRegularUserAndRejectsDuplicateUsername() throws Exception {
        String json = "{\"username\":\"new_user\",\"displayName\":\"新志愿者\",\"password\":\"secret123\"}";
        HttpResponse<String> registered = post("/api/auth/register", json);
        assertThat(registered.statusCode()).isEqualTo(200);
        assertThat(registered.body()).contains("\"username\":\"new_user\"", "\"role\":\"USER\"", "\"token\"");
        assertThat(post("/api/auth/register", json).statusCode()).isEqualTo(409);
        assertThat(login("new_user", "secret123")).isNotBlank();
        assertThat(login("admin", "admin123")).isNotBlank();
    }

    @Test void registrationValidatesInput() throws Exception {
        HttpResponse<String> response = post("/api/auth/register", "{\"username\":\"a\",\"displayName\":\"测试\",\"password\":\"123\"}");
        assertThat(response.statusCode()).isEqualTo(400);
    }

    private String login(String username, String password) throws Exception {
        String json = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";
        HttpResponse<String> response = post("/api/auth/login", json);
        assertThat(response.statusCode()).isEqualTo(200);
        Matcher matcher = Pattern.compile("\"token\"\s*:\s*\"([^\"]+)\"").matcher(response.body());
        assertThat(matcher.find()).isTrue();
        return matcher.group(1);
    }

    private HttpResponse<String> post(String path, String json) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + path)).header("Content-Type", "application/json").POST(HttpRequest.BodyPublishers.ofString(json)).build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> getAdminMetrics(String token) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(URI.create("http://127.0.0.1:" + port + "/api/admin/metrics")).header("Authorization", "Bearer " + token).GET().build();
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
