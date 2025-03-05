package org.example

class WorkShift {
    private String shiftName
    private double salary

    // Constructor
    WorkShift(String shiftName, double salary) {
        this.shiftName = shiftName
        this.salary = salary
    }

    // Getter và Setter
    String getShiftName() {
        return shiftName
    }

    void setShiftName(String shiftName) {
        this.shiftName = shiftName
    }

    double getSalary() {
        return salary
    }

    void setSalary(double salary) {
        this.salary = salary
    }
}

