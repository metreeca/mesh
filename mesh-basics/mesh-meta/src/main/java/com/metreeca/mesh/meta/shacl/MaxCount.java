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
 * Maximum cardinality constraint.
 *
 * <p>Method-level annotation that specifies the maximum number of values that a property can have.
 * Maps to the SHACL {@code sh:maxCount} constraint.</p>
 *
 * <p>For non-collection properties (those not returning a collection or map type), setting a maximum greater than 1
 * will cause an exception.</p>
 *
 * <p>If no {@code @MaxCount} annotation is provided for a non-collection property, a default constraint of 1
 * is automatically applied.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see MinCount Minimum cardinality constraint
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#MaxCountConstraintComponent">SHACL MaxCount Constraint Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface MaxCount {

    /**
     * Retrieves the maximum allowed number of property values.
     *
     * @return a positive integer representing the maximum cardinality constraint
     */
    public int value();

}
