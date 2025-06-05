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

package com.metreeca.mesh.toys.frames;

import com.metreeca.mesh.queries.Query;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.toys.frames.ToValue.BASE;
import static com.metreeca.mesh.toys.frames.ToValue.DFLT;
import static com.metreeca.shim.Collections.stash;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
final class ToValueTest {

    @Nested
    final class Full {

        @Test void testIncludeDefaultIds() {
            assertThat(new ToValueFrame().toValue().object().map(fs -> fs.get(ID)))
                    .contains(uri(BASE));
        }

        @Test void testIncludeDefaults() {
            assertThat(new ToValueFrame().toValue().object().map(fs -> fs.get("dflt")))
                    .contains(integral(DFLT));
        }

        @Test void testExcludeUndefinedValues() {
            assertThat(new ToValueFrame().toValue().object().map(fs -> fs.get("plain")))
                    .isEmpty();
        }

        @Test void testIncludeSetValues() {
            assertThat(new ToValueFrame().plain(10).toValue().object().map(fs -> fs.get("plain")))
                    .contains(integral(10));
        }

        @Test void testIncludeUnsetValues() {
            assertThat(new ToValueFrame().dflt(null).toValue().object().map(fs -> fs.get("dflt")))
                    .contains(Nil());
        }


        @Test void testPreserveStashingCollections() {
            assertThat(new ResourceFrame().resources(stash(query())).toValue()
                    .get("resources")
                    .value(Query.class)
            ).isPresent();
        }

    }

    @Nested
    final class Delta {

        @Test void testIncludeSetIds() {
            assertThat(new ToValueFrame(true).id(BASE.resolve("~")).toValue().object().map(fs -> fs.get(ID)))
                    .contains(uri(BASE.resolve("~")));
        }

        @Test void testExcludeDefaults() {
            assertThat(new ToValueFrame(true).toValue().object().map(fs -> fs.get("dflt")))
                    .isEmpty();
        }

        @Test void testExcludeUndefinedValues() {
            assertThat(new ToValueFrame(true).toValue().object().map(fs -> fs.get("plain")))
                    .isEmpty();
        }

        @Test void testIncludeSetValues() {
            assertThat(new ToValueFrame(true).plain(10).toValue().object().map(fs -> fs.get("plain")))
                    .contains(integral(10));
        }

        @Test void testIncludeUnsetValues() {
            assertThat(new ToValueFrame(true).dflt(null).toValue().object().map(fs -> fs.get("dflt")))
                    .contains(Nil());
        }


        @Test void testPreserveStashingCollections() {
            assertThat(new ResourceFrame(true).resources(stash(query())).toValue()
                    .get("resources")
                    .value(Query.class)
            ).isPresent();
        }

    }

}
