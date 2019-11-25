package plus.planner.project.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import plus.planner.project.model.Project;
import plus.planner.project.repository.ProjectRepository;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
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
    public String readProject(@PathVariable Long projectid){
        List<Project> projects = new ArrayList<>();
        for (String p :
                projectids) {
            projects.add((repo.findById(projectid)).get());
        }
        projects = projects.stream().filter(x -> x.getProjectid() == projectid).collect(Collectors.toList());
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
            json = mapper.writeValueAsString(projects);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        json = json.replace("\"[", "[");
        json = json.replace("]\"", "]");
        json = json.replace("\\\"", "\"");
        return "{\"projects\":" + json + "}";
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
