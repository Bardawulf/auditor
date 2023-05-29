package com.example.auditor.controller.transcript;


import com.example.auditor.domain.transcript.StudentRecord;
import com.example.auditor.service.transcript.TranscriptExportService;
import com.example.auditor.service.transcript.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("transcript")
public class TranscriptController {

    private final TranscriptService transcriptService;
    private final TranscriptExportService transcriptExportService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public StudentRecord createTranscript(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return transcriptService.createTranscript(file);
        } catch (Exception e) {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to parse the transcript");
        }
    }

    @GetMapping("all")
    public List<StudentRecord> getAll() {
        return transcriptService.getAll();
    }

    @GetMapping("student/{id}")
    public ResponseEntity<?> getByStudentId(@PathVariable Long id) {
        Optional<StudentRecord> optionalStudent = transcriptService.getByStudentId(id);

        if (optionalStudent.isPresent()) {
            return ResponseEntity.ok(optionalStudent.get());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found");
        }
    }

    @GetMapping("student/{id}/export")
    public ResponseEntity<Object> exportTranscript(@PathVariable Long id) throws IOException {

        Optional<StudentRecord> optionalStudent = transcriptService.getByStudentId(id);

        if (optionalStudent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found");
        }

        File spreadsheet = transcriptExportService.buildSpreadsheet(optionalStudent.get());
        FileInputStream fileInputStream = new FileInputStream(spreadsheet);

        HttpHeaders headers = new HttpHeaders();

        headers.add(
                "Content-Disposition",
                String.format("attachment; filename=\"%s\"", spreadsheet.getName())
        );

        headers.add(
                "Access-Control-Expose-Headers",
                "content-disposition"
        );

        headers.add(
                "Cache-Control",
                "no-cache, no-store, must-revalidate"
        );

        headers.add(
                "Pragma",
                "no-cache"
        );

        headers.add(
                "Expires",
                "0"
        );


        ResponseEntity<Object> responseEntity = ResponseEntity
                .ok().headers(headers)
                .contentLength(spreadsheet.length())
                .contentType(MediaType.parseMediaType("application/txt"))
                .body(new InputStreamResource(fileInputStream));


        return responseEntity;

    }

    @GetMapping("students/{id}")
    public ResponseEntity<?> getByStudentIds(@PathVariable Long[] id) {
        if (id == null || id.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ids param." +
                    " Use 'students/id1,id2,id3'");
        }
        return ResponseEntity.ok(transcriptService.getByStudentIds(id));
    }

    @GetMapping("studentsGraph/{id}")
    public ResponseEntity<?> getByStudentIdsGraph(@PathVariable Long[] id) {
        if (id == null || id.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ids param." +
                    " Use 'students/id1,id2,id3'");
        }
        return ResponseEntity.ok(transcriptService.getByStudentIdsGraph(id));
    }

    @DeleteMapping("{id}")
    public void deleteById(@PathVariable Long id) {
        transcriptService.deleteById(id);
    }

}
