package com.metreeca.mesh.tools;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.shim.URIs;

import java.net.URI;
import java.net.URLDecoder;
import java.util.Base64;
import java.util.List;
import java.util.regex.Pattern;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.shim.Collections.list;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.joining;

/**
 * Query string decoder for agent requests.
 *
 * <p>Provides automatic detection and decoding of different query string formats:</p>
 * <ul>
 *   <li>URL-encoded strings</li>
 *   <li>Base64-encoded strings</li>
 *   <li>URL-encoded form data with query operators</li>
 *   <li>Plain text strings (fallback)</li>
 * </ul>
 *
 * <p>Form data queries are automatically converted to collection queries using the first available
 * collection property from the target shape.</p>
 */
final class AgentQuery {

    /**
     * The RDF predicate URI for collection membership.
     */
    private static final URI MEMBERS=URIs.uri("http://www.w3.org/2000/01/rdf-schema#member");


    /**
     * Pattern for detecting URL-encoded strings.
     *
     * <p>Matches strings containing URL-encoded characters (% followed by hex digits).</p>
     */
    private static final Pattern URL_PATTERN=Pattern.compile(
            ".*%[0-9A-Fa-f]{2}.*"
    );

    /**
     * Pattern for detecting Base64-encoded strings.
     *
     * <p>Matches valid Base64 encoding with proper padding (0, 1, or 2 equals signs).</p>
     */
    private static final Pattern BASE64_PATTERN=Pattern.compile(
            "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$"
    );

    /**
     * Pattern for detecting URL-encoded form data.
     *
     * <p>Matches key-value pairs where keys can contain word characters and query operators
     * {@code ~<>^@#:*}. Coordinates with {@link Query#query(String, Shape, URI)} for operator support.</p>
     */
    private static final Pattern FORM_DATA_PATTERN=Pattern.compile(
            "^[\\w~<>^@#:*]*=[^&]*(?:&[\\w~<>^@#:*]*=[^&]*)*$"
    );


    /**
     * Processes a query string into a structured value using the provided codec and shape.
     *
     * <p>Automatically detects the query format and applies the appropriate decoding strategy:</p>
     * <ol>
     *   <li>URL-encoded strings: Decoded and recursively processed</li>
     *   <li>Base64-encoded: Decoded and recursively processed</li>
     *   <li>URL-encoded Form data: Converted to collection query using target shape</li>
     *   <li>Plain text: Processed directly by codec</li>
     * </ol>
     *
     * @param codec the delegate codec for processing decoded content
     * @param query the query string to process
     * @param shape the target shape for validation and structure
     * @param base  the base URI for resolving relative URIs in decoded content
     *
     * @return the processed value structure
     *
     * @throws NullPointerException if any parameter is {@code null}
     * @throws CodecException       if no suitable collection property is found for form data queries
     * @throws CodecException       if multiple collection properties are found without clear precedence
     */
    static Value query(final Codec codec, final String query, final Shape shape, final URI base) {

        if ( URL_PATTERN.matcher(query).matches() ) {

            return query(codec, URLDecoder.decode(query, UTF_8), shape, base);

        } else if ( BASE64_PATTERN.matcher(query).matches() ) {

            return query(codec, new String(Base64.getDecoder().decode(query), UTF_8), shape, base);

        } else if ( FORM_DATA_PATTERN.matcher(query).matches() ) {

            final Property collection=shape.properties().stream()

                    .filter(property -> property.forward()
                            .filter(MEMBERS::equals)
                            .isPresent()
                    )

                    .findFirst()

                    .orElseGet(() -> {

                        final List<Property> collections=list(shape.properties().stream().filter(property ->

                                property.shape().is(Object())
                                && property.shape().maxCount().orElse(Integer.MAX_VALUE) > 1

                        ));

                        if ( collections.size() == 1 ) {

                            return collections.getFirst();

                        } else if ( collections.isEmpty() ) {

                            throw new CodecException("no collection property found");

                        } else {

                            throw new CodecException(format("multiple collection properties found <%s>",
                                    collections.stream().map(Property::name).collect(joining(", "))
                            ));

                        }

                    });

            return object(
                    shape(shape),
                    field(collection.name(), value(Query.query(query, collection.shape(), base)))
            );

        } else {

            return codec.decode(query, shape);

        }

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private AgentQuery() { }

}
