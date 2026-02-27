package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "DESIGNATION")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "desig_id")
    private Long desigId;
    @Column(name = "DESIG_NAME", nullable = false, unique = true, length = 100)
    private String desigName;
}
