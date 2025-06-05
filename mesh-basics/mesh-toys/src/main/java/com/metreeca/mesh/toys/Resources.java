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

package com.metreeca.mesh.toys;

import com.metreeca.mesh.meta.jsonld.Frame;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import static com.metreeca.mesh.toys.Event.Action.HIRED;
import static com.metreeca.mesh.toys.Event.Action.PROMOTED;
import static com.metreeca.shim.Collections.*;
import static com.metreeca.shim.Resources.reader;
import static com.metreeca.shim.Resources.resource;
import static com.metreeca.shim.URIs.uri;

import static java.lang.Integer.parseInt;
import static java.util.Locale.*;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;

/**
 * Master resource catalog with test data and factory methods.
 *
 * <p>Provides comprehensive test datasets including offices and employees
 * with realistic relationships, constraints, and multilingual content. Data is loaded from TSV files and automatically
 * converted to frame instances for testing and demonstration purposes.</p>
 */
@Frame
public interface Resources extends Catalog<Resource> {

    /**
     * Comprehensive office test dataset.
     */
    public static final List<OfficeFrame> OFFICES=list(parse(Resources::offices));

    /**
     * Comprehensive employee test dataset.
     */
    public static final List<EmployeeFrame> EMPLOYEES=list(parse(Resources::employees));


    public static Optional<OfficeFrame> office(final URI id) {
        return OFFICES.stream().filter(office -> Objects.equals(office.id(), id)).findFirst();
    }

    public static Optional<EmployeeFrame> employee(final URI id) {
        return EMPLOYEES.stream().filter(employee -> Objects.equals(employee.id(), id)).findFirst();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    default String label() {
        return "Resources";
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static URI office(final String id) {
        return uri("/offices/").resolve(id);
    }

    private static URI employee(final String id) {
        return uri("/employees/").resolve(id);
    }


    static Stream<OfficeFrame> offices(final List<String> header, final Collection<List<String>> records) {
        return records.stream()

                .collect(groupingBy(record -> record.get(header.indexOf("office"))))

                .values()
                .stream()
                .map(List::getFirst)

                .map(record -> new OfficeFrame()
                        .id(office(record.get(header.indexOf("office"))))
                        .code(record.get(header.indexOf("office")))

                        .city(record.get(header.indexOf("cityName")))
                        .country(map(
                                entry(ENGLISH, record.get(header.indexOf("countryNameEN"))),
                                entry(FRENCH, record.get(header.indexOf("countryNameFR"))),
                                entry(GERMAN, record.get(header.indexOf("countryNameDE")))
                        ))

                        .employees(set(records.stream()
                                .filter(r -> r.get(header.indexOf("office")).equals(record.get(header.indexOf("office"))))
                                .map(r -> new EmployeeFrame()
                                        .id(employee(r.get(header.indexOf("code"))))
                                )
                        ))
                );
    }

    private static Stream<EmployeeFrame> employees(final List<String> header, final Collection<List<String>> records) {
        return records.stream().map(record -> new EmployeeFrame()

                .id(employee(record.get(header.indexOf("code"))))
                .code(record.get(header.indexOf("code")))

                .forename(record.get(header.indexOf("forename")))
                .surname(record.get(header.indexOf("surname")))
                .birthdate(LocalDate.parse(record.get(header.indexOf("birthdate"))))

                .title(record.get(header.indexOf("title")))
                .seniority(parseInt(record.get(header.indexOf("seniority"))))
                .email(record.get(header.indexOf("email")))

                .active(Instant.parse(record.get(header.indexOf("active"))))

                .ytd(Optional.ofNullable(record.get(header.indexOf("ytd")))
                        .map(Double::parseDouble)
                        .orElse(0.0)
                )

                .last(Optional.ofNullable(record.get(header.indexOf("last")))
                        .map(Double::parseDouble)
                        .orElse(0.0)
                )

                .delta(Optional.ofNullable(record.get(header.indexOf("delta")))
                        .map(Double::parseDouble)
                        .orElse(0.0)
                )

                .office(new OfficeFrame()
                        .id(office(record.get(header.indexOf("office"))))
                )

                .supervisor(Optional.ofNullable(record.get(header.indexOf("supervisor")))
                        .map(s -> new EmployeeFrame().id(employee(s)))
                        .orElse(null)
                )

                .reports(set(records.stream()
                        .filter(r -> Objects.equals(r.get(header.indexOf("supervisor")), record.get(header.indexOf("code"))))
                        .map(r -> new EmployeeFrame().id(employee(r.get(header.indexOf("code")))))

                ))

                .career(set(Stream.concat(

                        Optional.ofNullable(record.get(header.indexOf("hired")))
                                .filter(not(String::isBlank))
                                .map(LocalDate::parse)
                                .map(hired -> new EventFrame()
                                        .action(HIRED)
                                        .date(hired)
                                )
                                .stream(),

                        Optional.ofNullable(record.get(header.indexOf("promoted")))
                                .filter(not(String::isBlank))
                                .map(LocalDate::parse)
                                .map(promoted -> new EventFrame()
                                        .action(PROMOTED)
                                        .date(promoted)
                                        .notes(Optional.ofNullable(record.get(header.indexOf("notes")))
                                                .filter(not(String::isBlank))
                                                .orElse(null)
                                        )
                                )
                                .stream()


                )))

        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <V> Collection<V> parse(
            final BiFunction<? super List<String>, ? super List<List<String>>, Stream<V>> mapper
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

}
