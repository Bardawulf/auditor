package com.example.auditor.service.report;

import com.example.auditor.domain.curriculum.Curriculum;
import com.example.auditor.domain.curriculum.Requirement;
import com.example.auditor.domain.report.ReportRequirementWithCourse;
import com.example.auditor.domain.report.ReportTermCourse;
import com.example.auditor.domain.report.StudentReport;
import com.example.auditor.domain.transcript.StudentRecord;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
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

    public File buildSpreadsheetSingle(StudentRecord studentRecord, Curriculum curriculum, StudentReport studentReport) throws IOException {
        String filename = "audit " + studentRecord.getName() + " " + curriculum.getMajor() + ".xlsx";
        List<StudentRecord> students = List.of(studentRecord);
        List<StudentReport> studentReports = List.of(studentReport);

        return buildSpreadsheet(students, curriculum, studentReports, filename);
    }

    public File buildSpreadsheetMulti(List<StudentRecord> students, Curriculum curriculum, List<StudentReport> studentReports) throws IOException {
        String filename = "audit-multi " + curriculum.getMajor() + ".xlsx";

        return buildSpreadsheet(students, curriculum, studentReports, filename);
    }

    private File buildSpreadsheet(List<StudentRecord> students, Curriculum curriculum, List<StudentReport> studentReports, String filename) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();

        createStudentInformationSheet(workbook, students, studentReports);
        createAuditSheet(workbook, students, curriculum, studentReports);

        File spreadsheet = new File(filename);

        FileOutputStream fileOutputStream = new FileOutputStream(spreadsheet);

        workbook.write(fileOutputStream);
        workbook.close();
        fileOutputStream.close();

        return spreadsheet;
    }

    private void createStudentInformationSheet(XSSFWorkbook workbook, List<StudentRecord> students, List<StudentReport> studentReports) {
        Sheet sheet = workbook.createSheet("student information");
        int rowNum = 0;

        List<String> rowNames = List.of(
                "Name",
//                "schoolName",
                "ID#",
                "Major",
                "Admission Semester",
                "Current Semester",
                "cGPA",
                "Grade Points Total",
                "Credits Enrolled Total",
                "Credits Earned Total",
                "Credits Graded Total",
                "Unmet degree requirements",
                "Courses failed",
                "Unmapped courses"
                );

        for (String rowName : rowNames) {
            Cell cell = sheet.createRow(rowNum++).createCell(0);
            cell.setCellValue(rowName);
            setBorderRight(cell, BorderStyle.THICK);
        }
        setBorderBottom(sheet.getRow(rowNum-1).getCell(0), BorderStyle.THICK);

        int studentIdx = 0;
        for (StudentRecord student : students) {
            int colIdx = studentIdx + 1;
            rowNum = 0;
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getName());
//            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getSchoolName());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getId());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getMajor());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getAdmissionSemester());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getCurrentSemester());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getGpa());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getGradePointsTotal());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getCreditsEnrolledTotal());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getCreditsEarnedTotal());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(student.getCreditsGradedTotal());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(studentReports.get(studentIdx).getUnmappedRequirements().size());
            sheet.getRow(rowNum++).createCell(colIdx).setCellValue(studentReports.get(studentIdx).getFailedCourses().size());
            sheet.getRow(rowNum).createCell(colIdx).setCellValue(studentReports.get(studentIdx).getUnmappedCourses().size());

            setBoldFont(sheet.getRow(0).getCell(colIdx));
            for (int i = 0; i <= rowNum; i++) {
                Cell cell = sheet.getRow(i).getCell(colIdx);
                setCenteredText(cell);
                setBorderRight(cell, BorderStyle.THIN);
                if (studentIdx % 2 == 0) setBackgroundStudentEven(cell);
                else setBackgroundStudentOdd(cell);
            }
            setBorderBottom(sheet.getRow(rowNum).getCell(colIdx), BorderStyle.THICK);

            studentIdx++;
        }

        rowNum += 2;
        Row currentRow = sheet.createRow(rowNum);
        Cell disclaimerCell = currentRow.createCell(0);
        disclaimerCell.setCellValue("General student information - for audit go to next sheet");
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 3));
        setBackground(disclaimerCell, IndexedColors.ROSE.getIndex());
        setBoldFont(disclaimerCell);

        for (int i=0; i<=studentIdx + 1; i++) {
            sheet.autoSizeColumn(i);
        }
    }

    private void createAuditSheet(XSSFWorkbook workbook, List<StudentRecord> students, Curriculum curriculum, List<StudentReport> studentReports) {
        Sheet sheet = workbook.createSheet("audit");
        int studentsCount = students.size();
        int rowNum = 0;
        int totalCurriculumCredits = 0;
        int totalStudentTranscriptCredits = 0;

        System.out.println("START SHEET");

        Row curriculumAndStudentNamesRow = sheet.createRow(rowNum);
        Cell curriculumName = curriculumAndStudentNamesRow.createCell(0);
        curriculumName.setCellValue("Curriculum: " + curriculum.getMajor());
        sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 0, 1));
        setBoldFont(curriculumName);
        setCenteredText(curriculumName);
        for (int i=0; i<studentsCount; i++) {
            Cell studentName = curriculumAndStudentNamesRow.createCell(2 + 4*i);
            studentName.setCellValue(students.get(i).getName());
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2 + 4*i, 5 + 4*i));
            setBoldFont(studentName);
            setCenteredText(studentName);
        }
        rowNum++;

        Row columnNamesRow = sheet.createRow(rowNum++);
        columnNamesRow.createCell(0).setCellValue("Required Course");
        columnNamesRow.createCell(1).setCellValue("Credits");
        for (int i=0; i<studentsCount; i++) {
            columnNamesRow.createCell(2 + 4*i).setCellValue("Taken");
            columnNamesRow.createCell(3 + 4*i).setCellValue("Credits");
            columnNamesRow.createCell(4 + 4*i).setCellValue("Grade Points");
            columnNamesRow.createCell(5 + 4*i).setCellValue("Letter Grade");
        }
        for (int i=0; i<2 + 4*studentsCount; i++) {
            setBoldFont(columnNamesRow.getCell(i));
            setCenteredText(columnNamesRow.getCell(i));
        }

        System.out.println("START COMPLETE REQUIREMENTS");

        ArrayList<List<ReportRequirementWithCourse>> studentsCompleteRequrements = new ArrayList<List<ReportRequirementWithCourse>>();
        for (int i=0; i<studentsCount; i++) {
            studentsCompleteRequrements.add(studentReports.get(i).getCompleteRequirements());
        }

        ArrayList<Long> alreadyUsedCoursesIds = new ArrayList<>();
        for (Requirement requirement : curriculum.getRequirements()) {
            Row requirementRow = sheet.createRow(rowNum++);
            requirementRow.createCell(0).setCellValue(requirement.getName());
            requirementRow.createCell(1).setCellValue(requirement.getCredit());
            setCenteredText(requirementRow.getCell(1));
            totalCurriculumCredits += requirement.getCredit();

            for (int i=0; i<studentsCount; i++) {
                boolean isRequirementCompleted = false;
                for (ReportRequirementWithCourse completeRequirement : studentsCompleteRequrements.get(i)) {
                    if (completeRequirement.getRequirement().getName().equals(requirement.getName())) {
                        if (!alreadyUsedCoursesIds.contains(completeRequirement.getCourse().getId())) {
                            alreadyUsedCoursesIds.add(completeRequirement.getCourse().getId());
                        }
                        else {
                            continue;
                        }
                        isRequirementCompleted = true;
                        ReportTermCourse course = completeRequirement.getCourse();
                        totalStudentTranscriptCredits += course.getCredits();
                        String taken = course.getCode();
                        if (taken.equals(requirement.getPatterns())) {
                            taken = "✔";
                        }
                        requirementRow.createCell(2 + 4*i).setCellValue(taken);
                        requirementRow.createCell(3 + 4*i).setCellValue(course.getCredits());
                        requirementRow.createCell(4 + 4*i).setCellValue(course.getGradePoint());
                        requirementRow.createCell(5 + 4*i).setCellValue(course.getLetterGrade());
                        break;
                    }
                }
                if (!isRequirementCompleted) {
                    requirementRow.createCell(2 + 4*i).setCellValue("-");
                    requirementRow.createCell(3 + 4*i).setCellValue("-");
                    requirementRow.createCell(4 + 4*i).setCellValue("-");
                    requirementRow.createCell(5 + 4*i).setCellValue("-");
                }
                for (int j=2 + 4*i; j<=5 + 4*i; j++) {
                    setCenteredText(requirementRow.getCell(j));
                    if (i % 2 == 0) {
                        setBackgroundStudentEven(requirementRow.getCell(j));
                    }
                    else {
                        setBackgroundStudentOdd(requirementRow.getCell(j));
                    }
                }
            }
        }

        System.out.println("START FAILED COURSES");

        Row auditFailedCoursesRow = sheet.createRow(rowNum);
        setBackground(auditFailedCoursesRow.createCell(0), IndexedColors.GREY_25_PERCENT.getIndex());
        setBackground(auditFailedCoursesRow.createCell(1), IndexedColors.GREY_25_PERCENT.getIndex());
        int failedCoursesTopRowNum = rowNum;

        int maxFailedCoursesCount = 1;
        for (StudentReport studentReport : studentReports) {
            maxFailedCoursesCount = Math.max(studentReport.getFailedCourses().size(), maxFailedCoursesCount);
        }
        int failedCoursesBottomRowNum = failedCoursesTopRowNum + maxFailedCoursesCount;

        for (int i=0; i<studentsCount; i++) {
            rowNum = failedCoursesTopRowNum;
            Cell failedCoursesTitle = auditFailedCoursesRow.createCell(2 + 4*i);
            failedCoursesTitle.setCellValue("Failed courses");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2 + 4*i, 5 + 4*i));
            setBackground(failedCoursesTitle, IndexedColors.GREY_25_PERCENT.getIndex());
            rowNum++;

            System.out.println("checking student "+i);
            if (studentReports.get(i).getFailedCourses().isEmpty()) {
                System.out.println("failed is empty");
//                Row failedCourseRow = sheet.createRow(rowNum++);
                Row failedCourseRow = sheet.getRow(rowNum);
                if (failedCourseRow == null) {
                    failedCourseRow = sheet.createRow(rowNum);
                }
                setBackground(failedCourseRow.createCell(0), IndexedColors.GREY_25_PERCENT.getIndex());
                setBackground(failedCourseRow.createCell(1), IndexedColors.GREY_25_PERCENT.getIndex());
                failedCourseRow.createCell(2 + 4*i).setCellValue("None");
                failedCourseRow.createCell(3 + 4*i);
                failedCourseRow.createCell(4 + 4*i);
                failedCourseRow.createCell(5 + 4*i);

            }
            else {
                for (ReportTermCourse failedCourse : studentReports.get(i).getFailedCourses()) {
                    Row failedCourseRow = sheet.getRow(rowNum);
                    if (failedCourseRow == null) {
                        failedCourseRow = sheet.createRow(rowNum);
                    }
                    rowNum++;
                    setBackground(failedCourseRow.createCell(0), IndexedColors.GREY_25_PERCENT.getIndex());
                    setBackground(failedCourseRow.createCell(1), IndexedColors.GREY_25_PERCENT.getIndex());
                    failedCourseRow.createCell(2 + 4*i).setCellValue(failedCourse.getCode());
                    failedCourseRow.createCell(3 + 4*i).setCellValue(failedCourse.getCredits());
                    failedCourseRow.createCell(4 + 4*i).setCellValue(failedCourse.getGradePoint());
                    failedCourseRow.createCell(5 + 4*i).setCellValue(failedCourse.getLetterGrade());
//                    failedCourseRow.createCell(10 + 4*i).setCellValue("TestNotEmpty");
                    for (int j=2 + 4*i; j<=5 + 4*i; j++) {
                        setCenteredText(failedCourseRow.getCell(j));
                        if (i % 2 == 0) {
                            setBackgroundStudentEven(failedCourseRow.getCell(j));
                        }
                        else {
                            setBackgroundStudentOdd(failedCourseRow.getCell(j));
                        }
                    }
                }
            }
            for (; rowNum <= failedCoursesBottomRowNum; rowNum++) {
                Row fillerRow = sheet.getRow(rowNum);
                if (fillerRow == null) {
                    fillerRow = sheet.createRow(rowNum);
                }
                for (int j=2 + 4*i; j<=5 + 4*i; j++) {
                    if (i % 2 == 0) {
                        setBackgroundStudentEven(fillerRow.createCell(j));
                    }
                    else {
                        setBackgroundStudentOdd(fillerRow.createCell(j));
                    }
                }
            }
        }

        System.out.println("START UNMAPPED COURSES");

        rowNum = failedCoursesBottomRowNum + 1;

        Row auditUnmappedCoursesRow = sheet.createRow(rowNum);
        setBackground(auditUnmappedCoursesRow.createCell(0), IndexedColors.GREY_25_PERCENT.getIndex());
        setBackground(auditUnmappedCoursesRow.createCell(1), IndexedColors.GREY_25_PERCENT.getIndex());
        int unmappedCoursesTopRowNum = rowNum;

        int maxUnmappedCoursesCount = 1;
        for (StudentReport studentReport : studentReports) {
            maxUnmappedCoursesCount = Math.max(studentReport.getUnmappedCourses().size(), maxUnmappedCoursesCount);
        }
        int unmappedCoursesBottomRowNum = unmappedCoursesTopRowNum + maxUnmappedCoursesCount;

        for (int i=0; i<studentsCount; i++) {
            rowNum = unmappedCoursesTopRowNum;
            Cell unmappedCoursesTitle = auditUnmappedCoursesRow.createCell(2 + 4*i);
            unmappedCoursesTitle.setCellValue("Unmapped courses");
            sheet.addMergedRegion(new CellRangeAddress(rowNum, rowNum, 2 + 4*i, 5 + 4*i));
            setBackground(unmappedCoursesTitle, IndexedColors.GREY_25_PERCENT.getIndex());
            rowNum++;

            if (studentReports.get(i).getUnmappedCourses().isEmpty()) {
                Row unmappedCourseRow = sheet.getRow(rowNum);
                if (unmappedCourseRow == null) {
                    unmappedCourseRow = sheet.createRow(rowNum);
                }
//                rowNum++;
                setBackground(unmappedCourseRow.createCell(0), IndexedColors.GREY_25_PERCENT.getIndex());
                setBackground(unmappedCourseRow.createCell(1), IndexedColors.GREY_25_PERCENT.getIndex());
                unmappedCourseRow.createCell(2 + 4*i).setCellValue("None");
                unmappedCourseRow.createCell(3 + 4*i);
                unmappedCourseRow.createCell(4 + 4*i);
                unmappedCourseRow.createCell(5 + 4*i);
            }
            else {
                for (ReportTermCourse unmappedCourse : studentReports.get(i).getUnmappedCourses()) {
                    Row unmappedCourseRow = sheet.getRow(rowNum);
                    if (unmappedCourseRow == null) {
                        unmappedCourseRow = sheet.createRow(rowNum);
                    }
                    rowNum++;
                    setBackground(unmappedCourseRow.createCell(0), IndexedColors.GREY_25_PERCENT.getIndex());
                    setBackground(unmappedCourseRow.createCell(1), IndexedColors.GREY_25_PERCENT.getIndex());
                    unmappedCourseRow.createCell(2 + 4*i).setCellValue(unmappedCourse.getCode());
                    unmappedCourseRow.createCell(3 + 4*i).setCellValue(unmappedCourse.getCredits());
                    unmappedCourseRow.createCell(4 + 4*i).setCellValue(unmappedCourse.getGradePoint());
                    unmappedCourseRow.createCell(5 + 4*i).setCellValue(unmappedCourse.getLetterGrade());

                    for (int j=2 + 4*i; j<=5 + 4*i; j++) {
                        setCenteredText(unmappedCourseRow.getCell(j));
                        if (i % 2 == 0) {
                            setBackgroundStudentEven(unmappedCourseRow.getCell(j));
                        }
                        else {
                            setBackgroundStudentOdd(unmappedCourseRow.getCell(j));
                        }
                    }
                }
            }
            for (; rowNum <= unmappedCoursesBottomRowNum; rowNum++) {
                Row fillerRow = sheet.getRow(rowNum);
                if (fillerRow == null) {
                    fillerRow = sheet.createRow(rowNum);
                }
                for (int j=2 + 4*i; j<=5 + 4*i; j++) {
                    if (i % 2 == 0) {
                        setBackgroundStudentEven(fillerRow.createCell(j));
                    }
                    else {
                        setBackgroundStudentOdd(fillerRow.createCell(j));
                    }
                }
            }
        }

        System.out.println("START TOTAL CREDITS");

        rowNum = unmappedCoursesBottomRowNum + 1;
        Row auditTotalRow = sheet.createRow(rowNum);
        auditTotalRow.createCell(0).setCellValue("TOTAL credits");
        auditTotalRow.createCell(1).setCellValue(totalCurriculumCredits);
        for (int i=0; i<studentsCount; i++) {
            auditTotalRow.createCell(2 + 4*i);
            auditTotalRow.createCell(3 + 4*i).setCellValue(students.get(i).getCreditsEarnedTotal());
            auditTotalRow.createCell(4 + 4*i);
            auditTotalRow.createCell(5 + 4*i);
        }

        System.out.println("START FINAL STYLING");

        // LONG BORDERS
        // curriculum courses
        int lastColumnIdx = 1 + 4 * studentsCount;

        RegionUtil.setBorderRight(BorderStyle.THICK, new CellRangeAddress(0, rowNum, 1, 1), sheet);
        RegionUtil.setBorderBottom(BorderStyle.THICK, new CellRangeAddress(1, 1, 0, lastColumnIdx), sheet);

        // individual students
        for (int i=0; i<studentsCount; i++) {
            int columnIdx = 5 + 4*i;
            RegionUtil.setBorderRight(BorderStyle.THIN, new CellRangeAddress(0, rowNum, columnIdx, columnIdx), sheet);
        }

        System.out.println("START STYLING FAILED COURSES");
        int failedCoursesRowIdx = auditFailedCoursesRow.getRowNum();
        CellRangeAddress failedCoursesTopCRA = new CellRangeAddress(failedCoursesRowIdx, failedCoursesRowIdx, 0,lastColumnIdx);
        CellRangeAddress failedCoursesBottomCRA = new CellRangeAddress(failedCoursesRowIdx, failedCoursesRowIdx, 2,lastColumnIdx);
        System.out.println("START REGION UTIL");
        RegionUtil.setBorderTop(BorderStyle.THICK, failedCoursesTopCRA, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, failedCoursesBottomCRA, sheet);
        System.out.println("DONE STYLING FAILED COURSES");

        int unmappedCoursesRowIdx = auditUnmappedCoursesRow.getRowNum();
        CellRangeAddress unmappedCoursesCRA = new CellRangeAddress(unmappedCoursesRowIdx, unmappedCoursesRowIdx, 2,lastColumnIdx);
        RegionUtil.setBorderTop(BorderStyle.THICK, unmappedCoursesCRA, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THIN, unmappedCoursesCRA, sheet);

        int totalCreditsRowIdx = auditTotalRow.getRowNum();
        CellRangeAddress totalCreditsCRA = new CellRangeAddress(totalCreditsRowIdx, totalCreditsRowIdx, 0, lastColumnIdx);
        RegionUtil.setBorderTop(BorderStyle.THICK, totalCreditsCRA, sheet);
        RegionUtil.setBorderBottom(BorderStyle.THICK, totalCreditsCRA, sheet);
        for (int i=0; i<2 + 4*studentsCount; i++) {
            setBackground(auditTotalRow.getCell(i), IndexedColors.ROSE.getIndex());
        }

        // auto sizing and freezing headers
        for (int i=0; i<2 + 4*studentsCount; i++) {
            sheet.autoSizeColumn(i);
        }
        sheet.createFreezePane(2, 2);

        System.out.println("DONE SHEET");
    }

    private void setBoldFont(Cell cell) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        Font bold = cell.getSheet().getWorkbook().createFont();
        bold.setBold(true);
        cellStyle.setFont(bold);

        cell.setCellStyle(cellStyle);
    }

    private void setCenteredText(Cell cell) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        cellStyle.setAlignment(HorizontalAlignment.CENTER);

        cell.setCellStyle(cellStyle);
    }

    private void setBackgroundStudentEven(Cell cell) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_TURQUOISE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cell.setCellStyle(cellStyle);
    }

    private void setBackgroundStudentOdd(Cell cell) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cell.setCellStyle(cellStyle);
    }

    private void setBackground(Cell cell, short colorIndex) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillForegroundColor(colorIndex);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        cell.setCellStyle(cellStyle);
    }

    private void setBorderRight(Cell cell, BorderStyle thickness) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        cellStyle.setBorderRight(thickness);

        cell.setCellStyle(cellStyle);
    }

    private void setBorderBottom(Cell cell, BorderStyle thickness) {
        CellStyle cellStyle = cell.getSheet().getWorkbook().createCellStyle();
        CellStyle oldCellStyle = cell.getCellStyle();
        if(oldCellStyle == null) {
            oldCellStyle = cell.getSheet().getWorkbook().createCellStyle();
        }
        else {
            cellStyle.cloneStyleFrom(oldCellStyle);
        }
        cellStyle.setBorderBottom(thickness);

        cell.setCellStyle(cellStyle);
    }


//    private void s(Cell cell) {
//        CellStyle cellStyle = cell.getCellStyle();
//        if(cellStyle == null) {
//            cellStyle = workbook.createCellStyle();
//        }
//
//
//        cell.setCellStyle(cellStyle);
//    }

}