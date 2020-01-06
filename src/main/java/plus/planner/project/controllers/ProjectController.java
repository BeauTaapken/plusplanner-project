package plus.planner.project.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import plus.planner.project.model.Permission;
import plus.planner.project.model.Project;
import plus.planner.project.repository.ProjectRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestMapping("/project")
@RestController
public class ProjectController {
    private final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectRepository repo;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final JWTVerifier jwtVerifier;

    @Autowired
    public ProjectController(ProjectRepository repo, RestTemplate restTemplate, ObjectMapper objectMapper, JWTVerifier jwtVerifier) {
        this.repo = repo;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.jwtVerifier = jwtVerifier;
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public void createProject(@RequestBody Project project, @RequestHeader("Authorization") String token) {
        logger.info("verifying token");
        final DecodedJWT jwt = jwtVerifier.verify(token.replace("Bearer ", ""));
        logger.info("saving project: " + project.getProjectid());
        repo.save(project);
        logger.info("creating role");
        restTemplate.postForObject("http://plus-planner-role-management-service/role/create",
                new HttpEntity<>("{\"roleid\":\"" + UUID.randomUUID().toString() +
                        "\",\"userid\":\"" + jwt.getClaim("uid").asString() +
                        "\",\"projectid\":\"" + project.getProjectid() +
                        "\",\"role\":\"admin\"}", new HttpHeaders()), String.class);
        logger.info("created project and role");
    }

    @RequestMapping(path = "/read", method = RequestMethod.GET)
    public String readProject(@RequestHeader("Authorization") String token) throws IOException {
        logger.info("verifying token");
        final DecodedJWT jwt = jwtVerifier.verify(token.replace("Bearer ", ""));
        final Permission[] perms = objectMapper.readValue(((jwt.getClaims()).get("pms")).asString(), Permission[].class);
        final List<Project> projects = new ArrayList<>();
        for (Permission p :
                perms) {
            logger.info("getting project: " + p.getProjectid());
            projects.add((repo.findById(p.getProjectid())).get());
        }
        for (Project p :
                projects) {
            logger.info("getting chats for projectid: " + p.getProjectid());
            p.setChats(restTemplate.getForObject("http://plus-planner-channel-service/chat/read/" + p.getProjectid(), String.class));
            logger.info("getting parts for projectid: " + p.getProjectid());
            p.setParts(restTemplate.getForObject("http://plus-planner-container-service/containerservice/component/read/" + p.getProjectid(), String.class));
        }
        logger.info("constructing json");
        String json = null;
        try {
            json = objectMapper.writeValueAsString(projects);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        json = json.replace("\"[", "[");
        json = json.replace("]\"", "]");
        json = json.replace("\\\"", "\"");
        logger.info("returning json");
        return "{\"projects\":" + json + "}";
    }

    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public void updateProject(@RequestBody Project project) {
        logger.info("updating prohject: " + project.getProjectid());
        repo.save(project);
        logger.info("updated project");
    }

    @RequestMapping(path = "/delete/{projectidid}")
    public void deleteProject(@PathVariable String projectid) {
        logger.info("deleting project: " + projectid);
        repo.deleteById(projectid);
        logger.info("deleted project");
    }
}
