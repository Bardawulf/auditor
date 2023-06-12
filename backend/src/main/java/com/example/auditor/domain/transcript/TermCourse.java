package com.example.auditor.domain.transcript;


import com.example.auditor.EncryptionConverter;
import com.example.auditor.service.transcript.parser.LetterGrade.LetterGradeModifiedInstance;
import com.example.auditor.service.transcript.parser.LetterGrade.LiteralNotMatchedException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "course")
@Entity
public class TermCourse {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    private String code;
    private Integer credits;
    private Double gradePoint;

    @Convert(converter = EncryptionConverter.class)
    private String letterGradeLiteral;

    @Convert(converter = EncryptionConverter.class)
    private String letterGradeModifierLiteral;

    @Transient
    private LetterGradeModifiedInstance letterGradeModifiedInstance;


    @PostLoad
    private void postLoad() throws LiteralNotMatchedException {

        letterGradeModifiedInstance = new LetterGradeModifiedInstance(letterGradeLiteral + letterGradeModifierLiteral);
    }

}
