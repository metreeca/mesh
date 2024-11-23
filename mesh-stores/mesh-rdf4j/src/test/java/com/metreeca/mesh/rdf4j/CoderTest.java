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

package com.metreeca.mesh.rdf4j;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.rdf4j.Coder.text;

import static org.assertj.core.api.Assertions.assertThat;

final class CoderTest {

    private String code(final CharSequence code) {
        return text(code).toString();
    }


    @Test void testCollapseFeeds() {
        assertThat(code("x\fy")).isEqualTo("x\n\ny");
        assertThat(code("x\n\f\n\fy")).isEqualTo("x\n\ny");
    }

    @Test void testCollapseFolds() {
        assertThat(code("x\ry")).isEqualTo("x y");
        assertThat(code("x\r\r\ry")).isEqualTo("x y");
        assertThat(code("x\n\ry")).isEqualTo("x\n\ny");
        assertThat(code("x\n\r\r\ry")).isEqualTo("x\n\ny");
    }

    @Test void testCollapseNewlines() {
        assertThat(code("x\ny")).isEqualTo("x\ny");
        assertThat(code("x\n\n\n\ny")).isEqualTo("x\ny");
    }

    @Test void testCollapseSpaces() {
        assertThat(code("x y")).isEqualTo("x y");
        assertThat(code("x    y")).isEqualTo("x y");
    }


    @Test void testIgnoreLeadingWhitespace() {
        assertThat(code(" {}")).isEqualTo("{}");
        assertThat(code("\n{}")).isEqualTo("{}");
        assertThat(code("\r{}")).isEqualTo("{}");
        assertThat(code("\f{}")).isEqualTo("{}");
        assertThat(code("\f \n\r{}")).isEqualTo("{}");
    }

    @Test void testIgnoreTrailingWhitespace() {
        assertThat(code("{} ")).isEqualTo("{}");
        assertThat(code("{}\n")).isEqualTo("{}");
        assertThat(code("{}\r")).isEqualTo("{}");
        assertThat(code("{}\f")).isEqualTo("{}");
        assertThat(code("{} \f\n\r")).isEqualTo("{}");
    }


    @Test void testIgnoreLineLeadingWhitespace() {
        assertThat(code("x\n  x")).isEqualTo("x\nx");
    }

    @Test void testIgnoreLineTrailingWhitespace() {
        assertThat(code("x  \nx")).isEqualTo("x\nx");
    }


    @Test void tesExpandFolds() {
        assertThat(code("x\rx\n\rx")).isEqualTo("x x\n\nx");
    }

    @Test void testStripWhitespaceInsidePairs() {
        assertThat(code("( x )")).isEqualTo("(x)");
        assertThat(code("[ x ]")).isEqualTo("[x]");
        assertThat(code("{ x }")).isEqualTo("{ x }");
    }


    @Test void testIndentBraceBlocks() {
        assertThat(code("{\nx\n}\ny")).isEqualTo("{\n    x\n}\ny");
        assertThat(code("{\f{ x }\f}")).isEqualTo("{\n\n    { x }\n\n}");
    }

    @Test void testInlineBraceBlocks() {
        assertThat(code("{ {\nx\n} }\ny")).isEqualTo("{ {\n    x\n} }\ny");
    }

}
