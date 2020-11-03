package ObserverPattern;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class UpdateTime extends Subject implements ActionListener {
    private int updateTime = 0;
    private boolean isChanged = false;

    public UpdateTime(Observer observer) {
        this.attach(observer);

    }
    public int getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(int updateTime) {
        this.updateTime = updateTime * 1000;
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
        System.out.println(e.getActionCommand());
        this.setUpdateTime((Integer.parseInt(e.getActionCommand())));
    }
}
