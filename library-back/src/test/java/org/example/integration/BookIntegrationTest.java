package org.example.integration;

import org.example.Main;
import org.example.api.ApiPaths;
import org.example.testconstants.TestConstants;
import org.glassfish.grizzly.http.server.HttpServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.example.testconstants.TestConstants.CONTENT_TYPE_JSON;
import static org.example.testconstants.TestConstants.HEADER_CONTENT_TYPE;
import static org.example.testconstants.TestConstants.HTTP_CLIENT_CONNECT_TIMEOUT;
import static org.example.testconstants.TestConstants.HTTP_CLIENT_REQUEST_TIMEOUT;
import static org.example.testconstants.TestConstants.HTTP_STATUS_CREATED;
import static org.example.testconstants.TestConstants.HTTP_STATUS_OK;
import static org.example.testconstants.TestConstants.SAMPLE_BOOK_TITLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookIntegrationTest {

    private static HttpServer server;
    private static HttpClient client;
    private static String baseUri;

    private static int freePort() throws Exception {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private static URI booksUri() {
        return URI.create(baseUri).resolve(ApiPaths.BOOKS.substring(1));
    }

    @BeforeAll
    static void setUp() throws Exception {
        baseUri = TestConstants.integrationBaseUri(freePort());
        server = Main.startServer(URI.create(baseUri));
        client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(HTTP_CLIENT_CONNECT_TIMEOUT)
                .build();
    }

    @AfterAll
    static void tearDown() {
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void createAndGetBook_integrationTest() throws Exception {
        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(booksUri())
                .timeout(HTTP_CLIENT_REQUEST_TIMEOUT)
                .header(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON)
                .expectContinue(false)
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.sampleBookJson()))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HTTP_STATUS_CREATED, postResponse.statusCode());

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(booksUri())
                .timeout(HTTP_CLIENT_REQUEST_TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(HTTP_STATUS_OK, getResponse.statusCode());
        assertTrue(getResponse.body().contains(SAMPLE_BOOK_TITLE));
    }
}
