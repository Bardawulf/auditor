package com.example.auditor.domain.report;


import com.example.auditor.EncryptionConverter;
import com.example.auditor.domain.curriculum.Requirement;
import com.example.auditor.domain.transcript.TermCourse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report_term_course")
@Entity
public class ReportTermCourse {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    private String code;
    private Integer credits;
    private Double gradePoint;

    @Convert(converter = EncryptionConverter.class)
    private String letterGrade;

    public static ReportTermCourse fromTranscriptTermCourse(TermCourse termCourse) {
        return ReportTermCourse.builder()
                .id(termCourse.getId())
                .code(termCourse.getCode())
                .credits(termCourse.getCredits())
                .gradePoint(termCourse.getGradePoint())
                .letterGrade(termCourse.getLetterGradeLiteral())
                .build();
    }

}
