import java.time.LocalDate;

public class VacationRequest {
    private String employeeUsername;
    private LocalDate startDate;
    private LocalDate endDate;
    private String additionalInfo;

    public VacationRequest(String employeeUsername, LocalDate startDate, LocalDate endDate, String additionalInfo) {
        this.employeeUsername = employeeUsername;
        this.startDate = startDate;
        this.endDate = endDate;
        this.additionalInfo = additionalInfo;
    }

    public String getEmployeeUsername() {
        return employeeUsername;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }
}
