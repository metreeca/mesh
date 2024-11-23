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

package com.metreeca.mesh.mint.tools;

import com.metreeca.mesh.mint.FrameException;
import com.metreeca.mesh.mint.ast.Clazz;
import com.metreeca.mesh.mint.ast.Method;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.mint.ast.Method.*;
import static com.metreeca.mesh.mint.tools.IntrospectorTest.with;
import static com.metreeca.shim.Collections.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

final class IntrospectorTestMethod {

    @Test void testIdentifyRegularTypeSlots() {
        with("method.IdentifyRegularTypeSlots", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods))
                        .map(s -> entry(s.generic(), s.name()))
                        .containsExactly(
                                entry("int", "integer"),
                                entry("String", "string"),
                                entry("Set<String>", "strings")
                        )
        );
    }

    @Test void testIdentifyFrameTypeSlots() {
        with("method.IdentifyFrameTypeSlots", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)).satisfies(slots -> {

                    assertThat(slots.stream()

                            .filter(slot -> slot.name().equals("frame"))
                            .findFirst()
                            .map(Method::generic)

                    ).contains("%s.method.IdentifyFrameTypeSlots".formatted(Introspector.class.getPackageName()));

                    assertThat(slots.stream()

                            .filter(slot -> slot.name().equals("frames"))
                            .findFirst()
                            .map(Method::generic)

                    ).contains("Set<%s.method.IdentifyFrameTypeSlots>".formatted(Introspector.class.getPackageName()));

                })
        );
    }

    @Test void testIdentifyEnumTypeSlots() {
        with("method.IdentifyEnumTypeSlots", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)).satisfies(slots -> {

                    assertThat(slots.stream()

                            .filter(method -> method.name().equals("option"))
                            .findFirst()

                    ).hasValueSatisfying(method -> {

                        assertThat(method.isEnum()).isTrue();
                        assertThat(method.in()).isPresent();
                        assertThat(method.generic()).isEqualTo("%s.method.IdentifyEnumTypeSlots.Option".formatted(
                                Introspector.class.getPackageName()
                        ));

                    });


                    assertThat(slots.stream()

                            .filter(method -> method.name().equals("options"))
                            .findFirst()

                    ).hasValueSatisfying(method -> {

                        assertThat(method.isEnum()).isTrue();
                        assertThat(method.in()).isPresent();
                        assertThat(method.generic()).isEqualTo("Set<%s.method.IdentifyEnumTypeSlots.Option>".formatted(
                                Introspector.class.getPackageName()
                        ));

                    });

                })
        );
    }

    @Test void testIdentifyCollectionTypeSlots() {
        with("method.IdentifyCollectionTypeSlots", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods))
                        .map(slot -> entry(slot.generic(), slot.name()))
                        .containsExactly(
                                entry("Set<String>", "set"),
                                entry("List<String>", "list"),
                                entry("Collection<String>", "collection")
                        )
        );
    }

    @Test void testIdentifySpecialTypeSlots() {
        with("method.IdentifySpecialTypeSlots", (introspector, type) ->
                assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)).map(s -> entry(s.type(), s.name()))
                        .containsExactly(

                                entry(TEXT, "text"),
                                entry(TEXTS, "texts"),
                                entry(TEXTSETS, "textsets"),

                                entry(DATA, "data"),
                                entry(DATAS, "datas"),
                                entry(DATASETS, "datasets")

                        )
        );
    }

    @Test void testIdentifyGenericTypeSlots() {
        with("method.IdentifyGenericTypeSlots", (introspector, type) -> {
            assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods))
                    .map(slot -> entry(slot.generic(), slot.name()))
                    .containsExactly(
                            entry("String", "value")
                    );
        });
    }


    @Test void testIgnoreStaticMethods() {
        with("method.IgnoreStaticMethods", (introspector, type) -> assertThat(introspector.clazz(type).lineage().flatMap(Clazz::methods)).isEmpty());
    }


    @Test void testReportVoidSlots() {
        with("method.ReportVoidSlots", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportGenericSlots() {
        with("method.ReportGenericSlots", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportFieldClassNameClashes() {
        with("method.ReportFieldClassNameClashes", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportReservedFieldNames() {
        with("method.ReportReservedFieldNames", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void ReportMethodsWithArguments() {
        with("method.ReportMethodsWithArguments", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportUnsupportedTypes() {
        with("method.ReportUnsupportedTypes", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportUnsupportedCollections() {
        with("method.ReportUnsupportedCollections", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportUnsupportedCollectionsItems() {
        with("method.ReportUnsupportedCollectionsItems", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportMethodsWithArguments() {
        with("method.ReportMethodsWithArguments", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

    @Test void testReportSelfReferencingEmbeddedValues() {
        with("method.ReportSelfReferencingEmbeddedValues", (introspector, type) ->
                assertThatExceptionOfType(FrameException.class).isThrownBy(() -> introspector.clazz(type))
        );
    }

}