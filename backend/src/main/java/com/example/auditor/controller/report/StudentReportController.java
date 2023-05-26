package com.example.auditor.controller.report;

import com.example.auditor.controller.transcript.TranscriptController;
import com.example.auditor.domain.report.StudentReport;
import com.example.auditor.domain.transcript.StudentRecord;
import com.example.auditor.dto.StudentReportDto;
import com.example.auditor.service.report.ReportExportService;
import com.example.auditor.service.report.StudentReportService;
import com.example.auditor.service.transcript.TranscriptExportService;
import com.example.auditor.service.transcript.TranscriptService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.SQLOutput;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("report")
public class StudentReportController {

    private final StudentReportService reportService;
    private final ReportExportService reportExportService;
    private final TranscriptService transcriptService;

    @PostMapping
    public StudentReport create(@RequestBody
                                @Validated StudentReportDto dto) {
        return reportService.createReport(dto.getStudentId(), dto.getCurriculumId());
    }

    @GetMapping("{id}")
    public StudentReport getByID(@PathVariable Long id) {

        return reportService.getById(id);
    }

    @GetMapping("{id}/export")
    public ResponseEntity<Object> exportReport(@PathVariable Long id) throws IOException {

        Optional<StudentRecord> optionalStudent = transcriptService.getByStudentId(id);

        if (optionalStudent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found");
        }

        StudentReport studentReport = reportService.getById(id);

        File spreadsheet = reportExportService.buildSpreadsheet(optionalStudent.get(), studentReport);
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

    @GetMapping("batch/{ids}")
    public ResponseEntity<?> getByIDs(@PathVariable Long[] ids) {

        if (ids == null || ids.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ids param." +
                    " Use 'report/batch/id1,id2,id3'");
        }
        return ResponseEntity.ok(reportService.getByIds(ids));
    }

    @PostMapping("{reportId}/detachCourses")
    public void detachCourse(@PathVariable Long reportId, @RequestParam Long[] courseIds) {
        for (var i : courseIds) {
            reportService.detachCompletedCourse(reportId, i);
        }
    }

    @PostMapping("{reportId}/detachRequirements")
    public void detachRequirement(@PathVariable Long reportId, @RequestParam Long[] requirementIds) {
        for (var i : requirementIds) {
            reportService.detachRequirement(reportId, i);
        }
    }

    @PostMapping("{reportId}/mapRequirement")
    public void mapRequirement(@PathVariable Long reportId,
                               @RequestParam Long[] courseIds,
                               @RequestParam Long[] requirementIds) {
        if (courseIds.length != requirementIds.length) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Number of courses and requirements to map is not equal");
        }
        for (int i = 0; i < courseIds.length; i++) {
            reportService.mapRequirement(reportId, courseIds[i], requirementIds[i]);
        }
    }

    @DeleteMapping("{id}")
    public void deleteById(@PathVariable Long id) {
        reportService.deleteById(id);
    }

}
