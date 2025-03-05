package org.example

import org.apache.poi.ss.usermodel.Workbook

import java.text.DecimalFormat

static void main(String[] args) {
    def filePath = "C:\\Users\\HTLComputer\\OneDrive - University of transport and Communication\\Tài liệu\\BangCong.xlsx"
    def excelReader = new ExcelReader()
    Workbook workbook = excelReader.openFile(filePath)
    def employees = excelReader.readEmployeesFromExcel(workbook)
    def df = new DecimalFormat("#.##")
    employees.each {
        it.printWorkDetails()
        println "Tổng tiền của nhân viên là: ${df.format(it.getTotalSalary())} so với trong file là:  ${df.format(excelReader.getTotalSalary(workbook).get(it.getEmployeeId()))} "
    }
}