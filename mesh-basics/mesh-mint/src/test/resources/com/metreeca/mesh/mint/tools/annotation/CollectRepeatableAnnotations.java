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
import com.metreeca.mesh.meta.jsonld.Namespace;
import com.metreeca.mesh.meta.shacl.Constraint;

import java.util.function.Function;

import static com.metreeca.mesh.Value.Nil;

@Frame
@Namespace(prefix="x", value="https://example.com/x")
@Namespace(prefix="y", value="https://example.com/y")
@Constraint(CollectRepeatableAnnotations.C1.class)
@Constraint(CollectRepeatableAnnotations.C2.class)
public interface CollectRepeatableAnnotations {

    public static final class C1 implements Function<CollectRepeatableAnnotations, Value> {

        @Override public Value apply(final CollectRepeatableAnnotations object) { return Nil(); }

    }

    public static final class C2 implements Function<CollectRepeatableAnnotations, Value> {

        @Override public Value apply(final CollectRepeatableAnnotations object) { return Nil(); }

    }

}
