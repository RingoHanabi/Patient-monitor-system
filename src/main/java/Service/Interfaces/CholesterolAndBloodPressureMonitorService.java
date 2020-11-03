package Service.Interfaces;

import ObserverPattern.ThresholdNumber;
import ObserverPattern.UpdateTime;

public interface CholesterolAndBloodPressureMonitorService extends MonitorService {
    double getAverageCholeLevel();
    void setAverageCholeLevel(double averageCholeLevel);
    void setObservedUpdateTime(UpdateTime observedUpdateTime);
    void setObservedSystolicThresholdNumber(ThresholdNumber observedSystolicThresholdNumber);
}
