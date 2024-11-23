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

package com.metreeca.mesh.util;

import static java.lang.String.format;

public final class Exceptions {

    public static <T> T error(final RuntimeException error) {

        if ( error == null ) {
            throw new NullPointerException("null error");
        }

        throw error;
    }

    public static <V> V unsupported(final Object value) {
        return error(new UnsupportedOperationException(format(
                "unsupported value type <%s>", (value == null ? Void.class : value.getClass()).getSimpleName()
        )));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Exceptions() { }

}
