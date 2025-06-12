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

import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Criterion;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map.Entry;

import static com.metreeca.mesh.Value.Array;
import static com.metreeca.mesh.Value.Data;
import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.String;
import static com.metreeca.mesh.Value.Text;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.mesh.Value.data;
import static com.metreeca.mesh.Value.field;
import static com.metreeca.mesh.Value.id;
import static com.metreeca.mesh.Value.integer;
import static com.metreeca.mesh.Value.object;
import static com.metreeca.mesh.Value.shape;
import static com.metreeca.mesh.Value.string;
import static com.metreeca.mesh.Value.text;
import static com.metreeca.mesh.Value.type;
import static com.metreeca.mesh.Value.value;
import static com.metreeca.mesh.pipe.AgentModel.expand;
import static com.metreeca.mesh.pipe.AgentModel.populate;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Locales.ANY;
import static com.metreeca.shim.URIs.base;
import static com.metreeca.shim.URIs.uri;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
final class AgentModelTest {

    @Nested
    final class ExpandTest {

        @Test void testExpandLiteral() {
            assertThat(expand(string("value"))).isEqualTo(string("value"));
        }

        @Test void testExpandArray() {

            final Value array=array(
                    string("value"),
                    object(shape(shape().property(property("p").forward(true).shape(shape().datatype(object())))))
            );

            assertThat(expand(array)).isEqualTo(array(list(array.values().map(AgentModel::expand))));
        }


        @Test void testExpandObjectWithoutId() {
            assertThat(expand(object())).isEqualTo(object(
                    shape(shape()),
                    id(uri())
            ));
        }

        @Test void testExpandObjectWithId() {
            assertThat(expand(object(
                    id(base())
            ))).isEqualTo(object(
                    shape(shape()),
                    id(uri())
            ));
        }

        @Test void testExpandObjectWithSpecifiedProperty() {

            final Shape shape=shape().property(property("x").forward(true).shape(shape().datatype(String())));

            assertThat(expand(object(
                    shape(shape),
                    field("x", string("x"))
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri()),
                    field("x", string("x"))
            ));
        }

        @Test void testExpandObjectWithoutSpecifiedProperty() {

            final Shape shape=shape().property(
                    property("x").forward(true).shape(shape().datatype(String()).maxCount(1))
            );

            assertThat(expand(object(
                    shape(shape)
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri()),
                    field("x", String())
            ));
        }


        @Test void testExpandObjectNestedValues() {

            final Property q=property("q").forward(true).shape(shape().datatype(object()).maxCount(1));
            final Property p=property("p").forward(true).shape(shape().datatype(object()).maxCount(1));

            assertThat(expand(object(
                    shape(shape().property(p)),
                    field("p", object(
                            shape(shape().property(q))
                    ))
            ))).isEqualTo(object(
                    shape(shape().property(p)),
                    id(uri()),
                    field("p", object(
                            shape(shape().property(q)),
                            id(uri()),
                            entry("q", object(
                                    shape(q.shape()),
                                    id(uri())
                            ))
                    ))
            ));

        }

        @Test void testExpandQuery() {

            final Shape shape=shape().property(
                    property("x").forward(true).shape(shape().datatype(String()).maxCount(1))
            );

            assertThat(expand(value(query(object(
                    shape(shape)
            ))))).isEqualTo(value(query(object(
                    shape(shape),
                    id(uri()),
                    field("x", String())
            ))));
        }


        @Test void testIgnoreUnknownProperties() {
            assertThat(expand(object(
                    field("x", string("x"))
            ))).isEqualTo(object(
                    shape(shape()),
                    id(uri())
            ));
        }

        @Test void testIgnoreUndefinedDatatypes() {

            final Shape shape=shape().property(property("x").forward(true));

            assertThat(expand(object(
                    shape(shape)
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri())
            ));
        }

        @Test void testIgnoreNestedProperties() {

            final Property q=property("q").forward(true).shape(shape().datatype(String()).maxCount(1));
            final Property p=property("p").forward(true).shape(shape().property(q).maxCount(1));

            final Shape shape=shape().property(p);

            assertThat(expand(object(
                    shape(shape)
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri()),
                    field("p", object(
                            shape(p.shape()),
                            id(uri())
                    ))
            ));

        }

        @Test void testIgnoreHiddenProperties() {

            final Shape shape=shape()
                    .property(property("hidden").forward(true).hidden(true).shape(shape().datatype(String())))
                    .property(property("visible").forward(true).hidden(false).shape(shape().datatype(String())));

            assertThat(expand(object(
                    shape(shape)
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri()),
                    field("visible", array(String()))
            ));
        }


        @Test void testExcludeNils() {

            final Shape shape=shape()
                    .property(property("p").forward(true).shape(shape().datatype(String()).maxCount(1)))
                    .property(property("q").forward(true).shape(shape().datatype(String()).maxCount(1)))
                    .property(property("r").forward(true).shape(shape().datatype(String()).maxCount(1)));

            assertThat(expand(object(

                    shape(shape),

                    field("p", string("value")),
                    field("r", Nil())

            ))).isEqualTo(object(

                    shape(shape),
                    id(uri()),

                    field("p", string("value")),
                    field("q", String())

            ));
        }

        @Test void testIncludeEmptyArrays() {

            final Shape shape=shape()
                    .property(property("p").forward(true).shape(shape().datatype(String())));

            assertThat(expand(object(
                    shape(shape),
                    field("p", Array())
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri()),
                    field("p", Array())
            ));
        }


        @Test void testGenerateWildcardForEmptyTextArrays() {

            final Shape shape=shape().property(
                    property("p").forward(true).shape(shape().datatype(Text()))
            );

            assertThat(expand(object(
                    shape(shape),
                    field("p", Array())
            ))).isEqualTo(object(
                    shape(shape),
                    id(uri()),
                    field("p", array(Text()))
            ));

        }

    }

    @Nested
    final class PopulateTest {

        @Test void testPopulateLiteralFromLiteral() {
            assertThat(populate(string("x"), string("y"))).isEqualTo(string("y"));
        }

        @Test void testPopulateLiteralFromObject() {
            assertThat(populate(String(), object())).isEqualTo(object());
        }

        @Test void testPopulateLiteralFromArray() {
            assertThat(populate(String(), Array())).isEqualTo(Array());
        }

        @Test void testPopulateLiteralFromQuery() {
            assertThat(populate(String(), value(query()))).isEqualTo(value(query()));
        }


        @Test void testPopulateObjectFromString() {
            assertThat(populate(object(), String())).isEqualTo(String());
        }

        @Test void testPopulateObjectFromObject() {

            assertThat(populate(object(
                    field("w", integer(2)),
                    field("x", integer(3)),
                    field("y", object(entry("a", integer(4))))
            ), object(
                    field("x", integer(5)),
                    field("y", object(entry("a", integer(6)))),
                    field("z", integer(7))
            ))).isEqualTo(object(
                    field("x", integer(5)),
                    field("y", object(entry("a", integer(6))))
            ));
        }

        @Test void testPopulateObjectEWithIgnoredFieldsFromObject() {
            assertThat(populate(object(
                    shape(shape())
            ), object(
                    field("x", integer(5)),
                    field("y", object(entry("a", integer(6)))),
                    field("z", integer(7))
            ))).isEqualTo(object(
                    field("x", integer(5)),
                    field("y", object(entry("a", integer(6)))),
                    field("z", integer(7))
            ));
        }

        @Test void testPopulateObjectFromObjectWithEmptyValues() {
            assertThat(populate(object(
                    field("x", integer(1)),
                    field("y", integer(2))
            ), object(
                    field("x", Nil()),
                    field("y", Array())
            ))).isEqualTo(object(
                    field("x", Nil()),
                    field("y", Array())
            ));
        }

        @Test void testPopulateObjectFromObjectWithUndefinedValues() {

            final Shape shape=shape()
                    .property(property("p").forward(true).shape(
                            shape().datatype(String()).maxCount(1)
                    ));

            assertThat(populate(object(
                    shape(shape),
                    field("p", string("value"))
            ), object(
                    shape(shape)
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", String())
            ));

        }

        @Test void testPopulateObjectFromArray() {
            assertThat(populate(object(), Array())).isEqualTo(Array());
        }

        @Test void testPopulateObjectFromQuery() {
            assertThat(populate(object(
                    field("x", integer(1))
            ), value(query(object(
                            field("x", integer(2)),
                            field("y", integer(3))
                    ))
                            .where("", criterion().gt(integer(0)))
            ))).isEqualTo(value(query(object( // populate from query model
                            field("x", integer(2))
                    ))
                            .where("", criterion().gt(integer(0)))
            ));
        }

        @Test void testPopulateObjectReservedFields() {

            assertThat(populate(object(id("x")), object())).isEqualTo(object());
            assertThat(populate(object(id("x")), object(id("y")))).isEqualTo(object(id("y")));

            assertThat(populate(object(type("x")), object())).isEqualTo(object());
            assertThat(populate(object(type("x")), object(type("y")))).isEqualTo(object(type("y")));

            assertThat(populate(object(shape(shape().minInclusive(integer(1)))), object())).isEqualTo(object());

            final Entry<String, Value> x=shape(shape().minInclusive(integer(1)));
            final Entry<String, Value> y=shape(shape().maxInclusive(integer(10)));

            assertThat(populate(object(x), object())).isEqualTo(object());
            assertThat(populate(object(x), object(y))).isEqualTo(object(y));

        }


        @Test void testPopulateArrayFromString() {
            assertThat(populate(Array(), String())).isEqualTo(String());
        }

        @Test void testPopulateArrayFromObject() {
            assertThat(populate(Array(), object())).isEqualTo(object());
        }

        @Test void testPopulateArrayFromArray() {

            assertThat(populate(array(
                    object(
                            field("x", integer(1))
                    ),
                    object(
                            field("y", integer(2))
                    )
            ), array(
                    object(
                            field("x", integer(3))
                    )
            ))).isEqualTo(array( // pairwise merging
                    object(
                            field("x", integer(3))
                    ),
                    object(
                            field("y", integer(2))
                    )
            ));

            assertThat(populate(array(
                    object(
                            field("z", integer(1))
                    )
            ), array(
                    object(
                            field("x", integer(2))
                    ),
                    object(
                            field("y", integer(3))
                    )
            ))).isEqualTo(array( // pairwise merging
                    object(

                    ),
                    object(
                            field("y", integer(3))
                    )
            ));
        }

        @Test void testPopulateArrayFromQuery() {

            assertThat(populate(Array(), value(query()))).isEqualTo(value(query()));

            assertThat(populate(array(
                    object(field("x", integer(1))),
                    object(field("y", integer(2)))
            ), value(query(object(
                            field("x", integer(3)),
                            field("z", integer(4))
                    ))
                            .where("", criterion().gt(integer(0)))
            ))).isEqualTo(value(query(object( // populate from query model
                            field("x", integer(3))
                    ))
                            .where("", criterion().gt(integer(0)))
            ));
        }


        @Test void testPopulateQueryFromString() {
            assertThat(populate(value(query()), String())).isEqualTo(String());
        }

        @Test void testPopulateQueryFromObject() {
            assertThat(populate(value(query(object(
                            field("x", integer(1))
                    ))
                            .where("", criterion().gt(integer(0)))
            ), object(
                    field("x", integer(2)),
                    field("y", integer(3))
            ))).isEqualTo(value(query(object( // populate from object
                            field("x", integer(2))
                    ))
                            .where("", criterion().gt(integer(0))) // retain criteria
            ));
        }


        @Test void testPopulateQueryFromArray() {

            assertThat(populate(value(query()), Array())).isEqualTo(value(query()));

            assertThat(populate(value(query(object(
                            field("x", integer(1))
                    ))
                            .where("", criterion().gt(integer(0)))
            ), array(object(
                    field("x", integer(2)),
                    field("y", integer(3))
            )))).isEqualTo(value(query(object( // populate from object
                            field("x", integer(2))
                    ))
                            .where("", criterion().gt(integer(0))) // retain criteria
            ));
        }

        @Test void testPopulateQueryFromQuery() {
            assertThat(populate(value(query(object(
                            field("x", integer(1))
                    ))
                            .where("", criterion().gt(integer(0)))
                    .offset(100)
                    .limit(200)
            ), value(query(object(
                            field("x", integer(2))
                    ))
                            .where("", criterion().lt(integer(10)))
                    .limit(10)
            ))).isEqualTo(value(query(object( // populate from model
                            field("x", integer(2))
                    ))
                            .where("", criterion() // merge criteria
                                    .gt(integer(0))
                                    .lt(integer(10))
                            )
                    .offset(100) // retain offset
                    .limit(10) // lower limit
            ));
        }

        @Test void testPopulateQueryWithOnlyConstraintsFromObject() {

            // x is a query containing an empty model and some constraints

            final Shape shape=shape()
                    .property(property("x").forward(true));

            final Criterion criterion=criterion().gt(integer(0));

            final Value x=value(query()
                    .model(object(
                            shape(shape)
                    ))
                    .where("x", criterion)
            );

            // y is an object

            final Value y=object(
                    shape(shape),
                    field("x", string("test"))
            );

            assertThat(populate(x, y)).isEqualTo(value(query()
                    .model(y)
                    .where("x", criterion)
            ));
        }


        @Test void testPopulateSpecsFromObject() {
            assertThat(populate(value(new Specs(shape(), list())), object(field("x", String()))))
                    .isEqualTo(value(new Specs(shape(), list())));
        }

    }

    @Nested
    final class PopulateTextTest {

        private static final Shape shape=shape().property(
                property("p").forward(true).shape(shape().datatype(Text()))
        );


        @Test void testPopulateTextFromDefinedValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(text("en", "")))
            ), object(
                    shape(shape),
                    field("p", array(text("en", "one")))
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(text("en", "one")))
            ));
        }

        @Test void testPopulateTextFromWildcardValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(text("en", "")))
            ), object(
                    shape(shape),
                    field("p", array(text("*", "?")))
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(text("en", "?")))
            ));
        }

        @Test void testPopulateTextFromMismatchedValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(text("en", "")))
            ), object(
                    shape(shape),
                    field("p", array(text("it", "uno")))
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(text("en", "")))
            ));
        }

        @Test void testPopulateTextFromUndefinedValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(text("en", "")))
            ), object(
                    shape(shape),
                    field("p", array())
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(text("en", "")))
            ));
        }

        @Test void testPopulateTextWildcard() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(text(ANY, "")))
            ), object(
                    shape(shape),
                    field("p", array(
                            text("en", ""),
                            text("it", "")
                    ))
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(
                            text("en", ""),
                            text("it", "")
                    ))
            ));
        }

    }

    @Nested
    final class PopulateDataTest {

        private static final Shape shape=shape().property(
                property("p").forward(true).shape(shape().datatype(Data()))
        );


        @Test void testPopulateDataFromDefinedValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(data("test:x", "")))
            ), object(
                    shape(shape),
                    field("p", array(data("test:x", "one")))
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(data("test:x", "one")))
            ));
        }

        @Test void testPopulateDataFromMismatchedValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(data("test:x", "")))
            ), object(
                    shape(shape),
                    field("p", array(data("test:y", "uno")))
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(data("test:x", "")))
            ));
        }

        @Test void testPopulateDataFromUndefinedValue() {
            assertThat(populate(object(
                    shape(shape),
                    field("p", array(data("test:x", "")))
            ), object(
                    shape(shape),
                    field("p", array())
            ))).isEqualTo(object(
                    shape(shape),
                    field("p", array(data("test:x", "")))
            ));
        }

    }

}
