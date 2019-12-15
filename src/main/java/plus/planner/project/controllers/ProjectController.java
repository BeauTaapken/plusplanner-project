package plus.planner.project.controllers;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import plus.planner.project.model.Permission;
import plus.planner.project.model.Project;
import plus.planner.project.repository.ProjectRepository;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static plus.planner.project.utils.PemUtils.readPublicKeyFromFile;

@RequestMapping("/project")
@RestController
public class ProjectController {
    @Autowired
    private ProjectRepository repo;
    @Autowired
    private RestTemplate restTemplate;
    private ObjectMapper objectMapper;

    ProjectController() {
        this.objectMapper = new ObjectMapper();
    }

    @RequestMapping(path = "/create", method = RequestMethod.POST)
    public void createProject(@RequestBody Project project, @RequestHeader("Authorization") String token) {
        System.out.println(project.getProjectname());
        System.out.println(token);
        System.out.println(restTemplate == null);
        try {
            Algorithm algorithm = Algorithm.RSA512((RSAPublicKey) readPublicKeyFromFile("src/main/resources/PublicKey.pem", "RSA"), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("plus-planner-token-service")
                    .build();
            DecodedJWT jwt = verifier.verify(token.replace("Bearer ", ""));
            repo.save(project);
            System.out.println("{\"roleid\":\"" + UUID.randomUUID().toString() +
                    "\",\"userid\":\"" + jwt.getClaim("uid").asString() +
                    "\",\"projectid\":\"" + project.getProjectid() +
                    "\",\"role\":\"admin\"}");
            restTemplate.postForObject("http://plus-planner-role-management-service/role/create",
                    new HttpEntity<>("{\"roleid\":\"" + UUID.randomUUID().toString() +
                                         "\",\"userid\":\"" + jwt.getClaim("uid").asString() +
                                      "\",\"projectid\":\"" + project.getProjectid() +
                                           "\",\"role\":\"admin\"}", new HttpHeaders()), String.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path = "/read")
    public String readProject(@RequestHeader("Authorization") String token) {
        try {
            Algorithm algorithm = Algorithm.RSA512((RSAPublicKey) readPublicKeyFromFile("src/main/resources/PublicKey.pem", "RSA"), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("plus-planner-token-service")
                    .build();
            DecodedJWT jwt = verifier.verify(token.replace("Bearer ", ""));
            Permission[] perms = objectMapper.readValue(((jwt.getClaims()).get("pms")).asString(), Permission[].class);
            List<Project> projects = new ArrayList<>();
            for (Permission p :
                    perms) {
                projects.add((repo.findById(p.getProjectid())).get());
            }
            for (Project p :
                    projects) {
                String channel = restTemplate.getForObject("http://plus-planner-channel-service/chat/read/" + p.getProjectid(), String.class);
                String component = restTemplate.getForObject("http://plus-planner-container-service/containerservice/component/read/" + p.getProjectid(), String.class);
                p.setChats(channel);
                p.setComponents(component);
            }
            String json = null;
            try {
                json = objectMapper.writeValueAsString(projects);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            json = json.replace("\"[", "[");
            json = json.replace("]\"", "]");
            json = json.replace("\\\"", "\"");
            System.out.println(json);
            return "{\"projects\":" + json + "}";
        } catch (JsonParseException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(path = "/update/{project}")
    public void updateProject(@PathVariable String project) {
        try {
            repo.save(objectMapper.readValue(project, Project.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path = "/delete/{projectidid}")
    public void deleteProject(@PathVariable String projectid) {
        repo.deleteById(projectid);
    }
}
