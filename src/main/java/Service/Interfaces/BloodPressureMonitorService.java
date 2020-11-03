package Service.Interfaces;

import ObserverPattern.ThresholdNumber;
import ObserverPattern.UpdateTime;

public interface BloodPressureMonitorService extends MonitorService {
    void setObservedUpdateTime(UpdateTime observedUpdateTime) ;
    void setObservedSystolicThresholdNumber(ThresholdNumber observedSystolicThresholdNumber);
}
