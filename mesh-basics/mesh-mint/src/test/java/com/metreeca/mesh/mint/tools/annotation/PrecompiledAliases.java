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

package com.metreeca.mesh.mint.tools.annotation;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.shacl.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.function.Function;

import static com.metreeca.mesh.Value.Nil;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Target(TYPE)
@Retention(RUNTIME)
@Alias(
        MinCount=@MinCount(1),
        MaxCount=@MaxCount(10),
        Constraints=@Constraints({
                @Constraint(PrecompiledAliases.C1.class),
                @Constraint(PrecompiledAliases.C2.class)
        })
)
public @interface PrecompiledAliases {

    public static final class C1 implements Function<Object, Value> {

        @Override public Value apply(final Object object) { return Nil(); }

    }

    public static final class C2 implements Function<Object, Value> {

        @Override public Value apply(final Object object) { return Nil(); }

    }

}
