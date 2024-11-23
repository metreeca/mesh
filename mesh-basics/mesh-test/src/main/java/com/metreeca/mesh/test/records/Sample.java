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

package com.metreeca.mesh.test.records;

import com.metreeca.mesh.Value;
import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.jsonld.Id;
import com.metreeca.mesh.meta.jsonld.Type;
import com.metreeca.mesh.meta.shacl.Constraint;

import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import static com.metreeca.mesh.Value.Nil;

@Frame
@Constraint(Sample.C1.class)
public interface Sample {

    @Id
    URI id();

    @Type
    String type();


    default String label() { return value(); }


    int primitive();

    String value();

    Set<String> values();


    Sample object();

    Set<Sample> objects();


    Entry<URI, String> data();

    Entry<Locale, String> text();

    Map<Locale, String> texts();

    Map<Locale, Set<String>> textsets();


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static class C1 implements Function<Sample, Value> {

        @Override
        public Value apply(final Sample sample) {
            return Nil();
        }

    }

}
