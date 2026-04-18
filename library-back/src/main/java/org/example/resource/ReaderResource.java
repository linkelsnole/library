package org.example.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.example.dto.ReaderRequest;
import org.example.entity.Reader;
import org.example.service.ReaderService;

import java.util.List;

import static org.example.api.ApiPaths.BY_ID;
import static org.example.api.ApiPaths.READERS;

@Path(READERS)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class ReaderResource {

    private final ReaderService service;

    @GET
    public List<Reader> getAll() {
        return service.findAll();
    }

    @GET
    @Path(BY_ID)
    public Reader getById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Response create(@Valid @NotNull ReaderRequest request) {
        Reader reader = Reader.builder()
                .fullName(request.fullName())
                .gender(request.gender())
                .age(request.age())
                .build();
        return Response.status(201).entity(service.save(reader)).build();
    }

    @PUT
    @Path(BY_ID)
    public Reader update(@PathParam("id") Long id, @Valid @NotNull ReaderRequest request) {
        Reader reader = Reader.builder()
                .fullName(request.fullName())
                .gender(request.gender())
                .age(request.age())
                .build();
        return service.update(id, reader);
    }

    @DELETE
    @Path(BY_ID)
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
