package com.example.auditor.domain.curriculum;

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
@Table(name = "curriculum")
@Entity
@Builder
public class Curriculum {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    private String major;
    private Integer year;

    @OneToMany
    private List<Requirement> requirements;

}
