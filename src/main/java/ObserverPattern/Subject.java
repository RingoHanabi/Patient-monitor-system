package ObserverPattern;

import java.util.HashSet;

public abstract class Subject {

    HashSet<Observer> Observers = new HashSet<Observer>();

    public void attach(Observer o) {

        Observers.add(o);
    }

    public void detach(Observer o) {

        Observers.remove(o);
    }

    /* there is a method notify() in the Object base class in Java,
     * so we have to use a different name. This name makes the
     * meaning clearer anyway.
     */
    public void notifyObservers() {

        for (Observer o : Observers) {
            o.update();
        }
    }

}

