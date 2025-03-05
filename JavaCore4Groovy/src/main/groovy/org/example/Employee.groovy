package org.example

import java.text.DecimalFormat

class Employee {
    private String employeeId
    private String employeeName
    private List<WorkDay> workDays
    private double totalSalary

    Employee(String employeeId, String employeeName){
        this.employeeId = employeeId
        this.employeeName = employeeName
        this.workDays = new ArrayList<>()
        this.totalSalary = 0.0
    }

    void setEmployeeId(String employeeId) {
        this.employeeId = employeeId
    }

    String getEmployeeId(){
        return this.employeeId
    }

    String getEmployeeName() {
        return employeeName
    }

    void setEmployeeName(String employeeName) {
        this.employeeName = employeeName
    }

    List<WorkDay> getWorkDays() {
        return workDays
    }

    double getTotalSalary() {
        return totalSalary
    }

    void setWorkDay(ArrayList<WorkDay> days){
        this.workDays = days
    }

    void addWorkDay(WorkDay workDay){
        workDays.add(workDay)
    }

    void calculateSalary(){
        totalSalary = 0.0;
        workDays.each { workDay ->
            workDay.workShifts.each { shift, hours ->
                totalSalary += shift.salary * hours
            }
        }
    }

    void printWorkDetails() {
        def df = new DecimalFormat("#")
        println "\n=================================================================================================="
        println "Employee: ${employeeId} - ${employeeName}"
        println "=========================================================="

        def totalTimeWorkingInMonth = 0.0
        def totalSalaryInMonth = 0.0
        def hasWorkedInAnyDay = false

        // Header của bảng
        printf "%-17s | %-50s | %-10s | %-12s%n", "Date", "Shifts", "Hours", "Total (VND)"
        println "--------------------------------------------------------------------"

        workDays.each { workDay ->
            def totalTimeInDay = 0.0
            def totalMoneyInDay = 0.0
            def hasWorked = false
            def existWorkShift = true
            def formattedDate = workDay.date // Có thể truy cập trực tiếp thuộc tính thay vì getDate()

            // Danh sách lưu các ca làm việc trong ngày
            def shiftsInDay = []

            workDay.workShifts.each { shift, hours ->
                if (hours > 0) {
                    hasWorked = true
                    totalTimeInDay += hours
                    totalTimeWorkingInMonth += hours

                    if (shift.salary <= 0) {
                        existWorkShift = false
                    }
                    def moneyEarned = shift.salary * hours
                    totalMoneyInDay += moneyEarned
                    totalSalaryInMonth += moneyEarned

                    // Thêm tên ca + số giờ vào danh sách
                    shiftsInDay << "${shift.shiftName} (${hours}h)"
                }
            }

            if (hasWorked) {
                hasWorkedInAnyDay = true
                printf "%-17s | %-50s | %-10.1f | %-12s%n",
                        formattedDate, shiftsInDay.join(", "), totalTimeInDay, df.format(totalMoneyInDay)
            }
            if (!existWorkShift) {
                println "Không tìm thấy thông tin ca làm việc"
            }
        }

        println "--------------------------------------------------------------------"
        printf "%-70s | %-10.1f | %-12s%n", "TOTAL", totalTimeWorkingInMonth, df.format(totalSalaryInMonth)
        println "=========================================================="

        if (!hasWorkedInAnyDay) {
            println "    No work days for this employee."
        }
    }

}
