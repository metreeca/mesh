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

package com.metreeca.mesh.shapes;

import com.metreeca.mesh.Value;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.shapes.Type.type;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.Locales.locale;
import static com.metreeca.shim.URIs.item;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.ITALIAN;
import static org.assertj.core.api.Assertions.*;

@Nested
final class ShapeTest {

    @Nested
    final class DatatypeTest {

        @Test void testConfigureDatatype() {
            assertThat(shape().datatype()).isEmpty();
            assertThat(shape().datatype(Bit()).datatype()).contains(Bit());
        }

    }

    @Nested
    final class ClassTest {

        @Test void testImplicitClassesIncludeExplict() {

            final Type x=type(item("x"));
            final Type y=type(item("y"));

            assertThat(shape().clazz(x, y).clazzes()).contains(set(x, y));
        }
    }

    @Nested
    final class MinMaxLimitTest {

        @Test void testConfigureMinExclusive() {
            assertThat(shape().minExclusive()).isEmpty();
            assertThat(shape().minExclusive(integer(0)).minExclusive()).contains(integer(0));
        }

        @Test void testConfigureMaxExclusive() {
            assertThat(shape().maxExclusive()).isEmpty();
            assertThat(shape().maxExclusive(integer(0)).maxExclusive()).contains(integer(0));
        }

        @Test void testConfigureMinInclusive() {
            assertThat(shape().minInclusive()).isEmpty();
            assertThat(shape().minInclusive(integer(0)).minInclusive()).contains(integer(0));
        }

        @Test void testConfigureMaxInclusive() {
            assertThat(shape().maxInclusive()).isEmpty();
            assertThat(shape().maxInclusive(integer(0)).maxInclusive()).contains(integer(0));
        }


        @Test void testReportConflictingMinLimits() {
            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minInclusive(integer(1))
                    .minExclusive(integer(1))
            );
        }

        @Test void testReportConflictingMaxLimits() {
            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .maxInclusive(integer(1))
                    .maxExclusive(integer(1))
            );
        }

        @Test void testReportConflictingMinMaxLimits() {

            assertThatNoException().isThrownBy(() -> shape()
                    .minInclusive(integer(0))
                    .maxInclusive(integer(0))
            );

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minInclusive(integer(1))
                    .maxInclusive(integer(0))
            );

            assertThatNoException().isThrownBy(() -> shape()
                    .minInclusive(integer(0))
                    .maxExclusive(integer(1))
            );

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minInclusive(integer(0))
                    .maxExclusive(integer(0))
            );

            assertThatNoException().isThrownBy(() -> shape()
                    .minExclusive(integer(0))
                    .maxInclusive(integer(1))
            );

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minExclusive(integer(0))
                    .maxInclusive(integer(0))
            );

            assertThatNoException().isThrownBy(() -> shape()
                    .minExclusive(integer(0))
                    .maxExclusive(integer(1))
            );

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minExclusive(integer(0))
                    .maxExclusive(integer(0))
            );

        }

    }

    @Nested
    final class MinMaxLengthTest {

        @Test void testConfigureMinLength() {
            assertThat(shape().minLength()).isEmpty();
            assertThat(shape().minLength(1).minLength()).contains(1);
        }

        @Test void testConfigureMaxLength() {
            assertThat(shape().maxLength()).isEmpty();
            assertThat(shape().maxLength(1).maxLength()).contains(1);
        }

        @Test void testIgnoreZeroLimit() {
            assertThat(shape().minLength(0).minLength()).isEmpty();
            assertThat(shape().maxLength(0).maxLength()).isEmpty();
        }


        @Test void testReportConflictingMinMaxLengths() {

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minLength(100)
                    .maxLength(10)
            );

            assertThatNoException().isThrownBy(() -> shape()
                    .minLength(10)
                    .maxLength(10)
            );

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minLength(100)
                    .maxLength(10)
            );

        }

    }

    @Nested
    final class PatternTest {

        @Test void testConfigurePattern() {
            assertThat(shape().pattern()).isEmpty();
            assertThat(shape().pattern("x").pattern()).contains("x");
        }

        @Test void testIgnoreEmptyPattern() {
            assertThat(shape().pattern("").pattern()).isEmpty();
        }

    }

    @Nested
    final class MinMaxCountTest {

        @Test void testConfigureMinCount() {
            assertThat(shape().minCount()).isEmpty();
            assertThat(shape().minCount(1).minCount()).contains(1);
        }

        @Test void testConfigureMaxCount() {
            assertThat(shape().maxCount()).isEmpty();
            assertThat(shape().maxCount(1).maxCount()).contains(1);
        }

        @Test void testIgnoreZeroLimit() {
            assertThat(shape().minCount(0).minCount()).isEmpty();
            assertThat(shape().maxCount(0).maxCount()).isEmpty();
        }


        @Test void testReportConflictingMinMaxCounts() {

            assertThatNoException().isThrownBy(() -> shape()
                    .minCount(10)
                    .maxCount(10)
            );

            assertThatIllegalArgumentException().isThrownBy(() -> shape()
                    .minCount(100)
                    .maxCount(10)

            );

        }

    }

    @Nested
    final class InTest {

        @Test void testConfigureIn() {
            assertThat(shape().in()).isEmpty();
            assertThat(shape().in(set(integer(0), integer(1))).in()).contains(set(integer(0), integer(1)));
        }

        @Test void IgnoreEmptySets() {
            assertThat(shape().in(set()).in()).isEmpty();
        }

    }

    @Nested
    final class HasValueTest {

        @Test void testConfigureHas() {
            assertThat(shape().hasValue()).isEmpty();
            assertThat(shape().hasValue(set(integer(0))).hasValue()).contains(set(integer(0)));
        }

        @Test void IgnoreEmptySets() {
            assertThat(shape().hasValue(set()).hasValue()).isEmpty();
        }

    }

    @Nested
    final class LanguageInTest {

        @Test void testConfigureLanguageIn() {
            assertThat(shape().languageIn()).isEmpty();
            assertThat(shape().languageIn(set(ENGLISH, ITALIAN)).languageIn()).contains(set(ENGLISH, ITALIAN));
        }

        @Test void testIgnoreEmptySets() {
            assertThat(shape().languageIn(set()).languageIn()).isEmpty();
        }

    }

    @Nested
    final class PropertyTest {

        @Test void testConfigureProperty() {
            assertThat(shape().property("value")).isEmpty();
            assertThat(shape().property(property("value").forward(true)).property("value")).isNotEmpty();
        }


        @Test void testReportPropertyConflicts() {

            assertThatIllegalArgumentException().as("conflicting name").isThrownBy(() -> {
                final URI uri=URI.create("test:x");
                final URI uri1=URI.create("test:x");
                shape()
                        .property(property("a").forward(uri1))
                        .property(property("b").forward(uri));
            });

        }

    }

    @Nested
    final class ExtendTest {

        @Test void testRetainExtendingClass() {

            final Type x=type(item("x"));
            final Type y=type(item("y"));

            assertThat(shape().extend(shape()).clazz()).isEmpty();
            assertThat(shape().extend(shape().clazz(x)).clazz()).isEmpty();
            assertThat(shape().clazz(x).extend(shape()).clazz()).contains(x);
            assertThat(shape().clazz(x).extend(shape().clazz(y)).clazz()).contains(x);

        }

        @Test void testMergeClasses() {

            final Type x=type(item("x"));
            final Type y=type(item("y"));
            final Type z=type(item("z"));

            assertThat(shape().clazz(x, y).extend(shape().clazz(x, z)).clazzes()).contains(set(x, y, z));
        }

    }

    @Nested
    final class MergeTest {

        @Test void testMergeVirtual() {
            assertThat(shape().merge(shape()).virtual()).isFalse();
            assertThat(shape().virtual(true).merge(shape()).virtual()).isTrue();
            assertThat(shape().merge(shape().virtual(true)).virtual()).isTrue();
            assertThat(shape().virtual(false).merge(shape().virtual(true)).virtual()).isTrue();
        }

        @Test void testMergeId() {
            assertThat(shape().merge(shape()).id()).isEmpty();
            assertThat(shape().id("x").merge(shape()).id()).contains("x");
            assertThat(shape().merge(shape().id("y")).id()).contains("y");
            assertThatIllegalArgumentException().isThrownBy(() -> shape().id("x").merge(shape().id("y")));
        }

        @Test void testMergeType() {
            assertThat(shape().merge(shape()).type()).isEmpty();
            assertThat(shape().type("x").merge(shape()).type()).contains("x");
            assertThat(shape().merge(shape().type("y")).type()).contains("y");
            assertThatIllegalArgumentException().isThrownBy(() -> shape().type("x").merge(shape().type("y")));
        }

        @Test void testMergeClass() {

            final Type x=type(item("x"));
            final Type y=type(item("y"));
            final Type z=type(item("z"));

            assertThat(shape().merge(shape()).clazz()).isEmpty();
            assertThat(shape().merge(shape().clazz(y)).clazz()).contains(y);
            assertThat(shape().clazz(x).merge(shape()).clazz()).contains(x);

            assertThatIllegalArgumentException().isThrownBy(() -> shape().clazz(x).merge(shape().clazz(y)));

        }

        @Test void testMergeClasses() {

            final Type x=type(item("x"));
            final Type y=type(item("y"));
            final Type z=type(item("z"));

            assertThat(shape().clazz(x, y).merge(shape().clazz(x, z)).clazzes()).contains(set(x, y, z));
        }

        @Test void testMergeConstraints() {

            final Function<Value, Value> x=frame -> Nil();
            final Function<Value, Value> y=frame -> Nil();

            assertThat(shape().merge(shape()).constraints()).isEmpty();
            assertThat(shape().constraints(x).merge(shape()).constraints()).contains(set(x));
            assertThat(shape().merge(shape().constraints(y)).constraints()).contains(set(y));
            assertThat(shape().constraints(x).merge(shape().constraints(y)).constraints()).contains(set(x, y));

        }

        @Test void testMergeMinExclusive() {
            assertThat(shape().merge(shape()).minExclusive()).isEmpty();
            assertThat(shape().minExclusive(integer(10)).merge(shape()).minExclusive()).contains(integer(10));
            assertThat(shape().merge(shape().minExclusive(integer(10))).minExclusive()).contains(integer(10));
            assertThat(shape().minExclusive(integer(100)).merge(shape().minExclusive(integer(10))).minExclusive()).contains(integer(100));
            assertThatIllegalArgumentException().isThrownBy(() -> shape().minExclusive(integer(10)).merge(shape().minExclusive(integer(100))));
        }

        @Test void testMergeMaxExclusive() {
            assertThat(shape().merge(shape()).maxExclusive()).isEmpty();
            assertThat(shape().maxExclusive(integer(10)).merge(shape()).maxExclusive()).contains(integer(10));
            assertThat(shape().merge(shape().maxExclusive(integer(10))).maxExclusive()).contains(integer(10));
            assertThat(shape().maxExclusive(integer(10)).merge(shape().maxExclusive(integer(100))).maxExclusive()).contains(integer(10));
            assertThatIllegalArgumentException().isThrownBy(() -> shape().maxExclusive(integer(100)).merge(shape().maxExclusive(integer(10))));
        }

        @Test void testMergeMinInclusive() {
            assertThat(shape().merge(shape()).minInclusive()).isEmpty();
            assertThat(shape().minInclusive(integer(10)).merge(shape()).minInclusive()).contains(integer(10));
            assertThat(shape().merge(shape().minInclusive(integer(10))).minInclusive()).contains(integer(10));
            assertThat(shape().minInclusive(integer(100)).merge(shape().minInclusive(integer(10))).minInclusive()).contains(integer(100));
            assertThatIllegalArgumentException().isThrownBy(() -> shape().minInclusive(integer(10)).merge(shape().minInclusive(integer(100))));
        }

        @Test void testMergeMaxInclusive() {
            assertThat(shape().merge(shape()).maxInclusive()).isEmpty();
            assertThat(shape().maxInclusive(integer(10)).merge(shape()).maxInclusive()).contains(integer(10));
            assertThat(shape().merge(shape().maxInclusive(integer(10))).maxInclusive()).contains(integer(10));
            assertThat(shape().maxInclusive(integer(10)).merge(shape().maxInclusive(integer(100))).maxInclusive()).contains(integer(10));
            assertThatIllegalArgumentException().isThrownBy(() -> shape().maxInclusive(integer(100)).merge(shape().maxInclusive(integer(10))));
        }

        @Test void testMergeMinLength() {
            assertThat(shape().merge(shape()).minLength()).isEmpty();
            assertThat(shape().minLength(10).merge(shape()).minLength()).contains(10);
            assertThat(shape().merge(shape().minLength(10)).minLength()).contains(10);
            assertThat(shape().minLength(100).merge(shape().minLength(10)).minLength()).contains(100);
            assertThatIllegalArgumentException().isThrownBy(() -> shape().minLength(10).merge(shape().minLength(100)));
        }

        @Test void testMergeMaxLength() {
            assertThat(shape().merge(shape()).maxLength()).isEmpty();
            assertThat(shape().maxLength(10).merge(shape()).maxLength()).contains(10);
            assertThat(shape().merge(shape().maxLength(10)).maxLength()).contains(10);
            assertThat(shape().maxLength(10).merge(shape().maxLength(100)).maxLength()).contains(10);
            assertThatIllegalArgumentException().isThrownBy(() -> shape().maxLength(100).merge(shape().maxLength(10)));
        }

        @Test void testMergePattern() {
            assertThat(shape().merge(shape()).pattern()).isEmpty();
            assertThat(shape().pattern("x").merge(shape()).pattern()).contains("x");
            assertThat(shape().merge(shape().pattern("y")).pattern()).contains("y");
            assertThatIllegalArgumentException().isThrownBy(() -> shape().pattern("x").merge(shape().pattern("y")));
        }

        @Test void testMergeIn() {
            assertThat(shape().merge(shape()).in()).isEmpty();
            assertThat(shape().in(string("x")).merge(shape()).in()).contains(set(string("x")));
            assertThat(shape().merge(shape().in(string("y"))).in()).contains(set(string("y")));
            assertThat(shape().in(string("x")).merge(shape().in(string("y"))).in()).contains(set(string("x"), string("y")));
        }


        @Test void testMergeLanguageIn() {
            assertThat(shape().merge(shape()).languageIn()).isEmpty();
            assertThat(shape().languageIn(locale("xx")).merge(shape()).languageIn()).contains(set(locale("xx")));
            assertThat(shape().merge(shape().languageIn(locale("yy"))).languageIn()).contains(set(locale("yy")));
            assertThat(shape().languageIn(locale("xx")).merge(shape().languageIn(locale("yy"))).languageIn()).contains(set(locale("xx"), locale("yy")));
        }

        @Test void testMergeUniqueLang() {
            assertThat(shape().merge(shape()).uniqueLang()).isFalse();
            assertThat(shape().uniqueLang(true).merge(shape()).uniqueLang()).isTrue();
            assertThat(shape().merge(shape().uniqueLang(true)).uniqueLang()).isTrue();
            assertThat(shape().uniqueLang(false).merge(shape().uniqueLang(true)).uniqueLang()).isTrue();
        }

        @Test void testMergeMinCount() {
            assertThat(shape().merge(shape()).minCount()).isEmpty();
            assertThat(shape().minCount(10).merge(shape()).minCount()).contains(10);
            assertThat(shape().merge(shape().minCount(10)).minCount()).contains(10);
            assertThat(shape().minCount(100).merge(shape().minCount(10)).minCount()).contains(100);
            assertThatIllegalArgumentException().isThrownBy(() -> shape().minCount(10).merge(shape().minCount(100)));
        }

        @Test void testMergeMaxCount() {
            assertThat(shape().merge(shape()).maxCount()).isEmpty();
            assertThat(shape().maxCount(10).merge(shape()).maxCount()).contains(10);
            assertThat(shape().merge(shape().maxCount(10)).maxCount()).contains(10);
            assertThat(shape().maxCount(10).merge(shape().maxCount(100)).maxCount()).contains(10);
            assertThatIllegalArgumentException().isThrownBy(() -> shape().maxCount(100).merge(shape().maxCount(10)));
        }

        @Test void testMergeHasValue() {
            assertThat(shape().merge(shape()).hasValue()).isEmpty();
            assertThat(shape().hasValue(string("x")).merge(shape()).hasValue()).contains(set(string("x")));
            assertThat(shape().merge(shape().hasValue(string("y"))).hasValue()).contains(set(string("y")));
            assertThat(shape().hasValue(string("x")).merge(shape().hasValue(string("y"))).hasValue()).contains(set(string("x"), string("y")));
        }

        @Test void testMergeProperties() {

            assertThat(shape().merge(shape()).properties()).isEmpty();

            assertThat(shape().property(property("x").forward(true))
                    .merge(shape())
                    .properties()
            ).containsExactly(property("x").forward(true));

            assertThat(shape()
                    .merge(shape().property(property("y").forward(true)))
                    .properties()
            ).containsExactly(property("y").forward(true));

            assertThat(shape().property(property("x").forward(true))
                    .merge(shape().property(property("y").forward(true)))
                    .properties()
            )
                    .containsExactlyInAnyOrder(property("x").forward(true), property("y").forward(true));

            assertThat(shape().property(property("p").forward(true).shape(shape().minCount(100)))
                    .merge(shape().property(property("p").forward(true).shape(shape().minCount(10))))
                    .property("p")
            )
                    .hasValueSatisfying(p -> assertThat(p.shape().minCount()).contains(100));

        }

        @Test void testMergeRecursiveProperties() {

            final Supplier<Shape> shape=new Supplier<>() {

                @Override public Shape get() {
                    return shape().property(property("p").forward(true).shape(this));
                }

            };


            assertThatNoException().isThrownBy(() -> shape.get().extend(shape.get()));
        }


        @Test void testReportConflictingProperties() {

            assertThatIllegalArgumentException().isThrownBy(() ->
                    shape().property(property("p").forward(item("p")))
                            .merge(shape().property(property("p").forward(item("q"))))
            );

        }

    }

}
