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
 * Value enumeration constraint.
 *
 * <p>Method-level annotation that constrains property values to a specified set of allowed values.
 * Maps to the SHACL {@code sh:in} constraint.</p>
 *
 * <p>Value strings are decoded using the property's datatype, which is determined from the method's return type.</p>
 *
 * <p>When applied to properties with enum return types, the {@code Introspector} automatically generates
 * an implicit {@code @In} constraint with all enum constants.</p>
 *
 * <p>This annotation can be used directly on methods in frame interfaces or included in {@link Alias}
 * definitions for reusability.</p>
 *
 * @see com.metreeca.mesh.meta.jsonld.Frame Frame annotation for defining JSON-LD frame interfaces
 * @see <a href="https://www.w3.org/TR/shacl/#InConstraintComponent">SHACL In Constraint Component</a>
 */
@Target(METHOD)
@Retention(RUNTIME)
public @interface In {

    /**
     * Retrieves the allowed property values.
     *
     * @return an array of strings representing the values that the property is allowed to have
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public String[] value();

}
