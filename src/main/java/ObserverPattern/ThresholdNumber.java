package ObserverPattern;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ThresholdNumber extends Subject implements ActionListener {
    private float thresholdNumber = Float.POSITIVE_INFINITY;
    private boolean isChanged = false;

    public ThresholdNumber( Observer observer) {
        this.attach(observer);
    }
    public float getThresholdNumber() {
        return thresholdNumber;
    }

    public void setThresholdNumber(float thresholdNumber) {
        this.thresholdNumber = thresholdNumber;
        this.setIsChanged(true);
        notifyObservers();
    }

    public boolean isChanged() {
        return isChanged;
    }

    public void setIsChanged(boolean isChanged) {
        this.isChanged = isChanged;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.setThresholdNumber((Float.parseFloat(e.getActionCommand())));
    }
}
