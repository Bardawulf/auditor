package com.example.auditor.service.curriculum;

import com.example.auditor.domain.curriculum.Curriculum;
import com.example.auditor.dto.CurriculumDto;
import com.example.auditor.repository.curriculum.CurriculumRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;

    // Klucz szyfrowania
    private static final String SECRET_KEY = "This-is-your-key";

    public Curriculum getCurriculum(Long id) {
        Curriculum curriculum = curriculumRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot find curriculum with id: " + id));
        decryptCurriculum(curriculum); // Odszyfrowanie danych przed zwróceniem
        return curriculum;
    }

    public Curriculum createCurriculum(CurriculumDto dto) {
        Curriculum curriculum = Curriculum
                .builder()
                .id(null)
                .major(encrypt(dto.getMajor())) // Szyfrowanie pola major
                .year(dto.getYear())
                .requirements(new LinkedList<>())
                .build();
        return curriculumRepository.save(curriculum);
    }

    public List<Curriculum> getAll() {
        List<Curriculum> curricula = curriculumRepository.findAll();
        for (Curriculum curriculum : curricula) {
            decryptCurriculum(curriculum); // Odszyfrowanie danych przed zwróceniem
        }
        return curricula;
    }

    public void deleteById(Long id) {
        if (curriculumRepository.existsById(id)) {
            curriculumRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid curriculum id");
        }
    }

    // Metoda do szyfrowania tekstu
    private String encrypt(String attribute) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Metoda do odszyfrowywania tekstu
    private String decrypt(String dbData) {
        try {
            SecretKeySpec secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Metoda do odszyfrowywania pól Curriculum
    private void decryptCurriculum(Curriculum curriculum) {
        curriculum.setMajor(decrypt(curriculum.getMajor()));
    }
}