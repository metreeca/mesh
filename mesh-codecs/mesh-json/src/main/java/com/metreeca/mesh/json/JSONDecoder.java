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
import com.metreeca.mesh.queries.*;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.shim.Locales;
import com.metreeca.shim.URIs;

import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.json.JSONEvent.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Probe.probe;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Collections.set;

import static java.lang.Integer.parseInt;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.String.format;

/**
 * JSON-LD decoder for converting JSON to Value objects.
 *
 * <p>Processes JSON input streams and constructs {@linkplain Value} objects following JSON-LD semantics.
 * Handles the complete JSON-LD grammar including objects, arrays, literals, query expressions, and computed probes.
 * The decoder supports shape-guided validation and type coercion based on SHACL constraints.</p>
 *
 * <p>Key features:</p>
 * <ul>
 *   <li><strong>Shape-guided parsing</strong> - Uses SHACL shapes for type validation and conversion</li>
 *   <li><strong>Query syntax support</strong> - Parses query objects with criteria, pagination, and ordering</li>
 *   <li><strong>Computed probes</strong> - Handles tabular data specifications with computed expressions</li>
 *   <li><strong>Localized text</strong> - Processes language-tagged strings and locale-specific content</li>
 *   <li><strong>URI resolution</strong> - Resolves relative URIs against the specified base URI</li>
 * </ul>
 */
final class JSONDecoder {

    private static final int MAX_LIMIT=100;


    private static final Set<String> CLASSED=set(TYPE);
    private static final Set<String> TYPED=set(VALUE, TYPE);
    private static final Set<String> TAGGED=set(VALUE, LANGUAGE);

    private static final Value.Visitor<Object> NULL_VISITOR=new Value.Visitor<>() {

        @Override public Object visit(final Value host, final Void nil) { return Void.class; }

    };


    private static boolean isModel(final Value value) {
        return value != null && !(value instanceof Collection<?>) && value.value(Query.class).isEmpty();
    }


    private static Object equal(final String label) {
        return label.replace("\\=", "\\\\").indexOf('=');
    }

    private static String name(final String label, final int equal) {
        return label.substring(0, equal).replace("\\=", "=");
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final boolean prune;
    private final URI base;
    private final JSONReader reader;


    JSONDecoder(final JSONCodec codec, final Readable source) {
        this.prune=codec.prune();
        this.base=codec.base();
        this.reader=new JSONReader(codec, source);
    }


    Value decode(final Shape shape) throws IOException {

        final Value value=value(shape, false);

        reader.token(EOF);

        return value;

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Value value(final Shape shape, final boolean array) throws IOException {
        return switch ( reader.event() ) {

            case NULL -> nil(reader.token((Void)null));
            case TRUE -> bit(reader.token(true));
            case FALSE -> bit(reader.token(false));
            case NUMBER -> number(reader.token(), shape);
            case STRING -> string(reader.token(), shape);

            case LBRACKET -> array(shape);
            case LBRACE -> object(shape, array);

            default -> reader.semantics("unexpected %s", reader.event().description());

        };
    }


    private Value number(final String token, final Shape shape) {
        return Optional.ofNullable(shape)
                .flatMap(Shape::datatype)
                .orElseGet(Value::Number)
                .decode(token, base)
                .orElseThrow(() -> malformed(Number(), token));
    }

    private Value string(final String token, final Shape shape) {

        final Value datatype=Optional.ofNullable(shape)
                .flatMap(Shape::datatype)
                .orElseGet(Value::String);

        return datatype.equals(Object()) ? Value.object(id(URIs.absolute(base, URI.create(token))), shape(shape))
                : datatype.equals(URI()) && token.isEmpty() ? Value.uri(URI.create(""))
                : datatype.decode(token, base).orElseThrow(() -> malformed(datatype, token));

    }

    private Value array(final Shape shape) throws IOException {

        final List<Value> values=new ArrayList<>();

        reader.token(LBRACKET);

        while ( reader.event() != RBRACKET ) {

            reader.token(COMMA);
            values.add(value(shape, true));

        }

        reader.token(RBRACKET);

        if ( values.stream().anyMatch(v -> v.value(Query.class).isPresent()) ) {

            if ( values.size() > 1 ) {

                return reader.semantics("multiple query objects");

            } else {

                return values.getFirst();

            }

        } else if ( values.stream().anyMatch(v -> v.value(Specs.class).isPresent()) ) {

            if ( values.size() > 1 ) {

                return reader.semantics("multiple specs objects");

            } else {

                return Value.value(Query.query(values.getFirst()));

            }

        } else if ( shape != null && values.stream().anyMatch(v -> v.accept(NULL_VISITOR) != null) ) {

            return reader.semantics("unexpected null value");

        } else if ( shape != null && values.stream().anyMatch(v -> v.array().isPresent()) ) {

            return reader.semantics("unexpected nested array");

        } else {

            return Value.array(values);

        }
    }

    private Value object(final Shape shape, final boolean array) throws IOException {
        if ( shape != null && shape.is(Text()) ) { return texts(); } else {

            int offset=-1;
            int limit=-1;

            final Map<String, String> keywords=new LinkedHashMap<>();
            final Map<Expression, Criterion> criteria=new LinkedHashMap<>(); // preserve ordering priorities

            final List<Entry<String, Value>> fields=new ArrayList<>(shape != null ? list(shape(shape)) : list());
            final List<Probe> probes=new ArrayList<>();

            reader.token(LBRACE);

            while ( reader.event() != RBRACE ) {

                reader.token(COMMA);

                final String label=reader.token(STRING);

                reader.token(COLON);

                try {

                    if ( label.startsWith("<=") ) { // before <

                        final Expression expression=expression(label.substring(2));
                        final Value value=value(expression.apply(shape != null ? shape : Shape.shape()));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().lte(value)
                                        : criterion.lte().isEmpty() ? criterion.lte(value)
                                        : reader.semantics("multiple <lte> (<=) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith(">=") ) { // before >

                        final Expression expression=expression(label.substring(2));
                        final Value value=value(expression.apply(shape != null ? shape : Shape.shape()));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().gte(value)
                                        : criterion.gte().isEmpty() ? criterion.gte(value)
                                        : reader.semantics("multiple <gte> (>=) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith("<") ) {

                        final Expression expression=expression(label.substring(1));
                        final Value value=value(expression.apply(shape != null ? shape : Shape.shape()));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().lt(value)
                                        : criterion.lt().isEmpty() ? criterion.lt(value)
                                        : reader.semantics("multiple <lt> (<) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith(">") ) {

                        final Expression expression=expression(label.substring(1));
                        final Value value=value(expression.apply(shape != null ? shape : Shape.shape()));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().gt(value)
                                        : criterion.gt().isEmpty() ? criterion.gt(value)
                                        : reader.semantics("multiple <gt> (>) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith("~") ) {

                        final Expression expression=expression(label.substring(1));
                        final String value=reader.token(STRING);

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().like(value)
                                        : criterion.like().isEmpty() ? criterion.like(value)
                                        : reader.semantics("multiple <like> (~) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith("?") ) {

                        final Expression expression=expression(label.substring(1));
                        final Set<Value> options=values(expression.apply(shape != null ? shape : Shape.shape()));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().any(options)
                                        : criterion.any().isEmpty() ? criterion.any(options)
                                        : reader.semantics("multiple <any> (?) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith("^") ) {

                        final Expression expression=expression(label.substring(1));
                        final int priority=priority(reader.event() == STRING ? reader.token() : reader.token(NUMBER));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().order(priority)
                                        : criterion.order().isEmpty() ? criterion.order(priority)
                                        : reader.semantics("multiple <order> (^) criteria on expression <%s>", expression)

                        );

                    } else if ( label.startsWith("$") ) {

                        final Expression expression=expression(label.substring(1));
                        final Set<Value> targets=values(expression.apply(shape != null ? shape : Shape.shape()));

                        criteria.compute(expression, (key, criterion) ->

                                criterion == null ? criterion().focus(targets)
                                        : criterion.focus().isEmpty() ? criterion.focus(targets)
                                        : reader.semantics("multiple <focus> ($) criteria on expression <%s>", expression)

                        );

                    } else if ( label.equals("@") ) {

                        if ( offset >= 0 ) {
                            reader.semantics("multiple <offset> (@) values");
                        }

                        offset=offset(reader.token(NUMBER));

                    } else if ( label.equals("#") ) {

                        if ( limit >= 0 ) {
                            reader.semantics("multiple <limit> (#) values");
                        }

                        limit=limit(reader.token(NUMBER));

                    } else if ( equal(label) instanceof final Integer equal && equal >= 0 ) { // computed (after <=/>=)

                        final String name=name(label, equal);
                        final Expression expression=expression(label.substring(equal+1));
                        final Value model=value(expression.apply(shape != null ? shape : Shape.shape()), false);

                        if ( !isModel(model) ) {
                            return reader.semantics("unexpected value in model <%s>", model);
                        }

                        if ( probes.isEmpty() ) { // convert previously parsed plain fields

                            keywords.forEach((key, value) -> {
                                reader.semantics("reserved property <%s> on tabular model", key);
                            });

                            fields.forEach(field -> {

                                final String n=field.getKey();
                                final Value v=field.getValue();

                                if ( n.startsWith("@") ) {

                                    if ( !n.equals(CONTEXT) ) { // ignore shape
                                        reader.semantics("reserved property <%s> on tabular model", field);
                                    }

                                } else {

                                    probes.add(probe(n, expression(n), v));

                                }

                            });

                            fields.clear();

                        }

                        probes.add(probe(name, expression, model));

                    } else if ( label.equals(shape == null ? ID : shape.id().orElse(ID)) ) {

                        final JSONEvent event=reader.event();

                        if ( event != STRING ) {
                            return reader.semantics("unexpected %s after <%s> id field", event.description(), label);
                        }

                        if ( !probes.isEmpty() ) {
                            return reader.semantics("reserved property <%s> on tabular model", label);
                        }

                        final String token=reader.token();

                        fields.add(token.isEmpty()
                                ? field(ID, uri(URIs.uri())) // preserve empty ids as default model values
                                : id(base.resolve(token))
                        );

                    } else if ( label.equals(shape == null ? TYPE : shape.type().orElse(TYPE)) ) {

                        final JSONEvent event=reader.event();

                        if ( event != STRING ) {
                            return reader.semantics("unexpected %s after <%s> type field", event.description(), label);
                        }

                        if ( keywords.put(TYPE, reader.token()) != null ) {
                            return reader.semantics("duplicated keyword <%s>", label);
                        }

                        if ( !probes.isEmpty() ) {
                            return reader.semantics("reserved property <%s> on tabular model", label);
                        }

                    } else if ( label.startsWith("@") ) {

                        if ( !probes.isEmpty() ) {
                            return reader.semantics("reserved property <%s> on tabular model", label);
                        }

                        if ( keywords.put(label, reader.token(STRING)) != null ) {
                            return reader.semantics("duplicated keyword <%s>", label);
                        }

                    } else {

                        final Value value=value(

                                shape == null ? null : shape
                                        .property(label)
                                        .map(Property::shape)
                                        .orElseGet(() -> reader.semantics("unknown property <%s>".formatted(label))),

                                false

                        );

                        if ( !probes.isEmpty() ) {

                            if ( !isModel(value) ) {
                                return reader.semantics("unexpected value in model <%s>", value);
                            }

                            probes.add(probe(label, expression(label), value));

                        } else if ( !prune || !value.isEmpty() ) {

                            fields.add(field(label, value));

                        }

                    }

                } catch ( final NoSuchElementException e ) {

                    return reader.semantics(e.getMessage());

                }

            }

            reader.token(RBRACE);

            final boolean query=!criteria.isEmpty() || offset >= 0 || limit >= 0;
            final boolean specs=!probes.isEmpty();

            if ( query && !array ) {
                return reader.semantics("query object outside array");
            }

            if ( specs && !array ) {
                return reader.semantics("specs object outside array");
            }

            if ( keywords.isEmpty() || keywords.keySet().equals(CLASSED) ) { // ignore @type properties

                try {

                    final Value model=specs ? Value.value(

                            new Specs(shape != null ? shape : Shape.shape(), probes)

                    ) : Value.object(Optional.ofNullable(keywords.get(TYPE))

                            .map(type -> list(Stream.concat(Stream.of(type(type)), fields.stream())))
                            .orElse(fields)

                    );

                    return query
                            ? Value.value(new Query(model, criteria, max(offset, 0), min(max(limit, 0), MAX_LIMIT)))
                            : model;

                } catch ( final IllegalArgumentException e ) {

                    return reader.semantics(format("malformed query: %s", e.getMessage()));

                }

            } else if ( fields.isEmpty() && !query ) {

                try {

                    final String value=keywords.get(VALUE);
                    final String datatype=keywords.get(TYPE);
                    final String language=keywords.get(LANGUAGE);

                    return keywords.keySet().equals(TYPED) ? data(base.resolve(datatype), value)
                            : keywords.keySet().equals(TAGGED) ? text(locale(language), value)
                            : reader.semantics("malformed literal object");

                } catch ( final IllegalArgumentException e ) {

                    return reader.semantics(format("malformed literal object: %s", e.getMessage()));

                }

            } else {

                return reader.semantics("malformed literal object");

            }

        }
    }


    private int priority(final String priority) {
        try {

            return priority.equalsIgnoreCase("increasing") ? +1
                    : priority.equalsIgnoreCase("decreasing") ? -1
                    : parseInt(priority);

        } catch ( final NumberFormatException e ) {

            return reader.semantics(format("malformed priority value <%s>", priority));

        }
    }

    private int offset(final String offset) {
        try {

            return parseInt(offset);

        } catch ( final NumberFormatException e ) {

            return reader.semantics(format("malformed offset value <%s>", offset));

        }
    }

    private int limit(final String limit) {
        try {

            return parseInt(limit);

        } catch ( final NumberFormatException e ) {

            return reader.semantics(format("malformed limit value <%s>", limit));

        }
    }


    private Value value(final Shape shape) throws IOException {
        return switch ( reader.event() ) {

            case TRUE -> bit(reader.token(true));
            case FALSE -> bit(reader.token(false));
            case NUMBER -> number(reader.token(), shape);
            case STRING -> string(reader.token(), shape);

            case LBRACE -> shape.is(Object()) ? object(shape) : texts();

            default -> reader.semantics("unexpected %s", reader.event().description());

        };
    }

    private Set<Value> values(final Shape shape) throws IOException {

        final Set<Value> values=new LinkedHashSet<>();

        reader.token(LBRACKET);

        while ( reader.event() != RBRACKET ) {

            reader.token(COMMA);

            switch ( reader.event() ) {

                case NULL -> values.add(nil(reader.token((Void)null)));
                case TRUE -> values.add(bit(reader.token(true)));
                case FALSE -> values.add(bit(reader.token(false)));
                case NUMBER -> values.add(number(reader.token(), shape));
                case STRING -> values.add(string(reader.token(), shape));

                case LBRACE -> {

                    if ( shape.is(Object()) ) {
                        values.add(object(shape));
                    } else {
                        texts().array().ifPresent(values::addAll);
                    }

                }

                default -> reader.semantics("unexpected %s", reader.event().description());

            }

        }

        reader.token(RBRACKET);

        return values;

    }

    private Value object(final Shape shape) throws IOException {

        final String id=shape == null ? ID : shape.id().orElse(ID);

        Value object=null;

        reader.token(LBRACE);

        while ( reader.event() != RBRACE ) {

            reader.token(COMMA);

            final String label=reader.token(STRING);

            reader.token(COLON);

            if ( label.equals(id) ) {

                object=Value.object(
                        shape(shape),
                        id(base.resolve(reader.token(STRING)))
                );

            } else {

                value(

                        shape == null ? null : shape
                                .property(label)
                                .map(Property::shape)
                                .orElseGet(() -> reader.semantics("unknown property <%s>".formatted(label))),

                        false

                );

            }

        }

        reader.token(RBRACE);

        if ( object == null ) {
            return reader.semantics("missing id field <%s>", id);
        }

        return object;
    }


    private Value texts() throws IOException {

        final Set<Value> values=new LinkedHashSet<>();

        reader.token(LBRACE);

        while ( reader.event() != RBRACE ) {

            reader.token(COMMA);

            final Locale locale=locale(reader.token(STRING));

            reader.token(COLON);

            switch ( reader.event() ) {

                case LBRACKET -> values.addAll(texts(locale));
                case STRING -> values.add(text(locale, reader.token(STRING)));

                default -> reader.semantics("unexpected %s", reader.event().description());

            }

        }

        reader.token(RBRACE);

        return Value.array(values);
    }

    private Set<Value> texts(final Locale locale) throws IOException {

        final Set<Value> locals=new LinkedHashSet<>();

        reader.token(LBRACKET);

        while ( reader.event() != RBRACKET ) {

            reader.token(COMMA);

            locals.add(text(locale, reader.token(STRING)));

        }

        reader.token(RBRACKET);

        return locals;
    }

    private Locale locale(final String token) {
        try {

            return Locales.locale(token);

        } catch ( final IllegalArgumentException e ) {

            return reader.semantics("malformed locale <%s>", token);

        }
    }

}
