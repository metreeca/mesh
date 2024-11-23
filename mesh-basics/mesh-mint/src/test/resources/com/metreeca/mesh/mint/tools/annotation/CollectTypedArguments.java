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
import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.shacl.Constraint;
import com.metreeca.mesh.meta.shacl.HasValue;
import com.metreeca.mesh.meta.shacl.MinCount;
import com.metreeca.mesh.meta.shacl.Pattern;

import java.util.function.Function;

@Frame
@Constraint(CollectTypedArguments.Constraint.class)
public interface CollectTypedArguments {

    @Pattern("pattern")
    @HasValue({ "x", "y", "z" })
    @MinCount(10)
    String value();

    public final class Constraint implements Function<CollectTypedArguments, Value> {

        @Override public Value apply(final CollectTypedArguments target) {
            return Value.Nil();
        }
    }

}
