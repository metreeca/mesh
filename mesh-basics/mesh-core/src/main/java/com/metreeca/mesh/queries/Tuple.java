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

package com.metreeca.mesh.queries;

import com.metreeca.mesh.Value;

import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import static com.metreeca.shim.Collections.list;

import static java.lang.String.format;
import static java.util.stream.Collectors.joining;

/**
 * Tabular entry.
 */
public record Tuple(

        List<Entry<String, Value>> fields

) {

    public Tuple(final List<Entry<String, Value>> fields) {

        if ( fields == null || fields.stream().anyMatch(Objects::isNull) ) {
            throw new NullPointerException("null fields");
        }

        this.fields=list(fields);
    }


    public Optional<Value> value(final String name) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        return fields().stream()
                .filter(field -> field.getKey().equals(name))
                .findFirst()
                .map(Entry::getValue);
    }


    @Override public String toString() {
        return format("Tuple({%s})", fields.isEmpty() ? "" : fields.stream()
                .map(field -> format("%s: %s",
                        field.getKey(), field.getValue().toString().replace("\n", "\n\t")
                ))
                .collect(joining(",\n\t", "\n\t", "\n"))
        );
    }

}
