package seedu.nursesched.patient;

import seedu.nursesched.exception.ExceptionMessage;
import seedu.nursesched.exception.NurseSchedException;

import java.util.ArrayList;
import java.util.stream.Collectors;

/**
 * The Patient class represents a patient in the healthcare system.
 * It stores patient information such as ID, name, age, gender, contact, and notes (optional).
 * This class provides methods to add, remove, edit, and display patient information.
 */
public class Patient {
    protected static ArrayList<Patient> patientsList = new ArrayList<>();

    private final String id;
    private String name;
    private String age;
    private String gender;
    private String contact;
    private String notes;

    /**
     * Constructs a new Patient object with the specified details.
     *
     * @param id      The unique identifier for the patient.
     * @param name    The name of the patient.
     * @param age     The age of the patient.
     * @param gender  The gender of the patient.
     * @param contact The contact details of the patient.
     * @param notes   Additional notes about the patient (optional).
     * @throws NurseSchedException If any required field is empty.
     */
    public Patient(String id, String name, String age, String gender, String contact, String notes)
            throws NurseSchedException {
        assert id != null : "id cannot be null";
        assert name != null : "Name cannot be null";
        assert age != null : "Age cannot be null";
        assert gender != null : "Gender cannot be null";
        assert contact != null : "Contact cannot be null";

        if (name.trim().isEmpty() || age.trim().isEmpty() || gender.trim().isEmpty() || contact.trim().isEmpty()) {
            throw new NurseSchedException(ExceptionMessage.EMPTY_PATIENT_FIELDS);
        }

        this.id = id;
        this.name = name;
        this.age = age;
        this.notes = notes;
        this.gender = gender;
        this.contact = contact;
    }

    /**
     * Adds a patient to the list of patients.
     *
     * @param patient The Patient object to be added.
     */
    public static void addPatient(Patient patient) {
        assert patient != null : "Patient details are invalid";

        patientsList.add(patient);
        System.out.println("Patient information added for " + patient.name + ".");
    }

    /**
     * Removes a patient from the list of patients based on the provided index.
     *
     * @param index The index of the patient to be removed based on the patients list.
     */
    public static void removePatient(int index) throws NurseSchedException{
        assert index >= 0 : "Patient index number is invalid";
        if (index >= patientsList.size()) {
            throw new NurseSchedException(ExceptionMessage.INVALID_PATIENT_NUMBER);
        }
        System.out.println("Patient information removed for " + patientsList.get(index).name + ".");
        patientsList.remove(index);
    }

    /**
     * Prints the information of all patients in the list.
     * If the list is empty, it prints a message indicating that no patient information is available.
     */
    public static void listPatientInformation() {
        if (patientsList.isEmpty()) {
            System.out.println("Patient information is empty.");
            return;
        }
        for (Patient patient : patientsList) {
            System.out.println(patient.toString());
        }
    }

    /**
     * Prints the profile of a patient with the specified ID.
     *
     * @param id The unique identifier of the patient.
     * @throws NurseSchedException If the ID is invalid or no patient is found.
     */
    public static void printProfileWithID(String id) throws NurseSchedException {
        if (id.length() > 4 || id.length() < 3) {
            throw new NurseSchedException(ExceptionMessage.INVALID_ID_LENGTH);
        }

        for (char c : id.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new NurseSchedException(ExceptionMessage.INVALID_ID_INPUT);
            }
        }

        if (patientsList.isEmpty()) {
            System.out.println("There are no patients found!");
            return;
        }

        ArrayList<Patient> filteredList = patientsList.stream()
                .filter(patient -> patient.getId().equals(id))
                .collect(Collectors.toCollection(ArrayList::new));

        if (filteredList.isEmpty()) {
            System.out.println("No patient found with ID: " + id);
        } else {
            for (Patient patient : filteredList) {
                System.out.println(patient.toString());
            }
        }
    }

    /**
     * Edits the details of a patient based on the provided ID.
     *
     * @param id         The ID of the patient to update.
     * @param newName    The new name (if provided).
     * @param newAge     The new age (if provided).
     * @param newGender  The new gender (if provided).
     * @param newContact The new contact details (if provided).
     * @param newNotes   The new notes (if provided).
     */
    public static void editPatientDetails(String id, String newName, String newAge, String newGender,
                                          String newContact, String newNotes) {
        boolean found = false;
        for (Patient patient : patientsList) {
            if (patient.getId().equals(id)) {
                found = true;
                if (!newName.isEmpty()) {
                    patient.name = newName;
                }
                if (!newAge.isEmpty()) {
                    patient.age = newAge;
                }
                if (!newGender.isEmpty()) {
                    patient.gender = newGender.toUpperCase();
                }
                if (!newContact.isEmpty()) {
                    patient.contact = newContact;
                }
                if (!newNotes.isEmpty()) {
                    patient.notes = newNotes;
                }
                System.out.println("Patient information updated for ID: " + id);
                break;
            }
        }
        if (!found) {
            System.out.println("No patient found with ID: " + id);
        }
    }

    public String getId() {
        return id;
    }

    public static ArrayList<Patient> getPatientsList() {
        return patientsList;
    }

    /**
     * Returns a string representation of the patient's details.
     *
     * @return A formatted string containing patient details.
     */
    @Override
    public String toString() {
        return "Patient Details:\n" +
                "  ID: P" + id + "\n" +
                "  Name: " + name + "\n" +
                "  Age: " + age + " years old\n" +
                "  Gender: " + gender + "\n" +
                "  Contact: " + contact + "\n" +
                (notes.isEmpty() ? "" : "  Notes: " + notes + "\n");
    }
}
