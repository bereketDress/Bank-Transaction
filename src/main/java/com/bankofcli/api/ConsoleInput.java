package com.bankofcli.api;

import com.bankofcli.exception.BankException;
import java.math.BigDecimal;
import java.util.Scanner;
import java.util.NoSuchElementException;

public final class ConsoleInput {
    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public BigDecimal readAmount() {
        return new BigDecimal(readText("Amount: "));
    }

    public int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readText(prompt));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a whole number.");
            }
        }
    }

    public long readLong(String prompt) {
        while (true) {
            try {
                return Long.parseLong(readText(prompt));
            } catch (NumberFormatException exception) {
                System.out.println("Enter a valid ID.");
            }
        }
    }

    public String readText(String prompt) {
        return readLine(prompt).trim();
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine();
    }

    public void showError(RuntimeException exception) {
        if (exception instanceof NoSuchElementException) {
            throw exception;
        }
        if (exception instanceof BankException || exception instanceof IllegalArgumentException) {
            System.out.println("Error: " + exception.getMessage());
        } else {
            System.out.println("Sorry, the banking service is currently unavailable.");
        }
    }
}
