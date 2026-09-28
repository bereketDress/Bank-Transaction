package com.bankofcli.api;

import java.math.BigDecimal;
import java.util.Scanner;

public class ConsoleInput {

    private final Scanner scanner;

    public ConsoleInput(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readText(String message) {
        System.out.print(message);
        return scanner.nextLine().trim();
    }

    public long readLong(String message) {
        System.out.print(message);
        return Long.parseLong(scanner.nextLine());
    }

    public BigDecimal readAmount() {
        System.out.print("Amount: ");
        return new BigDecimal(scanner.nextLine());// created using constructor
    }
}