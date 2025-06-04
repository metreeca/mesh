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


import com.metreeca.mesh.shapes.Shape;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.shapes.Shape.shape;

/**
 * Value transform.
 */
public enum Transform {

    COUNT(true) {
        @Override public Shape apply(final Shape shape) {
            return shape().datatype(Integral()).required();
        }
    },

    MIN(true) {
        @Override public Shape apply(final Shape shape) {
            return datatype(shape).optional();
        }
    },

    MAX(true) {
        @Override public Shape apply(final Shape shape) {
            return datatype(shape).optional();
        }
    },

    SUM(true) {
        @Override public Shape apply(final Shape shape) {
            return datatype(shape).optional();
        }
    },

    AVG(true) {
        @Override public Shape apply(final Shape shape) {
            return shape().datatype(Decimal()).optional(); // !!! review
        }
    },


    ABS(false) {
        @Override public Shape apply(final Shape shape) {
            return datatype(shape).optional();
        }
    },

    ROUND(false) {
        @Override public Shape apply(final Shape shape) {
            return shape().datatype(Integer()).optional(); // !!! review
        }
    },

    YEAR(false) {
        @Override public Shape apply(final Shape shape) {
            return shape().datatype(Integral()).optional();
        }
    };


    private static Shape datatype(final Shape shape) {
        return shape.datatype().map(t -> shape().datatype(t)).orElseGet(Shape::shape);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final boolean aggregate;


    private Transform(final boolean aggregate) {
        this.aggregate=aggregate;
    }


    public boolean isAggregate() {
        return aggregate;
    }


    public abstract Shape apply(final Shape shape);

}
