package plus.planner.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import plus.planner.project.model.Project;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

}
