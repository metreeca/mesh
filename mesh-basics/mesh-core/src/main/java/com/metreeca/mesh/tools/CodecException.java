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

package com.metreeca.mesh.tools;

import java.io.Serial;

import static java.lang.String.format;

/**
 * Serialization exception.
 */
public final class CodecException extends RuntimeException {

    @Serial private static final long serialVersionUID=-1267685327499864471L;


    private final int line;
    private final int column;
    private boolean syntactic=true;


    public CodecException(final String message) {

        super(message);

        this.line=0;
        this.column=0;
    }

    public CodecException(final String message, final boolean syntactic, final int line, final int column) {

        super(format("(%d,%d) %s", line, column, message));

        if ( line < 1 ) {
            throw new IllegalArgumentException(format("illegal line <%d>", line));
        }

        if ( column < 1 ) {
            throw new IllegalArgumentException(format("illegal column <%d>", column));
        }

        this.syntactic=syntactic;

        this.line=line;
        this.column=column;
    }


    public boolean isSyntactic() {
        return syntactic;
    }


    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

}
