package vn.iotstar.jobhub_hcmute_be.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Nationalized;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "skill")
@JsonIgnoreProperties({"hibernateLazyInitializer"})
public class Skill implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String skillId;

    @Nationalized
    String name;

    @ManyToMany(mappedBy = "skills", cascade = CascadeType.ALL)
    @JsonBackReference
    @ToString.Exclude
    private List<Resume> resumes;

    @ManyToMany(mappedBy = "skills", cascade = CascadeType.ALL)
    @JsonBackReference
    @ToString.Exclude
    private List<Job> jobs;

    @OneToMany(mappedBy = "skill", fetch = FetchType.LAZY)
    @JsonBackReference
    @ToString.Exclude
    private List<Question> questions;

}
