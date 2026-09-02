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
class OperationsApiIntegrationTests {
    @LocalServerPort int port;
    private final HttpClient client=HttpClient.newHttpClient();

    @Test void rescueAndVolunteerSupportCreateEditAndDelete() throws Exception {
        String token=login();
        long rescueId=id(send("POST","/api/rescues",token,"{\"catName\":\"梨花\",\"title\":\"需要观察\",\"area\":\"图书馆\",\"priority\":\"MEDIUM\"}"));
        HttpResponse<String> editedRescue=send("PUT","/api/rescues/"+rescueId,token,"{\"catName\":\"梨花\",\"title\":\"已送医观察\",\"area\":\"校医院\",\"priority\":\"HIGH\",\"status\":\"治疗中\",\"ownerName\":\"测试志愿者\"}");
        assertThat(editedRescue.statusCode()).isEqualTo(200);assertThat(editedRescue.body()).contains("已送医观察","治疗中");
        assertThat(send("DELETE","/api/rescues/"+rescueId,token,null).statusCode()).isEqualTo(204);

        long volunteerId=id(send("POST","/api/volunteers",token,"{\"title\":\"饮水点清洁\",\"scheduleText\":\"周二 18:30\",\"status\":\"待安排\"}"));
        HttpResponse<String> editedVolunteer=send("PUT","/api/volunteers/"+volunteerId,token,"{\"title\":\"图书馆饮水点清洁\",\"scheduleText\":\"周三 18:30\",\"ownerName\":\"测试志愿者\",\"status\":\"进行中\",\"notes\":\"携带清洁工具\"}");
        assertThat(editedVolunteer.statusCode()).isEqualTo(200);assertThat(editedVolunteer.body()).contains("图书馆饮水点清洁","进行中");
        assertThat(send("DELETE","/api/volunteers/"+volunteerId,token,null).statusCode()).isEqualTo(204);
    }

    @Test void removedModulesNoLongerExposeEndpoints() throws Exception {
        String token=login();
        assertThat(send("GET","/api/adoptions",token,null).statusCode()).isEqualTo(404);
        assertThat(send("GET","/api/followups",token,null).statusCode()).isEqualTo(404);
    }

    private String login() throws Exception {HttpResponse<String> response=send("POST","/api/auth/login",null,"{\"username\":\"user\",\"password\":\"123456\"}");assertThat(response.statusCode()).isEqualTo(200);Matcher matcher=Pattern.compile("\"token\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());assertThat(matcher.find()).isTrue();return matcher.group(1);}
    private long id(HttpResponse<String> response){assertThat(response.statusCode()).isEqualTo(200);Matcher matcher=Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(response.body());assertThat(matcher.find()).isTrue();return Long.parseLong(matcher.group(1));}
    private HttpResponse<String> send(String method,String path,String token,String body) throws Exception {HttpRequest.Builder builder=HttpRequest.newBuilder(URI.create("http://127.0.0.1:"+port+path));if(token!=null)builder.header("Authorization","Bearer "+token);if(body!=null)builder.header("Content-Type","application/json");builder.method(method,body==null?HttpRequest.BodyPublishers.noBody():HttpRequest.BodyPublishers.ofString(body));return client.send(builder.build(),HttpResponse.BodyHandlers.ofString());}
}
