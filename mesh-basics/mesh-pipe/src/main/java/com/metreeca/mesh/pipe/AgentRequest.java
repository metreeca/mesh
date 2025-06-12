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

package com.metreeca.mesh.pipe;

import com.metreeca.mesh.Value;

import java.io.InputStream;
import java.net.URI;
import java.util.function.Function;

/**
 * HTTP-style request abstraction for Agent processing.
 *
 * <p>Provides access to request metadata and content in a format suitable
 * for REST API processing.</p>
 */
public interface AgentRequest {

    /**
     * Retrieves the HTTP method for this request.
     *
     * @return the HTTP method (e.g., GET, POST, PUT, DELETE)
     */
    String method();

    /**
     * Retrieves the target resource URI for this request.
     *
     * @return the resource URI
     */
    URI resource();

    /**
     * Retrieves the query string for this request.
     *
     * @return the query string, or empty string if none
     */
    String query();

    /**
     * Retrieves a header value by name.
     *
     * @param name the header name
     *
     * @return the header value, or {@code null} if not present
     */
    String header(String name);

    /**
     * Retrieves the request body using the provided decoder function.
     *
     * @param body the function to decode the input stream
     *
     * @return the decoded value from the request body
     */
    Value input(Function<InputStream, Value> body);

}
