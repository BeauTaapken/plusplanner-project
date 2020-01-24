package plus.planner.project.providers;

public interface IProjectProvider {
    String getUsers(String projectid);

    String getParts(String projectid);

    String getChats(String projectid);

    void createRole(String userid, String projectid);
}
