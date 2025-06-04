/*
 * Copyright © 2025 Metreeca srl
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

package com.metreeca.mesh.tools;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.shim.Locales;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.tools.AgentModel.expand;
import static com.metreeca.mesh.tools.AgentModel.populate;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.URIs.uri;
import static com.metreeca.shim.URIs.uuid;

import static java.lang.Float.parseFloat;
import static java.lang.String.format;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;

public final class Agent {

    private static final String GET="GET";
    private static final String PUT="PUT";
    private static final String UPDATE="UPDATE";
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

    private static final String JSON="application/json";
    private static final String JSONLD="application/ld+json";


    private static final Pattern QUALITY_PATTERN=compile("(?:\\s*;\\s*q\\s*=\\s*(\\d*(?:\\.\\d+)?))?");
    private static final Pattern LANG_PATTERN=compile("([a-zA-Z]{1,8}(?:-[a-zA-Z0-9]{1,8})*|\\*)"+QUALITY_PATTERN);
    private static final Pattern MIME_PATTERN=compile("((?:[-+\\w]+|\\*)/(?:[-+\\w]+|\\*))"+QUALITY_PATTERN);


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


    public Agent(final Value model, final Codec codec, final Store store) {
        this(model, codec, store, request -> uuid());
    }

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
                case PUT -> create(resource, request, response);
                case UPDATE -> update(resource, request, response);
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
                    .map(query -> codec.decode(query, model.shape().orElseGet(Shape::shape)))
                    .orElseGet(() -> object());

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

        } catch ( final NoSuchElementException e ) {

            response.status(UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        } catch ( final CodecException e ) {

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

        } catch ( final CodecException e ) {

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

        } catch ( final CodecException e ) {

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

        } catch ( final CodecException e ) {

            response.status(e.isSyntactic() ? BAD_REQUEST : UNPROCESSABLE_ENTITY);
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, string(e.getMessage())));

        }
    }

    private void delete(final URI resource, final AgentRequest request, final AgentResponse response) {
        if ( store.delete(model.merge(object(id(resource)))) > 0 ) {

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
