package Library;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;

class InputUtil {

    private static final Scanner SC = new Scanner(System.in);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;

    private InputUtil() {
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return SC.nextLine().trim();
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("ورودی پایان یافت؛ برنامه بسته می‌شود.");
            System.exit(0);
            return "";
        }
    }

    public static String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("مقدار نمی‌تواند خالی باشد.");
        }
    }

    public static int readInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("لطفا یک عدد صحیح وارد کنید.");
            }
        }
    }

    public static Integer readOptionalInt(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                return null;
            }
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.out.println("عدد صحیح وارد کنید یا برای رد شدن خط را خالی بگذارید.");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt);
            try {
                return LocalDate.parse(value, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("تاریخ را به شکل yyyy-mm-dd وارد کنید (مثال: 2026-10-20).");
            }
        }
    }

    public static boolean readYesNo(String prompt) {
        while (true) {
            String value = readLine(prompt).toLowerCase();
            if (value.equals("بله") || value.equals("ب") || value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("خیر") || value.equals("خ") || value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("با «بله» یا «خیر» پاسخ دهید.");
        }
    }
}
