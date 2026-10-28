package ru.ifmo.se.serialization;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Order;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.MutableHeaders;
import io.micronaut.http.MediaType;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.body.MessageBodyWriter;
import io.micronaut.http.codec.CodecException;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import ru.ifmo.se.dto.FlatDto;

@Singleton
@Produces(MediaType.APPLICATION_XML)
@Order(-100)
public class FlatListXmlWriter implements MessageBodyWriter<List<FlatDto>> {
    private final XmlMapper xmlMapper;

    public FlatListXmlWriter(@Named("xml") XmlMapper xmlMapper) {
        this.xmlMapper = xmlMapper;
    }

    @Override
    public void writeTo(@NonNull Argument<List<FlatDto>> type, @NonNull MediaType mediaType, List<FlatDto> flats,
            @NonNull MutableHeaders headers, @NonNull OutputStream outputStream) throws CodecException {
        try {
            headers.set(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_XML);
            xmlMapper.writer()
                    .withRootName("flats")
                    .without(JsonGenerator.Feature.AUTO_CLOSE_TARGET)
                    .writeValue(outputStream, Map.of("flat", flats));
        } catch (IOException e) {
            throw new CodecException("failed to serialize flats list", e);
        }
    }

    @Override
    public boolean isWriteable(Argument<List<FlatDto>> type, MediaType mediaType) {
        return List.class.isAssignableFrom(type.getType())
                && type.getFirstTypeVariable()
                        .map(el -> el.getType() == FlatDto.class)
                        .orElse(false);
    }

}
