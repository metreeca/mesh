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

package com.metreeca.mesh.json;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.Codec;
import com.metreeca.mesh.tools.CodecException;
import com.metreeca.shim.URIs;

import java.io.IOException;
import java.net.URI;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * JSON-LD codec for value serialization and deserialization.
 *
 * <p>Provides bidirectional conversion between Java {@link Value} objects
 * and JSON-LD representations while preserving semantic meaning and
 * structural integrity. Supports multiple encoding formats including
 * JSON, URL-encoded JSON, and Base64-encoded JSON.</p>
 *
 * <p>The codec handles JSON-LD 1.1 features including context processing,
 * frame-based data shaping, and semantic type preservation. Configuration
 * options include pruning empty values, base URI resolution, and output
 * formatting.</p>
 *
 * @see <a href="https://www.w3.org/TR/json-ld11/">JSON-LD 1.1 Specification</a>
 */
public final class JSONCodec implements Codec {

    private final boolean prune;
    private final URI base;
    private final int indent;


    /**
     * Creates a JSON codec with default configuration.
     *
     * @return a new JSON codec with pruning disabled, default base URI, and no indentation
     */
    public static JSONCodec json() {
        return new JSONCodec(
                false,
                URIs.base(),
                0
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates a JSON codec with the specified configuration.
     *
     * @param prune  if {@code true}, ignore null values, empty arrays and empty objects during encoding
     * @param base   the base URI for resolving relative URIs in JSON-LD content
     * @param indent the number of spaces for indentation; 0 for compact output
     *
     * @throws NullPointerException     if {@code base} is {@code null}
     * @throws IllegalArgumentException if {@code base} is not absolute or {@code indent} is negative
     */
    public JSONCodec(
            final boolean prune,
            final URI base,
            final int indent
    ) {

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        if ( !base.isAbsolute() ) {
            throw new IllegalArgumentException(format("relative base <%s>", base));
        }

        if ( indent < 0 ) {
            throw new IllegalArgumentException(format("negative indent <%d>", indent));
        }

        this.prune=prune;
        this.base=base;
        this.indent=indent;
    }


    /**
     * Retrieves the current prune setting.
     *
     * @return {@code true} if null values, empty arrays and empty objects are ignored during encoding
     */
    public boolean prune() { return prune; }

    /**
     * Configures handling of null values, empty arrays and empty objects.
     *
     * @param prune if {@code true}, ignore null values, empty arrays and empty objects;  if {@code false}, handle them
     *
     * @return a new JSON codec with the new {@code prune} value
     */
    public JSONCodec prune(final boolean prune) {
        return new JSONCodec(
                prune,
                base,
                indent
        );
    }


    /**
     * Retrieves the current base URI.
     *
     * @return the base URI used for resolving relative URIs in JSON-LD content
     */
    public URI base() { return base; }

    /**
     * Configures the base URI for relative URI resolution.
     *
     * @param base the new base URI to resolve against the current base
     *
     * @return a new JSON codec with the updated base URI
     *
     * @throws NullPointerException if {@code base} is {@code null}
     */
    public JSONCodec base(final URI base) {

        if ( base == null ) {
            throw new NullPointerException("null base");
        }

        return new JSONCodec(
                !prune,
                this.base.resolve(base),
                indent
        );
    }


    /**
     * Retrieves the current indentation setting.
     *
     * @return the number of spaces used for indentation; 0 for compact output
     */
    public int indent() { return indent; }

    /**
     * Configures indentation for pretty-printing.
     *
     * @param indent if {@code true}, use 4-space indentation; if {@code false}, use compact output
     *
     * @return a new JSON codec with the updated indentation setting
     */
    public JSONCodec indent(final boolean indent) {
        return indent(indent ? 4 : 0);
    }

    /**
     * Configures indentation for pretty-printing.
     *
     * @param indent the number of spaces for indentation; 0 for compact output
     *
     * @return a new JSON codec with the updated indentation setting
     *
     * @throws IllegalArgumentException if {@code indent} is negative
     */
    public JSONCodec indent(final int indent) {

        if ( indent < 0 ) {
            throw new IllegalArgumentException(format("negative indent <%d>", indent));
        }

        return new JSONCodec(
                !prune,
                base,
                indent
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public <A extends Appendable> A encode(final A target, final Valuable value) throws CodecException, IOException {
        if ( target == null ) {
            throw new NullPointerException("null target");
        }

        new JSONEncoder(this, target).encode(requireNonNull(value.toValue(), "null supplied value"));

        return target;
    }

    @Override public <R extends Readable> Value decode(final R source, final Shape shape) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return new JSONDecoder(this, source).decode(shape);
    }

    @Override public <R extends Readable> Value decode(final R source) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        return new JSONDecoder(this, source).decode(null);
    }

}
