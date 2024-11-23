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

    MinExclusive MinExclusive() default @MinExclusive("");

    MaxExclusive MaxExclusive() default @MaxExclusive("");

    MinInclusive MinInclusive() default @MinInclusive("");

    MaxInclusive MaxInclusive() default @MaxInclusive("");


    MinLength MinLength() default @MinLength(0);

    MaxLength MaxLength() default @MaxLength(0);

    Pattern Pattern() default @Pattern("");

    In In() default @In({});

    LanguageIn LanguageIn() default @LanguageIn({});


    MinCount MinCount() default @MinCount(0);

    MaxCount MaxCount() default @MaxCount(0);

    HasValue HasValue() default @HasValue({});


    Constraints Constraints() default @Constraints({});

}
