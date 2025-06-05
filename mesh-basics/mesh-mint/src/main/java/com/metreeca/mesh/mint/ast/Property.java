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

/**
 * Property URI mapping annotation for frame methods.
 *
 * <p>Internal annotation used by the code generation process to capture
 * the resolved URI mappings for frame interface properties. Associates methods with their corresponding RDF property
 * URIs for both forward and reverse relationships.</p>
 */
public @interface Property {

    /**
     * Retrieves the absolute implied forward URI.
     *
     * @return the absolute URI for the forward property mapping
     */
    String implied();

    /**
     * Retrieves the absolute forward URI.
     *
     * @return the absolute forward URI; empty string if undefined
     */
    String forward() default "";

    /**
     * Retrieves the absolute reverse URI.
     *
     * @return the absolute reverse URI; empty string if undefined
     */
    String reverse() default "";

}
