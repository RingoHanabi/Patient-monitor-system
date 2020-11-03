import Controller.GUIController;
import Service.Implementation.FHIRServiceImpl;
import Service.Implementation.GUIServiceImpl;
import Service.Interfaces.FHIRService;
import Service.Interfaces.GUIService;

/**
 * Main application class
 */
public class Application {

    private static FHIRService fhirServiceImpl = new FHIRServiceImpl();
    private static GUIService guiService = new GUIServiceImpl();

    public static void main(String[] args) {
        System.out.println("Program Started");
        GUIController guiController = new GUIController(guiService, fhirServiceImpl);
        guiController.generateGUI();
    }
}
