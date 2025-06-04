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
 * Minimum string length constraint.
 *
 * <p>Method-level annotation that specifies the minimum length required for string property values.
 * Maps to the SHACL {@code sh:minLength} constraint.</p>
 *
 * <p>Only values greater than 0 are considered; a value of 0 (the default in {@link Alias}) is ignored.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see MaxLength Maximum string length constraint
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#MinLengthConstraintComponent">SHACL MinLength Constraint Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface MinLength {

    /**
     * Retrieves the minimum string length constraint.
     *
     * @return a non-negative integer representing the minimum required length for string values
     */
    public int value();

}
