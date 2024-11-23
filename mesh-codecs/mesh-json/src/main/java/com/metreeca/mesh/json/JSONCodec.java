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

package com.metreeca.mesh.json;

import com.metreeca.mesh.Valuable;
import com.metreeca.mesh.Value;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.tools.Codec;
import com.metreeca.mesh.tools.CodecException;
import com.metreeca.shim.URIs;

import java.io.*;
import java.net.URI;
import java.net.URLDecoder;
import java.util.Base64;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

/**
 * JSON codec.
 */
public final class JSONCodec implements Codec {


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final boolean prune;
    private final URI base;
    private final int indent;


    public static JSONCodec json() {
        return new JSONCodec(
                false,
                URIs.base(),
                0
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

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


    public URI base() { return base; }

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


    public int indent() { return indent; }

    public JSONCodec indent(final boolean indent) {
        return indent(indent ? 4 : 0);
    }

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

    @Override public Value decode(final String source, final Shape shape) throws CodecException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        if ( source.startsWith("{") ) { // JSON

            try ( final StringReader reader=new StringReader(source) ) {

                return decode(reader, shape);

            } catch ( final IOException unexpected ) {
                throw new UncheckedIOException(unexpected);
            }

        } else if ( source.startsWith("%7B") ) { // URLEncoded JSON

            return decode(URLDecoder.decode(source, UTF_8), shape);

        } else if ( source.startsWith("e3") ) { // Base64 JSON

            return decode(new String(Base64.getDecoder().decode(source), UTF_8), shape);

        } else { // query parameters

            return Codec.query(source, shape);

        }

    }


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

        return new JSONDecoder(this, unwrap(source)).decode(shape);
    }

    @Override public <R extends Readable> Value decode(final R source) throws CodecException, IOException {

        if ( source == null ) {
            throw new NullPointerException("null source");
        }

        return new JSONDecoder(this, unwrap(source)).decode(null);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private <R extends Readable> Readable unwrap(final R source) throws IOException {

        if ( source instanceof final Reader reader && reader.markSupported() ) {

            final char[] buffer=new char[3];

            reader.mark(buffer.length);
            reader.read(buffer);
            reader.reset();

            final String head=new String(buffer);

            if ( head.startsWith("{") ) { // JSON

                return source;

            } else if ( head.startsWith("%7B") ) { // URLEncoded JSON

                return new StringReader(URLDecoder.decode(read(reader), UTF_8));

            } else if ( head.startsWith("e3") ) { // Base64 JSON

                return new StringReader(new String(Base64.getDecoder().decode(read(reader)), UTF_8));

            } else { // query parameters

                throw new UnsupportedOperationException(";( be implemented"); // !!!

            }

        } else {

            return source;

        }
    }

    private static String read(final Reader reader) throws IOException {

        final StringWriter writer=new StringWriter();

        reader.transferTo(writer);

        return writer.toString();
    }


}
