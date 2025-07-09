/*
 * Copyright © 2022-2025 Metreeca srl
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.metreeca.mesh.pipe;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.ValueException;
import com.metreeca.mesh.queries.Query;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.shim.Locales;
import com.metreeca.shim.URIs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.pipe.AgentModel.expand;
import static com.metreeca.mesh.pipe.AgentModel.populate;
import static com.metreeca.mesh.pipe.AgentQuery.query;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.URIs.uri;

import static java.lang.Float.parseFloat;
import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.text.Normalizer.normalize;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;

/**
 * REST API agent for handling HTTP-style operations.
 *
 * <p>Provides a complete REST API implementation that maps HTTP operations to store operations
 * with automatic content negotiation, validation, and error handling.</p>
 *
 * <h2>HTTP Method Mapping</h2>
 *
 * <p>HTTP methods are mapped to corresponding store operations:</p>
 * <ul>
 *   <li>{@code GET} → {@link Store#retrieve(Valuable, List)} - Retrieves resources with optional filtering and sorting</li>
 *   <li>{@code POST} → {@link Store#create(Valuable)} - Creates new resources with generated or provided identifiers</li>
 *   <li>{@code PUT} → {@link Store#update(Valuable)} - Replaces existing resources completely</li>
 *   <li>{@code PATCH} → {@link Store#mutate(Valuable)} - Partially updates existing resources</li>
 *   <li>{@code DELETE} → {@link Store#delete(Valuable)} - Removes existing resources</li>
 * </ul>
 *
 * <h2>Query String Processing</h2>
 *
 * <p>Query strings are automatically detected and processed using four strategies:</p>
 * <ul>
 *   <li><strong>URL-encoded strings</strong> - Automatically decoded and recursively processed</li>
 *   <li><strong>Base64-encoded strings</strong> - Automatically decoded and recursively processed</li>
 *   <li><strong>Form data with operators</strong> - Converted to structured queries as documented in
 *       {@link Query#query(String, Shape, URI)}</li>
 *   <li><strong>Plain strings</strong> - Processed directly by the configured codec</li>
 * </ul>
 *
 * <p>Form data queries are automatically converted to collection queries by identifying suitable
 * collection properties in the target shape (either properties with {@code rdfs:member} predicates
 * or Object-typed properties with {@code maxCount > 1}).</p>
 *
 * <h2>Content Negotiation</h2>
 *
 * <p>The agent supports content negotiation through standard HTTP headers:</p>
 * <ul>
 *   <li>{@code Accept} - Determines response format (JSON or JSON-LD)</li>
 *   <li>{@code Accept-Language} - Locale preferences for internationalised content</li>
 *   <li>{@code Content-Type} - Only {@code application/json} is supported for input</li>
 * </ul>
 */
public final class Agent {

    private static final String GET="GET";
    private static final String POST="POST";
    private static final String PUT="PUT";
    private static final String PATCH="PATCH";
    private static final String DELETE="DELETE";

    private static final int OK=200;
    private static final int CREATED=201;
    private static final int NO_CONTENT=204;
    private static final int BAD_REQUEST=400;
    private static final int NOT_FOUND=404;
    private static final int METHOD_NOT_ALLOWED=405;
    private static final int CONFLICT=409;
    private static final int UNSUPPORTED_MEDIA_TYPE=415;
    private static final int UNPROCESSABLE_ENTITY=422;

    private static final String ACCEPT="Accept";
    private static final String ACCEPT_LANGUAGE="Accept-Language";
    private static final String CONTENT_TYPE="Content-Type";
    private static final String LOCATION="Location";
    private static final String SLUG="Slug";

    private static final String JSON="application/json";
    private static final String JSONLD="application/ld+json";


    private static final Pattern QUALITY_PATTERN=compile("(?:\\s*;\\s*q\\s*=\\s*(\\d*(?:\\.\\d+)?))?");
    private static final Pattern LANG_PATTERN=compile("([a-zA-Z]{1,8}(?:-[a-zA-Z0-9]{1,8})*|\\*)"+QUALITY_PATTERN);
    private static final Pattern MIME_PATTERN=compile("((?:[-+\\w]+|\\*)/(?:[-+\\w]+|\\*))"+QUALITY_PATTERN);

    private static final Pattern MARK_PATTERN=compile("\\p{M}");
    private static final Pattern SPECIAL_PATTERN=compile("[^\\p{L}\\p{N}]");
    private static final Pattern SPACES_PATTERN=compile("\\s+");


    /**
     * Parses quality-weighted values from HTTP headers.
     *
     * @param values  the header value containing quality-weighted entries
     * @param pattern the regex pattern for parsing entries and quality values
     *
     * @return stream of values sorted by quality in descending order
     */
    private static Stream<String> values(final CharSequence values, final Pattern pattern) {

        final List<Entry<String, Float>> entries=new ArrayList<>();

        final Matcher matcher=pattern.matcher(values);

        while ( matcher.find() ) {

            final String media=matcher.group(1).toLowerCase(ROOT);
            final String quality=matcher.group(2);

            try {
                entries.add(entry(media, quality == null ? 1 : parseFloat(quality)));
            } catch ( final NumberFormatException ignored ) {
                entries.add(entry(media, 0.0f));
            }

        }

        return entries.stream()
                .sorted((x, y) -> -Float.compare(x.getValue(), y.getValue()))
                .map(Entry::getKey);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Value model;
    private final Codec codec;
    private final Store store;

    private final Function<AgentRequest, String> slug;


    /**
     * Creates an agent with automatic id generation.
     *
     * <p>Uses the default ID generation strategy that processes the Slug header from incoming requests.
     * The ID generation process attempts to use a sanitised version of the Slug header value, falling back
     * to a generated UUID if no valid slug is provided. The sanitisation follows RFC 5023 section 9.7
     * guidelines for processing client-suggested URI components.</p>
     *
     * <p>The sanitisation process:</p>
     *
     * <ul>
     *   <li>URL decodes the header value</li>
     *   <li>Removes diacritical marks from Unicode characters</li>
     *   <li>Replaces non-letter/digit characters with spaces</li>
     *   <li>Converts space sequences to hyphens</li>
     * </ul>
     *
     * @param model the data model for validation and transformation
     * @param codec the codec for serialization and deserialization
     * @param store the persistence store for data operations
     *
     * @throws NullPointerException if any parameter is {@code null}
     *
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc5023#section-9.7">RFC 5023 The Atom Publishing Protocol - § 9.7 - The Slug Header</a>
     */
    public Agent(final Value model, final Codec codec, final Store store) {
        this(model, codec, store, request -> Optional.ofNullable(request.header(SLUG))
                .map(s -> URLDecoder.decode(s, UTF_8))
                .map(String::trim)
                .filter(not(String::isEmpty))
                .map(s -> MARK_PATTERN.matcher(normalize(s, Form.NFD)).replaceAll(""))
                .map(s -> SPECIAL_PATTERN.matcher(s).replaceAll(" "))
                .map(s -> SPACES_PATTERN.matcher(s.trim()).replaceAll("-"))
                .orElseGet(URIs::uuid)
        );
    }

    /**
     * Creates an agent with custom id generation.
     *
     * @param model the data model for validation and transformation
     * @param codec the codec for serialization and deserialization
     * @param store the persistence store for data operations
     * @param slug  the function for generating resource identifiers
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public Agent(final Value model, final Codec codec, final Store store, final Function<AgentRequest, String> slug) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( codec == null ) {
            throw new NullPointerException("null codec");
        }

        if ( store == null ) {
            throw new NullPointerException("null store");
        }

        if ( slug == null ) {
            throw new NullPointerException("null slug processor");
        }

        this.model=expand(model);
        this.codec=codec;
        this.store=store;

        this.slug=slug;
    }


    /**
     * Processes an HTTP-style request and generates an appropriate response.
     *
     * @param request  the incoming request to process
     * @param response the response object to populate
     *
     * @throws NullPointerException     if either parameter is {@code null}
     * @throws IllegalArgumentException if the request contains invalid data
     */
    public void process(final AgentRequest request, final AgentResponse response) {

        if ( request == null ) {
            throw new NullPointerException("null request");
        }

        if ( response == null ) {
            throw new NullPointerException("null response");
        }

        final String method=requireNonNull(request.method(), "null method").toUpperCase(ROOT);
        final URI resource=requireNonNull(request.resource(), "null resource");
        final String content=request.header(CONTENT_TYPE);

        if ( !resource.isAbsolute() ) {
            throw new IllegalArgumentException(format("relative resource URI <%s>", resource));
        }

        if ( content != null && !content.equals(JSON) ) {

            response.status(UNSUPPORTED_MEDIA_TYPE);
            response.header(ACCEPT, JSON);

        } else {

            switch ( method ) {

                case GET -> retrieve(resource, request, response);
                case POST -> create(resource, request, response);
                case PUT -> update(resource, request, response);
                case PATCH -> mutate(resource, request, response);
                case DELETE -> delete(resource, request, response);

                default -> response.status(METHOD_NOT_ALLOWED);

            }

        }

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void retrieve(final URI resource, final AgentRequest request, final AgentResponse response) {
        try {

            final boolean jsonld=Optional.ofNullable(request.header(ACCEPT)).stream()
                    .flatMap(accept -> values(accept, MIME_PATTERN))
                    .anyMatch(mime -> mime.equals(JSONLD));

            final Value specs=Optional.of(request.query())
                    .filter(not(String::isEmpty))
                    .map(query -> query(codec, query, model.shape().orElseGet(Shape::shape), resource))
                    .orElseGet(Value::Object);

            final List<Locale> locales=Optional.ofNullable(request.header(ACCEPT_LANGUAGE)).stream()
                    .flatMap(accept -> values(accept, LANG_PATTERN))
                    .map(Locales::locale)
                    .toList();

            store

                    .retrieve(
                            (specs.isEmpty() ? model : populate(specs, model)).merge(object(id(resource))),
                            locales
                    )

                    .value()

                    .ifPresentOrElse(

                            value -> { // !!! validate value

                                response.status(OK);
                                response.header(CONTENT_TYPE, JSON);
                                response.output(output -> // include id only if required
                                        encode(output, value.merge(object(id((specs.id().orElse(uri()))))), jsonld)
                                );

                            },

                            () -> response.status(NOT_FOUND)

                    );

        } catch ( final NoSuchElementException|ValueException e ) {

            response.status(UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        } catch ( final CodecException e ) { // !!! merge after isSyntactic() is removed

            response.status(e.isSyntactic() ? BAD_REQUEST : UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        }
    }


    private void create(final URI resource, final AgentRequest request, final AgentResponse response) {
        try {

            final Value value=requireNonNull(request.input(this::decode), "null decoded value");

            value.validate().ifPresentOrElse(

                    trace -> {

                        response.status(UNPROCESSABLE_ENTITY);
                        response.header(CONTENT_TYPE, JSON);
                        response.output(output -> encode(output, trace));

                    },

                    () -> {

                        final URI id=resource.resolve(
                                requireNonNull(slug.apply(request), "null generated id")
                        );

                        if ( store.create(value.merge(object(id(id)))) > 0 ) {

                            response.status(CREATED);
                            response.header(LOCATION, id.toString());

                        } else {

                            response.status(CONFLICT);

                        }

                    }

            );

        } catch ( final ValueException e ) {

            response.status(UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        } catch ( final CodecException e ) { // !!! merge after isSyntactic() is removed

            response.status(e.isSyntactic() ? BAD_REQUEST : UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        }
    }

    private void update(final URI resource, final AgentRequest request, final AgentResponse response) {
        try {

            final Value value=requireNonNull(request.input(this::decode), "null decoded value");

            value.validate().ifPresentOrElse(

                    trace -> {

                        response.status(UNPROCESSABLE_ENTITY);
                        response.header(CONTENT_TYPE, JSON);
                        response.output(output -> encode(output, trace));

                    },

                    () -> {

                        if ( store.update(value.merge(object(id(resource)))) > 0 ) {

                            response.status(NO_CONTENT);

                        } else {

                            response.status(NOT_FOUND);

                        }

                    }

            );

        } catch ( final ValueException e ) {

            response.status(UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        } catch ( final CodecException e ) { // !!! merge after isSyntactic() is removed

            response.status(e.isSyntactic() ? BAD_REQUEST : UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        }
    }

    private void mutate(final URI resource, final AgentRequest request, final AgentResponse response) {
        try {

            final Value value=requireNonNull(request.input(this::decode), "null decoded value");

            value.validate().ifPresentOrElse( // !!! accept partial objects

                    trace -> {

                        response.status(UNPROCESSABLE_ENTITY);
                        response.header(CONTENT_TYPE, JSON);
                        response.output(output -> encode(output, trace));

                    },

                    () -> {

                        if ( store.mutate(value.merge(object(id(resource)))) > 0 ) {

                            response.status(NO_CONTENT);

                        } else {

                            response.status(NOT_FOUND);

                        }

                    }

            );

        } catch ( final ValueException e ) {

            response.status(UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        } catch ( final CodecException e ) { // !!! merge after isSyntactic() is removed

            response.status(e.isSyntactic() ? BAD_REQUEST : UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        }
    }

    private void delete(final URI resource, final AgentRequest request, final AgentResponse response) {
        if ( store.delete(object(id(resource), shape(model.shape().orElseGet(Shape::shape)))) > 0 ) {

            response.status(NO_CONTENT);

        } else {

            response.status(NOT_FOUND);

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void encode(final OutputStream output, final Value value) {
        encode(output, value, false);
    }

    private void encode(final OutputStream output, final Value value, final boolean jsonld) {
        try {

            codec.encode(output, value);

        } catch ( final IOException e ) {
            throw new UncheckedIOException(e);
        }
    }

    private Value decode(final InputStream input) {
        try {

            return codec.decode(input, model.shape().orElseGet(Shape::shape));

        } catch ( final IOException e ) {
            throw new UncheckedIOException(e);
        }
    }

}
