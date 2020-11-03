package Model;

import java.math.BigDecimal;

public class BloodPressure {

    private String unit;
    private BigDecimal diastolicMeasurement;
    private BigDecimal systolicMeasurement;
    private boolean diastolicHighlight = false;
    private boolean systolicHighlight = false;
    private String time;

    public BloodPressure(String unit, BigDecimal diastolicMeasurement, BigDecimal systolicMeasurement, String time) {
        this.unit = unit;
        this.diastolicMeasurement = diastolicMeasurement;
        this.systolicMeasurement = systolicMeasurement;
        this.time = time;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public BigDecimal getDiastolicMeasurement() {
        return diastolicMeasurement;
    }

    public void setDiastolicMeasurement(BigDecimal diastolicMeasurement) {
        this.diastolicMeasurement = diastolicMeasurement;
    }

    public BigDecimal getSystolicMeasurement() {
        return systolicMeasurement;
    }

    public void setSystolicMeasurement(BigDecimal systolicMeasurement) {
        this.systolicMeasurement = systolicMeasurement;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isDiastolicHighlight() {
        return diastolicHighlight;
    }

    public void setDiastolicHighlight(boolean diastolicHighlight) {
        this.diastolicHighlight = diastolicHighlight;
    }

    public boolean isSystolicHighlight() {
        return systolicHighlight;
    }

    public void setSystolicHighlight(boolean systolicHighlight) {
        this.systolicHighlight = systolicHighlight;
    }

    @Override
    public String toString() {
        return "BloodPressure{" +
                "unit='" + unit + '\'' +
                ", diastolicMeasurement=" + diastolicMeasurement +
                ", systolicMeasurement=" + systolicMeasurement +
                ", diastolicHighlight=" + diastolicHighlight +
                ", systolicHighlight=" + systolicHighlight +
                ", time=" + time +
                '}';
    }
}
