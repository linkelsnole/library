package org.example.testconstants;

import java.time.Duration;

public final class TestConstants {

    private TestConstants() {}

    public static final String HTTP_SCHEME = "http";
    public static final String LOCALHOST = "localhost";

    public static final Duration HTTP_CLIENT_CONNECT_TIMEOUT = Duration.ofSeconds(5);
    public static final Duration HTTP_CLIENT_REQUEST_TIMEOUT = Duration.ofSeconds(30);

    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final int HTTP_STATUS_OK = 200;
    public static final int HTTP_STATUS_CREATED = 201;

    public static final String SAMPLE_BOOK_TITLE = "Clean Code";
    public static final String SAMPLE_BOOK_AUTHOR = "Robert Martin";
    public static final int SAMPLE_BOOK_YEAR = 2008;

    public static String integrationBaseUri(int port) {
        return "%s://%s:%d/".formatted(HTTP_SCHEME, LOCALHOST, port);
    }

    public static String sampleBookJson() {
        return """
                {
                    "title": "%s",
                    "author": "%s",
                    "year": %d
                }
                """.formatted(SAMPLE_BOOK_TITLE, SAMPLE_BOOK_AUTHOR, SAMPLE_BOOK_YEAR);
    }
}
