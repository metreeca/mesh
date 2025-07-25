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
import java.io.OutputStream;
import java.io.Serial;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URLDecoder;
import java.text.Normalizer.Form;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.pipe.AgentModel.expand;
import static com.metreeca.mesh.pipe.AgentModel.populate;
import static com.metreeca.mesh.pipe.AgentQuery.query;
import static com.metreeca.shim.Collections.entry;

import static java.lang.Float.parseFloat;
import static java.lang.String.format;
import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.text.Normalizer.normalize;
import static java.util.Locale.ROOT;
import static java.util.Objects.requireNonNull;
import static java.util.function.Predicate.not;
import static java.util.regex.Pattern.compile;


/**
 * REST API agent for handling HTTP-based CRUD operations.
 *
 * <p>Provides a complete REST API implementation that maps HTTP operations to store operations
 * with automatic content negotiation, validation, and error handling.</p>
 *
 * <h2>HTTP Method Mapping</h2>
 *
 * <p>HTTP methods are mapped to corresponding store operations:</p>
 *
 * <ul>
 *
 *   <li>{@code GET} → {@linkplain Store#retrieve(Valuable, List)} - Retrieves resources with optional
 *       filtering, sorting and aggregation via client-provided {@linkplain Query} specifications
 *       (enabled via {@linkplain #retrieve(Valuable)} or {@linkplain #retrieve(AgentProcessor)})</li>
 *
 *   <li>{@code POST} → {@linkplain Store#create(Valuable)} - Creates new resources with auto-generated or custom
 *      identifiers (enabled via {@linkplain #create(Valuable)} or {@linkplain #create(AgentProcessor)})</li>
 *
 *   <li>{@code PUT} → {@linkplain Store#update(Valuable)} - Completely replaces existing resources
 *       (enabled via {@linkplain #update(Valuable)} or {@linkplain #update(AgentProcessor)})</li>
 *
 *   <li>{@code PATCH} → {@linkplain Store#mutate(Valuable)} - Partially updates existing resources
 *       (enabled via {@linkplain #mutate(Valuable)} or {@linkplain #mutate(AgentProcessor)})</li>
 *
 *   <li>{@code DELETE} → {@linkplain Store#delete(Valuable)} - Removes existing resources
 *       (enabled via {@linkplain #delete(Valuable)} or {@linkplain #delete(AgentProcessor)})</li>
 *
 * </ul>
 *
 * <h2>Query String Processing</h2>
 *
 * <p>Query strings are automatically detected and processed using one of the following strategies:</p>
 * <ul>
 *   <li><strong>URL-encoded strings</strong> - Automatically decoded and recursively processed</li>
 *   <li><strong>Base64-encoded strings</strong> - Automatically decoded and recursively processed</li>
 *   <li><strong>Form data with operators</strong> - Converted to structured queries as documented in
 *       {@linkplain Query#query(String, Shape, URI)}</li>
 *   <li><strong>Plain strings</strong> - Processed directly by the configured codec</li>
 * </ul>
 *
 * <p>Form data queries are automatically converted to collection queries by identifying suitable
 * collection properties in the target shape (either properties with {@code rdfs:member} predicates
 * or {@linkplain Value#Object()}-typed properties with {@code maxCount > 1}). In the latter case,
 * a unique matching property is expected; an exception is thrown if multiple or no matching properties are found.</p>
 *
 * <h2>Content Negotiation</h2>
 *
 * <p>The agent supports content negotiation through standard HTTP headers:</p>
 * <ul>
 *   <li>{@code Accept} - Determines response format (JSON or JSON-LD)</li>
 *   <li>{@code Accept-Language} - Locale preferences for internationalized content</li>
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
    private static final int FORBIDDEN=403;
    private static final int NOT_FOUND=404;
    private static final int METHOD_NOT_ALLOWED=405;
    private static final int CONFLICT=409;
    private static final int UNSUPPORTED_MEDIA_TYPE=415;
    private static final int UNPROCESSABLE_ENTITY=422;

    private static final String ACCEPT="Accept";
    private static final String ACCEPT_LANGUAGE="Accept-Language";
    private static final String CONTENT_TYPE="Content-Type";
    private static final String ALLOW="Allow";
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
     * Creates a URL-friendly slug from the given text.
     *
     * <p>Normalises the input text by:</p>
     *
     * <ul>
     *   <li>URL decoding the input text</li>
     *   <li>Removing diacritical marks from Unicode characters</li>
     *   <li>Replacing non-letter/digit characters with spaces</li>
     *   <li>Converting space sequences to hyphens</li>
     *   <li>Converting to lowercase</li>
     * </ul>
     *
     * <p>Processing follows RFC 5023 section 9.7 guidelines for handling client-suggested URI components.</p>
     *
     * @param slug the text to convert to a slug
     *
     * @return an optional containing the generated slug, or empty if the input becomes empty after processing
     *
     * @throws NullPointerException if {@code slug} is {@code null}
     * @see <a href="https://datatracker.ietf.org/doc/html/rfc5023#section-9.7">RFC 5023 The Atom Publishing
     *         Protocol - § 9.7 - The Slug Header</a>
     */
    public static Optional<String> slug(final String slug) {

        if ( slug == null ) {
            throw new NullPointerException("null slug");
        }

        return Optional.of(slug)
                .map(s -> URLDecoder.decode(s, UTF_8))
                .map(String::trim)
                .filter(not(String::isEmpty))
                .map(s -> MARK_PATTERN.matcher(normalize(s, Form.NFD)).replaceAll(""))
                .map(s -> SPECIAL_PATTERN.matcher(s).replaceAll(" "))
                .map(s -> SPACES_PATTERN.matcher(s.trim()).replaceAll("-"))
                .map(s -> s.toLowerCase(ROOT));
    }


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

    private final Codec codec;
    private final Store store;

    private final BiConsumer<AgentRequest, AgentResponse> retriever;

    private final BiConsumer<AgentRequest, AgentResponse> creator;
    private final BiConsumer<AgentRequest, AgentResponse> updater;
    private final BiConsumer<AgentRequest, AgentResponse> mutator;
    private final BiConsumer<AgentRequest, AgentResponse> deleter;


    /**
     * Creates a basic agent with no HTTP methods enabled.
     *
     * <p>Use configuration methods to enable specific HTTP operations.</p>
     *
     * @param codec the codec for serialization and deserialization
     * @param store the persistence store for data operations
     *
     * @throws NullPointerException if either {@code codec} or {@code store} is {@code null}
     */
    public Agent(
            final Codec codec,
            final Store store
    ) {
        this(
                codec,
                store,
                null,
                null,
                null,
                null,
                null
        );
    }

    private Agent(

            final Codec codec,
            final Store store,

            final BiConsumer<AgentRequest, AgentResponse> retriever,

            final BiConsumer<AgentRequest, AgentResponse> creator,
            final BiConsumer<AgentRequest, AgentResponse> updater,
            final BiConsumer<AgentRequest, AgentResponse> mutator,
            final BiConsumer<AgentRequest, AgentResponse> deleter

    ) {

        if ( codec == null ) {
            throw new NullPointerException("null codec");
        }

        if ( store == null ) {
            throw new NullPointerException("null store");
        }

        this.codec=codec;
        this.store=store;

        this.retriever=retriever;

        this.creator=creator;
        this.updater=updater;
        this.mutator=mutator;
        this.deleter=deleter;
    }


    /**
     * Enables the {@code GET} HTTP method.
     *
     * <p>This convenience method creates a default processor that uses the model directly for resource retrieval
     * without custom processing. For advanced customization, see {@linkplain #retrieve(AgentProcessor)}.</p>
     *
     * @param model the data model used directly for resource retrieval
     *
     * @return a new agent instance with {@code GET} support enabled
     *
     * @throws NullPointerException if {@code model} is {@code null}
     * @see #retrieve(AgentProcessor)
     */
    public Agent retrieve(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        final Value value=model.toValue();

        return retrieve(new AgentProcessor() {

            @Override public Value decode(final URI id, final Function<Shape, Value> decoder) {
                return value;
            }

        });
    }

    /**
     * Enables the {@code GET} HTTP method.
     *
     * <p>This method provides full processor-based customization for model generation and response handling.
     * For simple cases without custom processing, see {@linkplain #retrieve(Valuable)}.</p>
     *
     * <p>The processor provides custom model and response handling:</p>
     *
     * <ul>
     *
     *   <li>{@link AgentProcessor#decode(URI, Function) decode()} - Model customisation
     *
     *      <ul>
     *        <li>The value returned by the decode function is used as the default model for resource retrieval</li>
     *        <li>Its shape drives decoding of client-provided specs from the query string</li>
     *        <li>The model is merged into client-provided specs or used to generate server-defined specs</li>
     *        <li>The model id is ignored</li>
     *        <li>The decoder argument is a dummy function that returns an object with the provided shape</li>
     *      </ul>
     *
     *   </li>
     *
     *   <li>{@link AgentProcessor#review(Value) review()} - Response customisation
     *
     *      <ul>
     *        <li>The method is executed outside any store transaction (read-only operation)</li>
     *        <li>Non-empty return values are returned to the client with a {@code 200 OK} status code</li>
     *        <li>Empty return values cause the request to be reported with a {@code 404 NOT FOUND} status code</li>
     *      </ul>
     *
     *   </li>
     *
     * </ul>
     *
     * <p>Method handling includes automatic query string validation and response generation:</p>
     *
     * <ul>
     *   <li>{@code 200 OK} - successful retrieval with resources found and returned</li>
     *   <li>{@code 404 NOT FOUND} - no matching resources found or processor review returns empty value</li>
     *   <li>{@code 400 BAD REQUEST} - malformed query string syntax</li>
     *   <li>{@code 422 UNPROCESSABLE ENTITY} - query parameters fail validation</li>
     * </ul>
     *
     * @param processor the value processor for custom model and response transformations
     *
     * @return a new agent instance with {@code GET} support enabled
     *
     * @throws NullPointerException if {@code processor} is {@code null}
     */
    public Agent retrieve(final AgentProcessor processor) {

        if ( processor == null ) {
            throw new NullPointerException("null processor");
        }

        return new Agent(
                codec,
                store,
                new Retriever(processor),
                creator,
                updater,
                mutator,
                deleter
        );
    }


    /**
     * Enables the {@code POST} HTTP method.
     *
     * <p>This convenience method creates a default processor that: (1) uses the model's shape to decode and
     * validate the client-provided JSON payload, (2) merges the decoded client data into the model, allowing the model
     * to provide default field values for the creation operation. For advanced customization, see
     * {@linkplain #create(AgentProcessor)}.</p>
     *
     * @param model the data model providing the shape for payload decoding and validation, and default values for
     *              resource creation
     *
     * @return a new agent instance with {@code POST} support enabled
     *
     * @throws NullPointerException if {@code model} is {@code null}
     * @see #create(AgentProcessor)
     */
    public Agent create(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        final Value value=model.toValue();
        final Shape shape=value.shape().orElseGet(Shape::shape);

        return create(new AgentProcessor() {

            @Override public Value decode(final URI id, final Function<Shape, Value> decoder) {
                return value.merge(decoder.apply(shape));
            }

        });
    }

    /**
     * Enables the {@code POST} HTTP method.
     *
     * <p>This method provides full processor-based customization for payload handling and validation logic.
     * For simple cases without custom processing, see {@linkplain #create(Valuable)}.</p>
     *
     * <p>The processor provides custom payload and validation handling:</p>
     *
     * <ul>
     *
     *   <li>{@link AgentProcessor#decode(URI, Function) decode()} - Payload customization
     *
     *      <ul>
     *        <li>The decoder argument decodes the client-provided request payload using the provided shape</li>
     *        <li>The returned value is passed to {@linkplain Store#create(Valuable)} for resource creation</li>
     *        <li>Its id is used as the id of the resource to be created</li>
     *        <li>If undefined, an id is assigned using the default slug-based process</li>
     *      </ul>
     *
     *   </li>
     *
     *   <li>{@link AgentProcessor#review(Value) review()} - Custom validation and post-processing
     *
     *      <ul>
     *        <li>The input value is the same payload passed to {@linkplain Store#create(Valuable)}</li>
     *        <li>The method is executed within the same store transaction as the main store operations,
     *            offering an opportunity for custom store updates and synchronization to other services</li>
     *        <li>Empty return values allow the operation to proceed with a {@code 201 CREATED} status code</li>
     *        <li>Non-empty return values are reported as a validation trace with a
     *            {@code 422 UNPROCESSABLE ENTITY} status code</li>
     *      </ul>
     *
     *   </li>
     *
     * </ul>
     *
     * <p>Custom processors may provide an id by including it in the value returned by {@code decode()}. If the
     * returned value doesn't contain a custom id, the default id generation strategy processes the {@code Slug}
     * header from incoming requests, falling back to a generated UUID if no valid slug is provided. Client-provided
     * slug values are sanitized using the {@linkplain #slug(String)} method.</p>
     *
     * <p>Method handling includes automatic payload validation and response generation:</p>
     *
     * <ul>
     *   <li>{@code 201 CREATED} - successful resource creation</li>
     *   <li>{@code 400 BAD REQUEST} - malformed request payload syntax</li>
     *   <li>{@code 409 CONFLICT} - resource with the same id already exists</li>
     *   <li>{@code 422 UNPROCESSABLE ENTITY} - payload fails shape validation or processor review
     *       returns a non-empty trace, with detailed validation trace in response body</li>
     * </ul>
     *
     * @param processor the value processor for custom payload and validation transformations
     *
     * @return a new agent instance with {@code POST} support enabled
     *
     * @throws NullPointerException if {@code processor} is {@code null}
     */
    public Agent create(final AgentProcessor processor) {

        if ( processor == null ) {
            throw new NullPointerException("null processor");
        }

        return new Agent(
                codec,
                store,
                retriever,
                new Creator(processor),
                updater,
                mutator,
                deleter
        );
    }


    /**
     * Enables the {@code PUT} HTTP method.
     *
     * <p>This convenience method creates a default processor that: (1) uses the model's shape to decode and
     * validate the client-provided JSON payload, (2) merges the decoded client data into the model, allowing the model
     * to provide default field values for the update operation. For advanced customization, see
     * {@linkplain #update(AgentProcessor)}.</p>
     *
     * @param model the data model providing the shape for payload decoding and validation, and default values for
     *              resource updating
     *
     * @return a new agent instance with {@code PUT} support enabled
     *
     * @throws NullPointerException if {@code model} is {@code null}
     * @see #update(AgentProcessor)
     */
    public Agent update(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        final Value value=model.toValue();
        final Shape shape=value.shape().orElseGet(Shape::shape);

        return update(new AgentProcessor() {

            @Override public Value decode(final URI id, final Function<Shape, Value> decoder) {
                return value.merge(decoder.apply(shape));
            }

        });
    }

    /**
     * Enables the {@code PUT} HTTP method.
     *
     * <p>This method provides full processor-based customization for payload handling and validation logic.
     * For simple cases without custom processing, see {@linkplain #update(Valuable)}.</p>
     *
     * <p>The processor provides custom payload and validation handling:</p>
     *
     * <ul>
     *
     *   <li>{@link AgentProcessor#decode(URI, Function) decode()} - Payload customization
     *
     *      <ul>
     *        <li>The decoder argument decodes the client-provided request payload using the provided shape</li>
     *        <li>The returned value is passed to {@linkplain Store#update(Valuable)} for resource update</li>
     *        <li>The id of the returned value is ignored</li>
     *      </ul>
     *
     *   </li>
     *
     *   <li>{@link AgentProcessor#review(Value) review()} - Custom validation and post-processing
     *
     *      <ul>
     *        <li>The input value is the same payload passed to {@linkplain Store#update(Valuable)}</li>
     *        <li>The method is executed within the same store transaction as the main store operations,
     *            offering an opportunity for custom store updates and synchronization to other services</li>
     *        <li>Empty return values allow the operation to proceed with a {@code 204 NO CONTENT} status code</li>
     *        <li>Non-empty return values are reported as a validation trace with a
     *            {@code 422 UNPROCESSABLE ENTITY} status code</li>
     *      </ul>
     *
     *   </li>
     *
     * </ul>
     *
     * <p>Method handling includes automatic payload validation and response generation:</p>
     *
     * <ul>
     *   <li>{@code 204 NO CONTENT} - successful resource update</li>
     *   <li>{@code 400 BAD REQUEST} - malformed request payload syntax</li>
     *   <li>{@code 404 NOT FOUND} - target resource does not exist</li>
     *   <li>{@code 422 UNPROCESSABLE ENTITY} - payload fails shape validation or processor review
     *       returns a non-empty trace, with detailed validation trace in response body</li>
     * </ul>
     *
     * @param processor the value processor for custom payload and validation transformations
     *
     * @return a new agent instance with {@code PUT} support enabled
     *
     * @throws NullPointerException if {@code processor} is {@code null}
     */
    public Agent update(final AgentProcessor processor) {

        if ( processor == null ) {
            throw new NullPointerException("null processor");
        }

        return new Agent(
                codec,
                store,
                retriever,
                creator,
                new Updater(processor),
                mutator,
                deleter
        );
    }


    /**
     * Enables the {@code PATCH} HTTP method.
     *
     * <p>This convenience method creates a default processor that: (1) uses the model's shape to decode and
     * validate the client-provided JSON payload, (2) merges the decoded client data into the model, allowing the model
     * to provide default field values for the mutation operation. For advanced customization, see
     * {@linkplain #mutate(AgentProcessor)}.</p>
     *
     * @param model the data model providing the shape for payload decoding and validation, and default values for
     *              resource mutation
     *
     * @return a new agent instance with {@code PATCH} support enabled
     *
     * @throws NullPointerException if {@code model} is {@code null}
     * @see #mutate(AgentProcessor)
     */
    public Agent mutate(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        final Value value=model.toValue();
        final Shape shape=value.shape().orElseGet(Shape::shape);

        return mutate(new AgentProcessor() {

            @Override public Value decode(final URI id, final Function<Shape, Value> decoder) {
                return value.merge(decoder.apply(shape));
            }

        });
    }

    /**
     * Enables the {@code PATCH} HTTP method.
     *
     * <p>This method provides full processor-based customization for payload handling and validation logic.
     * For simple cases without custom processing, see {@linkplain #mutate(Valuable)}.</p>
     *
     * <p>The processor provides custom payload and validation handling:</p>
     *
     * <ul>
     *
     *   <li>{@link AgentProcessor#decode(URI, Function) decode()} - Payload customization
     *
     *      <ul>
     *        <li>The decoder argument decodes the client-provided request payload using the provided shape</li>
     *        <li>The returned value is passed to {@linkplain Store#mutate(Valuable)} for resource mutation</li>
     *        <li>The id of the returned value is ignored</li>
     *      </ul>
     *
     *   </li>
     *
     *   <li>{@link AgentProcessor#review(Value) review()} - Custom validation and post-processing
     *
     *      <ul>
     *        <li>The input value is the same payload passed to {@linkplain Store#mutate(Valuable)}</li>
     *        <li>The method is executed within the same store transaction as the main store operations,
     *            offering an opportunity for custom store updates and synchronization to other services</li>
     *        <li>Empty return values allow the operation to proceed with a {@code 204 NO CONTENT} status code</li>
     *        <li>Non-empty return values are reported as a validation trace with a
     *            {@code 422 UNPROCESSABLE ENTITY} status code</li>
     *      </ul>
     *
     *   </li>
     *
     * </ul>
     *
     * <p>Method handling includes automatic payload validation and response generation:</p>
     *
     * <ul>
     *   <li>{@code 204 NO CONTENT} - successful resource mutation</li>
     *   <li>{@code 400 BAD REQUEST} - malformed request payload syntax</li>
     *   <li>{@code 404 NOT FOUND} - target resource does not exist</li>
     *   <li>{@code 422 UNPROCESSABLE ENTITY} - payload fails shape validation or processor review
     *       returns a non-empty trace, with detailed validation trace in response body</li>
     * </ul>
     *
     * @param processor the value processor for custom payload and validation transformations
     *
     * @return a new agent instance with {@code PATCH} support enabled
     *
     * @throws NullPointerException if {@code processor} is {@code null}
     */
    public Agent mutate(final AgentProcessor processor) {

        if ( processor == null ) {
            throw new NullPointerException("null processor");
        }

        return new Agent(
                codec,
                store,
                retriever,
                creator,
                updater,
                new Mutator(processor),
                deleter
        );
    }


    /**
     * Enables the {@code DELETE} HTTP method.
     *
     * <p>This convenience method creates a default processor that: (1) uses the model's shape to generate
     * a dummy object, (2) merges it into the model, allowing the model to provide default field values for the deletion
     * operation. For advanced customization, see {@linkplain #delete(AgentProcessor)}.</p>
     *
     * @param model the data model providing the shape for object generation and default values for deletion criteria
     *
     * @return a new agent instance with {@code DELETE} support enabled
     *
     * @throws NullPointerException if {@code model} is {@code null}
     * @see #delete(AgentProcessor)
     */
    public Agent delete(final Valuable model) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        final Value value=model.toValue();
        final Shape shape=value.shape().orElseGet(Shape::shape);

        return delete(new AgentProcessor() {

            @Override public Value decode(final URI id, final Function<Shape, Value> decoder) {
                return value.merge(decoder.apply(shape));
            }

        });
    }

    /**
     * Enables the {@code DELETE} HTTP method.
     *
     * <p>This method provides full processor-based customization for model generation and validation logic.
     * For simple cases without custom processing, see {@linkplain #delete(Valuable)}.</p>
     *
     * <p>The processor provides custom model and validation handling:</p>
     *
     * <ul>
     *
     *   <li>{@link AgentProcessor#decode(URI, Function) decode()} - Model customization
     *
     *      <ul>
     *        <li>The returned value is passed to {@linkplain Store#delete(Valuable)} for resource deletion</li>
     *        <li>The model id is ignored</li>
     *        <li>The decoder argument is a dummy function that returns an object with the provided shape</li>
     *      </ul>
     *
     *   </li>
     *
     *   <li>{@link AgentProcessor#review(Value) review()} - Custom validation and post-processing
     *
     *      <ul>
     *        <li>The input value is the same model passed to {@linkplain Store#delete(Valuable)}</li>
     *        <li>The method is executed within the same store transaction as the main store operations,
     *            offering an opportunity for custom store updates and synchronization to other services</li>
     *        <li>Empty return values allow the operation to proceed with a {@code 204 NO CONTENT} status code</li>
     *        <li>Non-empty return values are reported as a validation trace with a
     *            {@code 409 CONFLICT} status code</li>
     *      </ul>
     *
     *   </li>
     *
     * </ul>
     *
     * <p>Method handling includes automatic model validation and response generation:</p>
     *
     * <ul>
     *   <li>{@code 204 NO CONTENT} - successful resource deletion</li>
     *   <li>{@code 404 NOT FOUND} - target resource does not exist</li>
     *   <li>{@code 409 CONFLICT} - processor review returns a non-empty trace,
     *       with detailed validation trace in response body</li>
     * </ul>
     *
     * @param processor the value processor for custom model and validation transformations
     *
     * @return a new agent instance with {@code DELETE} support enabled
     *
     * @throws NullPointerException if {@code processor} is {@code null}
     */
    public Agent delete(final AgentProcessor processor) {

        if ( processor == null ) {
            throw new NullPointerException("null processor");
        }

        return new Agent(
                codec,
                store,
                retriever,
                creator,
                updater,
                mutator,
                new Deleter(processor)
        );
    }


    /**
     * Processes an HTTP request and generates an appropriate response.
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

            // !!! HEAD

            if ( method.equals(GET) && retriever != null ) {

                retriever.accept(request, response);

            } else if ( method.equals(POST) && creator != null ) {

                creator.accept(request, response);

            } else if ( method.equals(PUT) && updater != null ) {

                updater.accept(request, response);

            } else if ( method.equals(PATCH) && mutator != null ) {

                mutator.accept(request, response);

            } else if ( method.equals(DELETE) && deleter != null ) {

                deleter.accept(request, response);

            } else {

                Optional.of(Map
                                .ofEntries(
                                        entry(GET, retriever != null),
                                        entry(POST, creator != null),
                                        entry(PUT, updater != null),
                                        entry(PATCH, mutator != null),
                                        entry(DELETE, deleter != null)
                                )
                                .entrySet()
                                .stream()
                                .filter(Entry::getValue)
                                .map(Entry::getKey)
                                .toList()
                        )
                        .filter(not(List::isEmpty))
                        .ifPresentOrElse(

                                methods -> {
                                    response.status(METHOD_NOT_ALLOWED);
                                    response.header(ALLOW, join(", ", methods));
                                },

                                () -> response.status(FORBIDDEN)

                        );

            }

        }

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Value decode(final AgentRequest request, final Shape shape) {
        return request.input(input -> {

            try {

                return codec.decode(input, shape);

            } catch ( final IOException e ) {
                throw new UncheckedIOException(e);
            }

        });
    }

    private void encode(final OutputStream output, final Value value, final boolean jsonld) {
        try {

            codec.encode(output, value);

        } catch ( final IOException e ) {
            throw new UncheckedIOException(e);
        }
    }


    private void execute(final AgentResponse response, final Consumer<Store> task) {
        try {

            store.execute(task);

        } catch ( final ValidationException e ) {
            response.status(e.getStatus());
            response.header(CONTENT_TYPE, JSON);
            response.output(output -> encode(output, e.getTrace(), false));
        }
    }


    private void report(final AgentResponse response, final RuntimeException e) {
        response.status(UNPROCESSABLE_ENTITY);
        response.header(CONTENT_TYPE, JSON);
        response.output(output -> encode(output, string(e.getMessage()), false));
    }

    private void report(final AgentResponse response, final CodecException e) { // !!! review after isSyntactic()
        response.status(e.isSyntactic() ? BAD_REQUEST : UNPROCESSABLE_ENTITY);
        response.header(CONTENT_TYPE, JSON);
        response.output(output -> encode(output, string(e.getMessage()), false));
    }

    private void report(final AgentResponse response, final Value trace) {
        response.status(UNPROCESSABLE_ENTITY);
        response.header(CONTENT_TYPE, JSON);
        response.output(output -> encode(output, trace, false));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final class Retriever implements BiConsumer<AgentRequest, AgentResponse> {

        private final AgentProcessor processor;

        private Retriever(final AgentProcessor processor) {
            this.processor=processor;
        }

        @Override
        public void accept(final AgentRequest request, final AgentResponse response) {
            try {

                final URI resource=request.resource();

                final boolean jsonld=Optional.ofNullable(request.header(ACCEPT)).stream()
                        .flatMap(accept -> values(accept, MIME_PATTERN))
                        .anyMatch(mime -> mime.equals(JSONLD));

                final Value model=processor.decode(resource, shape -> object(shape(shape))).toValue();

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
                                (specs.isEmpty()
                                        ? expand(model)
                                        : populate(specs, model)
                                ).merge(object(id(resource))),
                                locales
                        )

                        .value()

                        .ifPresentOrElse( // !!! validate value

                                value -> processor.review(value).toValue().value().ifPresentOrElse(

                                        payload -> {

                                            response.status(OK);
                                            response.header(CONTENT_TYPE, jsonld ? JSONLD : JSON);

                                            response.output(output -> encode(output, payload, jsonld));

                                        },

                                        () -> response.status(NOT_FOUND)

                                ),

                                () -> response.status(NOT_FOUND)

                        );

            } catch ( final NoSuchElementException|ValueException e ) {

                report(response, e);

            } catch ( final CodecException e ) {

                report(response, e);

            }
        }

    }


    private final class Creator implements BiConsumer<AgentRequest, AgentResponse> {

        private final AgentProcessor processor;

        private Creator(final AgentProcessor processor) {
            this.processor=processor;
        }

        @Override
        public void accept(final AgentRequest request, final AgentResponse response) {
            try {

                final URI resource=request.resource();

                final Value payload=processor.decode(resource, shape -> decode(request, shape)).toValue();

                final Value value=payload.merge(object(id(payload.id().orElseGet(() -> resource.resolve(Optional
                        .ofNullable(request.header(SLUG))
                        .flatMap(Agent::slug)
                        .orElseGet(URIs::uuid)
                )))));

                value.validate().ifPresentOrElse(

                        trace -> report(response, trace),

                        () -> execute(response, store -> {

                            if ( store.create(value) > 0 ) {

                                processor.review(value).toValue().value().ifPresentOrElse(

                                        trace -> {

                                            throw new ValidationException(UNPROCESSABLE_ENTITY, trace);

                                        },

                                        () -> {

                                            response.status(CREATED);
                                            response.header(LOCATION, value.id().orElseGet(URIs::uri).getPath());

                                        }

                                );

                            } else {

                                response.status(CONFLICT);

                            }

                        })

                );

            } catch ( final ValueException e ) {

                report(response, e);

            } catch ( final CodecException e ) {

                report(response, e);

            }
        }

    }

    private final class Updater implements BiConsumer<AgentRequest, AgentResponse> {

        private final AgentProcessor processor;

        private Updater(final AgentProcessor processor) {
            this.processor=processor;
        }

        @Override
        public void accept(final AgentRequest request, final AgentResponse response) {
            try {

                final URI resource=request.resource();

                final Value payload=processor.decode(resource, shape -> decode(request, shape)).toValue();
                final Value value=payload.merge(object(id(resource)));

                value.validate().ifPresentOrElse(

                        trace -> report(response, trace),

                        () -> execute(response, store -> {

                            if ( store.update(value) > 0 ) {

                                processor.review(value).toValue().value().ifPresentOrElse(

                                        trace -> { throw new ValidationException(UNPROCESSABLE_ENTITY, trace); },

                                        () -> response.status(NO_CONTENT)

                                );

                            } else {

                                response.status(NOT_FOUND);

                            }

                        })

                );

            } catch ( final ValueException e ) {

                report(response, e);

            } catch ( final CodecException e ) {

                report(response, e);

            }
        }

    }

    private final class Mutator implements BiConsumer<AgentRequest, AgentResponse> {

        private final AgentProcessor processor;

        private Mutator(final AgentProcessor processor) {
            this.processor=processor;
        }

        @Override
        public void accept(final AgentRequest request, final AgentResponse response) {
            try {

                final URI resource=request.resource();

                final Value payload=processor.decode(resource, shape -> decode(request, shape)).toValue();
                final Value value=payload.merge(object(id(resource)));

                value.validate().ifPresentOrElse( // !!! accept partial objects

                        trace -> report(response, trace),

                        () -> execute(response, store -> {

                            if ( store.mutate(value) > 0 ) {

                                processor.review(value).toValue().value().ifPresentOrElse(

                                        trace -> { throw new ValidationException(UNPROCESSABLE_ENTITY, trace); },

                                        () -> response.status(NO_CONTENT)

                                );

                            } else {

                                response.status(NOT_FOUND);

                            }

                        })

                );

            } catch ( final ValueException e ) {

                report(response, e);

            } catch ( final CodecException e ) {

                report(response, e);

            }
        }

    }

    private final class Deleter implements BiConsumer<AgentRequest, AgentResponse> {

        private final AgentProcessor processor;

        private Deleter(final AgentProcessor processor) {
            this.processor=processor;
        }

        @Override
        public void accept(final AgentRequest request, final AgentResponse response) {

            final URI resource=request.resource();

            final Value payload=processor.decode(resource, shape -> object(shape(shape))).toValue();
            final Value value=payload.merge(object(id(resource)));

            execute(response, store -> {

                if ( store.delete(value) > 0 ) {

                    processor.review(value).toValue().value().ifPresentOrElse(

                            trace -> { throw new ValidationException(CONFLICT, trace); },

                            () -> response.status(NO_CONTENT)

                    );

                } else {

                    response.status(NOT_FOUND);

                }

            });
        }

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class ValidationException extends RuntimeException {

        @Serial
        private static final long serialVersionUID=-5781370851139341872L;


        private final int status;
        private final Value trace;

        private ValidationException(final int status, final Value trace) {
            this.status=status;
            this.trace=trace;
        }


        private int getStatus() {
            return status;
        }

        private Value getTrace() {
            return trace;
        }

    }

}
