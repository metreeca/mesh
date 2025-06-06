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

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.XSD;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;

import static com.metreeca.mesh.rdf4j.Coder.*;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toList;

final class SPARQL {

    private static final ThreadLocal<DecimalFormat> DecimalFormat=ThreadLocal.withInitial(() ->
            new DecimalFormat("0.0#") // ;( thread-safe
    );


    static String sparql(final Coder sparql) {
        return sparql.toString();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder comment(final String text) {
        return space(text("# ", text));
    }


    static Coder base(final String base) {
        return space(items(text("base <", base, ">")));
    }

    static Coder prefix(final String prefix, final String name) {
        return line(text("prefix ", prefix, ": <", name, ">"));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder select(final Coder... expressions) {
        return select(false, expressions);
    }

    static Coder select(final boolean distinct, final Coder... expressions) {
        return items(text("\rselect"),
                distinct ? text(" distinct") : nothing(),
                expressions.length == 0 ? text(" *") : items(expressions)
        );
    }


    static Coder construct(final Coder... patterns) {
        return items(text("\rconstruct"), items(patterns));
    }


    static Coder where(final Coder... pattern) {
        return items(text("\rwhere"), block(pattern));
    }


    static Coder groupBy(final Coder... expressions) {
        return groupBy(asList(expressions));
    }

    static Coder groupBy(final Collection<Coder> expressions) {
        return expressions.isEmpty() ? nothing() : items(text("\rgroup by"), items(expressions));
    }


    static Coder having(final Collection<Coder> coders) {
        return coders.isEmpty() ? nothing() : having(indent(list("\n&& ", coders)));
    }

    static Coder having(final Coder expression) {
        return items(text("\rhaving ( "), items(expression), text(" )"));
    }


    static Coder orderBy(final Coder... expressions) {
        return orderBy(asList(expressions));
    }

    static Coder orderBy(final Collection<Coder> expressions) {
        return expressions.isEmpty() ? nothing() : orderBy(items(expressions));
    }

    static Coder orderBy(final Coder expression) {
        return items(text(" order by"), expression);
    }


    static Coder asc(final Coder expression) {
        return items(text(" asc("), expression, text(")"));
    }

    static Coder desc(final Coder expression) {
        return items(text(" desc("), expression, text(")"));
    }


    static Coder offset(final int offset) {
        return offset > 0
                ? items(text(" offset "), text(String.valueOf(offset)))
                : nothing();
    }

    static Coder limit(final int limit) {
        return limit > 0
                ? items(text(" limit "), text(String.valueOf(limit)))
                : nothing();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder optional(final Coder pattern) {
        return items(text(" optional"), block(pattern));
    }


    static Coder union(final Coder... patterns) {
        return union(asList(patterns));
    }

    static Coder union(final Collection<Coder> patterns) {

        final List<Coder> coders=patterns.stream()
                .filter(not(nothing()::equals))
                .toList();

        return coders.isEmpty() ? nothing()
                : coders.size() == 1 ? coders.getFirst()
                : list(" union ", coders.stream().map(Coder::block).collect(toList()));
    }


    static Coder values(final List<Coder> vars, final List<List<Value>> records) {
        return items(text(" values "), parens(vars), block(records.stream()
                .map(record -> parens(list(" ", record.stream()
                        .map(SPARQL::value)
                        .collect(toList())
                )))
                .map(Coder::line)
                .toList()
        ));
    }


    static Coder edge(final Coder source, final List<IRI> path, final Coder target) {

        throw new UnsupportedOperationException(";( be implemented"); // !!!

        // if ( path.isEmpty() ) {
        //
        //     throw new IllegalArgumentException("empty property path");
        //
        // } else if ( path.size() == 1 ) {
        //
        //     return edge(source,
        //             path.get(0), target
        //     );
        //
        // } else {
        //
        //     return edge(
        //             source,
        //             list("/", path.stream()
        //                     .map(step -> forward(step) ? iri(step) : items(text("^"), iri(reverse(step))))
        //                     .collect(toList())
        //             ),
        //             target
        //     );
        //
        // }
    }

    static Coder edge(final Coder source, final Coder path, final Coder target) {
        return items(text(' '), source, text(' '), path, text(' '), target, text(" ."));
    }


    static Coder value(final Value value) {
        return value.isIRI() ? iri((IRI)value)
                : value.isBNode() ? bnode((BNode)value)
                : literal((Literal)value);
    }


    static Coder bnode(final BNode bnode) {
        return items(text("_:"), text(bnode.getID()));
    }

    static Coder iri(final IRI iri) {
        return iri.equals(RDF.TYPE) ? text("a") : text("<", iri.stringValue(), ">");
    }

    static Coder literal(final Literal literal) {

        final IRI type=literal.getDatatype();

        return type.equals(XSD.BOOLEAN) ? text(String.valueOf(literal.booleanValue()))

                : type.equals(XSD.INTEGER) ? text(String.valueOf(literal.integerValue()))
                : type.equals(XSD.DECIMAL) ? text(DecimalFormat.get().format(literal.decimalValue()))

                : type.equals(XSD.DOUBLE) ? text(format("%e", literal.doubleValue()))
                : type.equals(XSD.STRING) ? quoted(literal.getLabel())

                : literal.getLanguage()
                .map(lang -> items(quoted(literal.getLabel()), text("@"), text(lang)))
                .orElseGet(() -> items(quoted(literal.getLabel()), text("^^"), iri(type)));

    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder filter(final Collection<Coder> coders) {
        return coders.isEmpty() ? nothing() : filter(indent(list("\n&& ", coders)));
    }

    static Coder filter(final Coder... expressions) {
        return items(text(" filter ( "), items(expressions), text(" )"));
    }


    static Coder bind(final Coder expression, final String id) {
        return items(text(" bind"), as(expression, id));
    }

    static Coder as(final Coder expression, final String id) {
        return items(text(" ("), expression, text(" as "), var(id), text(')'));
    }


    static Coder star() {
        return text(" *");
    }

    static Coder var(final String id) {
        return text(" ?", id);
    }


    static Coder op(final Coder x, final String name, final Coder y) {
        return items(x, text(' '), text(name), text(' '), y);
    }

    static Coder function(final String name, final Coder... args) {
        return items(text(' '), text(name), text('('), list(", ", args), text(')'));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder nt(final Coder expression) {
        return items(text('!'), expression);
    }

    static Coder or(final Coder... expressions) {
        return or(asList(expressions));
    }

    static Coder or(final Collection<Coder> expressions) {
        return list(" || ", expressions);
    }

    static Coder and(final Coder... expressions) {
        return and(asList(expressions));
    }

    static Coder and(final Collection<Coder> expressions) {
        return expressions.isEmpty() ? nothing() : list(" && ", expressions);
    }


    static Coder test(final Coder test, final Coder pass, final Coder fail) {
        return items(text(" if("), test, text(", "), pass, text(", "), fail, text(")"));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder eq(final Coder x, final Coder y) {
        return op(x, "=", y);
    }

    static Coder neq(final Coder x, final Coder y) {
        return op(x, "!=", y);
    }

    static Coder lt(final Coder x, final Coder y) {
        return op(x, "<", y);
    }

    static Coder gt(final Coder x, final Coder y) {
        return op(x, ">", y);
    }

    static Coder lte(final Coder x, final Coder y) {
        return op(x, "<=", y);
    }

    static Coder gte(final Coder x, final Coder y) {
        return op(x, ">=", y);
    }


    static Coder bound(final Coder expression) {
        return function("bound", expression);
    }

    static Coder in(final Coder expression, final Collection<Coder> expressions) {
        return items(expression, text(" in ("), list(", ", expressions), text(')'));
    }

    static Coder exists(final Coder pattern) {
        return items(text(" exists "), block(pattern));
    }


    static Coder isBlank(final Coder expression) {
        return function("isBlank", expression);
    }

    static Coder isIRI(final Coder expression) {
        return function("isIRI", expression);
    }

    static Coder isLiteral(final Coder expression) {
        return function("isLiteral", expression);
    }


    static Coder langMatches(final Coder lang, final Coder pattern) {
        return function("langMatches", lang, pattern);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder lang(final Coder expression) {
        return function("lang", expression);
    }

    static Coder datatype(final Coder expression) {
        return function("datatype", expression);
    }

    static Coder str(final Coder expression) {
        return function("str", expression);
    }

    static Coder strlen(final Coder expression) {
        return function("strlen", expression);
    }

    static Coder strstarts(final Coder expression, final Coder prefix) {
        return function("strstarts", expression, prefix);
    }

    static Coder regex(final Coder expression, final Coder pattern) {
        return function("regex", expression, pattern);
    }

    static Coder regex(final Coder expression, final Coder pattern, final Coder flags) {
        return function("regex", expression, pattern, flags);
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    static Coder min(final Coder expression) {
        return items(text(" min("), expression, text(")"));
    }

    static Coder max(final Coder expression) {
        return items(text(" max("), expression, text(")"));
    }

    static Coder count(final Coder expression) {
        return count(false, expression);
    }

    static Coder count(final boolean distinct, final Coder expression) {
        return items(text(" count("), distinct ? text("distinct ") : nothing(), expression, text(")"));
    }

    static Coder sample(final Coder coder) {
        return items(text(" sample("), coder, text(")"));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private SPARQL() { }

}
