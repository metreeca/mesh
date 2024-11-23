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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Container annotation for multiple SHACL constraints.
 *
 * <p>Class-level annotation that serves as a container for multiple {@link Constraint} annotations.
 * This annotation is automatically used by the Java compiler when multiple {@code @Constraint} annotations are applied
 * to the same class.</p>
 *
 * <p>This annotation can also be included in {@link Alias} definitions to incorporate multiple custom
 * constraints into a reusable constraint set.</p>
 *
 * @see Constraint Individual constraint annotation
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface Constraints {

    /**
     * Retrieves the contained constraint annotations.
     *
     * @return an array of {@link Constraint} annotations
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public Constraint[] value();

}
