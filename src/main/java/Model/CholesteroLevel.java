package Model;

import java.math.BigDecimal;

public class CholesteroLevel {

    private BigDecimal value;
    private String effectiveDateTime;
    private String unit;
    private boolean cholesteroHighlight = false;

    public CholesteroLevel(BigDecimal measurement, String effectiveDateTime, String unit) {
        this.value = measurement;
        this.effectiveDateTime = effectiveDateTime;
        this.unit = unit;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public String getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(String effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public boolean isCholesteroHighlight() {
        return cholesteroHighlight;
    }

    public void setCholesteroHighlight(boolean cholesteroHighlight) {
        this.cholesteroHighlight = cholesteroHighlight;
    }

    @Override
    public String toString() {
        return "CholesteroLevel{" +
                "value=" + value +
                ", effectiveDateTime=" + effectiveDateTime +
                ", unit='" + unit + '\'' +
                ", cholesteroHighlight=" + cholesteroHighlight +
                '}';
    }
}
