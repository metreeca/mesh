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
 * Exception thrown during codec operations.
 *
 * <p>Represents errors that occur during value encoding or decoding operations,
 * with optional position information for syntactic errors.</p>
 */
public final class CodecException extends RuntimeException {

    @Serial private static final long serialVersionUID=-1267685327499864471L;


    private final int line;
    private final int column;
    private boolean syntactic=true; // !!! remove


    /**
     * Creates a codec exception with a message.
     *
     * @param message the error message
     */
    public CodecException(final String message) {

        super(message);

        this.line=0;
        this.column=0;
    }

    /**
     * Creates a codec exception with position information.
     *
     * @param message   the error message
     * @param syntactic {@code true} if this is a syntactic error; {@code false} for semantic errors
     * @param line      the line number where the error occurred (1-based)
     * @param column    the column number where the error occurred (1-based)
     *
     * @throws IllegalArgumentException if line or column is less than 1
     */
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


    /**
     * Checks if this is a syntactic error.
     *
     * @return {@code true} for syntactic errors; {@code false} for semantic errors
     */
    public boolean isSyntactic() {
        return syntactic;
    }


    /**
     * Retrieves the line number where the error occurred.
     *
     * @return the line number (1-based), or 0 if not available
     */
    public int getLine() {
        return line;
    }

    /**
     * Retrieves the column number where the error occurred.
     *
     * @return the column number (1-based), or 0 if not available
     */
    public int getColumn() {
        return column;
    }

}
