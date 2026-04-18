package org.example.api;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ApiPaths {

    public static final String BOOKS = "/books";
    public static final String READERS = "/readers";
    public static final String LOANS = "/loans";

    public static final String BY_ID = "/{id}";
    public static final String LOANS_ISSUE = "/issue";
    public static final String LOANS_RETURN = "/{id}/return";
    public static final String LOANS_REPORT = "/report";
}
