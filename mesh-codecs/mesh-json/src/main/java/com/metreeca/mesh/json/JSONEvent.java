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

package com.metreeca.mesh.json;

/**
 * JSON streaming events for low-level JSON parsing.
 *
 * <p>Represents the different types of tokens encountered during
 * JSON parsing, including structural elements, literals, and
 * end-of-input markers.</p>
 */
enum JSONEvent {

    LBRACE("object opening brace"),
    RBRACE("object closing brace"),

    LBRACKET("array opening bracket"),
    RBRACKET("array closing bracket"),

    STRING("string literal"),
    NUMBER("number literal"),

    TRUE("<true> literal"),
    FALSE("<false> literal"),
    NULL("<null> literal"),

    COLON("colon"),
    COMMA("comma"),

    EOF("end of input");


    private final String description;


    private JSONEvent(final String description) {
        this.description=description;
    }


    /**
     * Retrieves the human-readable description of this event.
     *
     * @return a descriptive string for this JSON event type
     */
    public String description() {
        return description;
    }

}
