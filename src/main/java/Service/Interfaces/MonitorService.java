package Service.Interfaces;

import Model.Patient;

import java.util.List;

/**
 * MonitorService interface
 */
public interface MonitorService {
    void setMonitoringPatient(List<Patient> monitoringPatient);
    List<Patient> getMonitoringPatient();
    void monitorPatient(Patient patient);
    void stopMonitorPatient(Patient patient);
    void initialGUI();
}
