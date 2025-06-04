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

package com.metreeca.mesh.meta.shacl;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Language tag constraint for text properties.
 *
 * <p>Method-level annotation that restricts the language tags of textual property values to a set of allowed
 * languages.
 * Maps to the SHACL {@code sh:languageIn} constraint.</p>
 *
 * <p>May be only applied to textual properties (properties with return types {@code Entry<Locale, String>},
 * {@code Map<Locale, String>}, or {@code Map<Locale, Set<String>>}). The constraint restricts the language tags of text
 * values to the specified set of language tags.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#LanguageInConstraintComponent">SHACL LanguageIn Constraint
 *         Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface LanguageIn {

    /**
     * Retrieves the allowed language tags.
     *
     * @return an array of strings representing the language tags that are allowed for the property value
     * @throws NullPointerException if the value is {@code null}
     */
    public String[] value();

}
