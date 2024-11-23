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
import com.metreeca.mesh.queries.Probe;
import com.metreeca.mesh.queries.Specs;
import com.metreeca.mesh.queries.Table;
import com.metreeca.mesh.queries.Tuple;
import com.metreeca.mesh.tools.Store;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.LocalDate;

import static com.metreeca.mesh.Value.*;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Expression.expression;
import static com.metreeca.mesh.queries.Probe.probe;
import static com.metreeca.mesh.queries.Query.query;
import static com.metreeca.mesh.queries.Transform.*;
import static com.metreeca.mesh.test.stores.StoreTest.*;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.URIs.base;
import static com.metreeca.shim.URIs.item;

import static java.math.RoundingMode.HALF_UP;
import static java.util.Comparator.comparing;
import static java.util.Locale.ROOT;
import static java.util.Map.Entry.comparingByKey;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.assertj.core.api.Assertions.assertThat;

abstract class StoreTestRetrieveTuples {

    protected abstract Store store();


    @Nested
    final class Projecting {

        @Test void testProjectPlainTable() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("employee", expression().path(label), String()),
                                    probe("supervisor", expression().path(supervisor, label), String())
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .map(employee -> new Tuple(list(

                                    field("employee", employee.get(label)),
                                    field("supervisor", employee.get(supervisor).id()
                                            .flatMap(StoreTest::Employee)
                                            .map(supervisor -> supervisor.get(label))
                                            .orElseGet(Value::Nil)
                                    )

                            )))

                            .toList()

                    )))

            );
        }

        @Test void testProjectTotalTable() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(COUNT), Integer())
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(
                            new Tuple(list(field("value", integer(Employees.size()))))
                    ))))

            );
        }

        @Test void testProjectGroupedTable() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe(seniority, expression().path(seniority), Integer()),
                                    probe("value", expression().pipe(COUNT), Integer())
                            ))))

                    )))

            )).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .collect(groupingBy(

                                    employee -> employee.get(seniority).integer().orElse(BigInteger.ZERO),
                                    counting()

                            ))

                            .entrySet().stream()

                            .sorted(comparingByKey())

                            .map(entry -> new Tuple(list(
                                    field(seniority, integer(entry.getKey())),
                                    field("value", integer(entry.getValue()))
                            )))

                            .toList()

                    )))

            );
        }

        @Test void testProjectEmptyResultSet() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().path(label), String())
                            ))))
                                    .where(label, criterion().like("none"))

                    )))

            )).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list())))

            );
        }

        @Test void testProjectNestedModel() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("office", expression().path(office), object(entry(city, String()))),
                                    probe("employees", expression().pipe(COUNT), Integer())
                            ))))

                    )))

            )).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .collect(groupingBy(employee -> employee.get(office).id()
                                    .orElseThrow()
                            ))

                            .entrySet().stream()

                            .sorted(comparingByKey(URI::compareTo))

                            .map(entry -> new Tuple(list(
                                    entry("office", Office(entry.getKey())
                                            .map(office -> object(
                                                    shape(Office.required()),
                                                    field(city, office.get(city))
                                            ))
                                            .orElse(Nil())
                                    ),
                                    field("employees", integer(entry.getValue().size()))
                            )))

                            .toList()

                    )))

            );
        }

    }

    @Nested
    final class Computing {

        @Test void testComputeAbs() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(ABS).path(delta), Decimal())
                            ))))

                    ))


            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .map(employee -> new Tuple(list(
                                    field("value", employee.get(delta)
                                            .decimal()
                                            .map(BigDecimal::abs)
                                            .map(Value::decimal)
                                            .orElseGet(Value::Nil))
                            )))

                            .toList()

                    )))

            );
        }

        @Test void testComputeRound() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(ROUND).path(delta), Decimal())
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .map(employee -> new Tuple(list(
                                    entry("value", employee.get(delta)
                                            .decimal()
                                            .map(value -> value.setScale(0, HALF_UP))
                                            .map(Value::decimal)
                                            .orElseGet(Value::Nil)
                                    )
                            )))

                            .toList()

                    )))

            );
        }

        @Test void testComputeYear() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(YEAR).path(birthdate), Decimal())
                            ))))
                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .map(employee -> new Tuple(list(
                                    field("value", employee.get(birthdate)
                                            .localDate()
                                            .map(LocalDate::getYear)
                                            .map(Value::integer)
                                            .orElseGet(Value::Nil))
                            )))

                            .toList()

                    )))

            );
        }

    }

    @Nested
    final class Aggregating {

        @Test void testComputeCountDistinct() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(COUNT).path(office), Decimal())
                            ))))

                    )))

            )).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(
                            entry("value", integer(Employees.stream()
                                    .flatMap(employee -> employee.get(office).id().stream())
                                    .distinct()
                                    .count()
                            ))
                    ))))))

            );
        }

        @Test void testComputeMin() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(MIN).path(ytd), Decimal())
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(
                            entry("value", decimal(Employees.stream()
                                    .flatMap(employee -> employee.get(ytd).decimal().stream())
                                    .reduce((x, y) -> x.compareTo(y) <= 0 ? x : y)
                                    .orElse(null)
                            ))
                    ))))))

            );

        }

        @Test void testComputeMax() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                            probe("value", expression().pipe(MAX).path(ytd), Decimal())
                    ))))))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(
                            entry("value", decimal(Employees.stream()
                                    .flatMap(employee -> employee.get(ytd).decimal().stream())
                                    .reduce((x, y) -> x.compareTo(y) >= 0 ? x : y)
                                    .orElse(null)
                            ))
                    ))))))

            );
        }

        @Test void testComputeSum() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                            probe("value", expression().pipe(SUM).path(ytd), Decimal())
                    ))))))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(
                            entry("value", decimal(Employees.stream()
                                    .flatMap(employee -> employee.get(ytd).decimal().stream())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                            ))
                    ))))))

            );
        }

        @Test void testComputeAvg() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(ROUND, AVG).path(ytd), Decimal())
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(
                            entry("value", decimal(Employees.stream()
                                    .flatMap(employee -> employee.get(ytd).decimal().stream())
                                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                                    .divide(BigDecimal.valueOf(Employees.stream()
                                            .filter(not(employee -> employee.get(ytd).isEmpty()))
                                            .count()
                                    ), 0, HALF_UP)
                            ))
                    ))))))

            );
        }

    }

    @Nested
    final class Grouping {

        @Test void testGroupOnPlainExpression() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                            probe(seniority, expression().path(seniority), Integer()),
                            probe("value", expression().pipe(COUNT), Integer())
                    ))))))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .collect(groupingBy(

                                    employee -> employee.get(seniority),
                                    counting()

                            ))

                            .entrySet()
                            .stream()

                            .map(entry -> new Tuple(list(
                                    field(seniority, entry.getKey()),
                                    field("value", integer(entry.getValue()))
                            )))

                            .toList()

                    )))

            );
        }

        @Test void testGroupOnComputedExpression() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("x", expression().pipe(YEAR).path(birthdate), Integer()),
                                    probe("y", expression().pipe(COUNT), Integer())
                            ))))
                                    .where(expression().pipe(YEAR).path(birthdate), criterion().order(+1))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .collect(groupingBy(

                                    employee -> employee.get(birthdate).localDate()
                                            .map(LocalDate::getYear)
                                            .orElse(0),

                                    counting()

                            ))

                            .entrySet()
                            .stream()

                            .sorted(comparingByKey())

                            .map(entry -> new Tuple(list(
                                    field("x", integer(entry.getKey())),
                                    field("y", integer(entry.getValue()))
                            )))

                            .toList()

                    )))

            );
        }

    }

    @Nested
    final class Filtering {

        @Test void testFilterOnAggregateExpression() {
            assertThat(populate(store()).retrieve(object(

                    id(item("/employees/")),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    id(base())
                            ))

                            .where(
                                    expression().pipe(COUNT).path(reports),
                                    criterion().gte(integer(3))
                            ))))

            )).hasValueSatisfying(employees -> assertThat(members(employees))
                    .map(Value::id)
                    .containsExactlyElementsOf(Employees.stream()
                            .filter(employee -> employee.get(reports).values().count() >= 3)
                            .map(Value::id)
                            .toList()
                    )
            );
        }

        @Test void testFilteringIgnoresProjectedProperties() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().path(surname), String())
                            ))))
                                    .where(expression(label), criterion().like("mary"))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .filter(employee -> employee.get(label).string().stream()
                                    .anyMatch(label -> label.toLowerCase(ROOT).contains("mary"))
                            )

                            .map(employee -> new Tuple(list(
                                    field("value", employee.get(surname))
                            )))

                            .toList()

                    )))

            );
        }

    }

    @Nested
    final class Sorting {

        @Test void testSortOnAggregateExpression() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query()

                            .model(object(
                                    shape(Employee),
                                    entry(label, String())
                            ))

                            .where(expression().pipe(COUNT).path(reports), criterion().order(+1))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(array(Employees.stream()

                            .sorted(comparing(employee -> employee.get(reports).values().count()))

                            .map(employee -> object(
                                    shape(Employee),
                                    field(label, employee.get(label))
                            ))

                            .toList()

                    ))

            );
        }

        @Test void testSortingIgnoresProjectedProperties() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression(surname), String())
                            ))))
                                    .where(label, criterion().order(+1))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .sorted(comparing(employee -> employee.get(label).string().orElse("")))

                            .map(employee -> new Tuple(list(
                                    field("value", employee.get(surname))
                            )))

                            .toList()

                    )))

            );
        }

    }

    @Nested
    final class Focusing {

        @Test void testFocusingIgnoresProjectedProperties() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression(surname), String())
                            ))))
                                    .where(label, criterion().focus(string("Mary Patterson")))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .sorted(
                                    comparing((Value employee) -> employee.get(label).string()
                                            .filter("Mary Patterson"::equals)
                                            .isPresent()
                                    ).reversed()
                            )
                            .map(employee -> new Tuple(list(
                                    field("value", employee.get(surname))
                            )))

                            .toList()

                    )))
            );
        }

    }

    @Nested
    final class Analyzing {

        @Test void testAnalyzeStats() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    probe("value", expression().pipe(COUNT), Integer())
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(
                            field("value", integer(Employees.size()))
                    ))))))

            );
        }

        @Test void testAnalyzeOptions() {
            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    new Probe(office, expression().path(office), object(
                                            id(""),
                                            entry(label, String())
                                    )),
                                    probe("value", expression().pipe(COUNT), Integer())
                            ))))
                                    .where(expression().pipe(COUNT), criterion().order(-3))
                                    // !!! .criterion(expression().path(office, label), criterion().order(+2))
                                    .where(expression().path(office), criterion().order(+1))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(Employees.stream()

                            .collect(groupingBy(employee -> employee.get(office).id()
                                    .orElseThrow()
                            ))

                            .entrySet()
                            .stream()

                            .flatMap(e -> Office(e.getKey()).map(office -> new Tuple(list(
                                    field("office", object(
                                            shape(Office.required()),
                                            id(e.getKey()),
                                            entry(label, office.get(label))
                                    )),
                                    field("value", integer(e.getValue().size()))
                            ))).stream())

                            .sorted(comparing((Tuple tuple) -> tuple
                                            .value("value")
                                            .flatMap(Value::decimal)
                                            .orElseThrow()
                                    ).reversed()

                                            // !!! .thenComparing(tuple -> tuple
                                            //  .field("office")
                                            //  .map(Field::value)
                                            //  .map(x -> x.get(label))
                                            //  .flatMap(Value::string)
                                            //  .orElseThrow()
                                            //  )

                                            .thenComparing(tuple -> tuple
                                                    .value("office")
                                                    .flatMap(Value::id)
                                                    .map(URI::toString)
                                                    .orElseThrow()
                                            )

                            )

                            .toList()

                    )))

            );
        }

        @Test void testAnalyzeRange() {

            final Probe min=probe("min", expression().pipe(MIN).path(seniority), Integer());
            final Probe max=probe("max", expression().pipe(MAX).path(seniority), Integer());

            assertThat(populate(store()).retrieve(object(

                    id("/employees/"),
                    shape(Catalog(Employee)),

                    field(members, value(query(value(new Specs(Employee, list(
                                    min,
                                    max
                            ))))

                    ))

            ))).hasValueSatisfying(employees -> assertThat(employees.get(members))

                    .isEqualTo(value(new Table(list(new Tuple(list(

                            entry("min", integer(Employees.stream()
                                    .flatMap(e -> e.get(seniority).integral().stream())
                                    .mapToLong(value -> value)
                                    .min()
                                    .orElse(0)
                            )),

                            entry("max", integer(Employees.stream()
                                    .flatMap(e -> e.get(seniority).integral().stream())
                                    .mapToLong(value -> value)
                                    .max()
                                    .orElse(0)
                            ))

                    ))))))

            );
        }

    }

}
