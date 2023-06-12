package com.example.auditor.service.transcript;

import com.example.auditor.domain.transcript.StudentRecord;
import com.example.auditor.domain.transcript.StudentTerm;
import com.example.auditor.repository.transcript.StudentRecordRepository;
import com.example.auditor.service.transcript.parser.TranscriptParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranscriptService {

    private final StudentRecordRepository studentRecordRepository;
    private final BeanFactory beanFactory;

    private static final String ENCRYPTION_KEY = "This-is-your-key"; // Replace with your encryption key

    public StudentRecord createTranscript(MultipartFile file) throws IOException {
        PDDocument document = PDDocument.load(file.getInputStream());
        PDFTextStripper stripper = beanFactory.getBean(PDFTextStripper.class);
        String transcriptText = stripper.getText(document);
        TranscriptParser tParser = beanFactory.getBean(TranscriptParser.class, transcriptText);

        document.close();

        StudentRecord studentRecord = tParser.buildStudentRecord();
        studentRecord.setSchoolName(encrypt(studentRecord.getSchoolName()));
        studentRecord.setMajor(encrypt(studentRecord.getMajor()));
        studentRecord.setAdmissionSemester(encrypt(studentRecord.getAdmissionSemester()));
        studentRecord.setName(encrypt(studentRecord.getName()));

        return studentRecordRepository.save(studentRecord);
    }

    public List<StudentRecord> getAll() {
        List<StudentRecord> records = studentRecordRepository.findAll();
        decryptStudentRecords(records);
        return records;
    }

    public Optional<StudentRecord> getByStudentId(Long id) {
        Optional<StudentRecord> optionalRecord = studentRecordRepository.findById(id);
        optionalRecord.ifPresent(this::decryptStudentRecord);
        return optionalRecord;
    }

    public void deleteById(Long id) {
        if (studentRecordRepository.existsById(id)) {
            studentRecordRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student record id");
        }
    }

    public List<StudentRecord> getByStudentIds(Long[] ids) {
        return Arrays.stream(ids)
                .map(studentRecordRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Map<Object, Object> getByStudentIdsGraph(Long[] ids) {
        Map<Object, Object> resultMap = new HashMap<>();
        var records = Arrays.stream(ids)
                .map(studentRecordRepository::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        if (records.isEmpty()) {
            return Map.of();
        }

        var terms = records.stream()
                .flatMap(x -> x.getStudentTerms().stream())
                .map(StudentTerm::getName)
                .distinct()
                .sorted((term1, term2) -> {
                    var term1Lowercase = term1.toLowerCase().strip();
                    var term2Lowercase = term2.toLowerCase().strip();

                    var term1Year = Integer.parseInt(term1Lowercase.substring(term1Lowercase.length() - 4));
                    var term2Year = Integer.parseInt(term2Lowercase.substring(term2Lowercase.length() - 4));

                    var term1Season = term1Lowercase.substring(0, term1Lowercase.length() - 4).strip();
                    var term2Season = term2Lowercase.substring(0, term2Lowercase.length() - 4).strip();

                    var yearComparison = Integer.compare(term2Year, term1Year);
                    if (yearComparison != 0) {
                        return yearComparison;
                    }

                    var seasonComparison = getSeasonOrder(term2Season) - getSeasonOrder(term1Season);
                    if (seasonComparison != 0) {
                        return seasonComparison;
                    }

                    return term2.compareTo(term1);
                })
                .collect(Collectors.toList());

        resultMap.put("terms", terms);
        resultMap.put("records", records);

        return resultMap;
    }

    private int getSeasonOrder(String season) {
        switch (season.toLowerCase()) {
            case "spring":
                return 1;
            case "summer":
                return 2;
            case "fall":
                return 3;
            case "winter":
                return 4;
            default:
                return 5;
        }
    }

    private void decryptStudentRecord(StudentRecord record) {
        record.setSchoolName(decrypt(record.getSchoolName()));
        record.setMajor(decrypt(record.getMajor()));
        record.setAdmissionSemester(decrypt(record.getAdmissionSemester()));
        record.setName(decrypt(record.getName()));
    }

    private void decryptStudentRecords(List<StudentRecord> records) {
        for (StudentRecord record : records) {
            decryptStudentRecord(record);
        }
    }

    private String encrypt(String attribute) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Encryption error: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Encryption error");
        }
    }

    private String decrypt(String dbData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec secretKey = new SecretKeySpec(ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(dbData));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption error: {}", e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Decryption error");
        }
    }
}
