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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.shim.Locales;
import com.metreeca.shim.URIs;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;

import static com.metreeca.mesh.Value.Text;
import static com.metreeca.mesh.Value.ThrowingVisitor;
import static com.metreeca.shim.Locales.ANY;

import static java.util.Comparator.comparing;
import static java.util.Locale.ROOT;
import static java.util.stream.Collectors.*;

/**
 * JSON-LD encoder for converting Value objects to JSON.
 *
 * <p>Serializes {@linkplain Value} objects to JSON format following JSON-LD semantics and conventions.
 * Handles the complete range of Value types including primitives, collections, objects, and specialized constructs
 * like localized text and tabular data. The encoder supports shape-guided serialization with property filtering
 * and format optimization.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Shape-guided serialization</strong> - Uses SHACL shapes for property filtering and optimization</li>
 *   <li><strong>Localized text handling</strong> - Groups language-tagged strings by locale</li>
 *   <li><strong>Tabular data support</strong> - Serializes Table structures as JSON arrays</li>
 *   <li><strong>URI relativization</strong> - Converts absolute URIs to relative forms based on base URI</li>
 *   <li><strong>Content pruning</strong> - Optionally excludes empty values for compact output</li>
 * </ul>
 */
final class JSONEncoder {

    private final boolean prune;
    private final URI base;

    private final JSONWriter writer;


    JSONEncoder(final JSONCodec codec, final Appendable target) {

        this.prune=codec.prune();
        this.base=codec.base();

        this.writer=new JSONWriter(codec, target);
    }


    void encode(final Value value) throws IOException {
        value(value);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private void value(final Value value) throws IOException {
        value.accept(new ThrowingVisitor<Void, IOException>() {

            @Override public Void visit(final Value host, final Void nil) throws IOException {
                return literal(host.encode(base));
            }

            @Override public Void visit(final Value host, final Boolean bit) throws IOException {
                return literal(host.encode(base));
            }

            @Override public Void visit(final Value host, final Number number) throws IOException {
                return literal(host.encode(base));
            }

            @Override public Void visit(final Value host, final String string) throws IOException {
                return string(string);
            }

            @Override public Void visit(final Value host, final Locale locale, final String string) throws IOException {
                return text(string, locale);
            }

            @Override public Void visit(final Value host, final URI datatype, final String string) throws IOException {
                return data(string, datatype);
            }

            @Override public Void visit(final Value host, final List<Value> values) throws IOException {
                return array(values);
            }

            @Override public Void visit(final Value host, final Map<String, Value> fields) throws IOException {
                return object(fields, host);
            }

            @Override public Void visit(final Value host, final Object object) throws IOException {
                return object instanceof final Table table ? table(table)
                        : string(host.encode(base));
            }

        });
    }


    private Void literal(final String value) throws IOException {

        writer.literal(value);

        return null;
    }

    private Void string(final String value) throws IOException {

        writer.string(value);

        return null;
    }


    private Void text(final String text, final Locale locale) throws IOException {

        writer.object(true);

        writer.string(Value.VALUE);
        writer.colon();
        writer.string(text);

        writer.comma();

        writer.string(Value.LANGUAGE);
        writer.colon();
        writer.string(Locales.locale(locale));

        writer.object(false);

        return null;
    }

    private Void data(final String text, final URI datatype) throws IOException {

        writer.object(true);

        writer.string(Value.VALUE);
        writer.colon();
        writer.string(text);

        writer.comma();

        writer.string(Value.TYPE);
        writer.colon();
        writer.string(datatype.toString());

        writer.object(false);

        return null;
    }


    private Void array(final Iterable<Value> values) throws IOException {

        writer.array(true);

        for (final Value value : values) {

            writer.comma();
            value(value);

        }

        writer.array(false);

        return null;
    }

    private Void object(final Map<String, Value> fields, final Value host) throws IOException {

        final Optional<URI> id=host.id();
        final Optional<String> type=host.type();
        final Optional<Shape> shape=host.shape();

        writer.object(true);

        if ( id.isPresent() ) {
            writer.string(shape.flatMap(Shape::id).orElse(Value.ID));
            writer.colon();
            writer.string(URIs.relative(base, id.get()).toString());
        }

        if ( type.isPresent() ) {
            writer.string(shape.flatMap(Shape::type).orElse(Value.TYPE));
            writer.colon();
            writer.string(type.get());
        }

        // !!! @context

        for (final Entry<String, Value> entry : fields.entrySet()) {

            final String name=entry.getKey();
            final Value value=entry.getValue();

            if ( !name.startsWith("@") && shape.map(s -> s.property(name).isPresent()).orElse(true) ) {

                if ( !prune || !value.isEmpty() ) {

                    writer.comma();
                    writer.string(name);
                    writer.colon();

                    final Optional<Shape> p=shape.flatMap(s -> s.property(name)).map(Property::shape);

                    if ( p.map(s -> s.is(Text())).orElse(false) ) {
                        locals(value, p.map(Shape::uniqueLang).orElse(false));
                    } else {
                        value(value);
                    }

                }

            }

        }

        writer.object(false);

        return null;
    }


    private Void table(final Table table) throws IOException {

        writer.array(true);

        for (final Tuple row : table.rows()) {

            writer.comma();
            writer.object(true);

            for (final Entry<String, Value> field : row.fields()) {
                writer.comma();
                writer.string(field.getKey());
                writer.colon();
                value(field.getValue());
            }

            writer.object(false);

        }

        writer.array(false);

        return null;
    }

    private Void locals(final Value value, final boolean unique) throws IOException {
        return value.accept(new ThrowingVisitor<Void, IOException>() {

            @Override public Void visit(final Value host, final Locale locale, final String string) throws IOException {

                writer.object(true);

                if ( !locale.equals(ANY) ) {
                    writer.string(Locales.locale(locale));
                    writer.colon();
                    writer.string(string);
                }

                writer.object(false);

                return null;
            }

            @Override public Void visit(final Value host, final List<Value> values) throws IOException {

                writer.object(true);

                final Map<Locale, List<String>> groups=values.stream()

                        .filter(v -> v.string().isPresent() || v.text().isPresent())

                        .collect(groupingBy(

                                v -> v.accept(new ThrowingVisitor<>() {

                                    @Override public Locale visit(final Value host, final String string) {
                                        return ROOT;
                                    }

                                    @Override public Locale visit(final Value host, final Locale locale, final String string) { return locale; }

                                }),

                                mapping(v -> v.accept(new ThrowingVisitor<>() {

                                    @Override public String visit(final Value host, final String string) {
                                        return string;
                                    }

                                    @Override public String visit(final Value host, final Locale locale, final String string) { return string; }

                                }), filtering(Objects::nonNull, toList()))

                        ));

                for (final Locale locale : groups.keySet().stream().sorted(comparing(Locales::locale)).toList()) {
                    if ( !locale.equals(ANY) ) {

                        writer.comma();
                        writer.string(Locales.locale(locale));
                        writer.colon();

                        if ( unique ) {

                            writer.string(groups.get(locale).getFirst());

                        } else {

                            writer.array(true);

                            for (final String text : groups.get(locale)) {
                                writer.comma();
                                writer.string(text);
                            }

                            writer.array(false);

                        }

                    }
                }

                writer.object(false);

                return null;
            }

            @Override public Void visit(final Value host, final Object object) throws IOException {

                // ignore non-textual data

                writer.object(true);
                writer.object(false);

                return null;
            }

        });
    }

}
