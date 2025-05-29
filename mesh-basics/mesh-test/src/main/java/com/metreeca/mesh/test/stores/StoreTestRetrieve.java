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

package com.metreeca.mesh.test.stores;


import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.tools.Store;

import org.junit.jupiter.api.Test;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Probe.probe;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.test.stores.StoreTest.*;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.URIs.base;

import static org.assertj.core.api.Assertions.assertThat;

abstract class StoreTestRetrieve {

    // !!! virtual model merging

    protected abstract Store store();


    @Test void testIgnoreUnknownIds() {
        assertThat(store().retrieve(object(

                id(item("/employees/999")),
                shape(Employee),

                field(label, string(""))

        ))).satisfies(v -> assertThat(v.isEmpty()).isTrue());

    }

    @Test void testRetrieveObjects() {

        assertThat(populate(store()).retrieve(object(

                id(item("/employees/1702")),
                shape(Employee),

                field(label, string("")),
                field(code, string("")),
                field(seniority, integer(0))

        ))).satisfies(employee -> {

            // specified by model

            assertThat(employee.get(label)).isEqualTo(string("Martin Gerard"));
            assertThat(employee.get(code)).isEqualTo(string("1702"));
            assertThat(employee.get(seniority)).isEqualTo(integer(2));

            // not specified by model

            assertThat(employee.get(surname)).isEqualTo(Nil());
            assertThat(employee.get(supervisor)).isEqualTo(Nil());
            assertThat(employee.get(reports)).isEqualTo(Nil());

        });

    }

    @Test void testRetrieveNestedObjects() {

        assertThat(populate(store()).retrieve(object(

                id(item("/employees/1702")),
                shape(Employee),

                field(supervisor, object(
                        entry(label, string("")),
                        entry(seniority, integer(0))
                ))

        )))

                .extracting(employee -> employee.get(supervisor))
                .satisfies(supervisor -> {

                    assertThat(supervisor.get(label)).isEqualTo(string("Gerard Bondur"));
                    assertThat(supervisor.get(seniority)).isEqualTo(integer(4));

                });

    }


    @Test void testHandleNakedValuesQueries() {
        assertThat(populate(store()).retrieve(value(query()

                .model(object(
                        shape(Employee),
                        id(base())
                ))

                .where(supervisor, criterion().any(
                        object(id(item("/employees/1088")))
                ))

        ))).satisfies(values -> assertThat(values.array().orElseThrow())
                .map(Value::id)
                .containsExactlyElementsOf(Employees.stream()
                        .filter(employee -> employee.get(supervisor).id()
                                .filter(id -> id.equals(item("/employees/1088")))
                                .isPresent()
                        )
                        .map(Value::id)
                        .toList()
                )
        );
    }

    @Test void testHandleNakedTuplesQueries() {
        assertThat(populate(store()).retrieve(value(query()

                .model(value(new Specs(Employee, list(
                        probe("count", expression("count:"), Integer())
                ))))

                .where(supervisor, criterion().any(
                        object(id(item("/employees/1088")))
                ))

        ))).satisfies(tuples -> assertThat(tuples)
                .isEqualTo(value(new Table(list(
                        new Tuple(list(
                                entry("count", integer(Employees.stream()
                                        .filter(employee -> employee.get(supervisor).id()
                                                .filter(id -> id.equals(item("/employees/1088")))
                                                .isPresent()
                                        )
                                        .count()
                                ))
                        ))
                ))))
        );
    }

}
