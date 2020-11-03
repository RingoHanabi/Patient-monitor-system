package Service.Implementation;

import Model.CholesteroLevel;
import Model.Patient;
import ObserverPattern.Observer;
import ObserverPattern.ThresholdNumber;
import ObserverPattern.UpdateTime;
import Service.Interfaces.CholesterolLevelMonitorService;
import Service.Interfaces.FHIRService;
import Service.Interfaces.GUIService;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Observation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartFrame;
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

public class CholesterolLevelMonitorServiceImpl extends Observer implements CholesterolLevelMonitorService {

    private List<Patient> monitoringPatient = new ArrayList<Patient>();
    private UpdateTime observedUpdateTime = new UpdateTime(this);
    private ThresholdNumber observedThresholdNumber = new ThresholdNumber(this);
    private Timer timer = new Timer();
    private double averageCholeLevel = 0.0;
    private SmokerCheck smokerCheck = new SmokerCheck();
    private FHIRService fhirService;
    private GUIService guiService;
    private static DateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd hh:mm:ss");

    private final JPanel tablePanel = new JPanel();
    private final JPanel mainContentPanel = new JPanel();
    private final JLabel enterCholeThresholdLabel = new JLabel();
    private final JFrame mainFrame = new JFrame();
    private final JPanel leftLabelPanel = new JPanel();
    private final JLabel monitoringPatientLabel = new JLabel();
    private final JLabel enterUpdateTimeLabel = new JLabel();
    private final JLabel aboveAverageCount = new JLabel();
    private int aboveAverageNo = 0;
    private final JTextField updateTimeInputField = new JTextField();
    private JTextField choleThresholdInputField = new JTextField();
    private JTable table;
    private ChartFrame chartFrame;
    private JLabel neverSmokerCount = new JLabel();

    public CholesterolLevelMonitorServiceImpl(GUIService guiService, FHIRService fhirService) {
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
    public void setObservedThresholdNumber(ThresholdNumber observedThresholdNumber) {
        this.observedThresholdNumber = observedThresholdNumber;
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
    public void highlightPatient() {
        for (Patient p : monitoringPatient) {
            if (p.getCholeLevel() != null && p.getCholeLevel().getValue().floatValue() >= averageCholeLevel) {
                aboveAverageNo += 1;
                p.getCholeLevel().setCholesteroHighlight(true);
            }
        }
        aboveAverageCount.setText("Patients above average cholesterol: " + aboveAverageNo);
    }

    /**
     * Calculate the average cholesterol level.
     */
    public void calculateAverageCholeLevel() {
        double totalCholeLevel = 0;
        int availablePatient = 0;
        for (Patient patient : monitoringPatient) {

            // Add all the cholesterolevel together
            if (patient.getCholeLevel() != null) {
                totalCholeLevel += patient.getCholeLevel().getValue().doubleValue();
                availablePatient += 1;
            }
        }

        setAverageCholeLevel(totalCholeLevel / availablePatient);
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
        } else if (this.observedThresholdNumber.isChanged()) {
            System.out.println("Monitoring Service receive new threshold information");
            updateTable();

            // Set the change status to false so further changes can be detected.
            this.observedThresholdNumber.setIsChanged(false);
        }
    }

    /**
     * Function to initialise GUI
     */
    @Override
    public void initialGUI() {
        monitoringPatient.clear();

        monitoringPatientLabel.setText("Monitoring Patient");
        enterUpdateTimeLabel.setText("Enter the frequency you want to update the data: ");
        enterCholeThresholdLabel.setText("Enter the threshold number of Cholesterol: ");
        aboveAverageCount.setText("Patients above average cholesterol: " + aboveAverageNo);
        neverSmokerCount.setText("Never smokers: 0");

        updateTimeInputField.addActionListener(observedUpdateTime);
        choleThresholdInputField.addActionListener(observedThresholdNumber);

        leftLabelPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        leftLabelPanel.setLayout(new GridLayout(0, 1));
        tablePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        tablePanel.setLayout(new GridLayout(0, 1));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 10, 30));
        mainContentPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

        leftLabelPanel.add(monitoringPatientLabel);
        leftLabelPanel.add(enterUpdateTimeLabel);
        leftLabelPanel.add(updateTimeInputField);
        leftLabelPanel.add(enterCholeThresholdLabel);
        leftLabelPanel.add(choleThresholdInputField);
        leftLabelPanel.add(aboveAverageCount);
        leftLabelPanel.add(neverSmokerCount);

        // Creating the table
        String[] columnNames = {"patientID", "cholestrol", "issued"};
        DefaultTableModel defaultmodel = new DefaultTableModel(new Object[][]{}, columnNames);
        table = new JTable(defaultmodel) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                switch (convertColumnIndexToModel(column)) {
                    case 0:
                    case 2:
                        return String.class;
                    case 1:
                        return Float.class;
                }

                return super.getColumnClass(column);
            }
        };
        table.setDefaultRenderer(Float.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(monitoringPatient.get(row).getCholeLevel().isCholesteroHighlight() ? Color.RED : Color.black);
                return c;
            }
        });
        table.setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setForeground(monitoringPatient.get(row).getCholeLevel().isCholesteroHighlight() ? Color.RED : Color.black);
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
        TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
            switch (i) {
                case 0:
                case 1:
                    column = table.getColumnModel().getColumn(i);
                    column.setMaxWidth(50);
                case 2:
                    column = table.getColumnModel().getColumn(i);
                    column.setMaxWidth(200);
            }
        }
        JScrollPane scrollPane = new JScrollPane(table);
        table.setFillsViewportHeight(false);
        tablePanel.add(scrollPane);

        mainContentPanel.add(leftLabelPanel);
        mainContentPanel.add(tablePanel);
        mainFrame.setLayout(new BorderLayout());
        mainFrame.setContentPane(mainContentPanel);
        mainFrame.setTitle("Patient Cholesterol Level");
        mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        mainFrame.addWindowListener(new WindowAdapter() {
            public void windowClosed(WindowEvent e) {
                timer.cancel();
            }
        });
        mainFrame.pack();
        mainFrame.setVisible(true);

        asyncFetchCholeData();
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
            List<IBaseResource> patientCholList = fhirService.getCholesterolLevelByPatientID(patientID);
            CholesteroLevel patientChol;
            if (patientCholList.size() != 0) {
                Observation patientObservation = (Observation) patientCholList.get(0);
                String cholesterolIssuedDate = dateFormat.format(patientObservation.getIssued());
                patientChol = new CholesteroLevel(patientObservation.getValueQuantity().getValue(),
                        cholesterolIssuedDate,
                        patientObservation.getValueQuantity().getCode());
                Patient patient = monitoringPatient.get(i);
                patient.setCholeLevel(patientChol);
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                Object[] rowToAdd = new Object[]{patient.getId(), patient.getCholeLevel().getValue().floatValue() + patient.getCholeLevel().getUnit(),
                        patient.getCholeLevel().getEffectiveDateTime()};
                model.addRow(rowToAdd);
                table.repaint();
                monitoringPatient.set(i, patient);
            }
        }
        calculateAverageCholeLevel();
        highlightPatient();
        highlightPatientByThreshold();
        chartFrame.setVisible(false);
        createGraph();
    }

    /**
     * Highlight patients whose cholesterol level is higher than threshold number.
     */
    private void highlightPatientByThreshold() {
        for (Patient p : monitoringPatient) {
            if (p.getCholeLevel().getValue().floatValue() >= observedThresholdNumber.getThresholdNumber()) {
                p.getCholeLevel().setCholesteroHighlight(true);
            } else if (!p.getCholeLevel().isCholesteroHighlight()) {
                p.getCholeLevel().setCholesteroHighlight(false);
            }
        }
    }

    private void createGraph() {
        DefaultCategoryDataset dcd = new DefaultCategoryDataset();
        for (Patient patient : getMonitoringPatient()) {
            dcd.setValue(patient.getCholeLevel().getValue(),
                    "Cholesterol measurement",
                    patient.getName());
        }
        ;
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
        chartFrame.setVisible(true);
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

    private void asyncFetchCholeData() {
        new Thread(() -> {
            for (int i = 0; i < guiService.getData().size(); i++) {
                if (guiService.getTable().getValueAt(i, 2).equals(true)) {
                    String patientID = guiService.getRelatedPatientIDList().get(i);
                    List<IBaseResource> patientCholList = fhirService.getCholesterolLevelByPatientID(patientID);
                    CholesteroLevel patientChol;
                    Boolean status = this.smokerCheck.smokerCheck(patientID);
                    if (patientCholList.size() != 0) {
                        Observation patientObservation = (Observation) patientCholList.get(0);
                        String cholesterolIssuedDate = dateFormat.format(patientObservation.getIssued());
                        patientChol = new CholesteroLevel(patientObservation.getValueQuantity().getValue(),
                                cholesterolIssuedDate,
                                patientObservation.getValueQuantity().getCode());
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
                                patientChol,
                                null,
                                status);

                        DefaultTableModel model = (DefaultTableModel) table.getModel();
                        Object[] rowToAdd = new Object[]{patient.getId(), patient.getCholeLevel().getValue().floatValue() + patient.getCholeLevel().getUnit(),
                                patient.getCholeLevel().getEffectiveDateTime()};
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
