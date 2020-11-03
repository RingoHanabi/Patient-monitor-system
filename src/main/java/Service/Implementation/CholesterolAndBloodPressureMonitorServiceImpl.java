package Service.Implementation;

import Model.BloodPressure;
import Model.CholesteroLevel;
import Model.Patient;
import ObserverPattern.Observer;
import ObserverPattern.ThresholdNumber;
import ObserverPattern.UpdateTime;
import Service.Interfaces.CholesterolAndBloodPressureMonitorService;
import Service.Interfaces.FHIRService;
import Service.Interfaces.GUIService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CholesterolAndBloodPressureMonitorServiceImpl extends Observer implements CholesterolAndBloodPressureMonitorService {

    private List<Patient> monitoringPatient = new ArrayList<Patient>();
    private UpdateTime observedUpdateTime = new UpdateTime(this);
    private ThresholdNumber observedCholesterolThresholdNumber = new ThresholdNumber(this);
    private ThresholdNumber observedSystolicThresholdNumber = new ThresholdNumber(this);
    private ThresholdNumber observedDiastolicThresholdNumber = new ThresholdNumber(this);
    private double averageCholeLevel = 0.0;
    private FHIRService fhirService;
    private GUIService guiService;
    private SmokerCheck smokerCheck = new SmokerCheck();
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");
    private Timer timer = new Timer();

    // Initializing variable for GUI
    private final JPanel tabelPanel = new JPanel();
    private final JPanel mainContentPanel = new JPanel();
    private final JPanel leftContentPanel = new JPanel();
    private final JLabel enterSystolicThresholdLabel = new JLabel();
    private final JLabel enterDiastolicThresholdLabel = new JLabel();
    private final JLabel enterCholesterolThresholdLabel = new JLabel();
    private final  JFrame mainFrame = new JFrame();
    private final JPanel leftLabelPanel = new JPanel();
    private final JLabel monitoringPatientLabel = new JLabel();
    private final JLabel enterFrequencyLabel = new JLabel();
    private final JTextField updateTimeInputField = new JTextField();
    private final JTextField systolicThresholdNumberInputField = new JTextField();
    private final JTextField diastolicThresholdNumberInputField = new JTextField();
    private final JTextField cholesterolThresholdNumberInputField = new JTextField();
    private JTable table;
    private ChartFrame chartFrame;
    private final JLabel aboveAverageCount = new JLabel();
    private final JLabel highSystolicCount = new JLabel();
    private final JLabel highDiastolicCount = new JLabel();
    private final JLabel neverSmokerCount = new JLabel();
    private int highSystolicNo = 0;
    private int highDiastolicNo = 0;
    private int aboveAverageNo = 0;
    private JTextPane last5Measurements = new JTextPane();
    private JFrame frameForLineChart = new JFrame();

    public CholesterolAndBloodPressureMonitorServiceImpl(GUIService guiService, FHIRService fhirService) {
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
    public double getAverageCholeLevel() {
        return averageCholeLevel;
    }

    @Override
    public void setAverageCholeLevel(double averageCholeLevel) {
        this.averageCholeLevel = averageCholeLevel;
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
     * Highlight patient by comparing cholesterol measurement with the average number.
     */
    private void highlightPatient() {
        for (Patient p : monitoringPatient) {
            if (p.getCholeLevel() != null && p.getCholeLevel().getValue().floatValue() >= averageCholeLevel) {
                p.getCholeLevel().setCholesteroHighlight(true);
                aboveAverageNo += 1;
            }
        }

        aboveAverageCount.setText("Patients above average cholesterol: " + aboveAverageNo);
    }

    /**
     * Calculate the average cholesterol level.
     */
    private void calculateAverageCholeLevel() {
        double totalCholeLevel = 0;
        int availablePatient = 0;
        for (Patient patient : monitoringPatient) {

            // Add all the cholesterolevel together
            if (patient.getCholeLevel() != null) {
                totalCholeLevel += patient.getCholeLevel().getValue().doubleValue();
                availablePatient += 1;
            }
        }

        this.averageCholeLevel = totalCholeLevel / availablePatient;
    }

    /**
     * Implementing update function of observers.
     */
    @Override
    public void update() {
        if (this.observedUpdateTime.isChanged()) {
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
        } else if (this.observedCholesterolThresholdNumber.isChanged()) {
            updateTable();

            // Set the change status to false so further changes can be detected.
            this.observedSystolicThresholdNumber.setIsChanged(false);
        }
    }

    /**
     * Function to initialise GUI
     */
    @Override
    public void initialGUI() {
        monitoringPatient.clear();

        // Setting up labels
        monitoringPatientLabel.setText("Monitoring Patient");
        enterFrequencyLabel.setText("Enter the frequency you want to update the data: ");
        enterSystolicThresholdLabel.setText("Enter the threshold number of Systolic BP: ");
        enterDiastolicThresholdLabel.setText("Enter the threshold number of Diastolic BP: ");
        enterCholesterolThresholdLabel.setText("Enter the threshold number of Cholesterol: ");
        aboveAverageCount.setText("Patients above average cholesterol: " + aboveAverageNo);
        highSystolicCount.setText("Number of patients with high systolic bp: " + highSystolicNo);
        highDiastolicCount.setText("Number of patients with high diastolic bp: " + highDiastolicNo);
        neverSmokerCount.setText("Never smokers: 0");

        // Adding action listener to each textfield
        updateTimeInputField.addActionListener(observedUpdateTime);
        systolicThresholdNumberInputField.addActionListener(observedSystolicThresholdNumber);
        diastolicThresholdNumberInputField.addActionListener(observedDiastolicThresholdNumber);
        cholesterolThresholdNumberInputField.addActionListener(observedCholesterolThresholdNumber);

        // Setting up panels' layout and border
        leftLabelPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        leftLabelPanel.setLayout(new GridLayout(0, 1));
        tabelPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        tabelPanel.setLayout(new GridLayout(0, 1));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        mainContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        leftContentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        leftContentPanel.setLayout(new GridLayout(0, 1));

        // Setting up left label panel
        leftLabelPanel.add(monitoringPatientLabel);
        leftLabelPanel.add(enterFrequencyLabel);
        leftLabelPanel.add(updateTimeInputField);
        leftLabelPanel.add(enterSystolicThresholdLabel);
        leftLabelPanel.add(systolicThresholdNumberInputField);
        leftLabelPanel.add(enterDiastolicThresholdLabel);
        leftLabelPanel.add(diastolicThresholdNumberInputField);
        leftLabelPanel.add(enterCholesterolThresholdLabel);
        leftLabelPanel.add(cholesterolThresholdNumberInputField);
        leftLabelPanel.add(aboveAverageCount);
        leftLabelPanel.add(highSystolicCount);
        leftLabelPanel.add(highDiastolicCount);
        leftLabelPanel.add(neverSmokerCount);
        leftContentPanel.add(leftLabelPanel);
        last5Measurements.setPreferredSize(new Dimension(100,100));
        JScrollPane l5scrollPane = new JScrollPane(last5Measurements,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        leftContentPanel.add(l5scrollPane);

        // Creating the table
        String[] columnNames = {"patientID", "cholestrol", "issued", "Systolic Blood Pressure", "Diastolic Blood Pressure", "Time"};
        DefaultTableModel defaultmodel = new DefaultTableModel(new Object[][]{}, columnNames);
        table = new JTable(defaultmodel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return String.class;
            }
        };
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (column == 3) {
                    c.setForeground(!(monitoringPatient.get(row).getLatestBloodPressure() == null) && monitoringPatient.get(row).getLatestBloodPressure().isSystolicHighlight()
                            ? Color.MAGENTA : Color.black);
                }
                else if (column == 4) {
                    c.setForeground(!(monitoringPatient.get(row).getLatestBloodPressure() == null) && monitoringPatient.get(row).getLatestBloodPressure().isDiastolicHighlight()
                            ? Color.MAGENTA : Color.black);
                }
                else if (column == 1) {
                    c.setForeground(!(monitoringPatient.get(row).getCholeLevel() == null) && monitoringPatient.get(row).getCholeLevel().isCholesteroHighlight()
                            ? Color.RED : Color.black);
                }
                return c;
            }
        });

        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                JFrame patientDetailFrame = new JFrame();
                JLabel nameLB = new JLabel();
                JLabel genderLB = new JLabel();
                JLabel addressLB = new JLabel();
                JLabel birthLB = new JLabel();
                JPanel contentPane = new JPanel();
                Patient patient = monitoringPatient.get(table.getSelectedRow());
                nameLB.setText("Patient Name:" + patient.getName());
                genderLB.setText("Gender: " + patient.getGender());
                addressLB.setText("Address: " + patient.getAddress());
                birthLB.setText("Birth date: " + patient.getBirthDate());
                contentPane.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
                contentPane.setLayout(new GridLayout(0, 1));
                contentPane.add(nameLB);
                contentPane.add(genderLB);
                contentPane.add(addressLB);
                contentPane.add(birthLB);
                patientDetailFrame.setTitle("Patient Detail");
                patientDetailFrame.add(contentPane);
                patientDetailFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                patientDetailFrame.pack();
                patientDetailFrame.setVisible(true);

            }
        });
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(false);
        tabelPanel.add(scrollPane);
        mainContentPanel.add(leftContentPanel);
        mainContentPanel.add(tabelPanel);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setContentPane(mainContentPanel);
        mainFrame.setTitle("Cholesterol Level and Blood Pressure");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                // Cancel the timer after window is closed
                timer.cancel();
            }
        });
        mainFrame.pack();
        mainFrame.setVisible(true);
        asyncFetchCholeDataAndBP();
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
            List<IBaseResource> patientCholList = fhirService.getCholesterolLevelByPatientID(patientID);
            BloodPressure patientBP;
            CholesteroLevel patientChol;
            Boolean patientHasBP = false;
            Boolean patientHasChole = false;
            Patient patient = monitoringPatient.get(i);
            if (patientCholList.size() != 0) {
                patientHasChole = true;
                Observation patientObservation = (Observation) patientCholList.get(0);
                String cholesterolIssuedDate = dateFormat.format(patientObservation.getIssued());
                patientChol = new CholesteroLevel(patientObservation.getValueQuantity().getValue(),
                        cholesterolIssuedDate,
                        patientObservation.getValueQuantity().getCode());
                patient.setCholeLevel(patientChol);
            }
            if (patientBPlist.size() != 0) {
                patientHasBP = true;
                Observation patientObservation = (Observation) patientBPlist.get(0);
                String bpDate = dateFormat.format(patientObservation.getIssued());
                patientBP = new BloodPressure("mmHg",
                        patientObservation.getComponent().get(0).getValueQuantity().getValue(),
                        patientObservation.getComponent().get(1).getValueQuantity().getValue(),
                        bpDate);
                patient.setLatestBloodPressure(patientBP);
                addlast5BPMeasurements(patientBPlist, patient);
            }
            Object[] rowToAdd = new Object[]{patient.getId(),
                    patientHasChole ? String.valueOf(patient.getCholeLevel().getValue().doubleValue()) : '-',
                    patientHasChole ? patient.getCholeLevel().getEffectiveDateTime() : '-',
                    patientHasBP ? patient.getLatestBloodPressure().getSystolicMeasurement().doubleValue() : '-',
                    patientHasBP ? patient.getLatestBloodPressure().getDiastolicMeasurement().doubleValue() : '-',
                    patientHasBP ? patient.getLatestBloodPressure().getTime() : '-'};
            tableModel.addRow(rowToAdd);
            table.repaint();
        }

        calculateAverageCholeLevel();
        highlightPatient();
        highlightPatientByCholesterolThreshold();
        if(this.observedDiastolicThresholdNumber.getThresholdNumber() != Float.POSITIVE_INFINITY || this.observedSystolicThresholdNumber.getThresholdNumber() != Float.POSITIVE_INFINITY) {
            highlightPatientBySystolicThreshold();
            highlightPatientByDiastolicThreshold();
            frameForLineChart.setVisible(false);
            createBPLineChart();
        }
        chartFrame.setVisible(false);
        createGraph();
    }

    /**
     * Highlight patients whose cholesterol level is higher than threshold number.
     */
    private void highlightPatientByCholesterolThreshold() {
        for (Patient p : monitoringPatient) {
            // Need to make sure cholesterol level is not null
            if (p.getCholeLevel() != null) {
                if (p.getCholeLevel().getValue().floatValue() >= observedCholesterolThresholdNumber.getThresholdNumber()) {
                    p.getCholeLevel().setCholesteroHighlight(true);
                } else if (!p.getCholeLevel().isCholesteroHighlight()) {
                    p.getCholeLevel().setCholesteroHighlight(false);
                }
            }
        }
    }

    private void highlightPatientBySystolicThreshold() {
        String stringToAdd = "";
        highSystolicNo = 0;
        for (Patient p : monitoringPatient) {
            if (p.getLatestBloodPressure() != null) {
                if (p.getLatestBloodPressure().getSystolicMeasurement().floatValue() >= observedSystolicThresholdNumber.getThresholdNumber()) {
                    p.getLatestBloodPressure().setSystolicHighlight(true);
                    stringToAdd += p.displayLast5Tests();
                    highSystolicNo += 1;
                } else if (!p.getLatestBloodPressure().isSystolicHighlight()) {
                    p.getLatestBloodPressure().setSystolicHighlight(false);
                }
            }
        }
        highSystolicCount.setText("Number of patients with high systolic bp: " + highSystolicNo);
        last5Measurements.setText(stringToAdd);

    }

    private void highlightPatientByDiastolicThreshold() {
        highDiastolicNo = 0;
        for (Patient p : monitoringPatient) {
            if (p.getLatestBloodPressure() != null) {
                if (p.getLatestBloodPressure().getDiastolicMeasurement().floatValue() >= observedDiastolicThresholdNumber.getThresholdNumber()) {
                    p.getLatestBloodPressure().setDiastolicHighlight(true);
                    highDiastolicNo += 1;
                } else if (!p.getLatestBloodPressure().isDiastolicHighlight()) {
                    p.getLatestBloodPressure().setDiastolicHighlight(false);
                }
            }
        }
        highDiastolicCount.setText("Number of patients with high diastolic bp: " + highDiastolicNo);
    }

    private void createGraph() {
        DefaultCategoryDataset dcd = new DefaultCategoryDataset();
        Boolean dcdHasData = false;
        for (Patient patient : getMonitoringPatient()) {
            if (patient.getCholeLevel() != null) {
                dcd.setValue(patient.getCholeLevel().getValue(),
                        "Cholesterol measurement",
                        patient.getName());
                dcdHasData = true;
            }
        }

        JFreeChart chart = ChartFactory.createBarChart("Total Cholesterol level mg/dl",
                null,
                null, dcd,
                PlotOrientation.VERTICAL,
                true,
                true,
                false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setRangeGridlinePaint(Color.BLACK);

        chartFrame = new ChartFrame("Measurement chart", chart, true);
        chartFrame.setDefaultCloseOperation(ChartFrame.DISPOSE_ON_CLOSE);
        chartFrame.setSize(800, 800);
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //get dimension of screen size
        chartFrame.setLocation(screenSize.width-1000,screenSize.height-1000);
        chartFrame.setVisible(dcdHasData);
    }

    private void createBPLineChart() {
        frameForLineChart = new JFrame();
        JPanel contentP = new JPanel();
        JScrollPane scrollPane = new JScrollPane(contentP,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        contentP.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        contentP.setLayout(new GridLayout(0, 1));
        Boolean chartHasData = false;

        for (Patient patient : getMonitoringPatient()) {
            if (patient.getLatestBloodPressure() != null && patient.getLatestBloodPressure().isSystolicHighlight()) {
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
        frameForLineChart.setContentPane(scrollPane);
        frameForLineChart.setTitle("Systolic blood pressure charts");
        frameForLineChart.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frameForLineChart.pack();
        frameForLineChart.setVisible(chartHasData);
    }

    private void addlast5BPMeasurements(List<IBaseResource> resources, Patient patient) {
        patient.getLast5BloodPressures().clear();
        for (int i = 0; i <= 4; i++) {
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

    private void asyncFetchCholeDataAndBP() {
        new Thread(() -> {
            for (int i = 0; i < guiService.getData().size(); i++) {
                if (guiService.getTable().getValueAt(i, 2).equals(true)) {
                    String patientID = guiService.getRelatedPatientIDList().get(i);
                    Boolean status = this.smokerCheck.smokerCheck(patientID);
                    List<IBaseResource> patientBPlist = fhirService.getBloodPressure(patientID);
                    List<IBaseResource> patientCholList = fhirService.getCholesterolLevelByPatientID(patientID);
                    BloodPressure patientBP = null;
                    CholesteroLevel patientChol = null;
                    Boolean patientHasBP = false;
                    Boolean patientHasChole = false;
                    Patient patient = null;
                    if (patientCholList.size() != 0) {
                        patientHasChole = true;
                        Observation patientObservation = (Observation) patientCholList.get(0);
                        String issuedDate = dateFormat.format(patientObservation.getIssued());
                        patientChol = new CholesteroLevel(patientObservation.getValueQuantity().getValue(),
                                issuedDate, patientObservation.getValueQuantity().getCode());
                        org.hl7.fhir.r4.model.Patient patientR4 = fhirService.getPatientByID(patientID);
                        String birthDate = dateFormat.format(patientR4.getBirthDate());
                        patient = new Patient(patientR4.getIdElement().getIdPart(),
                                patientR4.getName().get(0).getNameAsSingleString(),
                                patientR4.getName().get(0).getGivenAsSingleString(),
                                birthDate,
                                patientR4.getGender().toString(),
                                patientR4.getAddress().get(0).getLine().get(0)
                                        + ", " + patientR4.getAddress().get(0).getCity()
                                        + ", " + patientR4.getAddress().get(0).getState()
                                        + ", " + patientR4.getAddress().get(0).getCountry(),
                                patientChol,
                                null,
                                status);
                    }
                    if (patientBPlist.size() != 0) {
                        patientHasBP = true;
                        Observation patientObservation = (Observation) patientBPlist.get(0);
                        String bpissued = dateFormat.format(patientObservation.getIssued());
                        patientBP = new BloodPressure("mmHg",
                                patientObservation.getComponent().get(0).getValueQuantity().getValue(),
                                patientObservation.getComponent().get(1).getValueQuantity().getValue(),
                                bpissued);
                        if (patient != null) {
                            patient.setLatestBloodPressure(patientBP);
                        }
                        else {
                            org.hl7.fhir.r4.model.Patient patientR4 = fhirService.getPatientByID(patientID);
                            String birthDate = dateFormat.format(patientR4.getBirthDate());
                            patient = new Patient(patientR4.getIdElement().getIdPart(),
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
                        }
                        addlast5BPMeasurements(patientBPlist,patient);
                    }
                    if (patient != null) {
                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        Object[] rowToAdd = new Object[]{patient.getId(),
                                patientHasChole ? String.valueOf(patient.getCholeLevel().getValue().doubleValue()) : '-',
                                patientHasChole ? patient.getCholeLevel().getEffectiveDateTime() : '-',
                                patientHasBP ? patient.getLatestBloodPressure().getSystolicMeasurement().doubleValue() : '-',
                                patientHasBP ? patient.getLatestBloodPressure().getDiastolicMeasurement().doubleValue() : '-',
                                patientHasBP ? patient.getLatestBloodPressure().getTime() : '-'};
                        model.addRow(rowToAdd);
                        table.repaint();
                        monitorPatient(patient);
                    }
                }
            }
            updateSmokerCountLabel();
            calculateAverageCholeLevel();
            highlightPatient();
            createGraph();
        }).start();
    }
}
