package Service.Implementation;

import Service.Interfaces.GUIService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class GUIServiceImpl implements GUIService {
    private final JButton loginButton = new JButton("Login");
    private final JButton showCholesterolMeasurement = new JButton("Show Cholesterol Level of patients");
    private final JButton showBloodPressure = new JButton("Show blood pressure of patients");
    private final JButton showCholesterolLevelandBloodP = new JButton("Show both measurements of patients");
    private JFrame f;
    private JLabel loginlb;
    private JLabel monitorPatientDetaillb;
    private JLabel patientNumberlb;
    private JTextField userIDInputField;
    private JPanel p1;
    private JTable table;
    public String userID;
    private int patientno = 0;
    private ArrayList<Object []> data = new ArrayList<>();
    private List<String> relatedPatientIDList = new ArrayList<>();
    private List<String> relatedPatientNameList = new ArrayList<>();

    public GUIServiceImpl() {

    }

    /**
     * This function will get called to initiate the GUI
     */
    public void run() {
        f = new JFrame();//creating instance of JFrame
        loginlb = new JLabel();
        userIDInputField = new JTextField();

        loginlb.setText("<html>Enter you id to login:<html>");
        p1 = new JPanel();
        p1.setBorder(BorderFactory.createEmptyBorder(30,30,10,30));
        p1.setLayout(new GridLayout(0,1));
        p1.add(loginlb);
        p1.add(userIDInputField);
        p1.add(loginButton,BorderLayout.CENTER);

        f.add(p1, BorderLayout.CENTER);
        f.setTitle("Patient Manager");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(400,500);//400 width and 500 height
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //get dimension of screen size
        f.setLocation(screenSize.width/2-200,screenSize.height/2-250);
        f.pack();
        f.setVisible(true);//making the frame visible
    }

    /**
     * This function will create the frame after entering the practitioner id
     */
    public void createFrameAfterLogin() {
        // Close the previous window
        f.setVisible(false);

        // Open a new frame
        f = new JFrame();

        p1 = new JPanel();

        monitorPatientDetaillb = new JLabel();
        patientNumberlb = new JLabel();

        JPanel p2 = new JPanel();
        JPanel p3 = new JPanel();

        p1.setBorder(BorderFactory.createEmptyBorder(30,30,10,40));
        p1.setLayout(new GridLayout(0,1));
        p2.setBorder(BorderFactory.createEmptyBorder(30,30,10,30));
        p2.setLayout(new GridLayout(0,1));
        FlowLayout flowLayout = new FlowLayout();
        p3.setLayout(flowLayout);


        monitorPatientDetaillb.setText("Practitioner ID: " + userID);
        patientNumberlb.setText("Total Patient: " + patientno);
        p1.add(monitorPatientDetaillb);
        p1.add(patientNumberlb);
        // Creating the table
        String[] columnNames = {"PatientID","Patient Name","Monitor"};
        DefaultTableModel model = new DefaultTableModel(new Object[][] {}, columnNames);
        table = new JTable(model) {

            private static final long serialVersionUID = 1L;

            @Override
            public Class getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    default:
                        // This is used to add a check box to the table
                        return Boolean.class;
                }
            }
        };

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        table.setFillsViewportHeight(false);
        f.setLayout(new BorderLayout());
        TableColumn column = null;
        for (int i = 0; i < columnNames.length; i++) {
            column = table.getColumnModel().getColumn(i);
            column.setPreferredWidth(100);
        }
        p1.add(showCholesterolLevelandBloodP);
        p1.add(showBloodPressure);
        p1.add(showCholesterolMeasurement);
        p2.add(scrollPane);
        p3.add(p1);
        p3.add(p2);
        f.add(p3);
        f.setTitle("Patient Manager");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setLocationRelativeTo(null);
        f.pack();
        f.setVisible(true);
    }

    /**
     * Function to refresh the table when a new patient is adding to the table
     */
    public void refreshTable() {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        Object[] rowToAdd = new Object[]{relatedPatientIDList.get(relatedPatientIDList.size() - 1),relatedPatientNameList.get(relatedPatientNameList.size() - 1) , false};
        model.addRow(rowToAdd);
        patientno ++;
        data.add(rowToAdd);
        patientNumberlb.setText("Total Patient: " + patientno);
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }
    public List<String> getRelatedPatientIDList() {
        return relatedPatientIDList;
    }
    public List<String> getRelatedPatientNameList() {
        return relatedPatientNameList;
    }
    public JTextField getUserIDInputField() {
        return userIDInputField;
    }
    public JTable getTable() {
        return table;
    }
    public void setTable(JTable table) {
        this.table = table;
    }
    public ArrayList<Object[]> getData() {
        return data;
    }
    public void setData(ArrayList<Object[]> data) {
        this.data = data;
    }
    public void addLoginActionListener(ActionListener actionListener) {
        loginButton.addActionListener(actionListener);
    }
    public void addShowCholesterolMeasurementListener(ActionListener actionListener) {
        showCholesterolMeasurement.addActionListener(actionListener);
    }
    public void addShowBloodPressure(ActionListener actionListener) {
        showBloodPressure.addActionListener(actionListener);
    }
    public void addShowCholesterolLevelandBloodP(ActionListener actionListener) {
        showCholesterolLevelandBloodP.addActionListener(actionListener);
    }
}
