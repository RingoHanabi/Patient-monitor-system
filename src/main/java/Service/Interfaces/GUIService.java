package Service.Interfaces;

import javax.swing.*;

import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public interface GUIService {
    void setUserID(String userID);
    void run();
    void createFrameAfterLogin();
    void refreshTable();
    JTextField getUserIDInputField();
    void addLoginActionListener(ActionListener actionListener);
    void addShowCholesterolMeasurementListener(ActionListener actionListener);
    void addShowBloodPressure(ActionListener actionListener);
    void addShowCholesterolLevelandBloodP(ActionListener actionListener);
    List<String> getRelatedPatientIDList();
    List<String> getRelatedPatientNameList();
    JTable getTable();
    void setTable(JTable table);
    ArrayList<Object[]> getData();
    void setData(ArrayList<Object[]> data);

}
