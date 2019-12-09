package plus.planner.project.model;

import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;

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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String projectid;
    @NotBlank
    private String projectname;
    @NotBlank
    private String description;
    @NotBlank
    private String enddate;
    @Transient
    private String chats;
    @Transient
    private String components;
}