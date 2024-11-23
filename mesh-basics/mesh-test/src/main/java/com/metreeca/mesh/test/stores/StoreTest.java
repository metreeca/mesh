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
import com.metreeca.mesh.shapes.Shape;
import com.metreeca.mesh.test.EmployeeFrame;
import com.metreeca.mesh.test.OfficeFrame;
import com.metreeca.mesh.test.Resources;
import com.metreeca.mesh.tools.Store;

import org.junit.jupiter.api.Nested;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.Decimal;
import static com.metreeca.mesh.Value.Instant;
import static com.metreeca.mesh.Value.Integer;
import static com.metreeca.mesh.Value.LocalDate;
import static com.metreeca.mesh.Value.String;
import static com.metreeca.mesh.Value.Text;
import static com.metreeca.mesh.Value.array;
import static com.metreeca.mesh.Value.decimal;
import static com.metreeca.mesh.Value.field;
import static com.metreeca.mesh.Value.id;
import static com.metreeca.mesh.Value.instant;
import static com.metreeca.mesh.Value.integer;
import static com.metreeca.mesh.Value.localDate;
import static com.metreeca.mesh.Value.object;
import static com.metreeca.mesh.Value.shape;
import static com.metreeca.mesh.Value.string;
import static com.metreeca.mesh.Value.text;
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.shapes.Type.type;
import static com.metreeca.shim.Collections.entry;
import static com.metreeca.shim.Collections.list;
import static com.metreeca.shim.Loggers.logging;
import static com.metreeca.shim.Resources.reader;
import static com.metreeca.shim.Resources.resource;
import static com.metreeca.shim.URIs.uri;

import static java.lang.Integer.parseInt;
import static java.lang.String.format;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.stream.Collectors.groupingBy;

public abstract class StoreTest {

    public static final URI BASE=uri("https://data.example.net/");


    static {
        logging(INFO, "com.metreeca");
        logging(FINE, "com.metreeca.mesh.rdf4j.SPARQLSelector");
    }


    public static EmployeeFrame employee(final URI id) {
        return new EmployeeFrame()
                .id(id)
                .code("0")
                .forename("forename")
                .surname("surname")
                .birthdate(LocalDate.EPOCH)
                .title("title")
                .seniority(1)
                .email("mail@example.net")
                .active(Instant.EPOCH)
                .office(new OfficeFrame().id(uri("/offices/1")));
    }


    //̸// !!! Remove ///////////////////////////////////////////////////////////////////////////////////////////////////


    public static URI item(final String path) {
        return BASE.resolve(path);
    }

    public static URI term(final String name) {
        return BASE.resolve("#"+name);
    }


    public static final URI OfficesId=item("/offices/");
    public static final URI EmployeesId=item("/employees/");


    public static final String label="label";
    public static final String members="members";
    public static final String code="code";

    public static final String city="city";
    public static final String country="country";
    public static final String employees="employees";

    public static final String forename="forename";
    public static final String surname="surname";
    public static final String birthdate="birthdate";
    public static final String title="title";
    public static final String seniority="seniority";
    public static final String email="email";
    public static final String active="active";
    public static final String ytd="ytd";
    public static final String last="last";
    public static final String delta="delta";
    public static final String office="office";
    public static final String supervisor="supervisor";
    public static final String reports="reports";


    public static final Shape Resource=shape().id("id")
            .property(property(label)
                    .forward(uri("http://www.w3.org/2000/01/rdf-schema#label"))
                    .shape(shape().datatype(String()).required())
            );

    public static final Shape Office=shape().clazz(type(term("Office"))).extend(Resource)

            .property(property(code).forward(term(code)).shape(shape().datatype(String()).required()
                    .pattern("^\\d+$")
            ))

            .property(property(city).forward(term(city)).shape(shape().datatype(String()).required()))
            .property(property(country).forward(term(country)).shape(shape().datatype(Text()).repeatable()
                    .uniqueLang(true)
            ))

            .property(property(employees).forward(term("employee")).shape(new Supplier<>() {
                @Override public Shape get() { return Employee.multiple(); }
            }));

    public static final Shape Employee=shape().clazz(type(term("Employee"))).extend(Resource)

            .property(property(code).forward(term(code)).shape(shape().datatype(String()).required()
                    .pattern("^\\d+$")
            ))

            .property(property(forename).forward(term(forename)).shape(shape().datatype(String()).required()))
            .property(property(surname).forward(term(surname)).shape(shape().datatype(String()).required()))
            .property(property(birthdate).forward(term(birthdate)).shape(shape().datatype(LocalDate()).required()))


            .property(property(title).forward(term(title)).shape(shape().datatype(String()).required()))

            .property(property(seniority).forward(term(seniority)).shape(shape().datatype(Integer()).required()
                    .minInclusive(integer(0))
                    .maxInclusive(integer(5))
            ))

            .property(property(email).forward(term(email)).shape(shape().datatype(String()).required()
                    .pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
            ))

            .property(property(active).forward(term(active)).shape(shape().datatype(Instant()).required()))


            .property(property(ytd).forward(term(ytd)).shape(shape().datatype(Decimal()).optional()
                    .minInclusive(decimal(0))
            ))

            .property(property(last).forward(term(last)).shape(shape().datatype(Decimal()).optional()
                    .minInclusive(decimal(0))
            ))

            .property(property(delta).forward(term(delta)).shape(shape().datatype(Decimal()).optional()))

            .property(property(office).forward(term(office)).shape(Office::required))

            .property(property(supervisor).forward(term(supervisor)).shape(new Supplier<>() {
                @Override public Shape get() { return Employee.optional(); }
            }))

            .property(property(reports).forward(term("report")).shape(new Supplier<>() {
                @Override public Shape get() { return Employee.multiple(); }
            }));


    public static Shape Catalog(final Shape shape) {
        return shape().extend(Resource)
                .virtual(true)
                .property(property(members).forward(term("member")).shape(shape));
    }


    public static final Collection<Value> Offices=list(parse(StoreTest::offices));
    public static final Collection<Value> Employees=list(parse(StoreTest::employees));


    public static List<Value> members(final Value container) {
        return container.get(members).array().orElseThrow();
    }

    public static Optional<Value> Office(final URI id) {
        return Offices.stream().filter(e -> e.id().filter(id::equals).isPresent()).findFirst();
    }

    public static Optional<Value> Employee(final URI id) {
        return Employees.stream().filter(e -> e.id().filter(id::equals).isPresent()).findFirst();
    }


    public static <T extends Store> T populate(final T store) {

        if ( store == null ) {
            throw new NullPointerException("null store");
        }

        store.execute(txn -> {
            txn.insert(array(Offices));
            txn.insert(array(Employees));
        });

        return store;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static Collection<Value> parse(
            final BiFunction<? super List<String>, ? super List<List<String>>, Stream<Value>> mapper
    ) {
        try ( final BufferedReader reader=new BufferedReader(reader(resource(Resources.class, ".tsv"))) ) {

            final List<List<String>> records=reader.lines()
                    .map(line -> Arrays.stream(line.split("\t"))
                            .map(v -> v.isBlank() ? null : v)
                            .toList()
                    )
                    .toList();

            final List<String> header=records.getFirst();

            return mapper.apply(header, records.subList(1, records.size())).toList();

        } catch ( final IOException e ) {

            throw new UncheckedIOException(e);

        }
    }


    private static Stream<Value> offices(final List<String> header, final Collection<List<String>> records) {
        return records.stream()

                .collect(groupingBy(record -> record.get(header.indexOf("office"))))

                .values()
                .stream()
                .map(List::getFirst)

                .map(record -> object(

                        id(OfficesId.resolve(record.get(header.indexOf("office")))),
                        shape(Office),

                        field(label, string(format("%s - %s",
                                record.get(header.indexOf("cityName")),
                                record.get(header.indexOf("countryNameEN"))
                        ))),

                        field(code, string(record.get(header.indexOf("office")))),
                        field(city, string(record.get(header.indexOf("cityName")))),
                        field(country, array(
                                text("en", record.get(header.indexOf("countryNameEN"))),
                                text("de", record.get(header.indexOf("countryNameDE"))),
                                text("fr", record.get(header.indexOf("countryNameFR"))),
                                text("it", record.get(header.indexOf("countryNameIT")))
                        )),

                        entry(employees, array(records.stream()
                                .filter(r -> r.get(header.indexOf("office")).equals(record.get(header.indexOf("office"))))
                                .map(r -> object(
                                        id(EmployeesId.resolve(r.get(header.indexOf("code")))),
                                        shape(Employee)
                                ))
                                .toList()
                        ))
                ));
    }

    private static Stream<Value> employees(final List<String> header, final Collection<List<String>> records) {
        return records.stream().map(record -> object(

                id(EmployeesId.resolve(record.get(header.indexOf("code")))),
                shape(Employee),

                field(label, string(format("%s %s",
                        record.get(header.indexOf("forename")),
                        record.get(header.indexOf("surname"))
                ))),

                field(code, string(record.get(header.indexOf("code")))),

                field(forename, string(record.get(header.indexOf("forename")))),
                field(surname, string(record.get(header.indexOf("surname")))),
                field(birthdate, localDate(LocalDate.parse(record.get(header.indexOf("birthdate"))))),

                field(title, string(record.get(header.indexOf("title")))),
                field(seniority, integer(parseInt(record.get(header.indexOf("seniority"))))),
                field(email, string(record.get(header.indexOf("email")))),
                field(active, instant(Instant.parse(record.get(header.indexOf("active"))))),

                field(ytd, Optional.ofNullable(record.get(header.indexOf("ytd")))
                        .map(Double::parseDouble)
                        .map(Value::decimal)
                        .orElseGet(Value::Nil)),

                field(last, Optional.ofNullable(record.get(header.indexOf("last")))
                        .map(Double::parseDouble)
                        .map(Value::decimal)
                        .orElseGet(Value::Nil)),

                field(delta, Optional.ofNullable(record.get(header.indexOf("delta")))
                        .map(Double::parseDouble)
                        .map(Value::decimal)
                        .orElseGet(Value::Nil)),

                field(office, object(
                        id(OfficesId.resolve(record.get(header.indexOf("office")))),
                        shape(Office)
                )),

                entry(supervisor, Optional.ofNullable(record.get(header.indexOf("supervisor")))
                        .map(s -> {
                            return object(
                                    id(EmployeesId.resolve(s)),
                                    shape(Employee)
                            );
                        })
                        .orElseGet(Value::Nil)
                ),

                entry(reports, array(records.stream()
                        .filter(r -> Objects.equals(r.get(header.indexOf("supervisor")), record.get(header.indexOf("code"))))
                        .map(r -> {
                            return object(
                                    id(EmployeesId.resolve(r.get(header.indexOf("code")))),
                                    shape(Employee)
                            );
                        })
                        .toList()
                ))

        ));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    protected abstract Store store();


    @Nested
    final class Retrieve extends StoreTestRetrieve {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class RetrieveFrame extends StoreTestRetrieveValues {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class RetrieveTuples extends StoreTestRetrieveTuples {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Create extends _StoreTestCreate {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Update extends _StoreTestUpdate {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Mutate extends _StoreTestMutate {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Delete extends _StoreTestDelete {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Insert extends _StoreTestInsert {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Remove extends _StoreTestRemove {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

    @Nested
    final class Modify extends _StoreTestModify {

        @Override public Store store() {
            return StoreTest.this.store();
        }

    }

}