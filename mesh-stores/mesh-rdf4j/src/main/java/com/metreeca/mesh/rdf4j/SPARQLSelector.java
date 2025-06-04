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


import com.metreeca.mesh.Value;
import com.metreeca.mesh.queries.*;
import com.metreeca.mesh.shapes.Property;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.shim.Collections;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.query.BindingSet;
import org.eclipse.rdf4j.query.TupleQuery;
import org.eclipse.rdf4j.query.impl.SimpleDataset;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.net.URI;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static com.metreeca.mesh.Value.Nil;
import static com.metreeca.mesh.Value.field;
import static com.metreeca.mesh.queries.Criterion.criterion;
import static com.metreeca.mesh.queries.Criterion.pattern;
import static com.metreeca.mesh.rdf4j.Coder.*;
import static com.metreeca.mesh.rdf4j.SPARQL.*;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.json;
import static com.metreeca.mesh.rdf4j.SPARQLConverter.rdf;
import static com.metreeca.shim.Collections.entry;

import static java.util.Collections.newSetFromMap;
import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.eclipse.rdf4j.model.vocabulary.RDF.NIL;

final class SPARQLSelector extends _StoreLoader.Worker {

    private static final Logger LOGGER=Logger.getLogger(SPARQLSelector.class.getName());

    private static final String RDF="http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String RDFS="http://www.w3.org/2000/01/rdf-schema#";

    private static final List<IRI> ROOT=Collections.list();


    private static Stream<Expression> expressions(final Specs specs) {
        return specs.columns().stream().map(Probe::expression);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final URI context;


    SPARQLSelector(final RDF4JStore rdf4j) {
        context=rdf4j.context();
    }


    private final Collection<Task<List<Value>>> values=newSetFromMap(new ConcurrentHashMap<>());
    private final Collection<Task<List<Tuple>>> tuples=newSetFromMap(new ConcurrentHashMap<>());


    CompletableFuture<List<com.metreeca.mesh.Value>> values(
            final boolean virtual,
            final URI id,
            final Property property,
            final Query query
    ) {

        return new Task<List<Value>>(virtual, id, property, query).schedule(values::add);

    }

    CompletableFuture<List<Tuple>> tuples(
            final boolean virtual,
            final URI id,
            final Property property,
            final Query query
    ) {

        return new Task<List<Tuple>>(virtual, id, property, query).schedule(tuples::add);

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override CompletableFuture<Void> run(final RepositoryConnection connection) { // !!! batch execution
        return allOf(Stream

                .concat(

                        snapshot(values).stream().map(task -> runAsync(() -> {

                            final TupleQuery query=connection.prepareTupleQuery(generate(task));

                            if ( context != null ) {

                                final SimpleDataset dataset=new SimpleDataset();

                                dataset.addDefaultGraph(rdf(context));

                                query.setDataset(dataset);

                            }

                            try ( final Stream<BindingSet> results=query.evaluate().stream() ) {

                                task.complete(results.map(bindings ->

                                        json(bindings.getValue(id(ROOT)))

                                ).toList());

                            }

                        })),

                        snapshot(tuples).stream().map(task -> runAsync(() -> {

                            try ( final Stream<BindingSet> results=connection.prepareTupleQuery(

                                    generate(task)

                            ).evaluate().stream() ) {

                                task.complete(results.map(bindings ->

                                        new Tuple(task.query.model().value(Specs.class)
                                                .map(Specs::columns)
                                                .orElseGet(List::of)
                                                .stream()
                                                .map(Probe::name)
                                                .map(name -> field(name, json(bindings.getValue(id(name)))))
                                                .toList()
                                        )

                                ).toList());

                            }

                        }))

                )

                .toArray(CompletableFuture[]::new)
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private String generate(final Task<?> task) {

        final boolean virtual=task.virtual;
        final IRI id=rdf(task.id);

        final Property property=task.property;
        final Optional<URI> forward=property.forward();
        final Optional<URI> reverse=property.reverse();

        final Query query=task.query;

        final Value model=query.model();
        final Optional<Specs> tuple=model.value(Specs.class);

        final Map<Expression, Criterion> criteria=query.criteria();

        final int offset=query.offset();
        final int limit=query.limit();

        final Map<Expression, Criterion> expressions=Stream

                .of(

                        criteria.entrySet().stream(),

                        tuple.stream().flatMap(t -> expressions(t)
                                .map(expression -> entry(expression, criterion()))
                        )

                )

                .flatMap(identity())

                .collect(toMap(
                        Entry::getKey,
                        Entry::getValue,
                        (x, y) -> x.isEmpty() ? y : x, // constraint clashes not expected
                        LinkedHashMap::new
                ));


        final boolean grouping=expressions.keySet().stream().anyMatch(Expression::isAggregate) && tuple
                .map(t -> expressions(t).anyMatch(not(Expression::isAggregate)))
                .orElse(true);


        final Flake flake=Flake.flake(property.shape(), expressions);

        final Coder root=var(id(ROOT));

        final String sparql=sparql(items(

                space(
                        prefix("rdf", RDF),
                        prefix("rdfs", RDFS)
                ),

                select(tuple.isEmpty(), tuple.map(this::projection).orElse(root)),

                space(where(space(

                        select(true, star()), where(space( // ;( required to sort on multiple localized values

                                // collection membership

                                space(virtual ? nothing() : forward.map(uri -> edge(value(id), iri(rdf(uri)), root))
                                        .or(() -> reverse.map(uri -> edge(root, iri(rdf(uri)), value(id))))
                                        .orElseGet(Coder::nothing)
                                ),

                                // type constraint from shape

                                space(property.shape().clazz()
                                        .map(type -> clazz(var(id(ROOT)), type))
                                        .orElseGet(Coder::nothing)
                                ),

                                space(flake(Collections.list(), flake)),
                                space(filters(Collections.list(), flake))

                        ))

                ))),

                // grouping

                space(grouping ?

                        groupBy(tuple
                                .map(t -> items(t.columns().stream()
                                        .map(Probe::expression)
                                        .filter(not(Expression::isAggregate))
                                        .map(this::expression)
                                        .toList()
                                ))
                                .orElse(root)
                        )

                        : nothing()

                ),

                // aggregate filters

                space(criteria.entrySet().stream().anyMatch(e -> e.getKey().isAggregate() && e.getValue().isFilter())

                                ? having(criteria.entrySet().stream()
                                .filter(entry -> entry.getKey().isAggregate())
                                .map(entry -> constraint(expression(entry.getKey()), entry.getValue()))
                                .toList()
                        )

                                : nothing()
                ),

                // ordering

                space(tuple.map(t -> expressions(t).anyMatch(not(Expression::isAggregate))).orElse(true)

                                ? orderBy(items(

                        focus(criteria), // focus values
                        order(criteria), // explicit criteria

                        Optional.ofNullable(criteria.get(Expression.expression()))
                                .filter(criterion -> criterion.order().isPresent())
                                .isPresent()

                                ? nothing() // already ordered on root
                                : asc(root) // default ordering on root

                        ))

                                : nothing() // all aggregates >> single record >> no order

                ),

                // slice

                space(
                        line(offset(offset)),
                        line(limit(limit))
                )

        ));

        LOGGER.fine(() -> "# select\n\n%s".formatted(sparql));

        return sparql;
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Coder flake(final List<String> path, final Flake flake) {
        return items(flake.flakes().entrySet().stream()

                .map(f -> {

                    final Property property=f.getKey();
                    final Flake child=f.getValue();

                    final Optional<URI> forward=property.forward();
                    final Optional<URI> reverse=property.reverse();

                    final List<String> next=Stream.concat(path.stream(), Stream.of(property.name())).toList();

                    final Coder source=var(id(path));
                    final Coder target=var(id(next));

                    final Coder items=items(

                            space(forward.map(uri -> edge(source, iri(rdf(uri)), target))
                                    .or(() -> reverse.map(uri -> edge(target, iri(rdf(uri)), source)))
                                    .orElseGet(Coder::nothing)
                            ),

                            space(flake(next, child)) // !!!

                    );

                    return space(child.required() ? items : optional(items));

                })

                .toList()
        );
    }

    private Coder filters(final List<String> path, final Flake flake) {
        return items(

                items(flake.criteria().entrySet().stream()

                        .map(c -> {

                            final List<Transform> pipe=c.getKey();
                            final Criterion criterion=c.getValue();

                            final Coder value=transform(path, pipe);

                            return !criterion.isFilter() || pipe.stream().anyMatch(Transform::isAggregate)
                                    ? nothing()
                                    : space(filter(constraint(value, criterion)));

                        })

                        .toList()
                ),

                items(flake.flakes().entrySet().stream()

                        .map(f -> {

                            final Flake child=f.getValue();

                            final Property property=f.getKey();
                            final List<String> next=Stream.concat(path.stream(), Stream.of(property.name())).toList();

                            return space(filters(next, child));

                        })

                        .toList()
                )

        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Coder projection(final Specs specs) {
        return specs.columns().isEmpty() ? star() : items(specs.columns().stream()
                .map(probe -> {

                    final String name=probe.name();
                    final Expression expression=probe.expression();

                    final boolean aggregate=expressions(specs)
                            .anyMatch(Expression::isAggregate);

                    return as(

                            aggregate && expression.isComputed() && !expression.isAggregate()
                                    ? sample(expression(expression))
                                    : expression(expression),

                            id(name)
                    );

                })
                .toList()
        );
    }


    private Coder focus(final Map<Expression, Criterion> criteria) {
        return items(criteria.entrySet().stream()
                .map(entry -> {

                    final Expression expression=entry.getKey();
                    final Criterion criterion=entry.getValue();

                    return criterion.focus()
                            .map(focus -> focus(focus, expression(expression)))
                            .orElseGet(Coder::nothing);

                })
                .toList()
        );
    }

    private Coder focus(final Collection<Value> values, final Coder value) {

        final boolean nulls=values.stream().anyMatch(v -> v.equals(Nil()));
        final boolean nonNulls=values.stream().anyMatch(v -> !v.equals(Nil()));

        final Coder nb=nt(bound(value));

        final Coder in=in(value, values.stream()
                .filter(not(Value::isEmpty))
                .map(v -> value(rdf(v).findFirst().orElse(NIL)))
                .toList()
        );

        return desc(nulls && nonNulls ? or(nb, in)
                : nonNulls ? and(bound(value), in)
                : nb
        );
    }


    private Coder order(final Map<Expression, Criterion> criteria) {
        return items(criteria.entrySet().stream()
                .filter(e -> e.getValue().order().isPresent())
                .map(entry -> {

                    final Expression expression=entry.getKey();
                    final Integer criterion=entry.getValue().order().get();

                    // ;( colour-patch for tagged literal sorting…

                    final Coder value=expression(expression);

                    return order(criterion, test(
                            and(isLiteral(value), langMatches(lang(value), text("'*'"))),
                            str(value),
                            value
                    ));

                })
                .toList()
        );
    }

    private Coder order(final Integer priority, final Coder value) {
        return priority >= 0 ? asc(value) : desc(value);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Coder expression(final Expression expression) {
        return transform(var(id(expression.path())), expression.pipe());
    }

    private Coder transform(final Coder anchor, final List<Transform> transforms) {
        if ( transforms.isEmpty() ) { return anchor; } else {

            final Transform head=transforms.getFirst();
            final List<Transform> tail=transforms.subList(1, transforms.size());

            return head == Transform.COUNT
                    ? count(true, transform(anchor, tail))
                    : function(head.name().toLowerCase(Locale.ROOT), transform(anchor, tail));

        }
    }

    private Coder transform(final List<String> anchor, final List<Transform> transforms) {
        if ( transforms.isEmpty() ) { return var(id(anchor)); } else {

            final Transform head=transforms.getFirst();
            final List<Transform> tail=transforms.subList(1, transforms.size());

            return head == Transform.COUNT
                    ? count(true, transform(anchor, tail))
                    : function(head.name().toLowerCase(Locale.ROOT), transform(anchor, tail));

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private Coder clazz(final Coder value, final Type type) {
        return edge(value, text("rdf:type/rdfs:subClassOf*"), iri(rdf(type.uri())));
    }

    private Coder constraint(final Coder value, final Criterion criterion) {
        return and(Stream.

                of(

                        criterion.lt().map(limit -> lt(value, limit)).stream(),
                        criterion.gt().map(limit -> gt(value, limit)).stream(),

                        criterion.lte().map(limit -> lte(value, limit)).stream(),
                        criterion.gte().map(limit -> gte(value, limit)).stream(),

                        criterion.like().stream().map(keywords -> like(value, keywords)),
                        criterion.any().stream().map(options -> any(value, options))

                )

                .flatMap(identity())

                .toList()
        );
    }


    private Coder lt(final Coder value, final Value limit) {
        return SPARQL.lt(value, value(rdf(limit).findFirst().orElse(NIL)));
    }

    private Coder gt(final Coder value, final Value limit) {
        return SPARQL.gt(value, value(rdf(limit).findFirst().orElse(NIL)));
    }


    private Coder lte(final Coder value, final Value limit) {
        return SPARQL.lte(value, value(rdf(limit).findFirst().orElse(NIL)));
    }

    private Coder gte(final Coder value, final Value limit) {
        return SPARQL.gte(value, value(rdf(limit).findFirst().orElse(NIL)));
    }


    private Coder like(final Coder value, final String keywords) {
        return regex(str(value), quoted(pattern(keywords, true)));
    }

    private Coder any(final Coder value, final Collection<Value> values) {
        if ( values.isEmpty() ) {

            return bound(value);

        } else {

            final Set<Value> options=values.stream()
                    .filter(not(v -> v.equals(Nil())))
                    .collect(toSet());

            final Coder negative=nt(bound(value));
            final Coder positive=options.size() == 1
                    ? eq(value, value(options.stream().flatMap(SPARQLConverter::rdf).findFirst().orElse(NIL)))
                    : in(value, options.stream().flatMap(SPARQLConverter::rdf).map(SPARQL::value).toList());

            return values.stream().noneMatch(v -> v.equals(Nil())) ? positive
                    : options.isEmpty() ? negative
                    : parens(or(negative, positive));

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final class Task<V> {

        final boolean virtual;

        final URI id;
        final Property property;
        final Query query;

        private final CompletableFuture<V> future=new CompletableFuture<>();


        private Task(
                final boolean virtual,
                final URI id,
                final Property property,
                final Query query
        ) {
            this.virtual=virtual;
            this.id=id;
            this.property=property;
            this.query=query;
        }


        private CompletableFuture<V> schedule(final Consumer<Task<V>> queue) {

            queue.accept(this);

            return future;
        }

        private void complete(final V value) {
            future.complete(value);
        }

    }

}
