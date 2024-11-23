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

package com.metreeca.mesh.mint.ast;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.shacl.*;
import com.metreeca.mesh.mint.FrameException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.Map;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.mint.ast.Annotation.*;
import static com.metreeca.mesh.mint.ast.Annotation.VALUE;
import static com.metreeca.mesh.mint.ast.Method.*;
import static com.metreeca.mesh.mint.ast.Method.SET;
import static com.metreeca.mesh.mint.ast.Reference.simple;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Collections.set;

import static java.util.Locale.*;
import static org.assertj.core.api.Assertions.*;

@Nested
final class MethodTest {

    private static final String INT="int";
    private static final String STRING="String";


    @Nested
    final class InfoTest {

        @Test void testIdentifyIdMethods() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .isId()
            ).isFalse();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Id.class)))
                    .type(simple(URI.class))
                    .isId()
            ).isTrue();

        }

        @Test void testReportNonURIIdMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Id.class)))
                    .type(simple(String.class))
                    .isId()
            );
        }

        @Test void testReportAnnotatedIdMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Id.class),
                            new Annotation(Virtual.class)
                    ))
                    .type(simple(String.class))
                    .isId()
            );
        }

        @Test void testIgnoreForeignAnnotationsOnIdMethods() {
            assertThatNoException().isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Id.class),
                            new Annotation(Override.class)
                    ))
                    .type(simple(URI.class))
                    .isId()
            );
        }


        @Test void testIdentifyTypeMethods() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .isType()
            ).isFalse();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Type.class)))
                    .type(simple(String.class))
                    .isType()
            ).isTrue();

        }

        @Test void testReportNonStringTypeMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Type.class)))
                    .type(simple(URI.class))
                    .isType()
            );
        }

        @Test void testReportAnnotatedTypeMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Type.class),
                            new Annotation(Virtual.class)
                    ))
                    .type(simple(String.class))
                    .isType()
            );
        }

        @Test void testIgnoreForeignAnnotationsOnTypeMethods() {
            assertThatNoException().isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Type.class),
                            new Annotation(Override.class)
                    ))
                    .type(simple(String.class))
                    .isType()
            );
        }

        @Test void testIdentifyInternalMethods() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .isInternal()
            ).isFalse();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Internal.class)))
                    .isInternal()
            ).isTrue();

        }

        @Test void testReportAnnotatedInternalMethods() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Internal.class),
                            new Annotation(Virtual.class)
                    ))
                    .isInternal()
            );
        }

        @Test void testIgnoreForeignAnnotationsOnInternalMethods() {
            assertThatNoException().isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Internal.class),
                            new Annotation(Override.class)
                    ))
                    .isInternal()
            );
        }

    }

    @Nested
    final class ShapeTest {

        @Test void testIdentifyHidden() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .hidden()
            ).isFalse();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Hidden.class)))
                    .hidden()
            ).isTrue();

        }

        @Test void testIdentifyForeign() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .foreign()
            ).isFalse();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Foreign.class)))
                    .foreign()
            ).isTrue();

        }

        @Test void testIdentifyEmbedded() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .embedded()
            ).isFalse();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Embedded.class)))
                    .embedded()
            ).isTrue();

        }

        @Test void testIdentifyForward() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .forward()
            ).isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Property.class).args(Map.of(FORWARD, "test:x"))))
                    .forward()
            ).contains(
                    "test:x"
            );

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Property.class).args(Map.of(IMPLIED, "test:x"))))
                    .forward()
            ).contains(
                    "test:x"
            );

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Property.class).args(Map.of(
                            IMPLIED, "test:x",
                            REVERSE, "test:y"
                    ))))
                    .forward()
            ).isEmpty();

        }

        @Test void testIdentifyReverse() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .reverse()
            ).isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(new Annotation(Property.class).args(Map.of(REVERSE, "test:x"))))
                    .reverse()
            ).contains(
                    "test:x"
            );

        }

        @Test void testReportConflictingProperties() {

            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Property.class).args(Map.of(FORWARD, "test:x")),
                            new Annotation(Property.class).args(Map.of(REVERSE, "test:x"))
                    ))
                    .forward()
            );

            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .annotations(list(
                            new Annotation(Property.class).args(Map.of(REVERSE, "test:x")),
                            new Annotation(Property.class).args(Map.of(REVERSE, "test:y"))
                    ))
                    .reverse()
            );

        }


        @Test void testIdentifyDatatype() {

            assertThat(new Method("x").type(simple(boolean.class)).datatype()).isEqualTo(Bit());
            assertThat(new Method("x").type(simple(byte.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(short.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(int.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(long.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(float.class)).datatype()).isEqualTo(Floating());
            assertThat(new Method("x").type(simple(double.class)).datatype()).isEqualTo(Floating());

            assertThat(new Method("x").type(simple(Boolean.class)).datatype()).isEqualTo(Bit());
            assertThat(new Method("x").type(simple(Number.class)).datatype()).isEqualTo(Number());
            assertThat(new Method("x").type(simple(Byte.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(Short.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(Integer.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(Long.class)).datatype()).isEqualTo(Integral());
            assertThat(new Method("x").type(simple(Float.class)).datatype()).isEqualTo(Floating());
            assertThat(new Method("x").type(simple(Double.class)).datatype()).isEqualTo(Floating());
            assertThat(new Method("x").type(simple(BigInteger.class)).datatype()).isEqualTo(Integer());
            assertThat(new Method("x").type(simple(BigDecimal.class)).datatype()).isEqualTo(Decimal());
            assertThat(new Method("x").type(simple(String.class)).datatype()).isEqualTo(String());
            assertThat(new Method("x").type(simple(URI.class)).datatype()).isEqualTo(Value.URI());
            assertThat(new Method("x").type(simple(Temporal.class)).datatype()).isEqualTo(Value.Temporal());
            assertThat(new Method("x").type(simple(Year.class)).datatype()).isEqualTo(Value.Year());
            assertThat(new Method("x").type(simple(YearMonth.class)).datatype()).isEqualTo(Value.YearMonth());
            assertThat(new Method("x").type(simple(LocalDate.class)).datatype()).isEqualTo(Value.LocalDate());
            assertThat(new Method("x").type(simple(LocalTime.class)).datatype()).isEqualTo(Value.LocalTime());
            assertThat(new Method("x").type(simple(OffsetTime.class)).datatype()).isEqualTo(OffsetTime());
            assertThat(new Method("x").type(simple(LocalDateTime.class)).datatype()).isEqualTo(LocalDateTime());
            assertThat(new Method("x").type(simple(OffsetDateTime.class)).datatype()).isEqualTo(OffsetDateTime());
            assertThat(new Method("x").type(simple(ZonedDateTime.class)).datatype()).isEqualTo(ZonedDateTime());
            assertThat(new Method("x").type(simple(Instant.class)).datatype()).isEqualTo(Instant());
            assertThat(new Method("x").type(simple(TemporalAmount.class)).datatype()).isEqualTo(Value.TemporalAmount());
            assertThat(new Method("x").type(simple(Period.class)).datatype()).isEqualTo(Period());
            assertThat(new Method("x").type(simple(Duration.class)).datatype()).isEqualTo(Duration());
            assertThat(new Method("x").type(simple(Object.class)).datatype()).isEqualTo(Object());

            assertThat(new Method("x").type(TEXT).datatype()).isEqualTo(Text());
            assertThat(new Method("x").type(TEXTS).datatype()).isEqualTo(Text());
            assertThat(new Method("x").type(TEXTSETS).datatype()).isEqualTo(Text());

            assertThat(new Method("x").type(DATA).datatype()).isEqualTo(Data());
            assertThat(new Method("x").type(DATAS).datatype()).isEqualTo(Data());
            assertThat(new Method("x").type(DATASETS).datatype()).isEqualTo(Data());

        }


        @Test void testIdentifyMinExclusive() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .minExclusive()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MinExclusive.class).args(Map.of(VALUE, "10"))))
                    .minExclusive()
            ).contains(
                    integral(10)
            );

        }

        @Test void testMergeMinExclusiveValues() {
            assertThat(new Method("value")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(
                            new Annotation(MinExclusive.class).args(Map.of(VALUE, "10")),
                            new Annotation(MinExclusive.class).args(Map.of(VALUE, "100"))
                    ))
                    .minExclusive()
            ).contains(
                    integral(100)
            );
        }

        @Test void testReportMalformedMinExclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MinExclusive.class).args(Map.of(VALUE, "x"))))
                    .minExclusive()
            );
        }

        @Test void testReportUnsupportedMinExclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(ZoneId.class.getCanonicalName())
                    .annotations(list(new Annotation(MinExclusive.class).args(Map.of(VALUE, "not an object"))))
                    .minExclusive()
            );
        }

        @Test void testReportConflictingMinExclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(
                            new Annotation(MinExclusive.class).args(Map.of(VALUE, "100")),
                            new Annotation(MinExclusive.class).args(Map.of(VALUE, "10"))
                    ))
                    .minExclusive()
            );
        }


        @Test void testIdentifyMaxExclusive() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .maxExclusive()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MaxExclusive.class).args(Map.of(VALUE, "10"))))
                    .maxExclusive()
            ).contains(
                    integral(10)
            );

        }

        @Test void testReportMalformedMaxExclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MaxExclusive.class).args(Map.of(VALUE, "x")))
                    ).maxExclusive());
        }

        @Test void testReportUnsupportedMaxExclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(ZoneId.class.getCanonicalName())
                    .annotations(list(new Annotation(MaxExclusive.class).args(Map.of(VALUE, "not an object"))))
                    .maxExclusive()
            );
        }


        @Test void testIdentifyMinInclusive() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .minInclusive()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MinInclusive.class).args(Map.of(VALUE, "10"))))
                    .minInclusive()
            ).contains(
                    integral(10)
            );

        }

        @Test void testMergeMinInclusiveValues() {
            assertThat(new Method("value")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(
                            new Annotation(MinInclusive.class).args(Map.of(VALUE, "10")),
                            new Annotation(MinInclusive.class).args(Map.of(VALUE, "100"))
                    ))
                    .minInclusive()
            ).contains(
                    integral(100)
            );
        }

        @Test void testReportMalformedMinInclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MinInclusive.class).args(Map.of(VALUE, "x"))))
                    .minInclusive()
            );
        }

        @Test void testReportUnsupportedMinInclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(ZoneId.class.getCanonicalName())
                    .annotations(list(new Annotation(MinInclusive.class).args(Map.of(VALUE, "not an object"))))
                    .minInclusive()
            );
        }


        @Test void testIdentifyMaxInclusive() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .maxInclusive()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MaxInclusive.class).args(Map.of(VALUE, "10"))))
                    .maxInclusive()
            ).contains(
                    integral(10)
            );

        }

        @Test void testReportMalformedMaxInclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(MaxInclusive.class).args(Map.of(VALUE, "x"))))
                    .maxInclusive()
            );
        }

        @Test void testReportUnsupportedMaxInclusiveValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(ZoneId.class.getCanonicalName())
                    .annotations(list(new Annotation(MaxInclusive.class).args(Map.of(VALUE, "not an object"))))
                    .maxInclusive()
            );
        }


        @Test void testIdentifyMinLength() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .minLength()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(MinLength.class).args(Map.of(VALUE, 10))))
                    .minLength()
            ).contains(
                    10
            );

        }

        @Test void testIdentifyMaxLength() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .maxLength()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(MaxLength.class).args(Map.of(VALUE, 10))))
                    .maxLength()
            ).contains(
                    10
            );

        }


        @Test void testIdentifyPattern() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .pattern()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(Pattern.class).args(Map.of(VALUE, "keywords"))))
                    .pattern()
            ).contains(
                    "keywords"
            );

        }

        @Test void testReportMalformedPatterns() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(Pattern.class).args(Map.of(VALUE, "(not a regex"))))
                    .pattern()
            );
        }


        @Test void testIdentifyIn() {
            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .in()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(In.class).args(Map.of(VALUE, list("1", "2", "3")))))
                    .in()
            ).contains(set(
                    integral(1), integral(2), integral(3)
            ));
        }

        @Test void testReportMalformedInValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT).annotations(list(new Annotation(In.class).args(Map.of(VALUE, list("not an int"))))
                    )
                    .in()
            );
        }


        @Test void testIdentifyLanguageIn() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .languageIn()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(TEXT)
                    .annotations(list(new Annotation(LanguageIn.class).args(Map.of(VALUE, list("en", "it", "de")))))
                    .languageIn()
            ).contains(set(
                    ENGLISH, ITALIAN, GERMAN
            ));

        }

        @Test void testReportMalformedLanguageInLocales() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(TEXT)
                    .annotations(list(new Annotation(LanguageIn.class).args(Map.of(VALUE, list("not a locale")))))
                    .languageIn()
            );
        }

        @Test void testReportOnNonTextualTypes() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(LanguageIn.class).args(Map.of(VALUE, list("en")))))
                    .languageIn()
            );
        }


        @Test void testIdentifyUniqueLang() {
            assertThat(new Method("x").type(STRING).uniqueLang()).isFalse();
            assertThat(new Method("x").type(TEXT).uniqueLang()).isFalse();
            assertThat(new Method("x").type(TEXTS).uniqueLang()).isTrue();
            assertThat(new Method("x").type(TEXTSETS).uniqueLang()).isFalse();
        }


        @Test void testIdentifyMinCount() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .minCount()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(SET)
                    .item(STRING)
                    .annotations(list(new Annotation(MinCount.class).args(Map.of(VALUE, 10))))
                    .minCount()
            ).contains(
                    10
            );

        }

        @Test void testIdentifyImpliedMinCountIfRequired() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(Required.class)))
                    .minCount()
            ).contains(
                    1
            );

        }

        @Test void testReportMultipleMinCountOnScalars() {

            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(MinCount.class).args(Map.of("value", 10))))
                    .minCount()
            );

        }


        @Test void testIdentifyMaxCount() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(SET)
                    .item(STRING)
                    .maxCount()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(SET)
                    .item(STRING)
                    .annotations(list(new Annotation(MaxCount.class).args(Map.of(VALUE, 10))))
                    .maxCount()
            ).contains(
                    10
            );

        }

        @Test void testIdentifyImpliedMaxCountIfScalar() {

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .maxCount()
            ).contains(
                    1
            );

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(SET)
                    .item(STRING)
                    .maxCount()
            ).isEmpty();

        }

        @Test void testReportMultipleMaxCountOnScalars() {

            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(STRING)
                    .annotations(list(new Annotation(MaxCount.class).args(Map.of("value", 10))))
                    .maxCount()
            );

        }


        @Test void testIdentifyHasValue() {
            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .hasValue()
            )
                    .isEmpty();

            assertThat(new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(HasValue.class).args(Map.of(VALUE, list("1", "2", "3")))))
                    .hasValue()
            ).contains(set(
                    integral(1), integral(2), integral(3)
            ));
        }

        @Test void testReportMalformedHasValueValues() {
            assertThatExceptionOfType(FrameException.class).isThrownBy(() -> new Method("x")
                    .clazz(new Clazz("X"))
                    .type(INT)
                    .annotations(list(new Annotation(HasValue.class).args(Map.of(VALUE, list("not an value")))))
                    .hasValue()
            );
        }

    }

}