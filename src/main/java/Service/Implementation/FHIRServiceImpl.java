package Service.Implementation;

import Iterator.EncounterIterator;
import Service.Interfaces.FHIRService;
import Service.Interfaces.GUIService;
import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.PerformanceOptionsEnum;
import ca.uhn.fhir.okhttp.client.OkHttpRestfulClientFactory;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import ca.uhn.fhir.rest.gclient.StringClientParam;
import ca.uhn.fhir.util.BundleUtil;
import org.hl7.fhir.instance.model.api.IBaseBundle;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Encounter;
import org.hl7.fhir.r4.model.Observation;
import org.hl7.fhir.r4.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class FHIRServiceImpl implements FHIRService {
    public static String BASE_URL = "https://fhir.monash.edu/hapi-fhir-jpaserver/fhir";

    private FhirContext ctx;
    private IGenericClient client;
    private EncounterIterator encounterIterator;

    public FHIRServiceImpl() {
        ctx = FhirContext.forR4();
        ctx.setRestfulClientFactory(new OkHttpRestfulClientFactory(ctx));
        ctx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
        ctx.getRestfulClientFactory().setConnectTimeout(50 * 1000);
        ctx.getRestfulClientFactory().setSocketTimeout(50 * 1000);
        ctx.setPerformanceOptions(PerformanceOptionsEnum.DEFERRED_MODEL_SCANNING);
        client = ctx.newRestfulGenericClient(BASE_URL);
    }

    /**
     * Function to fetch data of a Patient by patientID
     *
     * @param patientID
     * @return
     */
    @Override
    public Patient getPatientByID(String patientID) {
        // Read a patient with the given ID
        Patient patient = client.read()
                .resource(Patient.class)
                .withId(patientID).execute();

        return patient;
    }

   /**
     * Fetching related encounters of a given practitioner
     * @param practitionerIdentifier
     * @return A list of all the encounters
     */
   @Override
    public List<IBaseResource> getEncounterByPractitionerID(String practitionerIdentifier, GUIService service) {

        // Use this url as a prefix for identifier when searching with identifier
        List<IBaseResource> encounterList = new ArrayList<>();
        // Read a patient with the given ID
        System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
        Bundle bundle = client.search()
                .forResource(Encounter.class)
                .where(
                        // Encounter.PARTICIPANT.hasId(practitionerIdentifier)
                        new StringClientParam("participant.identifier")
                                .matches()
                                .value(practitionerIdentifier)
                )
                .where(new StringClientParam("_count")
                        .matches()
                        .value("20"))
                .returnBundle(Bundle.class)
                //.withAdditionalHeader("Prefer","respond-async")
                .execute();

        encounterList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        encounterIterator = new EncounterIterator(encounterList);
        encounterIterator.deleteRepeatedPatient(service);

        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = client
                    .loadPage()
                    .next(bundle)
                    .execute();
            encounterList.clear();
            encounterList.addAll(BundleUtil.toListOfResources(ctx, bundle));
            encounterIterator = new EncounterIterator(encounterList);
            encounterIterator.deleteRepeatedPatient(service);
        }

        // Print the output
        return encounterList;
    }

    /**
     * Function to get cholesterol observation of a patient by patient id
     * @param patientID: String, patient ID
     * @return A list of observations
     */
    @Override
    public List<IBaseResource> getCholesterolLevelByPatientID(String patientID) {
        List<IBaseResource> observationList = new ArrayList<>();

        // Read a patient with the given ID
        Bundle bundle = client.search()
                .forResource(Observation.class)
                .where(new StringClientParam("patient").matches().value(patientID))
                .where(new StringClientParam("code").matches().value("2093-3"))
                .where(new StringClientParam("_sort:desc").matches().value("date"))
                .where(new StringClientParam("_count").matches().value("10"))
                .returnBundle(Bundle.class)
                .execute();

        observationList.addAll(BundleUtil.toListOfResources(ctx, bundle));

        // Load the subsequent pages
        // Get next page
        while (bundle.getLink(IBaseBundle.LINK_NEXT) != null) {
            bundle = client
                    .loadPage()
                    .next(bundle)
                    .execute();
            observationList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        }

        return observationList;
    }

    /**
     * Function to fetch last 5 blood pressure of the patient provided
     * @param patientID: String, patient ID
     * @return A list of observations
     */
    @Override
    public List<IBaseResource> getBloodPressure(String patientID) {
        List<IBaseResource> observationList = new ArrayList<>();

        // Read a patient with the given ID
        Bundle bundle = client.search()
                .forResource(Observation.class)
                .where(new StringClientParam("patient").matches().value(patientID))
                .where(new StringClientParam("code").matches().value("55284-4"))
                .where(new StringClientParam("_sort:desc").matches().value("date"))
                .where(new StringClientParam("_count").matches().value("5"))
                .returnBundle(Bundle.class)
                .execute();

        observationList.addAll(BundleUtil.toListOfResources(ctx, bundle));
        return observationList;
    }

    /**
     * Function to get the latest smoke status observation
     * @param patientID: String, patient ID
     * @return A list of observations
     */
    @Override
    public List<IBaseResource> getRecentSmokeHistory(String patientID) {
        List<IBaseResource> observationList = new ArrayList<>();

        // Read a patient with the given ID
        Bundle bundle = client.search()
                .forResource(Observation.class)
                .where(new StringClientParam("patient").matches().value(patientID))
                .where(new StringClientParam("code").matches().value("72166-2"))
                .where(new StringClientParam("_sort:desc").matches().value("date"))
                .where(new StringClientParam("_count").matches().value("1"))
                .returnBundle(Bundle.class)
                .execute();
        observationList.addAll(BundleUtil.toListOfResources(ctx, bundle));

        return observationList;


    }
}
