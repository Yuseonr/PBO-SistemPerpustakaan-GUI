package com.library.tui.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class TuiUtils {
    public static final String RESET = "\u001B[0m";
    public static final String BOLD = "\u001B[1m";
    public static final String BLACK = "\u001B[30m";
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String PURPLE = "\u001B[35m";
    public static final String CYAN = "\u001B[36m";
    public static final String WHITE = "\u001B[37m";

    private static final Scanner scanner = new Scanner(System.in);

    public static void clearScreen() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void printAsciiLibrary() {
        String asciiArt = CYAN +
        "      __...--~~~~~-._   _.-~~~~~--...__\n" +
        "    //               `V'               \\\\ \n" +
        "   //                 |                 \\\\ \n" +
        "  //__...--~~~~~~-._  |  _.-~~~~~~--...__\\\\ \n" +
        " //__.....----~~~~._\\ | /_.~~~~----.....__\\\\\n" +
        "====================\\\\|//====================\n" +
        "                    `---`\n" + RESET;
        System.out.println(asciiArt);
    }

    public static void printHeader(String title) {
        clearScreen();
        printAsciiLibrary();
        
        int width = 50;
        String border = "═".repeat(width);
        
        System.out.println(CYAN + BOLD + "╔" + border + "╗" + RESET);
        int padding = (width - title.length()) / 2;
        String paddedTitle = String.format("%" + (padding + title.length()) + "s", title);
        String fullTitle = String.format("%-" + width + "s", paddedTitle);
        System.out.println(CYAN + BOLD + "║" + RESET + BOLD + YELLOW + fullTitle + CYAN + BOLD + "║" + RESET);
        System.out.println(CYAN + BOLD + "╚" + border + "╝" + RESET);
    }

    public static void printBoxMenu(String[] options) {
        int width = 54; // Inner width
        String border = "─".repeat(width);
        System.out.println(BLUE + "┌" + border + "┐" + RESET);
        for (String opt : options) {
            System.out.printf(BLUE + "│ " + WHITE + "%-52s" + BLUE + " │%n" + RESET, opt);
        }
        System.out.println(BLUE + "└" + border + "┘" + RESET);
    }

    public static void printBoxDetail(String title, String[] lines) {
        int width = 70; 
        String border = "═".repeat(width);
        System.out.println(PURPLE + "╔" + border + "╗" + RESET);
        String paddedTitle = String.format(" %s ", title);
        System.out.printf(PURPLE + "║" + YELLOW + BOLD + " %-68s " + PURPLE + "║%n" + RESET, paddedTitle);
        System.out.println(PURPLE + "╠" + border + "╣" + RESET);
        
        for (String line : lines) {
            // Very basic wrapping if line > 68 chars
            int start = 0;
            while(start < line.length()) {
                int end = Math.min(start + 68, line.length());
                String chunk = line.substring(start, end);
                System.out.printf(PURPLE + "║" + WHITE + " %-68s " + PURPLE + "║%n" + RESET, chunk);
                start += 68;
            }
        }
        System.out.println(PURPLE + "╚" + border + "╝" + RESET);
    }

    public static void printSuccess(String message) {
        System.out.println(GREEN + " ✔ " + message + RESET);
    }

    public static void printError(String message) {
        System.out.println(RED + " ✖ " + message + RESET);
    }

    public static void printInfo(String message) {
        System.out.println(YELLOW + " ℹ " + message + RESET);
    }

    public static String readString(String prompt) {
        System.out.print(CYAN + "? " + RESET + prompt);
        System.out.flush();
        return scanner.nextLine();
    }

    public static int readInt(String prompt) {
        while (true) {
            System.out.print(CYAN + "? " + RESET + prompt);
            System.out.flush();
            try {
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                printError("Invalid input. Please enter a number.");
            }
        }
    }

    public static LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(CYAN + "? " + RESET + prompt + " (YYYY-MM-DD): ");
            System.out.flush();
            String input = scanner.nextLine();
            try {
                return LocalDate.parse(input);
            } catch (DateTimeParseException e) {
                printError("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    public static void waitForEnter() {
        System.out.print(BLUE + "\n[Press ENTER to continue...]" + RESET);
        System.out.flush();
        scanner.nextLine();
    }

    public static void printTable(String[] headers, String[][] data) {
        if (headers == null || headers.length == 0) return;
        
        int[] colWidths = new int[headers.length];
        for (int i = 0; i < headers.length; i++) {
            colWidths[i] = headers[i].length();
        }
        
        if (data != null) {
            for (String[] row : data) {
                for (int i = 0; i < row.length; i++) {
                    if (row[i] != null && row[i].length() > colWidths[i]) {
                        colWidths[i] = row[i].length();
                    }
                }
            }
        }
        
        StringBuilder sep = new StringBuilder(BLUE + "├");
        StringBuilder topSep = new StringBuilder(BLUE + "┌");
        StringBuilder botSep = new StringBuilder(BLUE + "└");
        for (int i = 0; i < colWidths.length; i++) {
            String line = "─".repeat(colWidths[i] + 2);
            sep.append(line);
            topSep.append(line);
            botSep.append(line);
            if (i < colWidths.length - 1) {
                sep.append("┼");
                topSep.append("┬");
                botSep.append("┴");
            }
        }
        sep.append("┤" + RESET);
        topSep.append("┐" + RESET);
        botSep.append("┘" + RESET);
        
        System.out.println(topSep.toString());
        System.out.print(BLUE + "│" + RESET);
        for (int i = 0; i < headers.length; i++) {
            System.out.printf(YELLOW + BOLD + " %-" + colWidths[i] + "s " + RESET + BLUE + "│" + RESET, headers[i]);
        }
        System.out.println();
        System.out.println(sep.toString());
        
        if (data != null && data.length > 0) {
            for (String[] row : data) {
                System.out.print(BLUE + "│" + RESET);
                for (int i = 0; i < row.length; i++) {
                    String val = row[i] == null ? "" : row[i];
                    System.out.printf(" %-" + colWidths[i] + "s " + BLUE + "│" + RESET, val);
                }
                System.out.println();
            }
        } else {
            System.out.print(BLUE + "│" + RESET);
            System.out.print(YELLOW + " No data available. " + RESET);
            System.out.println();
        }
        System.out.println(botSep.toString());
    }
}
