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
 * Inclusive maximum value constraint.
 *
 * <p>Method-level annotation that specifies an upper bound (inclusive) for property values.
 * Maps to the SHACL {@code sh:maxInclusive} constraint.</p>
 *
 * <p>The string value is converted to the appropriate type based on the method's return type. The value string
 * must be decodable by the property's datatype.</p>
 *
 * <p>This constraint specifies that values must be less than or equal to the provided maximum value
 * (it includes the boundary value itself).</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see MaxExclusive Exclusive maximum value constraint
 * @see MinInclusive Inclusive minimum value constraint
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#MaxInclusiveConstraintComponent">SHACL MaxInclusive Constraint
 *         Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface MaxInclusive {

    /**
     * Retrieves the inclusive maximum value constraint.
     *
     * @return a string representation of the inclusive upper bound for property values
     * @throws NullPointerException if the value is {@code null}
     */
    public String value();

}
