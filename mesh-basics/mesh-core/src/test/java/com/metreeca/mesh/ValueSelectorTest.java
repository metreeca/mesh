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

package com.metreeca.mesh;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.ValueSelector.select;
import static com.metreeca.shim.Collections.entry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

final class ValueSelectorTest {

    private static final Value value=object(
            field("string", string("string")),
            field("number", integer(10)),
            field("object", object(
                    entry("one", integer(1)),
                    entry("two", integer(2))
            )),
            field("array", array(
                    integer(1),
                    integer(2)
            )),
            field("'", integer(1))
    );


    @Test void testRootSelector() {
        assertThat(select(value, "$")).isEqualTo(value);
    }

    @Test void testDotNameSelector() {
        assertThat(select(value, "$.string")).isEqualTo(value.get("string"));
        assertThat(select(value, "$.unknown")).isEqualTo(Nil());
    }

    @Test void testBracketIndexSelector() {
        assertThat(select(value, "$.array[0]")).isEqualTo(value.get("array").get(0));
        assertThat(select(value, "$.array[10]")).isEqualTo(Nil());
    }

    @Test void testBracketNameSelector() {
        assertThat(select(value, "$['object']['one']")).isEqualTo(value.get("object").get("one"));
        assertThat(select(value, "$['unknown']")).isEqualTo(Nil());
    }

    @Test void testEscapedBracketNameSelector() {
        assertThat(select(value, "$['\\'']")).isEqualTo(value.get("'"));
    }

    @Test void testWildcardNameSelector() {
        assertThat(select(value, "$.object.*")).isEqualTo(value.get("object").get());
    }

    @Test void testWildcardIndexSelector() {
        assertThat(select(value, "$.array[*]")).isEqualTo(value.get("array").get());
    }

    @Test void testWildcardLeadingSelector() {
        assertThat(select(value, "*")).isEqualTo(value.get());
    }

    @Test void testRootlessSelector() {
        assertThat(select(value, "")).isEqualTo(value);
        assertThat(select(value, "array[0]")).isEqualTo(value.get("array").get(0));
    }


    @Test void testNestedSelectors() {
        assertThat(value.select("object.one")).isEqualTo(integer(1));
    }

    @Test void testNestedWildcardSelectors() {
        assertThat(value.select("*.one")).isEqualTo(array(integer(1)));
    }


    @Test void testReportMalformedPath() {
        assertThatIllegalArgumentException().isThrownBy(() -> select(value, "$.["));
        assertThatIllegalArgumentException().isThrownBy(() -> select(value, "[*]name"));
    }

}
