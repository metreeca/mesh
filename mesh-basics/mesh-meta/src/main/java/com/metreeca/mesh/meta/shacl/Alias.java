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

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Composite SHACL constraint annotation.
 *
 * <p>Meta-annotation that groups multiple SHACL constraint annotations into a single custom annotation.
 * Used to define reusable constraint sets that can be applied as a single unit.</p>
 *
 * <p>All constraint values default to empty or zero to allow selective activation of specific constraints
 * in the alias definition.</p>
 */
@Target(ANNOTATION_TYPE)
@Retention(RUNTIME)
public @interface Alias {

    /**
     * Retrieves the minimum exclusive value constraint.
     *
     * @return the minimum exclusive constraint; ignored if empty
     */
    MinExclusive MinExclusive() default @MinExclusive("");

    /**
     * Retrieves the maximum exclusive value constraint.
     *
     * @return the maximum exclusive constraint; ignored if empty
     */
    MaxExclusive MaxExclusive() default @MaxExclusive("");

    /**
     * Retrieves the minimum inclusive value constraint.
     *
     * @return the minimum inclusive constraint; ignored if empty
     */
    MinInclusive MinInclusive() default @MinInclusive("");

    /**
     * Retrieves the maximum inclusive value constraint.
     *
     * @return the maximum inclusive constraint; ignored if empty
     */
    MaxInclusive MaxInclusive() default @MaxInclusive("");

    /**
     * Retrieves the minimum string length constraint.
     *
     * @return the minimum length constraint; ignored if 0
     */
    MinLength MinLength() default @MinLength(0);

    /**
     * Retrieves the maximum string length constraint.
     *
     * @return the maximum length constraint; ignored if 0
     */
    MaxLength MaxLength() default @MaxLength(0);

    /**
     * Retrieves the string pattern constraint.
     *
     * @return the pattern constraint; ignored if empty
     */
    Pattern Pattern() default @Pattern("");

    /**
     * Retrieves the enumeration value constraint.
     *
     * @return the enumeration constraint; ignored if empty
     */
    In In() default @In({});

    /**
     * Retrieves the language tag constraint.
     *
     * @return the language constraint; ignored if empty
     */
    LanguageIn LanguageIn() default @LanguageIn({});

    /**
     * Retrieves the minimum cardinality constraint.
     *
     * @return the minimum count constraint; ignored if 0
     */
    MinCount MinCount() default @MinCount(0);

    /**
     * Retrieves the maximum cardinality constraint.
     *
     * @return the maximum count constraint; ignored if 0
     */
    MaxCount MaxCount() default @MaxCount(0);

    /**
     * Retrieves the required value constraint.
     *
     * @return the required value constraint; ignored if empty
     */
    HasValue HasValue() default @HasValue({});

    /**
     * Retrieves the custom constraint collection.
     *
     * @return the custom constraints; ignored if empty
     */
    Constraints Constraints() default @Constraints({});

}
