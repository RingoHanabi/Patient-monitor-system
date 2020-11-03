package Model;

import ObserverPattern.Subject;

import java.util.ArrayList;
import java.util.Date;

public class Patient extends Subject {

    private String id;
    private String name;
    private String surname;
    private String birthDate;
    private String gender;
    private String address;
    private CholesteroLevel choleLevel;
    private BloodPressure latestBloodPressure;
    private ArrayList<BloodPressure> last5BloodPressures = new ArrayList<>();
    private Boolean neverSmoker = false;

    public Patient(String id,
                   String name,
                   String surname,
                   String birthDate,
                   String gender,
                   String address,
                   CholesteroLevel choleLevel,
                   BloodPressure latestBloodPressure,
                   Boolean neverSmoker) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.birthDate = birthDate;
        this.gender = gender;
        this.address = address;
        this.choleLevel = choleLevel;
        this.latestBloodPressure = latestBloodPressure;
        this.neverSmoker = neverSmoker;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        notifyObservers();
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
        notifyObservers();
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthdate) {
        this.birthDate = birthdate;
        notifyObservers();
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
        notifyObservers();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
        notifyObservers();
    }

    public CholesteroLevel getCholeLevel() {
        return choleLevel;
    }

    public void setCholeLevel(CholesteroLevel choleLevel) {
        this.choleLevel = choleLevel;
        notifyObservers();
    }

    public BloodPressure getLatestBloodPressure() {
        return latestBloodPressure;
    }

    public void setLatestBloodPressure(BloodPressure latestBloodPressure) {
        this.latestBloodPressure = latestBloodPressure;
    }

    public ArrayList<BloodPressure> getLast5BloodPressures() {
        return last5BloodPressures;
    }

    public Boolean getNeverSmoker() {
        return neverSmoker;
    }

    public void setNeverSmoker(Boolean neverSmoker) {
        this.neverSmoker = neverSmoker;
    }

    public String displayLast5Tests() {
        String returnString = name + ": \n";
        for (Integer i = last5BloodPressures.size() - 1; i >= 0; i--) {
            returnString += last5BloodPressures.get(i).getSystolicMeasurement() + " (" +
            last5BloodPressures.get(i).getTime() + "), \n";
        }
        return returnString;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", birthDate=" + birthDate +
                ", gender='" + gender + '\'' +
                ", address='" + address + '\'' +
                ", choleLevel=" + choleLevel +
                ", bloodPressure=" + latestBloodPressure +
                '}';
    }
}
