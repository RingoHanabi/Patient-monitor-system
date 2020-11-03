package Service.Implementation;

import Model.BloodPressure;
import Model.Patient;
import ObserverPattern.Observer;
import ObserverPattern.ThresholdNumber;
import ObserverPattern.UpdateTime;
import Service.Interfaces.BloodPressureMonitorService;
import Service.Interfaces.FHIRService;
import Service.Interfaces.GUIService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BloodPressureMonitorServiceImpl extends Observer implements BloodPressureMonitorService {

    private List<Patient> monitoringPatient = new ArrayList<Patient>();
    private UpdateTime observedUpdateTime = new UpdateTime(this);
    private ThresholdNumber observedSystolicThresholdNumber = new ThresholdNumber(this);
    private ThresholdNumber observedDiastolicThresholdNumber = new ThresholdNumber(this);
    private Timer timer = new Timer();
    private int highSystolicNo = 0;
    private int highDiastolicNo = 0;
    private SmokerCheck smokerCheck = new SmokerCheck();
    private FHIRService fhirService;
    private GUIService guiService;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    // Initializing variables for GUI
    private final JFrame mainFrame = new JFrame();
    private final JPanel leftLabelPanel = new JPanel();
    private final JPanel tablePanel = new JPanel();
    private final JPanel mainContentPanel = new JPanel();
    private final JPanel leftContentPanel = new JPanel();

    private final JLabel monitorPatientLabel = new JLabel();
    private final JLabel enterFrequencyLabel = new JLabel();
    private final JLabel enterSystolicThresholdLabel = new JLabel();
    private final JLabel enterDiastolicThresholdLabel = new JLabel();
    private final JLabel highSystolicCount = new JLabel();
    private final JLabel highDiastolicCount = new JLabel();
    private final JLabel neverSmokerCount = new JLabel();

    private JTextField updateTimeInputField = new JTextField();
    private JTextField systolicThresholdInputField = new JTextField();
    private JTextField diastolicThresholdInputField = new JTextField();
    private JTextArea last5Measurements = new JTextArea();

    private JTable table;
    private JFrame frameForLineChart = new JFrame();

    public BloodPressureMonitorServiceImpl(GUIService guiService, FHIRService fhirService) {
        this.guiService = guiService;
        this.fhirService = fhirService;
    }
    @Override
    public List<Patient> getMonitoringPatient() {
        return monitoringPatient;
    }

    @Override
    public void setMonitoringPatient(List<Patient> monitoringPatient) {
        this.monitoringPatient = monitoringPatient;
    }

    @Override
    public void setObservedUpdateTime(UpdateTime observedUpdateTime) {
        this.observedUpdateTime = observedUpdateTime;
    }

    @Override
    public void setObservedSystolicThresholdNumber(ThresholdNumber observedSystolicThresholdNumber) {
        this.observedSystolicThresholdNumber = observedSystolicThresholdNumber;
    }

    /**
     * Add a patient to monitor list
     *
     * @param patient: Patient to monitor
     */
    @Override
    public void monitorPatient(Patient patient) {
        if (!this.monitoringPatient.contains(patient)) {
            this.monitoringPatient.add(patient);
            System.out.println("Successfully monitored a patient");
        } else {
            // Todo: Throw an error when this happens
            System.out.println("Patient is already monitoring");
        }
    }

    /**
     * Remove a patient from monitor list
     * Throw an error when patient is not monitoring
     *
     * @param patient: Patient to stop monitoring
     */
    @Override
    public void stopMonitorPatient(Patient patient) {
        try {
            this.monitoringPatient.remove(patient);
            System.out.println("Successfully removed patient from monitoring list.");
        } catch (Exception e) {
            System.out.println(e.getCause());
        }
    }
    /**
     * Implementing update function of observers.
     */
    @Override
    public void update() {
        if (this.observedUpdateTime.isChanged()) {
            System.out.println("Monitoring Service receive update time information");
            // Check if the timer already exists
            if (timer != null) {
                // Cancel the timer when timer already exists.
                timer.cancel();
            }
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    updateTable();
                }
            }, 0, observedUpdateTime.getUpdateTime());

            // Set the change status to false so further changes can be detected.
            this.observedUpdateTime.setIsChanged(false);
        } else if (this.observedSystolicThresholdNumber.isChanged()) {
            updateTable();

            // Set the change status to false so further changes can be detected.
            this.observedSystolicThresholdNumber.setIsChanged(false);
        } else if (this.observedDiastolicThresholdNumber.isChanged()) {
            updateTable();

            // Set the change status to false so further changes can be detected.
            this.observedDiastolicThresholdNumber.setIsChanged(false);
        }
    }

    /**
     * Function to initialise GUI
     */
    @Override
    public void initialGUI() {
        monitoringPatient.clear();

        monitorPatientLabel.setText("Monitoring Patient");
        enterFrequencyLabel.setText("Enter the frequency you want to update the data: ");
        enterSystolicThresholdLabel.setText("Enter the threshold number of Systolic BP: ");
        enterDiastolicThresholdLabel.setText("Enter the threshold number of Diastolic BP: ");
        highSystolicCount.setText("Number of patients with high systolic bp: " + highSystolicNo);
        highDiastolicCount.setText("Number of patients with high diastolic bp: " + highDiastolicNo);
        neverSmokerCount.setText("Never smokers: 0");

        updateTimeInputField.addActionListener(observedUpdateTime);
        systolicThresholdInputField.addActionListener(observedSystolicThresholdNumber);
        diastolicThresholdInputField.addActionListener(observedDiastolicThresholdNumber);

        leftLabelPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        leftLabelPanel.setLayout(new GridLayout(0, 1));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        tablePanel.setLayout(new GridLayout(0, 1));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        mainContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        leftContentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        leftContentPanel.setLayout(new GridLayout(0, 1));

        // Setting up left label panel
        leftLabelPanel.add(monitorPatientLabel);
        leftLabelPanel.add(enterFrequencyLabel);
        leftLabelPanel.add(updateTimeInputField);
        leftLabelPanel.add(enterSystolicThresholdLabel);
        leftLabelPanel.add(systolicThresholdInputField);
        leftLabelPanel.add(enterDiastolicThresholdLabel);
        leftLabelPanel.add(diastolicThresholdInputField);
        leftLabelPanel.add(highSystolicCount);
        leftLabelPanel.add(highDiastolicCount);
        leftLabelPanel.add(neverSmokerCount);
        leftContentPanel.add(leftLabelPanel);
        JScrollPane l5scrollPane = new JScrollPane(last5Measurements);
        last5Measurements.setSize(100,100);
        leftContentPanel.add(l5scrollPane);

        // Creating the table
        String[] columnNames = {"patientID", "Systolic Blood Pressure", "Diastolic Blood Pressure", "Time"};
        DefaultTableModel defaultmodel = new DefaultTableModel(new Object[][]{}, columnNames);
        table = new JTable(defaultmodel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                switch (convertColumnIndexToModel(column)) {
                    case 0:
                    case 3:
                        return String.class;
                    case 1:
                    case 2:
                        return Float.class;
                }

                return super.getColumnClass(column);
            }
        };
        table.setDefaultRenderer(Float.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 1) {
                    c.setForeground(monitoringPatient.get(row).getLatestBloodPressure().isSystolicHighlight() ? Color.MAGENTA : Color.black);
                }
                else if (column == 2) {
                    c.setForeground(monitoringPatient.get(row).getLatestBloodPressure().isDiastolicHighlight() ? Color.MAGENTA : Color.black);
                }
                return c;
            }
        });
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Creating variable of a frame
                JFrame patientDetailFrame = new JFrame();
                JLabel nameLB = new JLabel();
                JLabel genderLB = new JLabel();
                JLabel addressLB = new JLabel();
                JLabel birthLB = new JLabel();
                JPanel contentPane = new JPanel();

                // Setting up patient data labels
                Patient patient = monitoringPatient.get(table.getSelectedRow());
                nameLB.setText("Patient Name:" + patient.getName());
                genderLB.setText("Gender: " + patient.getGender());
                addressLB.setText("Address: " + patient.getAddress());
                birthLB.setText("Birth date: " + patient.getBirthDate());

                // Setting up panel
                contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
                contentPane.setLayout(new GridLayout(0, 1));
                contentPane.add(nameLB);
                contentPane.add(genderLB);
                contentPane.add(addressLB);
                contentPane.add(birthLB);

                // Setting up frame
                patientDetailFrame.setTitle("Patient Detail");
                patientDetailFrame.add(contentPane);
                patientDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                patientDetailFrame.pack();
                patientDetailFrame.setVisible(true);

            }
        });
        TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
            switch (i) {
                case 0:
                case 1:
                case 2:
                    column = table.getColumnModel().getColumn(i);
                    column.setMaxWidth(50);
                case 3:
                    column = table.getColumnModel().getColumn(i);
                    column.setMaxWidth(200);
            }
        }

        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(false);
        tablePanel.add(scrollPane);
        mainContentPanel.add(leftContentPanel);
        mainContentPanel.add(tablePanel);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setContentPane(mainContentPanel);
        mainFrame.setTitle("Patient Blood pressure");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                timer.cancel();
            }
        });
        mainFrame.pack();
        mainFrame.setVisible(true);
        asyncFetchBPData();
    }

    /**
     * This function will refresh the table.
     * UpdateTable function to be called when either:
     * 1. Frequency of fetching data changes
     * 2. Threshold number changes
     */
    private void updateTable() {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
        tableModel.setRowCount(0);

        for (int i = 0; i < monitoringPatient.size(); i++) {
                String patientID = monitoringPatient.get(i).getId();
                List<IBaseResource> patientBPlist = fhirService.getBloodPressure(patientID);

                if (patientBPlist.size() != 0) {
                    Patient patient = monitoringPatient.get(i);
                    Observation patientObservation = (Observation) patientBPlist.get(0);
                    patient.getLatestBloodPressure().setSystolicHighlight(false);
                    patient.getLatestBloodPressure().setDiastolicHighlight(false);
                    patient.getLatestBloodPressure().setDiastolicMeasurement( patientObservation.getComponent().get(0).getValueQuantity().getValue());
                    patient.getLatestBloodPressure().setSystolicMeasurement(patientObservation.getComponent().get(1).getValueQuantity().getValue());
                    patient.getLatestBloodPressure().setTime(dateFormat.format(patientObservation.getIssued()));
                    DefaultTableModel model = (DefaultTableModel) table.getModel();
                    Object[] rowToAdd = new Object[]{patient.getId(),
                            patient.getLatestBloodPressure().getSystolicMeasurement().doubleValue(),
                            patient.getLatestBloodPressure().getDiastolicMeasurement().doubleValue(),
                            patient.getLatestBloodPressure().getTime()};

                    addlast5BPMeasurements(patientBPlist, patient);
                    model.addRow(rowToAdd);
                    table.repaint();
                    monitoringPatient.set(i, patient);
                }
            }

        updateSmokerCountLabel();
        if(this.observedDiastolicThresholdNumber.getThresholdNumber() != Float.POSITIVE_INFINITY || this.observedSystolicThresholdNumber.getThresholdNumber() != Float.POSITIVE_INFINITY) {
            highlightPatientBySystolicThreshold();
            highlightPatientByDiastolicThreshold();
            frameForLineChart.setVisible(false);
            createBPLineChart();
        }
    }

    /**
     * Highlight patients whose cholesterol level is higher than threshold number.
     */
    private void highlightPatientBySystolicThreshold() {
        String stringToAdd = "";
        highSystolicNo = 0;
        for (Patient p : monitoringPatient) {
            if (p.getLatestBloodPressure().getSystolicMeasurement().floatValue() >= observedSystolicThresholdNumber.getThresholdNumber()) {
                p.getLatestBloodPressure().setSystolicHighlight(true);
                stringToAdd += p.displayLast5Tests();
                highSystolicNo += 1;
            } else if (!p.getLatestBloodPressure().isSystolicHighlight()){
                p.getLatestBloodPressure().setSystolicHighlight(false);
            }
        }
        highSystolicCount.setText("Number of patients with high systolic bp: " + highSystolicNo);
        last5Measurements.setText(stringToAdd);

    }

    private void highlightPatientByDiastolicThreshold() {
        highDiastolicNo = 0;
        for (Patient p : monitoringPatient) {
            if (p.getLatestBloodPressure().getDiastolicMeasurement().floatValue() >= observedDiastolicThresholdNumber.getThresholdNumber()) {
                p.getLatestBloodPressure().setDiastolicHighlight(true);
                highDiastolicNo += 1;
            } else if (!p.getLatestBloodPressure().isDiastolicHighlight()) {
                p.getLatestBloodPressure().setDiastolicHighlight(false);
            }
        }
        highDiastolicCount.setText("Number of patients with high diastolic bp: " + highDiastolicNo);
    }

    private void createBPLineChart() {
        frameForLineChart = new JFrame();
        JPanel contentP = new JPanel();

        contentP.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        contentP.setLayout(new GridLayout(0, 1));
        Boolean chartHasData = false;

        for (Patient patient : getMonitoringPatient()) {
            if (patient.getLatestBloodPressure().isSystolicHighlight()) {
                DefaultCategoryDataset dcd = new DefaultCategoryDataset();
                int counter = 1;
                for (BloodPressure bp: patient.getLast5BloodPressures()) {
                    dcd.setValue(bp.getSystolicMeasurement(), "Values",String.valueOf(counter));
                    counter += 1;
                    chartHasData = true;
                }
                JFreeChart chart = ChartFactory.createLineChart(patient.getName(),
                        null,
                        null,
                        dcd,
                        PlotOrientation.VERTICAL,
                        true,
                        true,
                        false);
                CategoryPlot plot = chart.getCategoryPlot();
                plot.setRangeGridlinePaint(Color.BLACK);
                ChartPanel chartP = new ChartPanel(chart);
                contentP.add(chartP);
            }
        }

        frameForLineChart.setLayout(new BorderLayout());
        frameForLineChart.setContentPane(contentP);
        frameForLineChart.setTitle("Systolic blood pressure charts");
        frameForLineChart.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameForLineChart.pack();
        frameForLineChart.setVisible(chartHasData);
    }

    private void addlast5BPMeasurements(List<IBaseResource> resources, Patient patient) {
        patient.getLast5BloodPressures().clear();
        for (Integer i = 0; i <= 4; i++) {
            if ( i < resources.size()) {
                IBaseResource resource = resources.get(i);
                Observation patientObservation = (Observation) resource;
                String strDate = dateFormat.format(patientObservation.getIssued());
                BloodPressure patientBP = new BloodPressure("mmHg",
                        patientObservation.getComponent().get(0).getValueQuantity().getValue(),
                        patientObservation.getComponent().get(1).getValueQuantity().getValue(),
                        strDate);

                patient.getLast5BloodPressures().add(patientBP);
            }
        }
    }

    private void updateSmokerCountLabel() {
        int neverSmokerCount = 0;
        for (Patient patient: monitoringPatient) {
            if (patient.getNeverSmoker()) {
                neverSmokerCount += 1;
            }
        }
        this.neverSmokerCount.setText("Never smokers: " + neverSmokerCount);
    }

    private void asyncFetchBPData() {
        new Thread(() -> {
            for (int i = 0; i < guiService.getData().size(); i++) {
                if (guiService.getTable().getValueAt(i, 2).equals(true)) {
                    String patientID = guiService.getRelatedPatientIDList().get(i);
                    List<IBaseResource> patientBPlist = fhirService.getBloodPressure(patientID);
                    BloodPressure patientBP;
                    Boolean status = this.smokerCheck.smokerCheck(patientID);
                    if (patientBPlist.size() != 0) {
                        Observation patientObservation = (Observation) patientBPlist.get(0);
                        String bpIssued = dateFormat.format(patientObservation.getIssued());
                        patientBP = new BloodPressure("mmHg",
                                patientObservation.getComponent().get(0).getValueQuantity().getValue(),
                                patientObservation.getComponent().get(1).getValueQuantity().getValue(),
                                bpIssued);
                        org.hl7.fhir.r4.model.Patient patientR4 = fhirService.getPatientByID(patientID);
                        String birthDate = dateFormat.format(patientR4.getBirthDate());
                        Patient patient = new Patient(patientR4.getIdElement().getIdPart(),
                                patientR4.getName().get(0).getNameAsSingleString(),
                                patientR4.getName().get(0).getGivenAsSingleString(),
                                birthDate,
                                patientR4.getGender().toString(),
                                patientR4.getAddress().get(0).getLine().get(0)
                                        + ", " + patientR4.getAddress().get(0).getCity()
                                        + ", " + patientR4.getAddress().get(0).getState()
                                        + ", " + patientR4.getAddress().get(0).getCountry(),
                                null,
                                patientBP,
                                status);

                        addlast5BPMeasurements(patientBPlist, patient);
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        Object[] rowToAdd = new Object[]{patient.getId(),
                                patient.getLatestBloodPressure().getSystolicMeasurement().doubleValue(),
                                patient.getLatestBloodPressure().getDiastolicMeasurement().doubleValue(),
                                patient.getLatestBloodPressure().getTime()};
                        model.addRow(rowToAdd);
                        table.repaint();
                        monitorPatient(patient);
                    }
                }
            }
            updateSmokerCountLabel();
        }).start();
    }
}
