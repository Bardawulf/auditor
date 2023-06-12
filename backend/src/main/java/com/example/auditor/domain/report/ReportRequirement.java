package com.example.auditor.domain.report;

import com.example.auditor.EncryptionConverter;
import com.example.auditor.domain.curriculum.Requirement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report_requirement")
@Entity
@Builder
public class ReportRequirement {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    private String name;

    @Convert(converter = EncryptionConverter.class)
    private String patterns;

    @Convert(converter = EncryptionConverter.class)
    private String antipatterns;
    private Integer credit;
    private Integer semester;

    @Convert(converter = EncryptionConverter.class)
    private String type;


    public static ReportRequirement fromCurriculumRequirement(Requirement requirement) {
        return ReportRequirement.builder()
                .id(requirement.getId())
                .name(requirement.getName())
                .patterns(requirement.getPatterns())
                .antipatterns(requirement.getAntipatterns())
                .credit(requirement.getCredit())
                .type(requirement.getType())
                .semester(requirement.getSemester())
                .build();
    }

}
