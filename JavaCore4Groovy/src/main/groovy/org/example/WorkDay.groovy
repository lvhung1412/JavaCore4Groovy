package org.example

class WorkDay {
    private String date
    private HashMap<WorkShift, Double> workShifts

    public WorkDay(){
        workShifts = new HashMap<>()
    }

    // Constructor
    public WorkDay(String date) {
        this.date = date
        this.workShifts = new HashMap<>() // Khởi tạo HashMap để tránh NullPointerException
    }

    // Constructor
    public WorkDay(String date, HashMap<WorkShift, Double> workShifts) {
        this.date = date
        this.workShifts = workShifts
    }

    // Getter và Setter
    public String getDate() {
        return date
    }

    public void setDate(String date) {
        this.date = date
    }

    public HashMap<WorkShift, Double> getWorkShifts() {
        return workShifts
    }
    // Thêm một ca làm việc vào ngày
    public void addWorkShift(WorkShift workShift, double hours) {
        // Kiểm tra xem ca làm việc đã có chưa, nếu có thì cộng dồn số giờ
        workShifts.put(workShift, workShifts.getOrDefault(workShift, 0.0) + hours)
    }
}
