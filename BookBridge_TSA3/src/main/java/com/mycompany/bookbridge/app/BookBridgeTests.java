package com.mycompany.bookbridge.app;

import com.mycompany.bookbridge.model.*;
import com.mycompany.bookbridge.service.*;
import com.mycompany.bookbridge.exception.*;
import java.time.LocalDate;
import java.util.*;

public class BookBridgeTests {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean result) {
        System.out.printf("%-43s : %s%n",
                name, result ? "PASS" : "FAIL");

        if (result) {
            passed++;
        } else {
            failed++;
        }
    }

    private static boolean throwsType(
            Class<? extends Throwable> expected,
            Runnable action) {
        try {
            action.run();
            return false;
        } catch (RuntimeException ex) {
            return expected.isInstance(ex);
        }
    }

    public static void runAll() {
        passed = 0;
        failed = 0;

        System.out.println(
                "\n========== TSA3 AUTOMATED TESTS ==========");

        LibraryService service = new LibraryService();

        service.registerMember(
                new StudentMember("M001", "Maria Santos"));
        service.registerMember(
                new FacultyMember("M002", "John Reyes"));
        service.registerMember(
                new GuestMember("M003", "Anna Cruz"));

        service.addBook(
                new LibraryBook("ISBN001", "Java Programming"));
        service.addBook(
                new LibraryBook("ISBN002", "Database Systems"));
        service.addBook(
                new LibraryBook("ISBN003", "Computer Networks"));
        service.addBook(
                new LibraryBook("ISBN004", "Data Structures"));

        // TC01: Successful loan
        LoanRecord first = service.issueLoan(
                "M001", "ISBN001", 10,
                LocalDate.of(2026, 11, 1));

        check("TC01 - Successful Loan",
                first.getStatus() == LoanStatus.ACTIVE
                && service.isConsistent());

        // TC02: Duplicate member
        int memberCount = service.listMembers().size();

        boolean duplicate = throwsType(
                DuplicateEntityException.class,
                () -> service.registerMember(
                        new StudentMember("M001", "Duplicate")));

        check("TC02 - Duplicate Entity",
                duplicate
                && service.listMembers().size() == memberCount
                && service.isConsistent());

        // TC03: Unavailable item
        int historyBefore = service.listLoanHistory().size();

        boolean unavailable = throwsType(
                ItemUnavailableException.class,
                () -> service.issueLoan(
                        "M002", "ISBN001", 10));

        check("TC03 - Unavailable Item",
                unavailable
                && service.listLoanHistory().size() == historyBefore
                && service.isConsistent());

        // TC04: Membership loan limit
        boolean limit = throwsType(
                LoanLimitExceededException.class,
                () -> service.issueLoan(
                        "M003", "ISBN002", 10));

        check("TC04 - Loan Limit Violation",
                limit
                && service.findBook("ISBN002").orElseThrow()
                        .getStatus() == BookStatus.AVAILABLE
                && service.isConsistent());

        // TC05: Empty reservation queue
        check("TC05 - Empty Reservation Queue",
                service.processNextReservation().isEmpty()
                && service.listReservations().isEmpty());

        // TC06: Missing search
        check("TC06 - Missing Search",
                service.findBook("MISSING").isEmpty()
                && service.findMember("UNKNOWN").isEmpty());

        // TC07: Sorting with due-date tie
        LoanRecord second = service.issueLoan(
                "M002", "ISBN002", 10,
                LocalDate.of(2026, 11, 1));

        LoanRecord third = service.issueLoan(
                "M003", "ISBN003", 7,
                LocalDate.of(2026, 11, 4));

        List<LoanRecord> sorted = service.getSortedLoanView(
                LocalDate.of(2026, 11, 20));

        check("TC07 - Sort Tie by Loan ID",
                sorted.size() == 3
                && sorted.get(0).getLoanId().equals("L1")
                && sorted.get(1).getLoanId().equals("L2")
                && sorted.get(2).getLoanId().equals("L3"));

        // TC08: Aggregate verification
        CirculationSummary summary = service.getSummary(
                LocalDate.of(2026, 11, 20));

        check("TC08 - Aggregate Verification",
                summary.availableBooks() == 1
                && summary.checkedOutBooks() == 3
                && summary.activeLoansByType()
                        .get(MembershipType.STUDENT) == 1
                && summary.activeLoansByType()
                        .get(MembershipType.FACULTY) == 1
                && summary.activeLoansByType()
                        .get(MembershipType.GUEST) == 1
                && summary.totalEstimatedFines() == 153);

        // TC09: Successful return
        service.returnBook(
                "ISBN001", LocalDate.of(2026, 11, 15));

        check("TC09 - Successful Return",
                first.getStatus() == LoanStatus.RETURNED
                && service.findActiveLoan("ISBN001").isEmpty()
                && service.isConsistent());

        // TC10: Failed return does not mutate collections
        int activeBefore = service.filterLoans(
                LoanStatus.ACTIVE).size();

        boolean failedReturn = throwsType(
                ItemUnavailableException.class,
                () -> service.returnBook("ISBN001"));

        check("TC10 - Failed Return Consistency",
                failedReturn
                && service.filterLoans(LoanStatus.ACTIVE).size()
                        == activeBefore
                && service.isConsistent());

        // TC11: Reservation and queue processing
        Reservation reservation = service.reserveBook(
                "M002", "ISBN003", 20);

        boolean queued = reservation.getBook()
                .getIsbn().equals("ISBN003")
                && service.listReservations().size() == 1;

        service.returnBook(
                "ISBN003", LocalDate.of(2026, 11, 12));

        Optional<LoanRecord> processed =
                service.processNextReservation(
                        LocalDate.of(2026, 11, 12));

        check("TC11 - Reservation Processing",
                queued && processed.isPresent()
                && processed.get().getMember()
                        .getMemberId().equals("M002")
                && service.listReservations().isEmpty()
                && service.isConsistent());

        // TC12: Failed reservation consistency
        int queueBefore = service.listReservations().size();

        boolean invalidReserve = throwsType(
                IllegalStateException.class,
                () -> service.reserveBook(
                        "M001", "ISBN004", 5));

        check("TC12 - Failed Reservation Consistency",
                invalidReserve
                && service.listReservations().size() == queueBefore
                && service.isConsistent());

        // TC13: Searching and filtering
        check("TC13 - Search and Filters",
                service.searchByTitle("java").size() == 1
                && service.filterBooks(BookStatus.AVAILABLE)
                        .size() == 2
                && service.filterMembers(MembershipType.STUDENT)
                        .size() == 1);

        System.out.println(
                "\n========== TEST SUMMARY ==========");
        System.out.println("Passed: " + passed);
        System.out.println("Failed: " + failed);
        System.out.println("Overall Result: "
                + (failed == 0 ? "PASS" : "FAIL"));
    }
}