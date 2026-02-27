package com.rev.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "HOLIDAY")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Holiday {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "holiday_seq")
    @SequenceGenerator(name = "holiday_seq", sequenceName = "HOLIDAY_SEQ", allocationSize = 1)
    @Column(name = "HOLIDAY_ID")
    private Long holidayId;

    @Column(name = "HOLIDAY_NAME", nullable = false, length = 150)
    private String holidayName;

    @Column(name = "HOLIDAY_DATE", nullable = false, unique = true)
    private LocalDate holidayDate;
}
