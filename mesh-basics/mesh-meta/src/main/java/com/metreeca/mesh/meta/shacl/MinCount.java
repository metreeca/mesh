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
 * Minimum cardinality constraint.
 *
 * <p>Method-level annotation that specifies the minimum number of values that a property must have.
 * Maps to the SHACL {@code sh:minCount} constraint.</p>
 *
 * <p>For non-collection properties (those not returning a collection or map type), setting a minimum greater than 1
 * will cause an exception.</p>
 *
 * <p>Only values greater than 0 are processed. A value of 1 indicates that the property is required.
 * The {@link Required} annotation is a convenient shorthand for {@code @MinCount(1)}.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see MaxCount Maximum cardinality constraint
 * @see Required Required property shorthand
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#MinCountConstraintComponent">SHACL MinCount Constraint Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface MinCount {

    /**
     * Retrieves the minimum required number of property values.
     *
     * @return a non-negative integer representing the minimum cardinality constraint
     */
    public int value();

}
