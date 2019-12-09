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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.planner.project.model.Permission;
import plus.planner.project.model.Project;
import plus.planner.project.repository.ProjectRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;

import static plus.planner.project.utils.PemUtils.readPublicKeyFromFile;

@RequestMapping("project")
@RestController
public class ProjectController {
    @Autowired
    private ProjectRepository repo;
    private ObjectMapper objectMapper;

    ProjectController(){
        this.objectMapper = new ObjectMapper();
    }

    @RequestMapping(path = "/create/{project}")
    public void createProject(@PathVariable String project) {
        try {
            repo.save(objectMapper.readValue(project, Project.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path = "/read")
    public String readProject(@RequestHeader("Authorization") String token){
        try {
            Algorithm algorithm = Algorithm.RSA512((RSAPublicKey) readPublicKeyFromFile("src/main/resources/PublicKey.pem", "RSA"), null);
            JWTVerifier verifier = JWT.require(algorithm)
                    .withIssuer("data-editor-token-service")
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
                URL url = null;
                URLConnection conn = null;
                try {
                    url = new URL("http://localhost:8085/chat/read/" + p.getProjectid());
                    conn = url.openConnection();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                String output = null;
                while (true) {
                    try {
                        if (!((output = br.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb.append(output);
                }
                p.setChats(sb.toString());
            }
            for (Project p :
                    projects) {
                URL url = null;
                URLConnection conn = null;
                try {
                    url = new URL("http://localhost:8082/containerservice/component/read/" + p.getProjectid());
                    conn = url.openConnection();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                StringBuilder sb = new StringBuilder();
                String output = null;
                while (true) {
                    try {
                        if (!((output = br.readLine()) != null)) break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    sb.append(output);
                }
                p.setComponents(sb.toString());
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
    public void deleteProject(@PathVariable String projectid){
        repo.deleteById(projectid);
    }
}
