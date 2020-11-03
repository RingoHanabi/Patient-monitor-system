package Service.Interfaces;

import ObserverPattern.ThresholdNumber;
import ObserverPattern.UpdateTime;

public interface CholesterolLevelMonitorService extends MonitorService {
    double getAverageCholeLevel();
    void setAverageCholeLevel(double averageCholeLevel);
    void setObservedUpdateTime(UpdateTime observedUpdateTime);
    void setObservedThresholdNumber(ThresholdNumber observedThresholdNumber);
}
