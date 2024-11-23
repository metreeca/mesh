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

import com.metreeca.mesh.Value;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Custom SHACL constraint function annotation.
 *
 * <p>Class-level annotation that specifies a custom validation function to be applied during shape constraint
 * validation. Used to implement domain-specific constraints beyond the standard SHACL constraints.</p>
 *
 * <p>This annotation can be repeated on a class using {@link Constraints} as the container annotation.</p>
 *
 * @see Constraints Container annotation for multiple constraint declarations
 */
@Target(TYPE)
@Retention(RUNTIME)
@Repeatable(Constraints.class)
public @interface Constraint {

    /**
     * Retrieves the constraint function class.
     *
     * @return a class implementing {@code Function<?, Value>} that performs the validation
     *
     * @throws NullPointerException if the value is {@code null}
     */
    public Class<? extends Function<?, Value>> value();

}
