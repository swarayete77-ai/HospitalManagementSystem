package src;

class Orthopedic extends HospitalManagement {

    String doctorName;
    String hospitalName;

    Orthopedic(int patientID, String patientName, int age,
               String doctorName, String hospitalName) {

        super(patientID, patientName, age);

        this.doctorName = doctorName;
        this.hospitalName = hospitalName;
    }

    @Override
    void displayPatientDetails() {

        System.out.println("Hospital Name : " + hospitalName);
        System.out.println("Patient ID    : " + patientID);
        System.out.println("Patient Name  : " + patientName);
        System.out.println("Age           : " + age);
        System.out.println("Doctor Name   : " + doctorName);
    }
}