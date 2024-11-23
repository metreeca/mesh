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

package com.metreeca.mesh.meta;

import com.metreeca.mesh.Value;
import com.metreeca.shim.Collections;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Query.query;

import static org.assertj.core.api.Assertions.assertThat;

final class ValuesTest {

    @Nested
    final class ToValueTest {

        @Test void testConvertRegularCollections() {
            assertThat(Values.objects(Collections.set(List.of(1, 2, 3)), Value::integral))
                    .isEqualTo(array(integral(1), integral(2), integral(3)));
        }

        @Test void testConvertStashingCollections() {
            assertThat(Values.objects(Collections.<Integer>stash(query()), Value::integral))
                    .isEqualTo(value(query()));
        }

    }

}