package com.example.auditor.domain.curriculum;


import com.example.auditor.EncryptionConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "requirement")
@Entity
@Builder
public class Requirement {

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

}
