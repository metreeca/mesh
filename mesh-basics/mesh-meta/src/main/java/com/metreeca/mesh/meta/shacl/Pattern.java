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

package com.metreeca.mesh.meta.shacl;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Regular expression pattern constraint.
 *
 * <p>Method-level annotation that constrains string property values to match a regular expression pattern.
 * Maps to the SHACL {@code sh:pattern} constraint.</p>
 *
 * <p>The pattern must be a valid Java regular expression compatible with {@link java.util.regex.Pattern}.</p>
 *
 * <p>Empty patterns (the default in {@link Alias}) are ignored. When the annotation is provided with a non-empty
 * pattern, all string values of the property must match the regular expression.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#PatternConstraintComponent">SHACL Pattern Constraint Component</a>
 * @see java.util.regex.Pattern Java regular expression pattern
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface Pattern {

    /**
     * Retrieves the regular expression pattern.
     *
     * @return a string containing a valid Java regular expression
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public String value();

}
