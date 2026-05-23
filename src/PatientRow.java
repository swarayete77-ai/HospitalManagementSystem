package src;

public class PatientRow {

    private int patientId;
    private String name;
    private int age;
    private String doctor;
    private String hospital;
    private String status;
    private String conditionDesc;

    public PatientRow(int patientId, String name, int age,
                      String doctor, String hospital,
                      String status, String conditionDesc) {
        this.patientId     = patientId;
        this.name          = name;
        this.age           = age;
        this.doctor        = doctor;
        this.hospital      = hospital;
        this.status        = status != null ? status : "Outpatient";
        this.conditionDesc = conditionDesc != null ? conditionDesc : "Not specified";
    }

    // Getters
    public int    getPatientId()    { return patientId; }
    public String getName()         { return name; }
    public int    getAge()          { return age; }
    public String getDoctor()       { return doctor; }
    public String getHospital()     { return hospital; }
    public String getStatus()       { return status; }
    public String getConditionDesc(){ return conditionDesc; }

    // Setters (needed for inline editing)
    public void setName(String name)               { this.name = name; }
    public void setAge(int age)                    { this.age = age; }
    public void setDoctor(String doctor)           { this.doctor = doctor; }
    public void setHospital(String hospital)       { this.hospital = hospital; }
    public void setStatus(String status)           { this.status = status; }
    public void setConditionDesc(String c)         { this.conditionDesc = c; }
}