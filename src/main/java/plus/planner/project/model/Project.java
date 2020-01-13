package plus.planner.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Data
@Entity
@Table(name = "project")
@EntityListeners(AuditingEntityListener.class)
public class Project {
    @Id
    private String projectid;
    private String projectname;
    private String description;
    private String enddate;
    @Transient
    private String chats;
    @Transient
    private String parts;
}