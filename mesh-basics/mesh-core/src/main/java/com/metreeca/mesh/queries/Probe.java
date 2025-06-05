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

/**
 * Computed value definition for tabular queries.
 *
 * <p>Defines a named computed value that can be evaluated using an expression and provides type information
 * through a value model. Probes are used in tabular queries to specify which computed values should be
 * included in the results.</p>
 *
 * @param name       the display name for this computed value
 * @param expression the expression that defines how to compute the value
 * @param model      the value model that defines the expected type and constraints for the computed value
 */
public final record Probe(

        String name,
        Expression expression,
        Value model

) {

    /**
     * Creates a new probe with the specified parameters.
     *
     * @param name       the display name for the computed value
     * @param expression the expression that defines the computation
     * @param model      the value model for type information
     *
     * @return a new probe instance
     *
     * @throws NullPointerException if any parameter is {@code null}
     */
    public static Probe probe(final String name, final Expression expression, final Value model) {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( expression == null ) {
            throw new NullPointerException("null expression");
        }

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

        return new Probe(name, expression, model);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Probe {

        if ( name == null ) {
            throw new NullPointerException("null name");
        }

        if ( expression == null ) {
            throw new NullPointerException("null expression");
        }

        if ( model == null ) {
            throw new NullPointerException("null model");
        }

    }

}
