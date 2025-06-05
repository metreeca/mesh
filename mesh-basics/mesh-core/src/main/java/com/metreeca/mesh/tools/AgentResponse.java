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

import java.io.OutputStream;
import java.util.function.Consumer;

/**
 * HTTP-style response abstraction for Agent processing.
 *
 * <p>Provides methods to configure response metadata and content for
 * REST API operations.</p>
 */
public interface AgentResponse {

    /**
     * Configures the HTTP status code for this response.
     *
     * @param code the HTTP status code (e.g., 200, 404, 500)
     */
    void status(int code);

    /**
     * Configures a response header.
     *
     * @param name  the header name
     * @param value the header value
     */
    void header(String name, String value);

    /**
     * Configures the response body using the provided encoder function.
     *
     * @param body the function to write content to the output stream
     */
    void output(Consumer<OutputStream> body);

}
