package com.example.auditor.domain.template;


import com.example.auditor.EncryptionConverter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "template")
@Entity
@Builder
public class Template {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Convert(converter = EncryptionConverter.class)
    private String topic;

    @Convert(converter = EncryptionConverter.class)
    private String body;
}
