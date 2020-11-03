package Controller;

import Iterator.EncounterIterator;
import Service.Implementation.BloodPressureMonitorServiceImpl;
import Service.Implementation.CholesterolAndBloodPressureMonitorServiceImpl;
import Service.Implementation.CholesterolLevelMonitorServiceImpl;
import Service.Interfaces.FHIRService;
import Service.Interfaces.GUIService;
import Service.Interfaces.MonitorService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GUIController {

    private GUIService guiService;
    private FHIRService fhirService;
    private MonitorService monitorService;

    public static List<IBaseResource> encounterList;


    public GUIController(GUIService guiService, FHIRService FHIRService) {
        this.guiService = guiService;
        this.fhirService = FHIRService;
        guiService.addLoginActionListener(new LoginActionListener());
        guiService.addShowCholesterolMeasurementListener(new ShowCholesterolMeasurement());
        guiService.addShowBloodPressure(new ShowBloodPressure());
        guiService.addShowCholesterolLevelandBloodP(new ShowBloodPandCholeMeasurement());

    }

    public void asyncFetchingEncounterByPractitionerIdentifier(String practitionerID) {
        // An Async task always executes in new thread
        new Thread(
                () ->{
                    encounterList = fhirService.getEncounterByPractitionerID(practitionerID, guiService);
                }).start();
    }

    public void generateGUI() {
        guiService.run();
    }

    class LoginActionListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            String userID = guiService.getUserIDInputField().getText();
            if (!userID.isEmpty()) {
                guiService.setUserID(userID);
                asyncFetchingEncounterByPractitionerIdentifier(userID);
                guiService.createFrameAfterLogin();
            }
        }
    }

    class ShowBloodPressure implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            monitorService = new BloodPressureMonitorServiceImpl(guiService, fhirService);
            monitorService.initialGUI();
        }
    }

    class ShowCholesterolMeasurement implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            monitorService = new CholesterolLevelMonitorServiceImpl(guiService, fhirService);
            monitorService.initialGUI();
        }
    }

    class ShowBloodPandCholeMeasurement implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            monitorService = new CholesterolAndBloodPressureMonitorServiceImpl(guiService, fhirService);
            monitorService.initialGUI();
        }
    }
}
