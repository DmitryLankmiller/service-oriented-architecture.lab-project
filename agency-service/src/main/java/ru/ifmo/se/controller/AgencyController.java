package ru.ifmo.se.controller;

import java.math.BigDecimal;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.List;

import io.micronaut.context.annotation.Value;
import io.micronaut.core.bind.exceptions.UnsatisfiedArgumentException;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.exceptions.HttpClientException;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.client.exceptions.ReadTimeoutException;
import io.micronaut.http.exceptions.HttpStatusException;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.web.router.exceptions.UnsatisfiedRouteException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import ru.ifmo.se.api.AgencyApi;
import ru.ifmo.se.client.flat.dto.FlatDto;
import ru.ifmo.se.dto.AgencyDtoMapper;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetro200ResponseInnerDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetro400ResponseDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroModeParameterDto;
import ru.ifmo.se.dto.GetFlatsOrderedByTimeToMetroOrderParameterDto;
import ru.ifmo.se.dto.TotalCostResponseDto;
import ru.ifmo.se.service.AgencyService;

@Controller
@Produces(MediaType.APPLICATION_XML)
@ExecuteOn(TaskExecutors.BLOCKING)
@Slf4j
public class AgencyController implements AgencyApi {
    private final AgencyService service;
    private final String flatPublicBaseUrl;

    public AgencyController(AgencyService service,
            @Value("${app.flat-service.public.url}") String flatPublicBaseUrl) {
        this.service = service;
        this.flatPublicBaseUrl = flatPublicBaseUrl;
    }

    @Override
    public HttpResponse<@NotNull List<@Valid GetFlatsOrderedByTimeToMetro200ResponseInnerDto>> getFlatsOrderedByTimeToMetro(
            @NotNull GetFlatsOrderedByTimeToMetroModeParameterDto mode,
            @NotNull GetFlatsOrderedByTimeToMetroOrderParameterDto order) {
        List<FlatDto> flats = service.listFlatsOrderedByTimeToMetro(mode, order);

        return HttpResponse.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(flats.stream()
                        .map(AgencyDtoMapper::toDto)
                        .toList());
    }

    @Override
    public HttpResponse<TotalCostResponseDto> getTotalCost() {
        BigDecimal cost = service.calculateFlatsTotalCost();

        return HttpResponse.ok(new TotalCostResponseDto(cost))
                .contentType(MediaType.APPLICATION_XML);
    }

    @Override
    public HttpResponse<Void> redirectToFlat(@NotNull @Min(1) Long id) {
        URI target = UriBuilder.of(flatPublicBaseUrl)
                .path("flats")
                .path(id.toString())
                .build();

        return HttpResponse.temporaryRedirect(target);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> validationError(ConstraintViolationException e) {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> conversionError(ConversionErrorException e) {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> invalidValueError(IllegalArgumentException e) {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> missingArgumentError(UnsatisfiedArgumentException e) {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> missingRouteArgumentError(UnsatisfiedRouteException e) {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                400, "bad_request", e.getMessage(), ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> httpStatusError(HttpStatusException e) {
        HttpStatus status = e.getStatus();
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                status.getCode(), status.name().toLowerCase(java.util.Locale.ROOT), status.getReason(), ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(status)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> flatServiceResponseError(HttpClientResponseException e) {
        log.warn("Flat service returned an error response", e);
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                502, "flat_service_error", "Flat service returned an unsuccessful or invalid response", ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.BAD_GATEWAY)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> flatServiceTimeoutError(ReadTimeoutException e) {
        log.warn("Flat service request timed out", e);
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                504, "flat_service_timeout", "Flat service did not respond in time", ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.GATEWAY_TIMEOUT)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> flatServiceConnectionError(HttpClientException e) {
        log.warn("Flat service connection failed", e);
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                503, "flat_service_unavailable", "Flat service is unavailable", ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true, status = HttpStatus.NOT_FOUND)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> unknownRouteError() {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                404, "not_found", "Resource not found", ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true, status = HttpStatus.METHOD_NOT_ALLOWED)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> methodNotAllowedError() {
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                405, "method_not_allowed", "HTTP method is not allowed for this resource", ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>status(HttpStatus.METHOD_NOT_ALLOWED)
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }

    @Error(global = true)
    public HttpResponse<GetFlatsOrderedByTimeToMetro400ResponseDto> error(Throwable e) {
        log.error("Unhandled request error", e);
        GetFlatsOrderedByTimeToMetro400ResponseDto dto = new GetFlatsOrderedByTimeToMetro400ResponseDto(
                500, "server_error", "Internal server error", ZonedDateTime.now());
        return HttpResponse.<GetFlatsOrderedByTimeToMetro400ResponseDto>serverError()
                .contentType(MediaType.APPLICATION_XML_TYPE)
                .body(dto);
    }
}
