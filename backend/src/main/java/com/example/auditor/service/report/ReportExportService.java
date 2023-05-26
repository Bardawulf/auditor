package com.example.auditor.service.report;

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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExportService {

    @Value("${audit.parser.type-prefix}")
    private String requirementTypePrefix;

    @Value("${audit.parser.requirements-row-terminator}")
    private String requirementsRowTerminator;



    public File buildSpreadsheet(StudentRecord studentRecord, StudentReport studentReport) throws IOException {

        Workbook workbook = new XSSFWorkbook();
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

        // =================== pathological workaround to get correct types in spreadsheet
//        int rowNum = 0;
//        for (Pair<String, String> row : generalInformation) {
//            Row sheetRow = sheet.createRow(rowNum);
//            sheetRow.createCell(0).setCellValue(row.getFirst());
//            if (rowNum < 4) {
//                sheetRow.createCell(1).setCellValue(row.getSecond());
//            }
//            else if (rowNum < 6) {
//                sheetRow.createCell(1).setCellValue(Double.parseDouble(row.getSecond()));
//            }
//            else {
//                sheetRow.createCell(1).setCellValue(Integer.parseInt(row.getSecond()));
//            }
//            rowNum += 1;
//        }

        // =================== to think of ===== storing method name
        //        Method m = StudentRecord.class.getMethod("getName", null);

        // =========================================== OLD SHEET COMPOSING
//        String currentRequirementType = "";
//        for (Requirement requirement : studentRecord.getRequirements()) {
//
//            if (requirement.getType() != currentRequirementType) {
//
//                Row categoryRow = sheet.createRow(rowNum);
//                rowNum += 1;
//
//                currentRequirementType = requirement.getType();
//                Cell categoryCell = categoryRow.createCell(0);
//                Font bold = workbook.createFont();
//                bold.setBold(true);
//                CellStyle categoryStyle = workbook.createCellStyle();
//                categoryStyle.setFont(bold);
//                categoryCell.setCellStyle(categoryStyle);
//                categoryCell.setCellValue(requirementTypePrefix + " " + currentRequirementType);
//            }
//
//            Row row = sheet.createRow(rowNum);
//            rowNum += 1;
//
//            row.createCell(0).setCellValue(requirement.getName());
//            row.createCell(1).setCellValue(requirement.getCredit());
//            row.createCell(2).setCellValue(requirement.getPatterns());
//            row.createCell(3).setCellValue(requirement.getAntipatterns());
//        }

        for (int i=0; i<=1; i++) {

            sheetGeneral.autoSizeColumn(i);
        }

        Sheet sheetTerms = workbook.createSheet("terms");

        int rowNumTerms = 0;
        for (StudentTerm studentTerm : studentRecord.getStudentTerms()) {
            Row termTitleRow = sheetTerms.createRow(rowNumTerms);
            CellStyle titleStyle = workbook.createCellStyle();
            Cell titleCell = termTitleRow.createCell(0);
            Font bold = workbook.createFont();
            bold.setBold(true);
            titleStyle.setFont(bold);
            titleCell.setCellStyle(titleStyle);
            titleCell.setCellValue(studentTerm.getName());
            rowNumTerms += 1;

            for (TermCourse termCourse : studentTerm.getTermCourses()) {
                Row courseRow = sheetTerms.createRow(rowNumTerms);
                courseRow.createCell(0).setCellValue(termCourse.getCode());
                courseRow.createCell(1).setCellValue(termCourse.getCredits());
                courseRow.createCell(2).setCellValue(termCourse.getGradePoint());
                courseRow.createCell(3).setCellValue(termCourse.getLetterGradeLiteral());
                rowNumTerms += 1;
            }
        }

        Sheet sheetAudit = workbook.createSheet("audit");

        Row auditTitleRow = sheetAudit.createRow(0);
        auditTitleRow.createCell(0).setCellValue("Required Course");
        auditTitleRow.createCell(1).setCellValue("Credits");
        auditTitleRow.createCell(2).setCellValue("Taken");
        auditTitleRow.createCell(3).setCellValue("Credits");
        auditTitleRow.createCell(4).setCellValue("Grade Points");
        auditTitleRow.createCell(5).setCellValue("Letter Grade");

        int rowNumAudit = 1;
        for (ReportRequirementWithCourse completeRequirement : studentReport.getCompleteRequirements()) {
            Row requirementRow = sheetAudit.createRow(rowNumAudit);
            ReportRequirement requiement = completeRequirement.getRequirement();
            ReportTermCourse course = completeRequirement.getCourse();
            requirementRow.createCell(0).setCellValue(requiement.getName());
            requirementRow.createCell(1).setCellValue(requiement.getCredit());
            String taken = course.getCode();
            if (taken.equals(requiement.getPatterns())) {
                taken = "YES";
            }
            requirementRow.createCell(2).setCellValue(taken);
            requirementRow.createCell(3).setCellValue(course.getCredits());
            requirementRow.createCell(4).setCellValue(course.getGradePoint());
            requirementRow.createCell(5).setCellValue(course.getLetterGrade());
            rowNumAudit += 1;
        }

//  ==================================================

//        int rowNumTerms = 0;
//        for (StudentTerm studentTerm : studentRecord.getStudentTerms()) {
//            Row termTitleRow = sheetTerms.createRow(rowNumTerms);
//            CellStyle titleStyle = workbook.createCellStyle();
//            titleStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
//            titleStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
//            Cell titleCell = termTitleRow.createCell(0);
//            Font bold = workbook.createFont();
//            bold.setBold(true);
//            titleStyle.setFont(bold);
//            titleCell.setCellStyle(titleStyle);
//            titleCell.setCellValue(studentTerm.getName());
//            termTitleRow.createCell(1).setCellStyle(titleStyle);
//            termTitleRow.createCell(2).setCellStyle(titleStyle);
//            termTitleRow.createCell(3).setCellStyle(titleStyle);
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
