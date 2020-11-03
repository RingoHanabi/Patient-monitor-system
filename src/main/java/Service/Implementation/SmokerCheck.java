package Service.Implementation;

import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Observation;

import java.util.List;

public class SmokerCheck {
    private FHIRServiceImpl fhirServiceImpl = new FHIRServiceImpl();

    public Boolean smokerCheck(String patientID) {
        List<IBaseResource> observationList = this.fhirServiceImpl.getRecentSmokeHistory(patientID);
        if (observationList.size() != 0 ) {
            String status = ((CodeableConcept) ((Observation) observationList.get(0)).getValue()).getText();
            return status.equals("Never smoker");
        } else {
            return false;
        }
    }
}
