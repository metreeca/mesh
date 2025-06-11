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

package com.metreeca.mesh.tools;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;

import java.io.*;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Data serialization and deserialization interface.
 *
 * <p>Provides methods for encoding and decoding values to and from various
 * data formats with shape-based validation and transformation support.</p>
 */
public interface Codec {

    /**
     * Encodes a value to a string representation.
     *
     * @param value the value to encode
     *
     * @return the string representation of the value
     *
     * @throws CodecException if encoding fails
     */
    public default String encode(final Valuable value) throws CodecException {
        try ( final StringWriter writer=new StringWriter() ) {

            return encode(writer, value).toString();

        } catch ( final IOException unexpected ) {
            throw new UncheckedIOException(unexpected);
        }
    }

    /**
     * Decodes a string to a value using the provided shape for validation.
     *
     * @param source the string to decode
     * @param shape  the shape for validation and transformation
     *
     * @return the decoded value
     *
     * @throws NullPointerException if either parameter is {@code null}
     * @throws CodecException       if decoding fails
     */
    public default Value decode(final String source, final Shape shape) throws CodecException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        try ( final StringReader reader=new StringReader(source) ) {

            return decode(reader, shape);

        } catch ( final IOException unexpected ) {
            throw new UncheckedIOException(unexpected);
        }
    }

    /**
     * Decodes a string to a value without shape validation.
     *
     * @param source the string to decode
     *
     * @return the decoded value
     *
     * @throws NullPointerException if {@code source} is {@code null}
     * @throws CodecException       if decoding fails
     */
    public default Value decode(final String source) throws CodecException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        try ( final StringReader reader=new StringReader(source) ) {

            return decode(reader);

        } catch ( final IOException unexpected ) {
            throw new UncheckedIOException(unexpected);
        }
    }


    default <A extends Appendable> A encode(final A target, final Valuable value) throws CodecException, IOException {

        if ( target == null ) {
            throw new NullPointerException("null target");
        }

        throw new UnsupportedOperationException("encoding binary format to textual output");
    }

    default <R extends Readable> Value decode(final R source, final Shape shape) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        throw new UnsupportedOperationException("decoding binary format from textual input");
    }

    default <R extends Readable> Value decode(final R source) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        throw new UnsupportedOperationException("decoding binary format from textual input");
    }


    default <O extends OutputStream> O encode(final O target, final Valuable value) throws CodecException, IOException {

        if ( target == null ) {
            throw new NullPointerException("null target");
        }

        try ( final Writer writer=new OutputStreamWriter(target, UTF_8) ) {

            encode(writer, value);

            return target;
        }
    }

    default <I extends InputStream> Value decode(final I source, final Shape shape) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        try ( final Reader reader=new InputStreamReader(source, UTF_8) ) {
            return decode(reader, shape);
        }
    }

    default <I extends InputStream> Value decode(final I source) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        try ( final Reader reader=new InputStreamReader(source, UTF_8) ) {
            return decode(reader);
        }
    }

}
