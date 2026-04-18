package com.example.library.rcp2.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.library.rcp2.model.Book;
import com.example.library.rcp2.model.Reader;
import com.example.library.rcp2.model.Loan;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ApiClient {

    private static final ApiClient INSTANCE = new ApiClient();

    private static final int CONNECT_TIMEOUT_MS = 5000;
    private static final int READ_TIMEOUT_MS = 10000;
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String HEADER_CONTENT_TYPE = "Content-Type";

    private final String base;
    private final ObjectMapper mapper = new ObjectMapper()
            .configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);

    private ApiClient() {
        this.base = AppConfig.apiBase();
    }

    public static ApiClient getInstance() {
        return INSTANCE;
    }

    public List<Book> getBooks() { return getList("/books", new TypeReference<>() {}); }
    public void createBook(Book book) { post("/books", book); }
    public void updateBook(Long id, Book book) { put("/books/" + id, book); }
    public void deleteBook(Long id) { delete("/books/" + id); }

    public List<Reader> getReaders() { return getList("/readers", new TypeReference<>() {}); }
    public void createReader(Reader reader) { post("/readers", reader); }
    public void updateReader(Long id, Reader reader) { put("/readers/" + id, reader); }
    public void deleteReader(Long id) { delete("/readers/" + id); }

    public List<Loan> getLoans() { return getList("/loans", new TypeReference<>() {}); }

    public void issueBook(Long bookId, Long readerId) {
        post("/loans/issue", Map.of("bookId", bookId, "readerId", readerId));
    }

    public void returnBook(Long loanId) {
        try {
            HttpURLConnection con = open(base + "/loans/" + loanId + "/return");
            con.setRequestMethod("PUT");
            con.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
            checkResponse(con);
            con.disconnect();
        } catch (Exception e) {
            throw asApiException(e);
        }
    }

    public long getReport(Long readerId, String from, String to) {
        try {
            String url = base + "/loans/report?readerId=" + readerId + "&from=" + from + "&to=" + to;
            HttpURLConnection con = open(url);
            con.setRequestMethod("GET");
            String body = readBody(con);
            con.disconnect();
            Map<String, Object> map = mapper.readValue(body, new TypeReference<>() {});
            return ((Number) map.get("count")).longValue();
        } catch (Exception e) {
            throw asApiException(e);
        }
    }

    private <T> List<T> getList(String path, TypeReference<List<T>> ref) {
        try {
            HttpURLConnection con = open(base + path);
            con.setRequestMethod("GET");
            String body = readBody(con);
            con.disconnect();
            return mapper.readValue(body, ref);
        } catch (Exception e) {
            throw asApiException(e);
        }
    }

    private void post(String path, Object body) {
        try {
            byte[] json = mapper.writeValueAsBytes(body);
            HttpURLConnection con = open(base + path);
            con.setRequestMethod("POST");
            con.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json);
            }
            checkResponse(con);
            con.disconnect();
        } catch (Exception e) {
            throw asApiException(e);
        }
    }

    private void put(String path, Object body) {
        try {
            byte[] json = mapper.writeValueAsBytes(body);
            HttpURLConnection con = open(base + path);
            con.setRequestMethod("PUT");
            con.setRequestProperty(HEADER_CONTENT_TYPE, CONTENT_TYPE_JSON);
            con.setDoOutput(true);
            try (OutputStream os = con.getOutputStream()) {
                os.write(json);
            }
            checkResponse(con);
            con.disconnect();
        } catch (Exception e) {
            throw asApiException(e);
        }
    }

    private void delete(String path) {
        try {
            HttpURLConnection con = open(base + path);
            con.setRequestMethod("DELETE");
            checkResponse(con);
            con.disconnect();
        } catch (Exception e) {
            throw asApiException(e);
        }
    }

    private HttpURLConnection open(String url) throws Exception {
        HttpURLConnection con = (HttpURLConnection) new URL(url).openConnection();
        con.setConnectTimeout(CONNECT_TIMEOUT_MS);
        con.setReadTimeout(READ_TIMEOUT_MS);
        return con;
    }

    private String readBody(HttpURLConnection con) throws Exception {
        int code = con.getResponseCode();
        InputStream is = code >= 400 ? con.getErrorStream() : con.getInputStream();
        String body = is == null ? "" : new String(is.readAllBytes(), StandardCharsets.UTF_8);
        if (code >= 400) {
            throw new ApiException(extractErrorMessage(body, code));
        }
        return body;
    }

    private void checkResponse(HttpURLConnection con) throws Exception {
        int code = con.getResponseCode();
        if (code >= 400) {
            InputStream es = con.getErrorStream();
            String err = es == null ? "" : new String(es.readAllBytes(), StandardCharsets.UTF_8);
            throw new ApiException(extractErrorMessage(err, code));
        }
    }

    @SuppressWarnings("unchecked")
    private String extractErrorMessage(String body, int code) {
        if (body == null || body.isEmpty()) {
            return "HTTP " + code;
        }
        try {
            Map<String, Object> map = mapper.readValue(body, Map.class);
            Object error = map.get("error");
            if (error != null) {
                return error.toString();
            }
            Object validation = map.get("validation_errors");
            if (validation instanceof List<?> list && !list.isEmpty()) {
                return String.join(", ", list.stream().map(Object::toString).toList());
            }
        } catch (Exception ignore) {
        }
        return "HTTP " + code + ": " + body;
    }

    private RuntimeException asApiException(Exception e) {
        if (e instanceof ApiException ae) {
            return ae;
        }
        return new ApiException(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
    }
}