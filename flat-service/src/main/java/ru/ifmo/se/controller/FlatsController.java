package ru.ifmo.se.controller;

import ru.ifmo.se.api.FlatsApi;
import ru.ifmo.se.dto.CountFlatsByHouseHouseParameterDto;
import ru.ifmo.se.dto.CountResponseDto;
import ru.ifmo.se.dto.DtoMapper;
import ru.ifmo.se.dto.ErrorDto;

import static ru.ifmo.se.dto.DtoMapper.toDto;
import static ru.ifmo.se.dto.DtoMapper.toEntity;

import ru.ifmo.se.dto.FlatDto;
import ru.ifmo.se.dto.FlatInputDto;
import ru.ifmo.se.dto.FlatPatchDto;
import ru.ifmo.se.dto.ViewDto;
import ru.ifmo.se.exception.BadHouseException;
import ru.ifmo.se.exception.InvalidPaginationException;
import ru.ifmo.se.exception.MissingHouseException;
import ru.ifmo.se.exception.NoFlatToDeleteException;
import ru.ifmo.se.exception.NoSuchFlatException;
import ru.ifmo.se.model.Flat;
import ru.ifmo.se.service.FlatService;

import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.StreamSupport;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.codec.CodecException;
import io.micronaut.http.context.ServerContextPathProvider;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ElementKind;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@Produces(MediaType.APPLICATION_XML)
@ExecuteOn(TaskExecutors.BLOCKING)
@RequiredArgsConstructor
@Slf4j
public class FlatsController implements FlatsApi {
    private final FlatService service;
    private final ServerContextPathProvider contextPathProvider;

    @Override
    public CountResponseDto countFlatsByHouse(@Nullable(inherited = true) CountFlatsByHouseHouseParameterDto house,
            @Nullable(inherited = true) String houseName, @Nullable(inherited = true) @Min(1) @Max(148) Long houseYear,
            @Nullable(inherited = true) @Min(1) Integer houseNumberOfFlatsOnFloor) {
        long count = service.countFlatsByProvidedHouse(house, houseName, houseYear, houseNumberOfFlatsOnFloor);

        return new CountResponseDto(count);
    }

    @Override
    public HttpResponse<@Valid FlatDto> createFlat(@NotNull @Valid FlatInputDto flatInputDto) {
        Flat flat = service.createFlat(flatInputDto);

        URI uri = UriBuilder.of("/")
                .path(contextPathProvider.getContextPath())
                .path("/flats")
                .path(Long.toString(flat.getId()))
                .build();

        return HttpResponse.created(uri)
                .body(toDto(flat));
    }

    @Override
    public HttpResponse<Void> deleteFlat(@NotNull @Min(1) Long id) {
        service.deleteFlatById(id);

        return HttpResponse.noContent();
    }

    @Override
    public FlatDto deleteOneFlatByNumberOfRooms(@NotNull @Min(1) @Max(20) Integer numberOfRooms) {
        Flat flat = service.deleteOneFlatByNumberOfRooms(numberOfRooms);

        return toDto(flat);
    }

    @Override
    public FlatDto getFlat(@NotNull @Min(1) Long id) {
        Flat flat = service.getFlatById(id);

        return toDto(flat);
    }

    @Override
    public List<@Valid FlatDto> getFlatsWithGreaterView(@NotNull ViewDto view) {
        List<Flat> flats = service.getFlatsWhereViewGreaterThan(toEntity(view));

        return flats.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public List<@Valid FlatDto> listFlats(@Nullable(inherited = true) List<@NotNull String> filter,
            @Nullable(inherited = true) @Size(min = 1) List<@NotNull String> sort,
            @Nullable(inherited = true) @Min(0) Integer page, @Nullable(inherited = true) @Min(1) Integer size) {
        List<Flat> flatsPage = service.getFlats(filter, sort, page, size);

        return flatsPage.stream()
                .map(DtoMapper::toDto)
                .toList();
    }

    @Override
    public FlatDto patchFlat(@NotNull @Min(1) Long id, @NotNull @Valid FlatPatchDto flatPatchDto) {
        Flat flat = service.updateFlatById(id, flatPatchDto);

        return toDto(flat);
    }

    @Override
    public FlatDto replaceFlat(@NotNull @Min(1) Long id, @NotNull @Valid FlatInputDto flatInputDto) {
        Flat flat = service.replaceFlatById(id, flatInputDto);

        return toDto(flat);
    }

    @Error
    public HttpResponse<ErrorDto> paginationError(InvalidPaginationException e) {
        ErrorDto dto = new ErrorDto(400, "pagination_error", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error
    public HttpResponse<ErrorDto> badHouseError(BadHouseException e) {
        ErrorDto dto = new ErrorDto(400, "bad_house_params", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error
    public HttpResponse<ErrorDto> missingHouseError(MissingHouseException e) {
        ErrorDto dto = new ErrorDto(409, "missing_house", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error
    public HttpResponse<ErrorDto> noFlatToDeleteHouseError(NoFlatToDeleteException e) {
        ErrorDto dto = new ErrorDto(409, "no_flat_to_delete", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error
    public HttpResponse<ErrorDto> noSuchFlatError(NoSuchFlatException e) {
        ErrorDto dto = new ErrorDto(404, "no_such_flat", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> validationError(ConstraintViolationException e) {
        // Bean properties belong to the XML body; scalar method parameters come from the URL.
        boolean bodyViolation = e.getConstraintViolations().stream()
                .flatMap(violation -> StreamSupport.stream(violation.getPropertyPath().spliterator(), false))
                .anyMatch(node -> node.getKind() == ElementKind.PROPERTY);
        HttpStatus status = bodyViolation ? HttpStatus.UNPROCESSABLE_ENTITY : HttpStatus.BAD_REQUEST;
        String code = bodyViolation ? "validation_error" : "bad_request";
        ErrorDto dto = new ErrorDto(status.getCode(), code, e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(status)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> conversionError(ConversionErrorException e) {
        ErrorDto dto = new ErrorDto(400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    // The generated enum converters throw IllegalArgumentException directly.
    @Error(global = true)
    public HttpResponse<ErrorDto> invalidValueError(IllegalArgumentException e) {
        ErrorDto dto = new ErrorDto(400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> databaseConstraintError(org.hibernate.exception.ConstraintViolationException e) {
        ErrorDto dto = new ErrorDto(422, "validation_error", "Data violates integrity constraints", ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.UNPROCESSABLE_ENTITY)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> missingArgumentError(UnsatisfiedArgumentException e) {
        ErrorDto dto = new ErrorDto(400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> missingRouteArgumentError(UnsatisfiedRouteException e) {
        ErrorDto dto = new ErrorDto(400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> malformedXmlError(JsonProcessingException e) {
        ErrorDto dto = new ErrorDto(400, "bad_request", "Malformed XML or invalid field value", ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> bodyDecodingError(CodecException e) {
        ErrorDto dto = new ErrorDto(400, "bad_request", "Malformed XML or invalid field value", ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> httpStatusError(HttpStatusException e) {
        HttpStatus status = e.getStatus();
        String code = status.name().toLowerCase(java.util.Locale.ROOT);
        ErrorDto dto = new ErrorDto(status.getCode(), code, status.getReason(), ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(status)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true, status = HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public HttpResponse<ErrorDto> unsupportedMediaTypeError() {
        ErrorDto dto = new ErrorDto(415, "unsupported_media_type", "Expected Content-Type: application/xml", ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true, status = HttpStatus.NOT_FOUND)
    public HttpResponse<ErrorDto> unknownRouteError() {
        ErrorDto dto = new ErrorDto(404, "not_found", "Resource not found", ZonedDateTime.now());
        return HttpResponse.<ErrorDto>status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<ErrorDto> error(Throwable e) {
        log.error("Unhandled request error", e);
        ErrorDto dto = new ErrorDto(500, "server_error", "Internal server error", ZonedDateTime.now());
        return HttpResponse.<ErrorDto>serverError()
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }
}
