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
import com.metreeca.mesh.tools.Store;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.queries.Transform.YEAR;
import static com.metreeca.mesh.test.stores.StoreTest.*;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.set;
import static com.metreeca.shim.URIs.base;
import static com.metreeca.shim.URIs.item;

import static java.util.Comparator.comparing;
import static org.assertj.core.api.Assertions.assertThat;

public abstract class StoreTestRetrieveValues {

    protected abstract Store store();

    @Nested
    final class Fetching {

        @Test void testFetchEmptyModel() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .hasSameSizeAs(Employees)
            );
        }

        @Test void testFetchEmptyResultSet() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()
                            .model(object(shape(Employee)))
                            .where(label, criterion().like("none"))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .isEmpty()
            );
        }

        @Test void testFetchModel() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(
                            id(base()),
                            entry(label, String()),
                            entry(seniority, Integer())
                    ))))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .allSatisfy(employee -> {

                        final Value expected=Employees.stream()
                                .filter(e -> e.id().equals(employee.id()))
                                .findFirst()
                                .orElseThrow();

                        // specified by model

                        assertThat(employee.get(label)).isEqualTo(expected.get(label));
                        assertThat(employee.get(seniority)).isEqualTo(expected.get(seniority));

                        // not specified by model

                        assertThat(employee.get(surname)).isEqualTo(Nil());
                        assertThat(employee.get(supervisor)).isEqualTo(Nil());
                        assertThat(employee.get(birthdate)).isEqualTo(Nil());

                    })
            );
        }

        @Test void testFetchNestedModel() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(
                            id(base()),
                            entry(supervisor, object(
                                    entry(label, String())
                            ))
                    ))))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))

                    .allSatisfy(employee -> assertThat(employee.get(supervisor).get(label))
                            .isEqualTo(employee.id()
                                    .flatMap(StoreTest::Employee)
                                    .flatMap(value -> value.get(supervisor).id())
                                    .flatMap(StoreTest::Employee)
                                    .map(supervisor -> supervisor.get(label))
                                    .orElseGet(Value::Nil)
                            )
                    )

            );
        }

    }

    @Nested
    final class Filtering {

        @Test void testHandleLtConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(seniority, criterion().lt(integer(3)))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(seniority).integral().orElse(0L) < 3)
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleGtConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(seniority, criterion().gt(integer(3)))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(seniority).integral().orElse(0L) > 3)
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleLteConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(seniority, criterion().lte(integer(3)))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(seniority).integral().orElse(0L) <= 3)
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleGteConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(seniority, criterion().gte(integer(3)))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(seniority).integral().orElseThrow() >= 3)
                            .map(Value::id)
                            .toList()
                    )
            );
        }


        @Test void testHandleLikeConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(label, criterion().like("ger bo"))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(label).string().orElseThrow().equals("Gerard Bondur"))
                            .map(Value::id)
                            .toList()
                    )
            );
        }


        @Test void testHandleRootAnyConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(id(base())))
                            .where("", criterion().any(
                                    object(id(item("/employees/1056"))),
                                    object(id(item("/employees/1088")))
                            ))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .map(Value::id)
                            .filter(id -> set(
                                    item("/employees/1056"),
                                    item("/employees/1088")
                            ).contains(id.orElseThrow()))
                            .toList()
                    )
            );
        }

        @Test void testHandleSingletonAnyConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(supervisor, criterion().any(
                                    object(id("/employees/1088"))
                            ))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
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

        @Test void testHandleMultipleAnyConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(supervisor, criterion().any(
                                    object(id(item("/employees/1056"))),
                                    object(id(item("/employees/1088")))
                            ))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(supervisor).id()
                                    .filter(id -> id.equals(item("/employees/1056"))
                                                  || id.equals(item("/employees/1088"))
                                    )
                                    .isPresent()
                            )
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleExistentialAnyConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(supervisor, criterion().any(set()))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(supervisor).object()
                                    .isPresent()
                            )
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleNonExistentialAnyConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(supervisor, criterion().any(Nil()))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(supervisor).id().isEmpty())
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleMixedAnyConstraints() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(supervisor, criterion().any(
                                    Nil(),
                                    object(id(item("/employees/1002")))
                            ))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> {

                                final Optional<URI> id=employee.get(supervisor).id();

                                return id.isEmpty()
                                       || id.filter(uri -> uri.equals(item("/employees/1002"))).isPresent();

                            })
                            .map(Value::id)
                            .toList()
                    )
            );
        }


        @Test void testFilterOnExpression() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(
                                    expression().path(supervisor, seniority),
                                    criterion().gte(integer(3))
                            )
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(supervisor).id()
                                    .flatMap(StoreTest::Employee)
                                    .flatMap(supervisor -> supervisor.get(seniority).integral())
                                    .filter(seniority -> seniority >= 3)
                                    .isPresent()
                            )
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testFilterOnComputedExpression() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(
                                    expression().pipe(YEAR).path(birthdate),
                                    criterion().gte(integer(2000))
                            )
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(birthdate).localDate()
                                    .filter(birthdate -> birthdate.getYear() >= 2000)
                                    .isPresent()
                            )
                            .map(Value::id)
                            .toList()

                    )
            );
        }

    }

    @Nested
    final class Sorting {

        @Test void testSortByDefault() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(id(base())))))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testSortOnRoot() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(id(base())))
                            .where("", criterion().order(-1))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .sorted(Comparator.<Value, String>comparing(employee -> employee.id()
                                    .map(URI::toString)
                                    .orElseThrow()
                            ).reversed())
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testSortOnFieldIncreasing() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(expression().path(label), criterion().order(1))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .sorted(comparing(employee -> employee
                                    .get(label)
                                    .string()
                                    .orElseThrow()
                            ))
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testSortOnFieldDecreasing() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(expression().path(label), criterion().order(-1))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .sorted(Comparator.<Value, String>comparing(employee -> employee
                                    .get(label)
                                    .string()
                                    .orElseThrow()
                            ).reversed())
                            .map(Value::id)
                            .toList()
                    )
            );
        }


        @Test void testSortOnExpression() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(expression().path(supervisor, label), criterion().order(1))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .sorted(comparing(employee -> employee.get(supervisor).id()
                                    .flatMap(StoreTest::Employee)
                                    .flatMap(supervisor -> supervisor.get(label).string())
                                    .orElse("")
                            ))
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testSortOnComputedExpression() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(expression().pipe(YEAR).path(birthdate), criterion().order(1))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .sorted(comparing(employee -> employee
                                    .get(birthdate).localDate()
                                    .map(LocalDate::getYear)
                                    .orElse(0)
                            ))
                            .map(Value::id)
                            .toList()
                    )
            );
        }


        @Test void testSortOnMultipleCriteria() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(seniority, criterion().order(-2))
                            .where(label, criterion().order(1))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .sorted(Comparator.<Value, Long>comparing(employee -> employee
                                            .get(seniority).integral()
                                            .orElseThrow()
                                    ).reversed()
                                    .thenComparing(comparing(employee -> employee
                                            .get(label).string()
                                            .orElseThrow()
                                    )))
                            .map(Value::id)
                            .toList()
                    )
            );
        }


        @Test void testHandleRange() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(id(base())))

                            .offset(5)
                            .limit(10))))

            )).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .skip(5)
                            .limit(10)
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testHandleDefaultRange() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(object(id(base())))

                            .offset(0)
                            .limit(0))))

            )).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .map(Value::id)
                            .toList()
                    )
            );
        }

    }

    @Nested
    final class Focusing {

        @Test void testFocusOnPlainExpression() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    entry(label, String())
                            ))

                            .where(label, criterion().focus(string("Mary Patterson")))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(array(Employees.stream()

                            .sorted(
                                    comparing((Value employee) -> employee.get(label).string()
                                            .filter("Mary Patterson"::equals)
                                            .isPresent()
                                    ).reversed()
                            )
                            .map(employee -> object(
                                    shape(Employee),
                                    field(label, employee.get(label))
                            ))

                            .toList()

                    ))

            );
        }

    }

}
