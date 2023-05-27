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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    @Value("${audit.parser.type-prefix}")
    private String requirementTypePrefix;

    @Value("${audit.parser.requirements-row-terminator}")
    private String requirementsRowTerminator;

    private XSSFWorkbook workbook;


    public File buildSpreadsheet(StudentRecord studentRecord, Curriculum curriculum, StudentReport studentReport) throws IOException {

        workbook = new XSSFWorkbook();

        Font bold = workbook.createFont();
        bold.setBold(true);

//        XSSFColor color = new XSSFColor(new java.awt.Color(239, 195, 239));

        CellStyle cellStyleBoldCentered = workbook.createCellStyle();
        cellStyleBoldCentered.setAlignment(HorizontalAlignment.CENTER);
        cellStyleBoldCentered.setFont(bold);

        CellStyle cellStyleCentered = workbook.createCellStyle();
        cellStyleCentered.setAlignment(HorizontalAlignment.CENTER);

        CellStyle cellStyleCenteredAndBackgroundOdd = workbook.createCellStyle();
        cellStyleCenteredAndBackgroundOdd.setAlignment(HorizontalAlignment.CENTER);
        cellStyleCenteredAndBackgroundOdd.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        cellStyleCenteredAndBackgroundOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        CellStyle cellStyleCenteredAndBorderRightBold = workbook.createCellStyle();
        cellStyleCenteredAndBorderRightBold.setAlignment(HorizontalAlignment.CENTER);
        cellStyleCenteredAndBorderRightBold.setBorderRight(BorderStyle.THICK);

        CellStyle cellStyleCenteredAndBorderRight = workbook.createCellStyle();
        cellStyleCenteredAndBorderRight.setAlignment(HorizontalAlignment.CENTER);
        cellStyleCenteredAndBorderRight.setBorderRight(BorderStyle.THIN);

        CellStyle cellStyleCenteredAndBorderBottom = workbook.createCellStyle();
        cellStyleCenteredAndBorderBottom.setAlignment(HorizontalAlignment.CENTER);
        cellStyleCenteredAndBorderBottom.setBorderBottom(BorderStyle.THIN);

        CellStyle cellStyleBoldCenteredAndBorderBottom = workbook.createCellStyle();
        cellStyleBoldCenteredAndBorderBottom.setFont(bold);
        cellStyleBoldCenteredAndBorderBottom.setAlignment(HorizontalAlignment.CENTER);
        cellStyleBoldCenteredAndBorderBottom.setBorderBottom(BorderStyle.THIN);

        CellStyle cellStyleBoldCenteredAndBorderBottomAndBorderRight = workbook.createCellStyle();
        cellStyleBoldCenteredAndBorderBottomAndBorderRight.setFont(bold);
        cellStyleBoldCenteredAndBorderBottomAndBorderRight.setAlignment(HorizontalAlignment.CENTER);
        cellStyleBoldCenteredAndBorderBottomAndBorderRight.setBorderBottom(BorderStyle.THIN);
        cellStyleBoldCenteredAndBorderBottomAndBorderRight.setBorderRight(BorderStyle.THICK);

        Sheet sheetGeneral = workbook.createSheet("general");

        ArrayList<Pair<String, String>> generalInformation = new ArrayList<Pair<String, String>>();
        // Strings
        generalInformation.add(Pair.of("Name", studentRecord.getName()));
        generalInformation.add(Pair.of("schoolName", studentRecord.getSchoolName()));
        generalInformation.add(Pair.of("Major", studentRecord.getMajor()));
        generalInformation.add(Pair.of("Admission Semester", studentRecord.getAdmissionSemester()));
        // Doubles
        generalInformation.add(Pair.of("GPA", studentRecord.getGpa().toString()));
        generalInformation.add(Pair.of("Grade Points Total", studentRecord.getGradePointsTotal().toString()));
        // Integers
        generalInformation.add(Pair.of("Credits Enrolled Total", studentRecord.getCreditsEnrolledTotal().toString()));
        generalInformation.add(Pair.of("Credits Earned Total", studentRecord.getCreditsEarnedTotal().toString()));
        generalInformation.add(Pair.of("credits Graded Total", studentRecord.getCreditsGradedTotal().toString()));
        generalInformation.add(Pair.of("Current Semester", studentRecord.getCurrentSemester().toString()));

        int rowNumGeneral = 0;
        for (Pair<String, String> row : generalInformation) {
            Row sheetRow = sheetGeneral.createRow(rowNumGeneral);
            sheetRow.createCell(0).setCellValue(row.getFirst());
            sheetRow.createCell(1).setCellValue(row.getSecond());
            rowNumGeneral += 1;
        }

        for (int i=0; i<=1; i++) {

            sheetGeneral.autoSizeColumn(i);
        }

        // =================================== TERMS =======================================

//        Sheet sheetTerms = workbook.createSheet("terms");
//
//        int rowNumTerms = 0;
//        for (StudentTerm studentTerm : studentRecord.getStudentTerms()) {
//            Row termTitleRow = sheetTerms.createRow(rowNumTerms);
//            Cell titleCell = termTitleRow.createCell(0);
//            titleCell.setCellStyle(cellStyleBoldCentered);
//            titleCell.setCellValue(studentTerm.getName());
//            rowNumTerms += 1;
//
//            for (TermCourse termCourse : studentTerm.getTermCourses()) {
//                Row courseRow = sheetTerms.createRow(rowNumTerms);
//                courseRow.createCell(0).setCellValue(termCourse.getCode());
//                courseRow.createCell(1).setCellValue(termCourse.getCredits());
//                courseRow.createCell(2).setCellValue(termCourse.getGradePoint());
//                courseRow.createCell(3).setCellValue(termCourse.getLetterGradeLiteral());
//                rowNumTerms += 1;
//            }
//        }

        // ================================= AUDIT v1 ========== only complete requirements

//        Sheet sheetAudit = workbook.createSheet("audit");
//
//        Row auditTitleRow = sheetAudit.createRow(0);
//        auditTitleRow.createCell(0).setCellValue("Required Course");
//        auditTitleRow.createCell(1).setCellValue("Credits");
//        auditTitleRow.createCell(2).setCellValue("Taken");
//        auditTitleRow.createCell(3).setCellValue("Credits");
//        auditTitleRow.createCell(4).setCellValue("Grade Points");
//        auditTitleRow.createCell(5).setCellValue("Letter Grade");
//        for (int i=0; i<=5; i++) {
//            auditTitleRow.getCell(i).setCellStyle(cellStyleBoldCentered);
//        }
//
//        int rowNumAudit = 1;
//        for (ReportRequirementWithCourse completeRequirement : studentReport.getCompleteRequirements()) {
//            Row requirementRow = sheetAudit.createRow(rowNumAudit);
//            ReportRequirement requirement = completeRequirement.getRequirement();
//            ReportTermCourse course = completeRequirement.getCourse();
//            requirementRow.createCell(0).setCellValue(requirement.getName());
//            requirementRow.createCell(1).setCellValue(requirement.getCredit());
//            String taken = course.getCode();
//            if (taken.equals(requirement.getPatterns())) {
//                taken = "✔";
//            }
//            requirementRow.createCell(2).setCellValue(taken);
//            requirementRow.createCell(3).setCellValue(course.getCredits());
//            requirementRow.createCell(4).setCellValue(course.getGradePoint());
//            requirementRow.createCell(5).setCellValue(course.getLetterGrade());
//            for (int i=1; i<=5; i++) {
//                requirementRow.getCell(i).setCellStyle(cellStyleCentered);
//            }
//            rowNumAudit += 1;
//        }
//
//        for (int i=0; i<=5; i++) {
//            sheetAudit.autoSizeColumn(i);
//        }

        // =========================== AUDIT v2 ====== ALL REQUIREMENTS ON ONE SHEET ===========

        Sheet sheetAudit = workbook.createSheet("audit");
        int rowNumAudit = 0;
        int totalCurriculumCredits = 0;
        int totalStudentTranscriptCredits = 0;

        Row auditStudentsNameRow = sheetAudit.createRow(rowNumAudit++);
        auditStudentsNameRow.createCell(1).setCellStyle(cellStyleCenteredAndBorderRightBold);
        auditStudentsNameRow.createCell(2).setCellValue(studentRecord.getName());
        auditStudentsNameRow.getCell(2).setCellStyle(cellStyleBoldCentered);
        sheetAudit.addMergedRegion(new CellRangeAddress(rowNumAudit-1, rowNumAudit-1, 2, 5));

        Row auditTitleRow = sheetAudit.createRow(rowNumAudit++);
        auditTitleRow.createCell(0).setCellValue("Required Course");
        auditTitleRow.createCell(1).setCellValue("Credits");
        auditTitleRow.createCell(2).setCellValue("Taken");
        auditTitleRow.createCell(3).setCellValue("Credits");
        auditTitleRow.createCell(4).setCellValue("Grade Points");
        auditTitleRow.createCell(5).setCellValue("Letter Grade");
        for (int i=0; i<=5; i++) {
            auditTitleRow.getCell(i).setCellStyle(cellStyleBoldCenteredAndBorderBottom);
        }
        auditTitleRow.getCell(1).setCellStyle(cellStyleBoldCenteredAndBorderBottomAndBorderRight);

        List<ReportRequirementWithCourse> studentCompleteRequrements = studentReport.getCompleteRequirements();

        for (Requirement requirement : curriculum.getRequirements()) {
            Row requirementRow = sheetAudit.createRow(rowNumAudit++);
            requirementRow.createCell(0).setCellValue(requirement.getName());
            requirementRow.createCell(1).setCellValue(requirement.getCredit());
            totalCurriculumCredits += requirement.getCredit();
            Boolean isRequirementCompleted = false;
            for (ReportRequirementWithCourse completeRequirement : studentCompleteRequrements) {
                if (completeRequirement.getRequirement().getName().equals(requirement.getName())) {
                    isRequirementCompleted = true;
                    ReportTermCourse course = completeRequirement.getCourse();
                    totalStudentTranscriptCredits += course.getCredits();
                    String taken = course.getCode();
                    if (taken.equals(requirement.getPatterns())) {
                        taken = "✔";
                    }
                    requirementRow.createCell(2).setCellValue(taken);
                    requirementRow.createCell(3).setCellValue(course.getCredits());
                    requirementRow.createCell(4).setCellValue(course.getGradePoint());
                    requirementRow.createCell(5).setCellValue(course.getLetterGrade());
                    break;
                }
            }
            if (!isRequirementCompleted) {
                requirementRow.createCell(2).setCellValue("-");
                requirementRow.createCell(3).setCellValue("-");
                requirementRow.createCell(4).setCellValue("-");
                requirementRow.createCell(5).setCellValue("-");
            }
            requirementRow.getCell(1).setCellStyle(cellStyleCenteredAndBorderRightBold);
            for (int i=2; i<=5; i++) {
                requirementRow.getCell(i).setCellStyle(cellStyleCenteredAndBackgroundOdd);
            }
        }

        Row auditFailedCoursesRow = sheetAudit.createRow(rowNumAudit++);
        auditFailedCoursesRow.createCell(0).setCellValue("Failed courses");

        for (ReportTermCourse failedCourse : studentReport.getFailedCourses()) {
            Row failedCourseRow = sheetAudit.createRow(rowNumAudit++);
            failedCourseRow.createCell(3).setCellValue(failedCourse.getCredits());
            failedCourseRow.createCell(4).setCellValue(failedCourse.getGradePoint());
            failedCourseRow.createCell(5).setCellValue(failedCourse.getLetterGrade());
        }

        rowNumAudit++;
        Row auditTotalRow = sheetAudit.createRow(rowNumAudit++);
        auditTotalRow.createCell(0).setCellValue("TOTAL");
        auditTotalRow.createCell(2).setCellValue(totalCurriculumCredits);
        auditTotalRow.createCell(4).setCellValue(totalStudentTranscriptCredits);

        for (int i=0; i<=5; i++) {
            sheetAudit.autoSizeColumn(i);
        }

        // ======================================================================================

        File spreadsheet = new File(
                studentRecord.getName()
                        + " transcript"
                        + ".xlsx"
        );

        FileOutputStream fileOutputStream = new FileOutputStream(spreadsheet);

        workbook.write(fileOutputStream);
        workbook.close();
        fileOutputStream.close();

        return spreadsheet;
    }

}
