package com.example.auditor.domain.transcript;


import com.example.auditor.EncryptionConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transcript")
@Entity
@Builder
public class StudentRecord {

    @Id
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    private String name;

    @Convert(converter = EncryptionConverter.class)
    private String schoolName;

    @Convert(converter = EncryptionConverter.class)
    private String major;

    @Convert(converter = EncryptionConverter.class)
    private String admissionSemester;

    private Double gpa;
    private Double gradePointsTotal;

    private Integer creditsEnrolledTotal;
    private Integer creditsEarnedTotal;
    private Integer creditsGradedTotal;

    private Integer currentSemester;

    @OneToMany(cascade = CascadeType.ALL)
    private List<StudentTerm> studentTerms;

}