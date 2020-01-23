package plus.planner.project.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRawValue;
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
    @JsonRawValue
    private String chats;
    @Transient
    @JsonRawValue
    private String parts;
    @Transient
    @JsonRawValue
    private String users;
}