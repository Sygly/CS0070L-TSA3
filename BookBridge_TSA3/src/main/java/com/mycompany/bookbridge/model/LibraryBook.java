package com.mycompany.bookbridge.model;

public class LibraryBook {
    private final String isbn;
    private final String title;
    private BookStatus status;

    public LibraryBook(String isbn, String title) {
        if (isbn == null || isbn.isBlank()
                || title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "ISBN and title are required.");
        }
        this.isbn = isbn.trim();
        this.title = title.trim();
        this.status = BookStatus.AVAILABLE;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getTitle() {
        return title;
    }

    public BookStatus getStatus() {
        return status;
    }

    public void markBorrowed() {
        if (status != BookStatus.AVAILABLE) {
            throw new IllegalStateException(
                    "Book is already checked out.");
        }
        status = BookStatus.CHECKED_OUT;
    }

    public void markReturned() {
        if (status != BookStatus.CHECKED_OUT) {
            throw new IllegalStateException(
                    "Book is not checked out.");
        }
        status = BookStatus.AVAILABLE;
    }
}