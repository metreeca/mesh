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

package com.metreeca.mesh;

import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.mesh.tools.CodecException;
import com.metreeca.mesh.util.URIs;

import java.net.URI;
import java.util.Optional;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.util.Exceptions.error;
import static com.metreeca.mesh.util.URIs.item;

import static java.lang.String.format;

/**
 * Extended JSON object field.
 */
public final class Field {

    public static final String ID="@id";
    public static final String TYPE="@type";
    public static final String VALUE="@value";
    public static final String LANGUAGE="@language";
    public static final String CONTEXT="@context";


    public static boolean isReserved(final String name) {
        return name.startsWith("@");
    }


    public static Field id(final String id) {

        if ( id == null ) {
            throw new NullPointerException("null id");
        }

        return new Field(ID, uri(item(id)));
    }

    public static Field id(final URI id) {

        if ( id == null ) {
            throw new NullPointerException("null id");
        }

        if ( !id.isAbsolute() ) {
            throw new IllegalArgumentException(format("relative id <%s>", id));
        }

        return new Field(ID, id.equals(URIs.uri()) ? Nil() : uri(id));
    }

    public static Field type(final String type) {

        if ( type == null ) {
            throw new NullPointerException("null type");
        }

        return new Field(TYPE, type.isEmpty() ? Nil() : string(type));
    }

    public static Field shape(final Shape shape) {

        if ( shape == null ) {
            throw new NullPointerException("null shape");
        }

        return new Field(CONTEXT, Value.value(shape));
    }


    public static Field field(final String name, final Value value) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( isReserved(name) ) {
            throw new IllegalArgumentException(format("reserved field name <%s>", name));
        }

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return new Field(name, value);
    }


    public static Value malformed(final Value model, final String value) {

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return error(new CodecException(format(
                "malformed <%s> value <%s>", model, value
        )));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Optional<URI> id(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.object()
                .map(fields -> fields.get(ID))
                .flatMap(Value::uri);
    }

    public static Optional<String> type(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.object()
                .map(fields -> fields.get(TYPE))
                .flatMap(Value::string)
                .or(() -> shape(value)
                        .flatMap(Shape::clazz)
                        .map(Type::name)
                );
    }

    public static Optional<Shape> shape(final Value value) {

        if ( value == null ) {
            throw new NullPointerException("null value");
        }

        return value.object()
                .map(fields -> fields.get(CONTEXT))
                .flatMap(v -> v.value(Shape.class));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final String name;
    private final Value value;


    private Field(final String name, final Value value) {
        this.name=name;
        this.value=value;
    }


    public String name() { return name; }

    public Value value() { return value; }


    @Override public boolean equals(final Object object) {
        return this == object || object instanceof final Field field
                                 && name.equals(field.name)
                                 && value.equals(field.value);
    }

    @Override public int hashCode() {
        return name.hashCode()
               ^value.hashCode();
    }

    @Override public String toString() {
        return "Field[%s=%s]".formatted(name, value);
    }

}
