package seedu.nursesched.parser;

import seedu.nursesched.exception.ExceptionMessage;
import seedu.nursesched.exception.NurseSchedException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.logging.FileHandler;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;
import java.io.File;

/**
 * Parses the input of the user to make sense of the command.
 * It extracts commands and relevant parameters, validating them before processing.
 * This class supports various appointment-related commands, including add, del, mark, unmark,
 * list, find, edit and sort.
 * Each command follows a specific format and requires valid parameters. The parser extracts
 * values from the input, verifies them, and encapsulates them in a {@code ApptParser} object
 * for further processing.
 * Exceptions are thrown if the input format is incorrect or required parameters are missing.
 */
public class ApptParser extends Parser {

    private static final Logger logr = Logger.getLogger("ApptParser");


    private static int apptIndex;
    private static int id;
    private static String searchKeyword;
    private static String sortBy;
    private static String searchBy = null;
    private final String command;
    private final String name;

    private final LocalTime startTime;
    private final LocalTime endTime;
    private final LocalDate date;
    private final String notes;
    private final int importance;

    static {
        try {
            File logDir = new File("logs/parser");
            if (!logDir.exists()) {
                logDir.mkdirs();  // Creates the directory and any missing parent directories
            }

            LogManager.getLogManager().reset();
            FileHandler fh = new FileHandler("logs/parser/apptParser.log", true);
            fh.setFormatter(new SimpleFormatter());
            logr.addHandler(fh);
            logr.setLevel(Level.ALL);
        } catch (IOException e) {
            logr.log(Level.SEVERE, "File logger not working", e);
        }
    }


    /**
     * Constructs a new ApptParser object with the specified parameters.
     *
     * @param command The command associated with the input given.
     * @param name The name of the patient.
     * @param startTime The start time of the appointment.
     * @param endTime The end time of the appointment.
     * @param date The date of the appointment.
     * @param notes The additional things to note about the patient.
     */
    public ApptParser(String command, String name, LocalTime startTime, LocalTime endTime,
                      LocalDate date, String notes, int apptIndex, String searchKeyword,
                      int importance, String sortBy, int id, String searchBy) {
        this.command = command;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.date = date;
        this.notes = notes;
        this.apptIndex = apptIndex;
        this.searchKeyword = searchKeyword;
        this.searchBy = searchBy;
        this.importance = importance;
        this.id =id;
        this.sortBy = sortBy;

        logr.info("ApptParser created: " + this);
    }

    /**
     * Extracts and parses the inputs from the given command for appointment-related operations.
     * This method supports three commands "add", "del" and "mark".
     *
     * @param line The user's input command to be parsed.
     * @return An {@link ApptParser} object which contains the parsed commands and associated parameters.
     *         Returns {@code null} if the input parameters are missing or invalid.
     *
     * @throws IndexOutOfBoundsException If the input line does not contain the expected parameters.
     * @throws DateTimeParseException If the input time or date is not of the expected format.
     * @throws NurseSchedException If input fields are invalid or missing.
     */
    public static ApptParser extractInputs (String line) throws NurseSchedException {
        assert line != null : "Input line should not be null";
        logr.info("Extracting inputs from: " + line);
        line = line.trim();
        line = line.substring(line.indexOf(" ") + 1);
        String command = "";
        String name = "";
        LocalTime startTime = null;
        LocalTime endTime = null;
        LocalDate date = null;
        String notes = "";
        int importance = 1;

        try {
            if (line.contains(" ")) {
                command = line.substring(0, line.indexOf(" ")).toLowerCase();
                line = line.substring(line.indexOf(" ") + 1);
            } else {
                command = line.toLowerCase();
                line = null;
            }
        } catch (IndexOutOfBoundsException e) {
            logr.warning("Invalid command: " + command);
            System.out.println("Invalid inputs! Please try again.");
            return null;
        }

        switch (command) {
        case "add" -> {

            if (line == null){
                throw new NurseSchedException(ExceptionMessage.INVALID_APPTADD_FORMAT);
            }
            if (!line.contains("id/") || !line.contains("s/") ||
                    !line.contains("d/") || !line.contains("e/")) {
                logr.warning("Missing fields");
                throw new NurseSchedException(ExceptionMessage.INVALID_APPTADD_FORMAT);
            }

            try {
                // Extract patient ID
                int idIndex = line.indexOf("id/") + 3;
                int idEnd = findNextFieldIndex(line, idIndex);
                id = parseID(line.substring(idIndex, idEnd).trim());

                // Extract appointment's start time
                int startIndex = line.indexOf("s/") + 2;
                int startEnd = findNextFieldIndex(line, startIndex);
                startTime = LocalTime.parse(line.substring(startIndex, startEnd).trim());

                // Extract appointment's end time
                int endIndex = line.indexOf("e/") + 2;
                int endEnd = findNextFieldIndex(line, endIndex);
                endTime = LocalTime.parse(line.substring(endIndex, endEnd).trim());

                // Extract appointment's date
                String abstractedLine = line.substring(endEnd);
                int dateIndex = abstractedLine.indexOf("d/") + 2;
                int dateEnd = findNextFieldIndex(abstractedLine, dateIndex);
                date = LocalDate.parse(abstractedLine.substring(dateIndex, dateEnd).trim());

                // Extract importance if present
                if (line.contains("im/")) {
                    int imIndex = line.indexOf("im/") + 3;
                    int imEnd = findNextFieldIndex(line, imIndex);
                    String importanceStr = line.substring(imIndex, imEnd).trim();
                    importance = parseImportance(importanceStr);
                } else {
                    importance = 2; // Default medium importance
                }

                // Extract notes if present
                if (line.contains("n/")) {
                    int notesIndex = line.indexOf("n/") + 2;
                    int notesEnd = findNextFieldIndex(line, notesIndex);
                    // If notesEnd is the same as line.length(), it means there's no next field
                    // so take the rest of the line
                    notes = line.substring(notesIndex, notesEnd).trim();
                } else {
                    notes = "";
                }
            } catch (DateTimeParseException e) {
                throw new NurseSchedException(ExceptionMessage.INVALID_DATETIME_FORMAT);
            }

            return new ApptParser(command, name, startTime, endTime, date, notes,
                    apptIndex, searchKeyword, importance, sortBy, id, searchBy);
        }

        case "del", "mark", "unmark" -> {
            if (line == null || line.trim().isEmpty()) {
                logr.warning("Missing index field in command");
                throw new NurseSchedException(ExceptionMessage.MISSING_INDEX_PARAMETER);
            }

            String indexStr = line.trim();
            if (!indexStr.toLowerCase().startsWith("aid/") || indexStr.length() <= 4) {
                logr.warning("Missing index field in command");
                throw new NurseSchedException(ExceptionMessage.MISSING_INDEX_PARAMETER);
            }

            apptIndex = parseIndex(indexStr.substring(4));
            return new ApptParser(command, name, startTime, endTime, date, notes,
                    apptIndex, searchKeyword, importance, sortBy, id, searchBy);
        }

        case "list" -> {
            return new ApptParser(command, name, startTime, endTime, date, notes,
                    apptIndex, searchKeyword, importance, sortBy, id, searchBy);
        }

        case "sort" -> {
            if (line != null && line.contains("by/")) {
                int byIndex = line.indexOf("by/") + 3;
                sortBy = line.substring(byIndex).trim().toLowerCase();
                if (!sortBy.equals("time") && !sortBy.equals("importance")) {
                    logr.warning("Invalid sort parameter: " + sortBy);
                    throw new NurseSchedException(ExceptionMessage.INVALID_SORT_PARAMETER);
                }
                logr.info("Sorting by: " + sortBy);
            } else {
                // Default to sorting by time if no parameter specified
                throw new NurseSchedException(ExceptionMessage.INVALID_SORT_FORMAT);
            }
            return new ApptParser(command, name, startTime, endTime, date, notes,
                    apptIndex, searchKeyword, importance, sortBy, id, searchBy);
        }

        case "find" -> {
            if (line == null) {
                throw new NurseSchedException(ExceptionMessage.MISSING_SEARCH_TERM);
            }
            if (!(line.contains("id/") || line.contains("p/"))){
                throw new NurseSchedException(ExceptionMessage.INVALID_FIND_PARAMETER);
            }
            if (line.contains("id/")){
                int idIndex = line.indexOf("id/") + 3;
                searchKeyword = line.substring(idIndex).trim();
                int testID = parseID(searchKeyword);
                searchBy = "id";
            } else if (line.contains("p/")){
                int nameIndex = line.indexOf("p/") + 2;
                if (nameIndex>= line.length()) {
                    throw new NurseSchedException(ExceptionMessage.MISSING_NAME_PARAMETER);
                }
                searchKeyword = line.substring(nameIndex).trim();
                searchBy = "p";
            }

            return new ApptParser(command, name, startTime, endTime, date, notes,
                    apptIndex, searchKeyword, importance, sortBy, id, searchBy);
        }

        case "edit" -> {
            if (line == null || line.trim().isEmpty() || !line.contains("aid/")) {
                logr.warning("Missing index field in edit command");
                throw new NurseSchedException(ExceptionMessage.INVALID_APPTEDIT_FORMAT);
            }

            try {
                // Extract index
                int indexStart = line.indexOf("aid/") + 4;
                int indexEnd = findNextFieldIndex(line, indexStart);
                String indexStr = line.substring(indexStart, indexEnd).trim();

                // Check if there's actually a number after id/
                if (indexStr.isEmpty()) {
                    logr.warning("Missing index number after aid/ prefix");
                    throw new NurseSchedException(ExceptionMessage.MISSING_INDEX_PARAMETER);
                }

                try {
                    apptIndex = parseIndex(indexStr);
                } catch (NumberFormatException e) {
                    // This is caught by parseIndex
                    throw e;
                }

                line = line.substring(indexEnd).trim();
                if (line.isEmpty()){
                    System.out.println("At least one optional field must be provided for an edit (see below).");
                    throw new NurseSchedException(ExceptionMessage.INVALID_APPTEDIT_FORMAT);
                }

                // To separate detection for pid/ and d/
                String abstractedLine = line;

                // Process optional fields
                try {
                    if (line.contains("id/")) {
                        int pidStart = line.indexOf("id/") + 3;
                        int pidEnd = findNextFieldIndex(line, pidStart);
                        String pidStr = line.substring(pidStart, pidEnd).trim();
                        abstractedLine = line.substring(pidEnd).trim();
                        id = parseID(pidStr);
                        if (pidStr.isEmpty()) {
                            System.out.println("No ID found in id field. Defaulting to previous ID.");
                            id = -1;
                        }
                    } else {
                        id = -1;
                    }

                    if (line.contains("s/")) {
                        int sIndex = line.indexOf("s/") + 2;
                        int sEnd = findNextFieldIndex(line, sIndex);
                        startTime = LocalTime.parse(line.substring(sIndex, sEnd).trim());
                    }

                    if (line.contains("e/")) {
                        int eIndex = line.indexOf("e/") + 2;
                        int eEnd = findNextFieldIndex(line, eIndex);
                        endTime = LocalTime.parse(line.substring(eIndex, eEnd).trim());
                    }

                    if (abstractedLine.contains("d/")) {
                        int dIndex = abstractedLine.indexOf("d/") + 2;
                        int dEnd = findNextFieldIndex(abstractedLine, dIndex);
                        date = LocalDate.parse(abstractedLine.substring(dIndex, dEnd).trim());
                    }

                    if (line.contains("im/")) {
                        int imIndex = line.indexOf("im/") + 3;
                        int imEnd = findNextFieldIndex(line, imIndex);
                        importance = parseImportance(line.substring(imIndex, imEnd).trim());
                    }else{
                        importance = -1;
                    }

                    if (line.contains("n/")) {
                        int nIndex = line.indexOf("n/") + 2;
                        notes = line.substring(nIndex).trim();
                        if (notes.isEmpty()){
                            System.out.println("No notes found in notes field. Defaulting to previous note.");
                            notes = null;
                        }
                    } else {
                        notes = null;
                    }
                } catch (DateTimeParseException e) {
                    throw new NurseSchedException(ExceptionMessage.INVALID_DATETIME_FORMAT);
                } catch (NurseSchedException e) {
                    // Pass through exceptions from parseImportance
                    throw e;
                }
            } catch (StringIndexOutOfBoundsException e) {
                logr.warning("Missing index number after aid/ prefix");
                throw new NurseSchedException(ExceptionMessage.MISSING_INDEX_PARAMETER);
            }

            return new ApptParser(command, name, startTime, endTime, date, notes,
                    apptIndex, searchKeyword, importance, sortBy, id, searchBy);
        }

        default -> {
            logr.warning("Unrecognized command: " + command);
            return null;
        }

        }
    }

    /**
     * Parses a string representation of a patient ID into an integer.
     * <p>
     * Validates that the input string contains exactly four digits and no other characters.
     * @param id    The string representation of the patient ID to parse.
     * @return      The parsed integer ID if validation passes.
     * @throws NurseSchedException If the input string:
     *                             <ul>
     *                                 <li>Contains non-digit characters.</li>
     *                                 <li>Contains internal spaces.</li>
     *                                 <li>Does not have exactly four digits.</li>
     *                                 <li>Cannot be parsed as an integer for other reasons.</li>
     *                             </ul>
     */
    private static int parseID(String id) throws NurseSchedException {
        for (char c : id.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new NurseSchedException(ExceptionMessage.INVALID_ID_INPUT);
            }
        }

        // Validate ID format (4 digits)
        if (id.length() != 4) {
            if (id.trim().length() != 4) {
                if (id.contains(" ")) {
                    throw new NurseSchedException(ExceptionMessage.ID_CONTAINS_SPACES);
                } else {
                    throw new NurseSchedException(ExceptionMessage.INVALID_ID_LENGTH);
                }
            } else {
                id = id.trim();
            }
        }

        try {
            return Integer.parseInt(id);
        } catch (NumberFormatException e) {
            throw new NurseSchedException(ExceptionMessage.INVALID_ID_INPUT);
        }
    }


    /**
     * Parses a string representation of an appointment importance level into an integer.
     * <p>
     * Validates that the input string is a valid integer between 1 and 3 (inclusive).
     *
     * @param importanceStr        The string representation of the importance level (e.g., "1", "2", "3").
     * @return                     The parsed and validated integer importance level (1, 2, or 3).
     * @throws NurseSchedException If the input string is not a valid integer or if the integer
     *                             is outside the acceptable range [1, 3]
     */
    public static int parseImportance(String importanceStr) throws NurseSchedException {
        try {
            int newImportance = Integer.parseInt(importanceStr);
            if (newImportance <= 0 || newImportance > 3) {
                logr.warning("Invalid importance (must be 0-3): " + newImportance);
                throw new NurseSchedException(ExceptionMessage.INVALID_IMPORTANCE_FORMAT);
            }
            return newImportance;
        } catch (NumberFormatException e) {
            logr.warning("Invalid importance format: " + importanceStr);
            throw new NurseSchedException(ExceptionMessage.INVALID_IMPORTANCE_FORMAT);
        }
    }


    /**
     * Parses a string representation of a 1-based list index into a 0-based integer index.
     * <p>
     * Validates that the input string represents a positive integer and converts it
     * to the corresponding 0-based index for list access. Also checks for potential
     * integer overflow and invalid formats.
     *
     * @param line The string containing the 1-based index to parse.
     * @return The parsed 0-based integer index.
     * @throws NurseSchedException If the input string:
     *                             <ul>
     *                                 <li>Is empty or blank </li>
     *                                 <li>Represents a number too large to fit in an int </li>
     *                                 <li>Represents zero or a negative number </li>
     *                                 <li>Contains non-digit characters or is otherwise not a valid integer format</li>
     *                             </ul>
     */
    public static int parseIndex (String line) throws NurseSchedException {
        if (line.trim().isEmpty()){
            throw new NurseSchedException(ExceptionMessage.MISSING_INDEX_PARAMETER);
        }

        int index = 0;
        try {
            if (line.length() > 10) { // Max int is 10 digits
                logr.warning("Index value too large: " + line);
                throw new NurseSchedException(ExceptionMessage.INDEX_PARAMETER_TOO_LARGE);
            }

            index = Integer.parseInt(line) - 1;
            if (index < 0) {
                logr.warning("Negative index: " + line);
                throw new NurseSchedException(ExceptionMessage.NEGATIVE_INDEX);
            }
        } catch (NumberFormatException e) {

            boolean containsDigits = line.matches(".*\\d.*");
            if (containsDigits) {
                // It contains digits, but wasn't parseable as a whole int. Examples: "12a", "a12", "12 34", "1.5"
                logr.warning("Invalid index format (contains digits but not purely an integer): " + line);
                throw new NurseSchedException(ExceptionMessage.INVALID_INDEX_PARAMETER);
            } else {
                // It contains NO digits at all.
                logr.warning("Invalid input: String contains no digits (only words): " + line);
                throw new NurseSchedException(ExceptionMessage.INVALID_INDEX_PARAMETER);
            }
        }
        return index;
    }

    /**
     * Helper method to locate the start index of the next field marker in a command string.
     * <p>
     * Searches the input line starting from startPos for the earliest occurrence
     * of any known field marker (e.g., "id/", "s/", "d/", "e/", "im/", "n/").
     *
     * @param line     The command string being parsed.
     * @param startPos The 0-based index within line from where the search should begin.
     * @return The 0-based index of the beginning of the *next* field marker found at or after startPos.
     *         Returns the length of the line if no field markers are found after startPos.
     */
    private static int findNextFieldIndex(String line, int startPos) {
        // All possible next field markers
        int[] markers = {
                line.indexOf("id/", startPos),
                line.indexOf("s/", startPos),
                line.indexOf("e/", startPos),
                line.indexOf("d/", startPos),
                line.indexOf("im/", startPos),
                line.indexOf("n/", startPos)
        };

        // Find the closest marker that's not -1 (not found)
        int nextIndex = line.length();
        for (int marker : markers) {
            if (marker != -1 && marker < nextIndex) {
                nextIndex = marker;
            }
        }
        return nextIndex;
    }


    //Getters
    public String getCommand () {
        return command;
    }

    public String getName () {
        return name;
    }

    public LocalTime getStartTime () {
        return startTime;
    }

    public LocalTime getEndTime () {
        return endTime;
    }

    public LocalDate getDate () {
        return date;
    }

    public String getNotes () {
        return notes;
    }

    public int getImportance () {
        return importance;
    }

    public int getID () {
        return id;
    }

    public int getIndex () {
        return apptIndex;
    }

    public String getSearchKeyword () {
        return searchKeyword;
    }

    public String getSearchBy () {
        return searchBy;
    }

    public String getSortBy () {
        return sortBy;
    }

}
