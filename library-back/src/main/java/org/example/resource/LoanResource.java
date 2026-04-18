package org.example.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.example.dto.IssueLoanRequest;
import org.example.entity.Loan;
import org.example.service.LoanService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.example.api.ApiPaths.LOANS;
import static org.example.api.ApiPaths.LOANS_ISSUE;
import static org.example.api.ApiPaths.LOANS_REPORT;
import static org.example.api.ApiPaths.LOANS_RETURN;

@Path(LOANS)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class LoanResource {

    private final LoanService service;

    @GET
    public List<Loan> getAll() {
        return service.findAll();
    }

    @POST
    @Path(LOANS_ISSUE)
    public Response issue(@Valid @NotNull IssueLoanRequest request) {
        Loan loan = service.issue(request.bookId(), request.readerId());
        return Response.status(201).entity(loan).build();
    }

    @PUT
    @Path(LOANS_RETURN)
    public Loan returnBook(@PathParam("id") Long id) {
        return service.returnBook(id);
    }

    @GET
    @Path(LOANS_REPORT)
    public Map<String, Object> report(
            @QueryParam("readerId") Long readerId,
            @QueryParam("from") String from,
            @QueryParam("to") String to) {
        long count = service.countByReaderAndPeriod(
                readerId, LocalDate.parse(from), LocalDate.parse(to));
        return Map.of("readerId", readerId, "from", from, "to", to, "count", count);
    }
}
