package Service.Interfaces;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Patient;

import java.util.List;

public interface FHIRService {
    Patient getPatientByID(String patientID);
    List<IBaseResource> getEncounterByPractitionerID(String practitionerIdentifier, GUIService service);
    List<IBaseResource> getCholesterolLevelByPatientID(String patientID);
    List<IBaseResource> getBloodPressure(String patientID);
    List<IBaseResource> getRecentSmokeHistory(String patientID);
}
