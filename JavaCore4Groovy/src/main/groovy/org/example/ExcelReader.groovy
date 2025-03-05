package org.example

import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.DateUtil
import org.apache.poi.ss.usermodel.FormulaEvaluator
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.util.CellRangeAddress
import org.apache.poi.xssf.usermodel.XSSFWorkbook

import java.text.SimpleDateFormat

class ExcelReader {
    static Workbook openFile(String filePath){
        FileInputStream file = null
        Workbook workbook
        try{
            file = new FileInputStream(filePath)
            workbook = new XSSFWorkbook(file)
            return workbook
        } catch (IOException e){
            println "Lỗi khi mở file: ${e.getMessage()}"
        } finally {
            try{
                if(file != null) file.close()
            } catch (IOException e){
                println"Lỗi khi đóng file: ${e.getMessage()}"
            }
        }
        return null
    }

    static List<Employee> readEmployeesFromExcel(Workbook workbook){
        if (!workbook) return []

        List<Employee> employees = []
        Sheet sheet = workbook.getSheetAt(0)
        FormulaEvaluator evaluator = workbook.creationHelper.createFormulaEvaluator()

        List<WorkShift> workShifts = []
        List<String> nameWorkShift = []

        // Đọc tiền lương ca làm việc
        for (int rowIndex = 6; rowIndex <= sheet.lastRowNum; rowIndex++) {
            workShifts = [] // Khởi tạo danh sách ca làm việc
            Row row = sheet.getRow(rowIndex)
            def index = getCellValue(row.getCell(1), evaluator)
            if (index.trim().isEmpty()) break // Dừng nếu ô trống

            for (int colIndex = 3; colIndex <= 15; colIndex++) {
                def valueString = getCellValue(row.getCell(colIndex), evaluator)
                def value = 0.0
                if (!valueString.isEmpty()) {
                    value = Double.parseDouble(valueString)
                }

                Row rowName = sheet.getRow(5)
                def name = getCellValue(rowName.getCell(colIndex), evaluator)
                if (name.isEmpty()) continue // Bỏ qua nếu tên ca trống

                if (name != "\$") {
                    nameWorkShift.add(name) // Thêm tên ca vào danh sách
                } else {
                    nameWorkShift.each { s ->
                        workShifts.add(new WorkShift(s, value)) // Thêm ca làm việc vào danh sách
                    }
                    nameWorkShift = [] // Reset danh sách tên ca
                }
            }

            def employeeId = getCellValue(row.getCell(1), evaluator)
            if (employeeId.trim().isEmpty()) break // Dừng nếu mã nhân viên trống
            def employee = new Employee(employeeId, getCellValue(row.getCell(2), evaluator))

            // Đọc thông tin ngày làm việc
            def workDates = getWorkDates(sheet, evaluator)
            def maxCol = Math.min(row.lastCellNum, workDates.size() + 17)
            ArrayList<WorkDay> days = []
            LinkedHashMap<String, WorkDay> workDayMap = [:]

            for (int colIndex = 17; colIndex < maxCol; colIndex++) {
                def shiftName = getCellValue(sheet.getRow(5).getCell(colIndex), evaluator)
                def hoursString = getCellValue(row.getCell(colIndex), evaluator)
                def hours = 0.0

                if (!hoursString.isEmpty() && hoursString != "-") {
                    try {
                        hours = Double.parseDouble(hoursString)
                    } catch (NumberFormatException ignored) {
                        println "Lỗi khi chuyển đổi giờ làm việc: $hoursString"
                    }
                }

                def salary = 0.0
                workShifts.each { shift ->
                    if (shift.shiftName == shiftName) {
                        salary = shift.salary
                        return // Tương đương với break trong Java
                    }
                }

                def workDate = workDates[colIndex - 17]
                def workDay = workDayMap.get(workDate)
                if (workDay == null) {
                    workDay = new WorkDay(workDate) // Tạo mới nếu ngày chưa tồn tại
                    workDayMap.put(workDate, workDay)
                }
                workDay.addWorkShift(new WorkShift(shiftName, salary), hours) // Thêm ca làm việc
            }

            // Chuyển HashMap thành danh sách WorkDay
            days.addAll(workDayMap.values())
            employee.workDay = days
            employee.calculateSalary()
            employees.add(employee)
        }
        return employees
    }

    private static List<String> getWorkDates(Sheet sheet, FormulaEvaluator evaluator){
        def row = sheet.getRow(3)
        List<String> workDates = []
        Map<String, Integer> countMap = [:]
        for(int colIndex = 17; colIndex < row.lastCellNum; colIndex++){
            def cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK)
            def cellValue = getCellValue(cell, evaluator)

            CellRangeAddress mergedRegion = getMergedRegion(sheet, cell)
            if (mergedRegion){
                cellValue = getCellValue(sheet.getRow(mergedRegion.firstRow).getCell(mergedRegion.firstColumn), evaluator)
                if (countMap[cellValue]){
                    cellValue += " ofNextMonth"
                }
                countMap[cellValue] = 1
                workDates.addAll(Collections.nCopies(mergedRegion.lastColumn - mergedRegion.firstColumn + 1, cellValue))
                colIndex += mergedRegion.lastColumn - mergedRegion.firstColumn
            } else {
                workDates.add(cellValue)
            }
        }
        return  workDates
    }

    private static CellRangeAddress getMergedRegion(Sheet sheet, Cell cell){
        sheet.mergedRegions.find{
            it.isInRange(cell.rowIndex, cell.columnIndex)
        }
    }

    private static String getCellValue(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) {
            return ""
        }
        def dateFormat = new SimpleDateFormat("dd/MM/yyyy")
        switch (cell.getCellType()) {
            case CellType.STRING:
                return cell.stringCellValue
            case CellType.NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    def date = cell.dateCellValue
                    return dateFormat.format(date)
                }
                return String.valueOf(cell.numericCellValue)
            case CellType.BOOLEAN:
                return String.valueOf(cell.booleanCellValue)
            case CellType.FORMULA:
                return String.valueOf(evaluator.evaluate(cell).numberValue)
            case CellType.BLANK:
                return ""
            default:
                return "UNKNOWN"
        }
    }

    static Map<String, Double> getTotalSalary(Workbook workbook) {
        Map<String, Double> totalSalary = [:]
        Sheet sheet = workbook.getSheetAt(0)
        FormulaEvaluator evaluator = workbook.creationHelper.createFormulaEvaluator()

        for (int rowIndex = 6; rowIndex <= sheet.lastRowNum; rowIndex++) {
            Row row = sheet.getRow(rowIndex)
            String employeeId = getCellValue(row.getCell(1), evaluator)
            if (!employeeId?.trim()) continue

            double salary = getCellValue(row.getCell(16), evaluator)?.toDouble() ?: 0.0
            totalSalary[employeeId] = salary
        }
        return totalSalary
    }

}
