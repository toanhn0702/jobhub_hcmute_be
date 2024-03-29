package vn.iotstar.jobhub_hcmute_be.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "transactions")
public class Transactions {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String Id;

    @Column(name = "createAt")
    private final Date createAt = new Date();

    private long time;
    private String status;
    private long amount;
    @Column(columnDefinition = "Nvarchar(max)")
    private String name;

    private String code;

    private String bank;
    @ManyToOne(fetch = FetchType.EAGER)
    @JsonBackReference
    @ToString.Exclude
    private Employer employer;


}
