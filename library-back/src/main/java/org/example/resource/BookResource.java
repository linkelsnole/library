package org.example.resource;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.example.dto.BookRequest;
import org.example.entity.Book;
import org.example.service.BookService;

import java.util.List;

import static org.example.api.ApiPaths.BOOKS;
import static org.example.api.ApiPaths.BY_ID;

@Path(BOOKS)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RequiredArgsConstructor(onConstructor_ = {@Inject})
public class BookResource {

    private final BookService service;

    @GET
    public List<Book> getAll() {
        return service.findAll();
    }

    @GET
    @Path(BY_ID)
    public Book getById(@PathParam("id") Long id) {
        return service.findById(id);
    }

    @POST
    public Response create(@Valid @NotNull BookRequest request) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .year(request.year())
                .isbn(request.isbn())
                .build();
        return Response.status(201).entity(service.save(book)).build();
    }

    @PUT
    @Path(BY_ID)
    public Book update(@PathParam("id") Long id, @Valid @NotNull BookRequest request) {
        Book book = Book.builder()
                .title(request.title())
                .author(request.author())
                .year(request.year())
                .isbn(request.isbn())
                .build();
        return service.update(id, book);
    }

    @DELETE
    @Path(BY_ID)
    public Response delete(@PathParam("id") Long id) {
        service.delete(id);
        return Response.noContent().build();
    }
}
