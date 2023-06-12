package com.example.auditor.dto;

import com.example.auditor.EncryptionConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Convert;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CurriculumDto {

    @NotNull(message = "Curriculum major cannot be null", groups = Create.class)
    @Convert(converter = EncryptionConverter.class)
    private String major;

    @NotNull(message = "Curriculum year cannot be null", groups = Create.class)
    private Integer year;

    public interface Create {

    }

}
