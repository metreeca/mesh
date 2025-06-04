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

package com.metreeca.mesh.mint.ast;

import static java.util.Objects.requireNonNull;

/**
 * Associates a value with its source element reference.
 *
 * <p>A utility record that links a value of any type with a string that identifies the source element
 * from which the value was derived. This allows for tracking the origin of values throughout the introspection process,
 * which is particularly useful for error reporting.</p>
 *
 * <p>Provides utility methods for working with class names, converting between fully qualified
 * names and simple names.</p>
 *
 * @param <T> The type of the referenced value
 */
public record Reference<T>(T value, String source) {

    public static String simple(final Class<?> clazz) {
        return clazz.getSimpleName();
    }

    public static String simple(final String type) {
        return type.substring(type.lastIndexOf('.')+1);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Reference {
        requireNonNull(value, "null value");
        requireNonNull(source, "null soure");
    }


    @Override public String toString() {
        return "<%s> @ <%s>".formatted(value, source);
    }

}
