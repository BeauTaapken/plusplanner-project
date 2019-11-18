package plus.planner.project.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.planner.project.model.Project;
import plus.planner.project.repository.ProjectRepository;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RequestMapping("project")
@RestController
public class ProjectController {
    @Autowired
    private ProjectRepository repo;
    private ObjectMapper mapper;

    ProjectController(){
        mapper = new ObjectMapper();
    }

    @RequestMapping(path = "/create/{project}")
    public void createProject(@PathVariable String project) {
        try {
            repo.save(mapper.readValue(project, Project.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path = "/read/{projectid}")
    public List<Project> readProject(@PathVariable Long projectid){
        List<Project> projects = repo.findAll();
        return projects.stream().filter(x -> x.getProjectid() == projectid).collect(Collectors.toList());
    }

    @RequestMapping(path = "/update/{project}")
    public void updateSubPart(@PathVariable String project) {
        try {
            repo.save(mapper.readValue(project, Project.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RequestMapping(path = "/delete/{projectidid}")
    public void deleteProject(@PathVariable Long projectid){
        repo.deleteById(projectid);
    }
}
