package plus.planner.project.controllers;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import plus.planner.project.model.Permission;
import plus.planner.project.model.Project;
import plus.planner.project.providers.IProjectProvider;
import plus.planner.project.repository.ProjectRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@CrossOrigin
@RequestMapping("/project")
@RestController
public class ProjectController {
    private final Logger logger = LoggerFactory.getLogger(ProjectController.class);
    private final ProjectRepository repo;
    private final ObjectMapper objectMapper;
    private final JWTVerifier jwtVerifier;
    private final IProjectProvider projectProvider;

    @Autowired
    public ProjectController(ProjectRepository repo, ObjectMapper objectMapper, JWTVerifier jwtVerifier, IProjectProvider projectProvider) {
        this.repo = repo;
        this.objectMapper = objectMapper;
        this.jwtVerifier = jwtVerifier;
        this.projectProvider = projectProvider;
    }

    @PostMapping(path = "/create")
    public void createProject(@RequestBody String prj, @RequestHeader("Authorization") String token) throws IOException {
        final Project project = objectMapper.readValue(prj, Project.class);
        logger.info("verifying token");
        final DecodedJWT jwt = jwtVerifier.verify(token.replace("Bearer ", ""));
        logger.info("saving project: {}", project.getProjectid());
        repo.save(project);
        logger.info("creating role");
        projectProvider.createRole(jwt.getClaim("uid").asString(), project.getProjectid());
        logger.info("created project and role");
    }

    @GetMapping(path = "/read")
    public List<Project> readProject(@RequestHeader("Authorization") String token) throws IOException {
        logger.info("verifying token");
        final DecodedJWT jwt = jwtVerifier.verify(token.replace("Bearer ", ""));
        final Permission[] perms = objectMapper.readValue(((jwt.getClaims()).get("pms")).asString(), Permission[].class);
        final List<Project> projects = new ArrayList<>();
        for (Permission p :
                perms) {
            logger.info("getting project: {}", p.getProjectid());
            projects.add((repo.findById(p.getProjectid())).orElse(new Project()));
        }
        for (Project p :
                projects) {
            logger.info("getting chats for projectid: {}", p.getProjectid());
            p.setChats(projectProvider.getChats(p.getProjectid()));
            logger.info("getting parts for projectid: {}", p.getProjectid());
            p.setParts(projectProvider.getParts(p.getProjectid()));
            logger.info("getting users for projectid: {}", p.getProjectid());
            p.setUsers(projectProvider.getUsers(p.getProjectid()));
        }
        logger.info("returning projects");
        return projects;
    }

    @PostMapping(path = "/update")
    public void updateProject(@RequestBody String prj) throws IOException {
        final Project project = objectMapper.readValue(prj, Project.class);
        logger.info("updating project: {}", project.getProjectid());
        repo.save(project);
        logger.info("updated project");
    }

    @PostMapping(path = "/delete/{projectidid}")
    public void deleteProject(@PathVariable String projectid) {
        logger.info("deleting project: {}", projectid);
        repo.deleteById(projectid);
        logger.info("deleted project");
    }
}
