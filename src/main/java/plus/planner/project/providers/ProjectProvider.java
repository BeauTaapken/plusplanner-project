package plus.planner.project.providers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

@Component
public class ProjectProvider implements IProjectProvider {
    private final RestTemplate restTemplate;

    @Autowired
    public ProjectProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getUsers(String projectid) {
        return restTemplate.getForObject("https://plus-planner-role-management-service/user/read/" + projectid, String.class);
    }

    @Override
    public String getParts(String projectid) {
        return restTemplate.getForObject("https://plus-planner-container-service/part/read/" + projectid, String.class);
    }

    @Override
    public String getChats(String projectid) {
        return restTemplate.getForObject("https://plus-planner-channel-service/chat/read/" + projectid, String.class);
    }

    @Override
    public void createRole(String userid, String projectid) {
        final HttpHeaders headers = new HttpHeaders();
        headers.set("Content Type", "application/json");
        final HttpEntity<String> entity = new HttpEntity<>("{\"roleid\":\"" + UUID.randomUUID().toString() +
                "\",\"userid\":\"" + userid +
                "\",\"projectid\":\"" + projectid +
                "\",\"role\":\"OWNER\"}");
        restTemplate.postForObject("https://plus-planner-role-management-service/role/create",
                entity, String.class);
    }
}
