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

import java.util.Map;

import static com.metreeca.mesh.util.Strings.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

@Nested
final class StringsTest {

    @Nested
    final class TrimTest {

        @Test void testTrimLeading() {
            assertThat(trim(" \t\n<")).isEqualTo("<");
        }

        @Test void testTrimTrailing() {
            assertThat(trim(">\t\n")).isEqualTo(">");
        }

        @Test void testTrimLineLeading() {
            assertThat(trim("x\n \ty")).isEqualTo("x\ny");
        }

        @Test void testTrimLineTrailing() {
            assertThat(trim("x \t\ny")).isEqualTo("x\ny");
        }

        @Test void testCompactNewlines() {
            assertThat(trim("<\n>")).isEqualTo("<\n>");
            assertThat(trim("<\n\n\n\n>", 0)).isEqualTo("<\n>");
            assertThat(trim("<\n\n\n\n>", 1)).isEqualTo("<\n\n>");
            assertThat(trim("<\n\n\n\n>", 2)).isEqualTo("<\n\n\n>");
        }

        @Test void testPreserveEmbedded() {
            assertThat(trim("< \t>")).isEqualTo("< \t>");
        }

    }

    @Nested
    final class FoldTest {

        @Test void testFoldNewlines() {
            assertThat(fold("<\n>")).isEqualTo("<\n>");
            assertThat(fold("<\n\n\n\n>", 0)).isEqualTo("<\n>");
            assertThat(fold("<\n\n\n\n>", 1)).isEqualTo("<\n\n>");
            assertThat(fold("<\n\n\n\n>", 2)).isEqualTo("<\n\n\n>");
        }

        @Test void testIgnoreBlankLines() {
            assertThat(fold("<\n \t\n>")).isEqualTo("<\n>");
        }

    }

    @Nested
    final class PrettifyTest {

        @Test void test() {
            System.out.println(prettify("""
                    public static final Shape Employee=Shape.shape()
                        .clazz(Type.type("Employee", URI.create("https://data.example.org/#Employee")),
                            Type.type("Resource", URI.create("https://data.example.org/#Resource"))
                    )"""));
        }

        @Test void testPrettifyEmptyBlocks() {
            assertThat(prettify("{}")).isEqualTo("{}");
            assertThat(prettify("{\n\n}")).isEqualTo("{}");
            assertThat(prettify("{(\n\n)}")).isEqualTo("{()}");
            assertThat(prettify("{([\n\n])}")).isEqualTo("{([])}");
        }

        @Test void testIndent() {
            assertThat(prettify("""
                    {
                    
                        line
                    line
                    
                    }"""
            )).isEqualTo("""
                    {
                    
                        line
                        line
                    
                    }"""
            );
        }

    }

    @Nested
    final class QuoteTest {

        @Test void testQuote() {
            assertThat(quote("\"\t\n\\", '"')).isEqualTo("\"\\\"\\t\\n\\\\\"");
            assertThat(quote("\"\t\n\\", '\'')).isEqualTo("'\"\\t\\n\\\\'");
        }

    }


    @Nested
    final class FillTest {

        @Test void testReplaceVariables() {

            assertThat(fill("name:text", Map.of("name", "value")))
                    .as("no variables")
                    .isEqualTo("name:text");

            assertThat(fill("head name:{name} tail", Map.of("name", "value")))
                    .as("single")
                    .isEqualTo("head name:value tail");

            assertThat(fill("head x:{x} y:{y} tail", Map.of("x", "1", "y", "2")))
                    .as("multiple")
                    .isEqualTo("head x:1 y:2 tail");

        }

        @Test void testHandleModifiers() {

            assertThat(fill("http://{name}.com/?%{name}", Map.of("name", "a+b")))
                    .as("encoded")
                    .isEqualTo("http://a+b.com/?a%2Bb");

            assertThat(fill("#{name}={name}", Map.of("name", "value")))
                    .as("escaped")
                    .isEqualTo("{name}=value");

        }

        @Test void testReportUndefinedVariables() {
            assertThatIllegalArgumentException().isThrownBy(() ->
                    fill("head name:{name} tail", Map.of("none", "value"))
            );
        }

    }

}