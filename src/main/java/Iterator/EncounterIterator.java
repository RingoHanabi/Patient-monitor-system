package Iterator;

import Service.Interfaces.GUIService;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Reference;

import java.util.List;

public class EncounterIterator implements AbstractIterator{

    private List<Encounter> encounterList;

    private int position = 0;

    public EncounterIterator (List encounterList){
        this.encounterList = encounterList;
    }
    @Override
    public boolean hasNext() {
        return position < this.encounterList.size();
    }

    @Override
    public Encounter getNext() {
        Encounter result = this.encounterList.get(position);
        position += 1;
        return result;
    }

    @Override
    public void reset() {
        position = 0;
    }

    public void deleteRepeatedPatient(GUIService guiService) {
        List<String> patientIDList = guiService.getRelatedPatientIDList();
        List<String> patientNameList = guiService.getRelatedPatientNameList();
        while (hasNext()) {
            Encounter encounter = this.getNext();
            // Get the related patients
            Reference subject = encounter.getSubject();

            // Remove "Patient/" from the string
            String patientID = subject.getReference().substring(8);
            String patientName = subject.getDisplay();

            if (!patientIDList.contains(patientID)) {
                // patientIDList.add(patientID);
                patientIDList.add(patientID);
                patientNameList.add(patientName);
                guiService.refreshTable();
            }
        }
    }
}
