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

package com.metreeca.mesh.util;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static com.metreeca.mesh.util.Collections.*;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

@Nested
@SuppressWarnings("DataFlowIssue")
final class CollectionsTest {

    private static final Collection<Object> NULLS=asList(null, null);


    @Nested
    final class SetTest {

        @Test void testIsIdempotent() {

            final Set<Integer> set=set(1);

            assertThat(set(set)).isSameAs(set);
        }

        @Test void testIsInteroperable() {

            assertThat(Set.of()).isEqualTo(set());
            assertThat(set()).isEqualTo(Set.of());
            assertThat(set().hashCode()).isEqualTo(Set.of().hashCode());

            assertThat(Set.of(1)).isEqualTo(set(1));
            assertThat(set(1)).isEqualTo(Set.of(1));
            assertThat(set(1).hashCode()).isEqualTo(Set.of(1).hashCode());

            assertThat(Set.of(1, 2, 3)).isEqualTo(set(1, 2, 3));
            assertThat(set(1, 2, 3)).isEqualTo(Set.of(1, 2, 3));
            assertThat(set(1, 2, 3).hashCode()).isEqualTo(Set.of(1, 2, 3).hashCode());

        }

        @Test void testPreserveInsertionOrder() {
            assertThat(set(1, 2, 3)).containsExactly(1, 2, 3);
        }


        @Test void testReportNulls() {
            assertThatNullPointerException().isThrownBy(() -> set((Object)null));
            assertThatNullPointerException().isThrownBy(() -> set(null, null));
            assertThatNullPointerException().isThrownBy(() -> set((Collection<?>)null));
            assertThatNullPointerException().isThrownBy(() -> set(Stream.of(null, null)));
        }

        @Test void testReportDeepNulls() {
            assertThatNullPointerException().isThrownBy(() -> set(NULLS));
            assertThatNullPointerException().isThrownBy(() -> set(NULLS, NULLS));
            assertThatNullPointerException().isThrownBy(() -> set(new HashSet<>(asList(NULLS, NULLS))));
            assertThatNullPointerException().isThrownBy(() -> set(Stream.of(NULLS, NULLS)));
        }

    }

    @Nested
    final class ListTest {

        @Test void testIsIdempotent() {

            final List<Integer> list=list(1);

            assertThat(list(list)).isSameAs(list);
        }

        @Test void testIsInteroperable() {

            assertThat(List.of()).isEqualTo(list());
            assertThat(list()).isEqualTo(List.of());
            assertThat(list().hashCode()).isEqualTo(List.of().hashCode());

            assertThat(List.of(1)).isEqualTo(list(1));
            assertThat(list(1)).isEqualTo(List.of(1));
            assertThat(list(1).hashCode()).isEqualTo(List.of(1).hashCode());

            assertThat(List.of(1, 2, 3)).isEqualTo(list(1, 2, 3));
            assertThat(list(1, 2, 3)).isEqualTo(List.of(1, 2, 3));
            assertThat(list(1, 2, 3).hashCode()).isEqualTo(List.of(1, 2, 3).hashCode());

        }

        @Test void testPreserveInsertionOrder() {
            assertThat(list(1, 2, 3)).containsExactly(1, 2, 3);
        }


        @Test void testReportNulls() {
            assertThatNullPointerException().isThrownBy(() -> list((Object)null));
            assertThatNullPointerException().isThrownBy(() -> list(null, null));
            assertThatNullPointerException().isThrownBy(() -> list((Collection<?>)null));
            assertThatNullPointerException().isThrownBy(() -> list(Stream.of(null, null)));
        }

        @Test void testReportDeepNulls() {
            assertThatNullPointerException().isThrownBy(() -> list(NULLS));
            assertThatNullPointerException().isThrownBy(() -> list(NULLS, NULLS));
            assertThatNullPointerException().isThrownBy(() -> list(new ArrayList<>(asList(NULLS, NULLS))));
            assertThatNullPointerException().isThrownBy(() -> list(Stream.of(NULLS, NULLS)));
        }

    }

    @Nested
    final class MapTest {

        @Test void testIsIdempotent() {

            final Map<String, Integer> map=map(entry("one", 1));

            assertThat(map(map)).isSameAs(map);
        }

        @Test void testIsInteroperable() {

            assertThat(Map.of()).isEqualTo(map());
            assertThat(map()).isEqualTo(Map.of());
            assertThat(map().hashCode()).isEqualTo(Map.of().hashCode());

            assertThat(Map.of("one", 1)).isEqualTo(map(entry("one", 1)));
            assertThat(map(entry("one", 1))).isEqualTo(Map.of("one", 1));
            assertThat(map(entry("one", 1)).hashCode()).isEqualTo(Map.of("one", 1).hashCode());

            assertThat(Map.of(
                    "one", 1,
                    "two", 2,
                    "three", 3
            )).isEqualTo(map(
                    entry("one", 1),
                    entry("two", 2),
                    entry("three", 3)
            ));

            assertThat(map(
                    entry("one", 1),
                    entry("two", 2),
                    entry("three", 3)
            )).isEqualTo(Map.of(
                    "one", 1,
                    "two", 2,
                    "three", 3
            ));

            assertThat(map(
                    entry("one", 1),
                    entry("two", 2),
                    entry("three", 3)
            ).hashCode()).isEqualTo(Map.of(
                    "one", 1,
                    "two", 2,
                    "three", 3
            ).hashCode());

        }

        @Test void testPreserveInsertionOrder() {
            assertThat(map(
                    entry("one", 1),
                    entry("two", 2),
                    entry("three", 3)
            )).containsExactly(
                    entry("one", 1),
                    entry("two", 2),
                    entry("three", 3)
            );
        }


        @Test void testReportNulls() {
            assertThatNullPointerException().isThrownBy(() -> map(null, null));
            assertThatNullPointerException().isThrownBy(() -> map(Stream.of(null, null)));
        }

        @Test void testReportDeepNullKeys() {

            final Entry<Object, Integer> entry=new SimpleImmutableEntry<>(null, 1);
            final Map<Object, Integer> map=new HashMap<>();

            map.put(null, 1);

            assertThatNullPointerException().isThrownBy(() -> map(entry, entry));
            assertThatNullPointerException().isThrownBy(() -> map(map));
            assertThatNullPointerException().isThrownBy(() -> map(Stream.of(entry)));

        }

        @Test void testReportDeeperNullKeys() {

            final Entry<Object, Integer> entry=new SimpleImmutableEntry<>(NULLS, 1);
            final Map<Object, Integer> map=new HashMap<>();

            map.put(NULLS, 1);

            assertThatNullPointerException().isThrownBy(() -> map(entry, entry));
            assertThatNullPointerException().isThrownBy(() -> map(map));
            assertThatNullPointerException().isThrownBy(() -> map(Stream.of(entry)));

        }

        @Test void testReportDeepNullValues() {

            final Entry<String, Integer> entry=new SimpleImmutableEntry<>("one", null);
            final Map<String, Integer> map=new HashMap<>();

            map.put("one", null);

            assertThatNullPointerException().isThrownBy(() -> map(entry, entry));
            assertThatNullPointerException().isThrownBy(() -> map(map));
            assertThatNullPointerException().isThrownBy(() -> map(Stream.of(entry)));

        }

        @Test void testReportDeeperNullValues() {

            final Entry<String, Collection<Object>> entry=new SimpleImmutableEntry<>("one", NULLS);
            final Map<String, Collection<Object>> map=new HashMap<>();

            map.put("one", NULLS);

            assertThatNullPointerException().isThrownBy(() -> map(entry, entry));
            assertThatNullPointerException().isThrownBy(() -> map(map));
            assertThatNullPointerException().isThrownBy(() -> map(Stream.of(entry)));

        }

    }

    @Nested
    final class EntryTest {

        @Test void testIsIdempotent() {

            final Entry<String, Integer> entry=entry("one", 1);

            assertThat(entry(entry)).isSameAs(entry);
        }

        @Test void testIsInteroperable() {

            assertThat(Map.entry("one", 1)).isEqualTo(entry("one", 1));
            assertThat(entry("one", 1)).isEqualTo(Map.entry("one", 1));
            assertThat(entry("one", 1).hashCode()).isEqualTo(Map.entry("one", 1).hashCode());

        }


        @Test void testReportNulls() {
            assertThatNullPointerException().isThrownBy(() -> entry("one", null));
            assertThatNullPointerException().isThrownBy(() -> entry(null, 1));
        }

        @Test void testReportDeepNulls() {
            assertThatNullPointerException().isThrownBy(() -> entry("one", null));
            assertThatNullPointerException().isThrownBy(() -> entry(null, 1));
        }

        @Test void testReportDeeperNulls() {
            assertThatNullPointerException().isThrownBy(() -> entry("one", NULLS));
            assertThatNullPointerException().isThrownBy(() -> entry(NULLS, 1));
        }

    }

}