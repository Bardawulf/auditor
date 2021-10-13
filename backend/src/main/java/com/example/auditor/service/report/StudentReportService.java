package com.example.auditor.service.report;

import com.example.auditor.domain.curriculum.Curriculum;
import com.example.auditor.domain.curriculum.Requirement;
import com.example.auditor.domain.report.ReportRequirement;
import com.example.auditor.domain.report.ReportRequirementWithCourse;
import com.example.auditor.domain.report.ReportTermCourse;
import com.example.auditor.domain.report.StudentReport;
import com.example.auditor.domain.transcript.StudentRecord;
import com.example.auditor.domain.transcript.StudentTerm;
import com.example.auditor.domain.transcript.TermCourse;
import com.example.auditor.repository.curriculum.CurriculumRepository;
import com.example.auditor.repository.report.ReportRequirementRepository;
import com.example.auditor.repository.report.ReportTermCourseRepository;
import com.example.auditor.repository.report.StudentReportRepository;
import com.example.auditor.repository.transcript.StudentRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentReportService {

    private final CurriculumRepository curriculumRepository;
    private final StudentRecordRepository recordRepository;
    private final StudentReportRepository reportRepository;
    private final ReportRequirementRepository requirementRepository;
    private final ReportTermCourseRepository courseRepository;

    public StudentReport createReport(Long studentId, Long curriculumId) {

        if (reportRepository.existsById(studentId)) {
            reportRepository.deleteById(studentId);
        }


        Curriculum curriculum = curriculumRepository.findById(curriculumId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid curriculum id"));
        StudentRecord studentRecord = recordRepository.findById(studentId).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid student record id"));


        List<ReportRequirement> requirements = curriculum
                .getRequirements()
                .stream()
                .map(ReportRequirement::fromCurriculumRequirement)
                .sorted(Comparator.comparingInt(requirement -> longestPatternLength((ReportRequirement) requirement)).reversed())
                .collect(Collectors.toList());

        List<ReportTermCourse> completedCourses = studentRecord
                .getStudentTerms()
                .stream()
                .flatMap(x -> x.getTermCourses().stream())
                .sorted(Comparator.comparingInt(TermCourse::getCredits))
                .map(termCourse -> ReportTermCourse.fromTranscriptTermCourse(termCourse))
                .collect(Collectors.toList());


        List<ReportTermCourse> unmappedCourses = completedCourses;
        List<ReportRequirement> unmappedRequirements = requirements;
        List<ReportRequirementWithCourse> completeRequirements = new ArrayList<>();
        List<ReportTermCourse> mappedCourses = new ArrayList<>();

        for (ReportTermCourse completedCourse : completedCourses) {

            ReportRequirement reportRequirement = firstMatchingRequirement(completedCourse, unmappedRequirements);
            if (reportRequirement != null) {

                completeRequirements.add(
                        ReportRequirementWithCourse
                                .builder()
                                    .requirement(reportRequirement)
                                    .course(completedCourse)
                                .build()
                );

                unmappedRequirements.remove(reportRequirement);
                mappedCourses.add(completedCourse);

            }

        }

        unmappedCourses.removeAll(mappedCourses);


        return reportRepository.save(
                StudentReport
                .builder()
                        .id(studentRecord.getId())
                        .curriculumId(curriculum.getId())
                        .credits(studentRecord.getCreditsEarned())
                        .completeRequirements(completeRequirements)
                        .unmappedRequirements(unmappedRequirements)
                        .unmappedCourses(unmappedCourses)
                .build()
        );

    }


    private ReportRequirement firstMatchingRequirement(ReportTermCourse completedCourse, List<ReportRequirement> requirements) {

        String courseCode = completedCourse.getCode();

        for (ReportRequirement requirement : requirements) {

            String[] patterns = requirement.getPatterns().split(",");
            for (String pattern : patterns) {

                if (courseCode.toLowerCase().startsWith(pattern.strip().toLowerCase()) || pattern.equals("*")) {

                    return requirement;
                }
            }
        }

        return null;
    }


    private int longestPatternLength(ReportRequirement requirement) {

        return Collections.max(
                Arrays.stream(
                        requirement
                        .getPatterns()
                        .split(","))
                        .map(pattern -> pattern.length())
                        .collect(Collectors.toList()
                )
        );
    }


    public Optional<StudentReport> getById(Long id) {
        return reportRepository.findById(id);
    }

    public void mapRequirement(Long reportId, Long courseId, Long requirementId) {
        var reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report id");
        }
        var courseOpt = courseRepository.findById(courseId);
        if (courseOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid course id");
        }
        var requirementOpt = requirementRepository.findById(requirementId);
        if (requirementOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid requirement id");
        }
        var reportEntity = reportOpt.get();
        for (var item : reportEntity.getCompleteRequirements()
                .stream().map(ReportRequirementWithCourse::getRequirement)
                .collect(Collectors.toList())
        ) {
            if (item.getId().equals(requirementId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This requirement is already assigned. To re-assign detach it from the mapped course");
            }
        }

        for (var item : reportEntity.getCompleteRequirements()
                .stream().map(ReportRequirementWithCourse::getCourse)
                .collect(Collectors.toList())
        ) {
            if (item.getId().equals(courseId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "This course is already assigned. To re-assign detach it from the mapped requirement");
            }
        }
        reportEntity.getCompleteRequirements()
                .add(
                        new ReportRequirementWithCourse(
                                null,
                                requirementOpt.get(),
                                courseOpt.get()
                        )
                );
        reportEntity.getUnmappedCourses().remove(courseOpt.get());
        reportEntity.getUnmappedRequirements().remove(requirementOpt.get());
        reportRepository.save(reportEntity);
    }

    public void detachRequirement(Long reportId, Long requirementId) {
        var reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report id");
        }
        var reportEntity = reportOpt.get();
        ReportRequirementWithCourse reportRequirementWithCourse = null;
        for (var i : reportEntity.getCompleteRequirements()) {
            if (i.getRequirement().getId().equals(requirementId)) {
                reportEntity.getUnmappedRequirements().add(i.getRequirement());
                reportEntity.getUnmappedCourses().add(i.getCourse());
                reportRequirementWithCourse = i;
                break;
            }
        }
        if (reportRequirementWithCourse != null) {
            reportEntity.getCompleteRequirements().remove(reportRequirementWithCourse);
            reportRepository.save(reportEntity);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid requirement id to detach");
        }
    }

    public void detachCompletedCourse(Long reportId, Long courseId) {
        var reportOpt = reportRepository.findById(reportId);
        if (reportOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report id");
        }
        var reportEntity = reportOpt.get();
        ReportRequirementWithCourse reportRequirementWithCourse = null;
        for (var i : reportEntity.getCompleteRequirements()) {
            if (i.getCourse().getId().equals(courseId)) {
                reportEntity.getUnmappedCourses().add(i.getCourse());
                reportEntity.getUnmappedRequirements().add(i.getRequirement());
                reportRequirementWithCourse = i;
                break;
            }
        }
        if (reportRequirementWithCourse != null) {
            reportEntity.getCompleteRequirements().remove(reportRequirementWithCourse);
            reportRepository.save(reportEntity);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid course id to detach");
        }
    }

    public void deleteById(Long id) {
        if (reportRepository.existsById(id)) {
            reportRepository.deleteById(id);
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid report id");
        }
    }

}
