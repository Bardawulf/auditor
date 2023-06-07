package com.example.auditor.controller.report;

import com.example.auditor.domain.curriculum.Curriculum;
import com.example.auditor.domain.report.StudentReport;
import com.example.auditor.domain.transcript.StudentRecord;
import com.example.auditor.dto.StudentReportDto;
import com.example.auditor.service.curriculum.CurriculumService;
import com.example.auditor.service.report.ReportExportService;
import com.example.auditor.service.report.StudentReportService;
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
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("report")
public class StudentReportController {

    private final StudentReportService reportService;
    private final ReportExportService reportExportService;
    private final TranscriptService transcriptService;
    private final CurriculumService curriculumService;

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
    public ResponseEntity<Object> exportReportSingle(@PathVariable Long id) throws IOException {

        Optional<StudentRecord> optionalStudent = transcriptService.getByStudentId(id);

        if (optionalStudent.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Student not found");
        }

        StudentReport studentReport = reportService.getById(id);

        Long curriculumId = studentReport.getCurriculumId();
        if (curriculumId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Student does not have curriculum linked");
        }
        Curriculum curriculum = curriculumService.getCurriculum(studentReport.getCurriculumId());

        File spreadsheet = reportExportService.buildSpreadsheetSingle(optionalStudent.get(), curriculum, studentReport);
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

    @GetMapping("batch/{ids}/export")
    public ResponseEntity<Object> exportReportMulti(@PathVariable Long[] ids) throws IOException{

        if (ids == null || ids.length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student ids param." +
                    " Use 'report/batch/id1,id2,id3/export'");
        }

        if (Arrays.stream(ids).anyMatch(Objects::isNull)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "One or more student ids are null.");
        }

        List<StudentRecord> students = transcriptService.getByStudentIds(ids);

        if (students.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No students found with the provided IDs");
        }

        if (students.size() < ids.length) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some students were not found");
        }

        List<StudentReport> studentReports = reportService.getByIds(ids);

        Long curriculumId = studentReports.get(0).getCurriculumId();
        for (StudentReport studentReport : studentReports) {
            if (!Objects.equals(studentReport.getCurriculumId(), curriculumId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Not all students have the same curriculum linked");
            }
        }

        Curriculum curriculum = curriculumService.getCurriculum(curriculumId);

        File spreadsheet = reportExportService.buildSpreadsheetMulti(students, curriculum, studentReports);
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

        // if some, but not all students were found by their IDs, return the spreadsheet with information regarding
        // only those found students, but use status code 207 - MULTI STATUS
//        int status = (students.size() < ids.length) ? HttpStatus.MULTI_STATUS.value() : HttpStatus.OK.value();

        ResponseEntity<Object> responseEntity = ResponseEntity
                .ok().headers(headers)
//                .status(status).headers(headers)
                .contentLength(spreadsheet.length())
                .contentType(MediaType.parseMediaType("application/txt"))
                .body(new InputStreamResource(fileInputStream));


        return responseEntity;
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
