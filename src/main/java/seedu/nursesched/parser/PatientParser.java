package seedu.nursesched.parser;

import seedu.nursesched.exception.ExceptionMessage;
import seedu.nursesched.exception.NurseSchedException;
import seedu.nursesched.patient.MedicalTest;
import seedu.nursesched.patient.Patient;

/**
 * The PatientParser class parses the input of the user to make sense of the command.
 * It extracts commands and relevant parameters, validating them before processing.
 * This class supports various patient-related commands, including add, del, list, search,
 * edit, result.
 *
 * Each command follows a specific format and requires valid parameters. The parser extracts
 * values from the input, verifies them, and encapsulates them in a {@code PatientParser} object
 * for further processing.
 *
 * Exceptions are thrown if the input format is incorrect or required parameters are missing.
 */
public class PatientParser extends Parser {
    private final String command;
    private final String id;
    private final String name;
    private final String age;
    private final String gender;
    private final String contact;
    private final String notes;
    private final int index;

    /**
     * Constructs a new {@code PatientParser} object with extracted input details.
     *
     * @param command The parsed command indicating the operation to perform.
     * @param id The unique ID of the patient.
     * @param name The name of the patient.
     * @param age The age of the patient.
     * @param gender The gender of the patient.
     * @param contact The contact information of the patient.
     * @param notes Additional notes about the patient.
     * @param index The index of the patient in the list (if applicable).
     */
    public PatientParser(String command, String id, String name, String age, String gender,
                         String contact, String notes, int index) {
        assert command != null : "Command cannot be null";
        assert index >= 0 : "Patient index cannot be negative";

        this.command = command;
        this.id = id;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contact = contact;
        this.notes = notes;
        this.index = index;
    }

    /**
     * Extracts and parses the inputs from the given command line for patient-related operations.
     * The method supports several commands: "add", "del", "list", "search", "edit", "result".
     * For the "add" command, the input line should follow the format:
     * {@code pf add id/<ID> p/<name> a/<age> g/<gender> c/<contact> n/<notes>}
     *
     * For the "del" command, the input line should follow the format:
     * {@code pf del <index>}
     *
     * For the "list" command, the input line should follow the format:
     * {@code pf list}
     *
     * For the "search" command, the input line should follow the format:
     * {@code pf search <id>}
     *
     * For the "edit" command, the input line should follow the format:
     * {@code pf edit <id>} followed by any necessary fields that they would like to change.
     *
     * For the "result" command, the input line supports 3 other cases and should follow the following format::
     * {@code pf result add id/<ID> t/<test> r/<results>}
     *
     * @param line The input command line to be parsed. It should start with "pf" followed by the command.
     * @return A {@link PatientParser} object containing the parsed command and its associated parameters.
     *         Returns {@code null} if the input line is invalid or if required parameters are missing.
     *
     * @throws NurseSchedException If input is invalid or missing required fields.
     * @throws IndexOutOfBoundsException If the input line does not contain the expected parameters.
     */
    public static PatientParser extractInputs(String line) throws NurseSchedException {
        assert line != null : "Input line cannot be null";

        if (line.trim().isEmpty()) {
            throw new NurseSchedException(ExceptionMessage.INPUT_EMPTY);
        }

        line = line.trim();
        line = line.substring(line.indexOf(" ") + 1);
        String command;
        String id = "";
        String name = "";
        String age = "";
        String gender = "";
        String contact = "";
        String notes = "";
        int index = 0;

        // Handle cases where the line is just the command itself
        // If there are additional parameters, the command will be correctly parsed
        // If there are no parameters like "pf list", then throw an exception that treats
        // the line as the command
        try {
            if (line.contains(" ")) {
                command = line.substring(0, line.indexOf(" "));
                line = line.substring(line.indexOf(" ") + 1);
            } else {
                command = line;
            }
        } catch (IndexOutOfBoundsException e) {
            System.out.println("Invalid inputs! Please try again.");
            return null;
        }

        switch (command) {
        case "add" -> {
            if (line.equals("add")) {
                throw new NurseSchedException(ExceptionMessage.EMPTY_PATIENT_INFO);
            }

            // Extract and validate ID first
            try {
                id = line.substring(line.indexOf("id/") + 3, line.indexOf("p/") - 1);
            } catch (StringIndexOutOfBoundsException e) {
                if (!line.contains("p/")) {
                    throw new NurseSchedException(ExceptionMessage.MISSING_PATIENT_FIELDS);
                }
                throw new NurseSchedException(ExceptionMessage.INVALID_PATIENT_INFO);
            }

            // Validate ID format (4 digits)
            if (id.trim().length() != 4) {
                throw new NurseSchedException(ExceptionMessage.INVALID_ID_LENGTH);
            }

            for (char c : id.toCharArray()) {
                if (!Character.isDigit(c)) {
                    throw new NurseSchedException(ExceptionMessage.INVALID_ID_INPUT);
                }
            }

            try {
                line = line.substring(line.indexOf("p/"));

                name = line.substring(line.indexOf("p/") + 2, line.indexOf("a/") - 1);
                line = line.substring(line.indexOf("a/"));

                age = line.substring(line.indexOf("a/") + 2, line.indexOf("g/") - 1);
                line = line.substring(line.indexOf("g/"));

                gender = line.substring(line.indexOf("g/") + 2, line.indexOf("c/") - 1);
                gender = gender.toUpperCase();
                line = line.substring(line.indexOf("c/"));

                contact = line.substring(line.indexOf("c/") + 2, line.indexOf("n/") - 1);
                line = line.substring(line.indexOf("n/"));

                notes = line.substring(line.indexOf("n/") + 2);
                return new PatientParser(command, id, name, age, gender, contact, notes, index);
            } catch (IndexOutOfBoundsException e) {
                if (!line.contains("p/") || !line.contains("a/") || !line.contains("g/") || !line.contains("c/")) {
                    throw new NurseSchedException(ExceptionMessage.INVALID_PATIENT_ADD_FORMAT);
                }
                throw new NurseSchedException(ExceptionMessage.MISSING_PATIENT_FIELDS);
            }
        }
        case "del" -> {
            index = parseIndex(line);
            return new PatientParser(command, id, name, age, gender, contact, notes, index);
        }
        case "list" -> {
            return new PatientParser(command, id, name, age, gender, contact, notes, index);
        }
        case "search" -> {
            try {
                if (line.length() != 7) {
                    throw new NurseSchedException(ExceptionMessage.INVALID_ID_LENGTH);
                }
                id = line.substring(line.indexOf("id/") + 3, line.indexOf("id/") + 7);
            } catch (StringIndexOutOfBoundsException e) {
                throw new NurseSchedException(ExceptionMessage.INVALID_ID_LENGTH);
            }
            return new PatientParser(command, id, name, age, gender, contact, notes, index);
        }
        case "edit" -> {
            if (line.contains("id/")) {
                int idStart = line.indexOf("id/") + 3;
                int idEnd = findNextFieldIndex(line, idStart);
                id = line.substring(idStart, idEnd).trim();
                line = line.substring(idEnd);

                // Validate ID format (4 digits)
                if (id.trim().length() != 4) {
                    throw new NurseSchedException(ExceptionMessage.INVALID_ID_LENGTH);
                }

                for (char c : id.toCharArray()) {
                    if (!Character.isDigit(c)) {
                        throw new NurseSchedException(ExceptionMessage.INVALID_ID_INPUT);
                    }
                }

            } else {
                throw new NurseSchedException(ExceptionMessage.MISSING_ID);
            }

            if (line.isEmpty()) {
                throw new NurseSchedException(ExceptionMessage.EMPTY_INPUT_DETAILS);
            }

            try {
                if (line.contains("p/")) {
                    int nameStart = line.indexOf("p/") + 2;
                    int nameEnd = findNextFieldIndex(line, nameStart);
                    name = line.substring(nameStart, nameEnd).trim();

                    if (name.isEmpty()) {
                        throw new NurseSchedException(ExceptionMessage.MISSING_EDIT_INPUT);
                    }
                } else {
                    name = null;
                }

                if (line.contains("a/")) {
                    int ageStart = line.indexOf("a/") + 2;
                    int ageEnd = findNextFieldIndex(line, ageStart);
                    age = line.substring(ageStart, ageEnd).trim();

                    if (age.isEmpty()) {
                        throw new NurseSchedException(ExceptionMessage.MISSING_EDIT_INPUT);
                    }
                } else {
                    age = null;
                }

                if (line.contains("g/")) {
                    int genderStart = line.indexOf("g/") + 2;
                    int genderEnd = findNextFieldIndex(line, genderStart);
                    gender = line.substring(genderStart, genderEnd).trim();

                    if (gender.isEmpty()) {
                        throw new NurseSchedException(ExceptionMessage.MISSING_EDIT_INPUT);
                    }
                } else {
                    gender = null;
                }

                if (line.contains("c/")) {
                    int contactStart = line.indexOf("c/") + 2;
                    int contactEnd = findNextFieldIndex(line, contactStart);
                    contact = line.substring(contactStart, contactEnd).trim();

                    if (contact.isEmpty()) {
                        throw new NurseSchedException(ExceptionMessage.MISSING_EDIT_INPUT);
                    }
                } else {
                    contact = null;
                }

                if (line.contains("n/")) {
                    int noteStart = line.indexOf("n/") + 2;
                    notes = line.substring(noteStart).trim();

                    if (notes.isEmpty()) {
                        throw new NurseSchedException(ExceptionMessage.MISSING_EDIT_INPUT);
                    }
                } else {
                    notes = null;
                }
            } catch (IndexOutOfBoundsException e) {
                System.out.print(e.getMessage());
            }

            return new PatientParser(command, id, name, age, gender, contact, notes, index);
        }
        case "result" -> {
            try {
                command = line.substring(0, line.indexOf(" "));
                line = line.substring(line.indexOf(" ") + 1);
            } catch (StringIndexOutOfBoundsException e) {
                throw new NurseSchedException(ExceptionMessage.INVALID_COMMAND);
            }

            switch (command) {
            case "add" -> {
                try {
                    command = "result add";

                    // Extract patient ID
                    id = extractValue(line, "id/");
                    // Extract test details (test name, date, result)
                    String testName = extractValue(line, "t/");
                    String testResult = extractValue(line, "r/");

                    // Find the patient by ID and add the test
                    Patient patient = findPatientById(id);

                    if (patient == null) {
                        throw new NurseSchedException(ExceptionMessage.PATIENT_NOT_FOUND);
                    }

                    // Validate and create the medical test
                    MedicalTest test = new MedicalTest(id, testName, testResult);

                    MedicalTest.addMedicalTest(test);

                    System.out.println("Medical test added for patient with ID " + id + ".");
                    return new PatientParser(command, id, name, age, gender, contact, notes, index);
                } catch (IndexOutOfBoundsException e) {
                    throw new NurseSchedException(ExceptionMessage.MISSING_PATIENT_FIELDS);
                }
            }
            case "del" -> {
                // Extract patient ID to delete all medical tests
                command = "result del";

                id = extractValue(line, "id/");

                // Find patient by ID
                Patient patient = findPatientById(id);

                if (patient == null) {
                    throw new NurseSchedException(ExceptionMessage.PATIENT_NOT_FOUND);
                }

                MedicalTest.removeTestsForPatient(id);

                System.out.println("All medical tests deleted for patient ID " + id);

                return new PatientParser(command, id, name, age, gender, contact, notes, index);
            }
            case "list" -> {
                command = "result list";

                id = extractValue(line, "id/");

                // Find patient by ID
                Patient patient = findPatientById(id);

                if (patient == null) {
                    throw new NurseSchedException(ExceptionMessage.PATIENT_NOT_FOUND);
                }

                System.out.println("All medical tests listed for patient ID " + id);
                MedicalTest.listTestsForPatient(id);

                return new PatientParser(command, id, name, age, gender, contact, notes, index);
            }
            default -> throw new NurseSchedException(ExceptionMessage.INVALID_COMMAND);
            }
        }
        }
        return null;
    }

    /**
     * Parses the given line to extract and validate the patient index.
     * The index is expected to be a positive integer (1-based).
     *
     * @param line The input string containing the index.
     * @return The zero-based index parsed from the input.
     * @throws NurseSchedException If the index is zero, negative, or not a valid number.
     */
    public static int parseIndex (String line) throws NurseSchedException {
        int index;
        try {
            index = Integer.parseInt(line) - 1;
            if (index == -1) {
                throw new NurseSchedException(ExceptionMessage.ZERO_INDEX);
            } else if (index < 0) {
                throw new NurseSchedException((ExceptionMessage.NEGATIVE_INDEX));
            }
        } catch (NumberFormatException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_PATIENT_NUMBER);
        }
        return index;
    }

    // Helper method to find where the next field starts
    private static int findNextFieldIndex(String line, int startPos) {
        int[] markers = {
                line.indexOf("id/", startPos),
                line.indexOf("p/", startPos),
                line.indexOf("a/", startPos),
                line.indexOf("g/", startPos),
                line.indexOf("c/", startPos),
                line.indexOf("n/", startPos)
        };

        int nextIndex = line.length();
        for (int marker : markers) {
            if (marker != -1 && marker < nextIndex) {
                nextIndex = marker;
            }
        }
        return nextIndex;
    }

    private static Patient findPatientById(String id) {
        for (Patient patient : Patient.getPatientsList()) {
            if (patient.getId().equals(id)) {
                return patient;
            }
        }
        return null;
    }

    private static String extractValue(String line, String key) throws NurseSchedException {
        int startIdx = line.indexOf(key) + key.length();
        if (startIdx == -1) {
            System.out.println("Missing value for " + key);
            throw new NurseSchedException(ExceptionMessage.PARSING_ERROR);
        }
        int endIdx = line.indexOf(" ", startIdx);
        if (endIdx == -1) {
            endIdx = line.length();
        }
        return line.substring(startIdx, endIdx).trim();
    }

    public String getCommand() {
        return command;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAge() {
        return age;
    }

    public String getGender() {
        return gender;
    }

    public String getContact() {
        return contact;
    }

    public String getNotes() {
        return notes;
    }

    public int getIndex() {
        return index;
    }
}
