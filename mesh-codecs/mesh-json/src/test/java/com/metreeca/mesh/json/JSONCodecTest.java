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

package com.metreeca.mesh.json;

import com.metreeca.mesh.Value;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.json.JSONCodec.json;

import static org.assertj.core.api.Assertions.assertThat;

@Nested
final class JSONCodecTest {

    @Nested
    final class EncodedQueryTest {

        @Test void testDecodeJSON() {
            assertThat(json().decode("{}")).isEqualTo(Value.Object());
        }

        @Test void testDecodeURLEncodedJSON() {
            assertThat(json().decode("%7B%7D")).isEqualTo(Value.Object());
        }

        @Test void testDecodeBase64JSON() {
            assertThat(json().decode("e30=")).isEqualTo(Value.Object());
        }

    }

}