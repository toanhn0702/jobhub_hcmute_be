package vn.iotstar.jobhub_hcmute_be.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Nationalized;

@Data
@Entity
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    String title;


    String location;

    String yearBegin;

    String yearEnd;

    String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonBackReference
    @ToString.Exclude
    private Resume resume;

}
