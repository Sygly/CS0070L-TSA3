package com.mycompany.bookbridge.app;

import com.mycompany.bookbridge.model.*;
import com.mycompany.bookbridge.service.*;
import com.mycompany.bookbridge.exception.*;

public class BookBridgeApp {

    public static void main(String[] args) {

        System.out.println("================================");
        System.out.println("       BOOKBRIDGE TSA3");
        System.out.println(" Collections and Exception Handling");
        System.out.println("================================");

        BookBridgeTests.runAll();

        System.out.println(
                "\n========== CIRCULATION DEMO ==========");

        LibraryService library = new LibraryService();

        try {
            library.registerMember(
                    new StudentMember("S100", "Sample Student"));

            library.addBook(
                    new LibraryBook("9780000000001",
                            "Object Oriented Programming"));

            LoanRecord loan = library.issueLoan(
                    "S100", "9780000000001", 14);

            System.out.println("Loan ID: " + loan.getLoanId());
            System.out.println("Member: "
                    + loan.getMember().getMemberName());
            System.out.println("Book: "
                    + loan.getBook().getTitle());
            System.out.println("Loan Status: "
                    + loan.getStatus());
            System.out.println("Due Date: "
                    + loan.getDueDate());

            CirculationSummary summary = library.getSummary();

            System.out.println("Available Books: "
                    + summary.availableBooks());
            System.out.println("Checked Out Books: "
                    + summary.checkedOutBooks());

            System.out.println("Collection Consistency: "
                    + (library.isConsistent() ? "PASS" : "FAIL"));

        } catch (DuplicateEntityException
                | ItemUnavailableException
                | LoanLimitExceededException ex) {

            System.out.println(
                    "Circulation error: " + ex.getMessage());

        } catch (IllegalArgumentException ex) {

            System.out.println(
                    "Invalid input: " + ex.getMessage());
        }
    }
}