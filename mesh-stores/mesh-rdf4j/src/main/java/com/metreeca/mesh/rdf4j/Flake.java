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

package com.metreeca.mesh.rdf4j;

import com.metreeca.mesh.queries.Criterion;
import com.metreeca.mesh.queries.Expression;
import com.metreeca.mesh.queries.Transform;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Shape;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.shim.Collections.map;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

final class Flake {

    static Flake flake(final Shape shape, final Map<Expression, Criterion> expressions) {

        final Map<List<Transform>, Criterion> criteria=expressions.entrySet().stream()
                .filter(e -> e.getKey().path().isEmpty())
                .collect(toMap(
                        e -> e.getKey().pipe(),
                        Entry::getValue
                ));

        final Map<Property, Flake> flakes=expressions.entrySet().stream()
                .filter(not(e -> e.getKey().path().isEmpty()))
                .collect(groupingBy(
                        e -> shape.property(e.getKey().path().getFirst()).orElseThrow(() ->
                                new RuntimeException(format("unknown property <%s>", e.getKey().path().getFirst()))
                        ),
                        toMap(
                                e -> new Expression(
                                        e.getKey().pipe(),
                                        e.getKey().path().subList(1, e.getKey().path().size())
                                ),
                                Entry::getValue
                        )
                ))
                .entrySet()
                .stream()
                .collect(toMap(Entry::getKey, e ->
                        flake(e.getKey().shape(), e.getValue())
                ));

        return new Flake(shape, criteria, flakes);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Shape shape;
    private final Map<List<Transform>, Criterion> criteria;
    private final Map<Property, Flake> flakes;


    private Flake(final Shape shape, final Map<List<Transform>, Criterion> criteria, final Map<Property, Flake> flakes) {
        this.shape=shape;
        this.criteria=map(criteria);
        this.flakes=map(flakes);
    }


    public boolean required() {
        return criteria.entrySet().stream()

                       .anyMatch(e -> {

                           final List<Transform> pipe=e.getKey();
                           final Criterion criterion=e.getValue();

                           final boolean filter=criterion.lt().isPresent()
                                                || criterion.gt().isPresent()
                                                || criterion.lte().isPresent()
                                                || criterion.gte().isPresent()

                                                || criterion.like().isPresent()
                                                || criterion.any()
                                                        .filter(values -> values.stream()
                                                                .noneMatch(value -> value.equals(Nil()))
                                                        )
                                                        .isPresent();

                           return pipe.stream().noneMatch(Transform::isAggregate) && filter;

                       })

               || flakes.values().stream().anyMatch(Flake::required);
    }


    public Shape shape() {
        return shape;
    }

    Map<List<Transform>, Criterion> criteria() {
        return criteria;
    }

    Map<Property, Flake> flakes() {
        return flakes;
    }

}
