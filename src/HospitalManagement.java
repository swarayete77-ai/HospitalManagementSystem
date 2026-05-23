package src;

abstract class HospitalManagement {

    int patientID;
    String patientName;
    int age;

    HospitalManagement(int patientID, String patientName, int age) {
        this.patientID = patientID;
        this.patientName = patientName;
        this.age = age;
    }

    abstract void displayPatientDetails();
}