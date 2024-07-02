package ru.ylab.util;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import ru.ylab.in.InputReader;
import ru.ylab.out.OutputWriter;

public class DateUtils {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static Timestamp parseDate(String prompt, InputReader inputReader, OutputWriter outputWriter) {
        while (true) {
            outputWriter.print(prompt);
            String dateStr = inputReader.readLine().trim();

            if (dateStr.equalsIgnoreCase("back")) {
                return null;
            }

            try {
                LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
                return Timestamp.valueOf(localDate.atStartOfDay());
            } catch (DateTimeParseException e) {
                outputWriter.printLine("Invalid date format. Please use yyyy-MM-dd.");
            }
        }
    }

    public static Timestamp parseDateTime(String prompt, InputReader inputReader, OutputWriter outputWriter) {
        while (true) {
            outputWriter.print(prompt);
            String dateTimeStr = inputReader.readLine().trim();

            if (dateTimeStr.equalsIgnoreCase("back")) {
                return null;
            }

            try {
                LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
                return Timestamp.valueOf(localDateTime);
            } catch (DateTimeParseException e) {
                outputWriter.printLine("Invalid date/time format. Please use yyyy-MM-dd HH:mm.");
            }
        }
    }

    public static Timestamp[] parseTimeRange(InputReader inputReader, OutputWriter outputWriter) {
        Timestamp start = parseDateTime("Enter start date and time (yyyy-MM-dd HH:mm): ", inputReader, outputWriter);
        if (start == null) return null;

        Timestamp end = parseDateTime("Enter end date and time (yyyy-MM-dd HH:mm): ", inputReader, outputWriter);
        if (end == null) return null;

        return new Timestamp[]{start, end};
    }

}
