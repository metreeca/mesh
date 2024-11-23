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

package com.metreeca.mesh.rdf4j;


@SuppressWarnings("NonBooleanMethodNameMayNotStartWithQuestion")
final class _ShapeEncoder {

    // static Collection<Statement> encode(final Collection<Shape> shapes) {
    //     return shapes.stream()
    //
    //             .flatMap(shape -> shape.target().stream().map(target -> merge(
    //
    //                     Stream.of(
    //                             field(RDF.TYPE, SHACL.NODE_SHAPE),
    //                             field(SHACL.TARGET_CLASS, target)
    //                     ),
    //
    //                     constraints(shape),
    //                     properties(shape)
    //
    //             )))
    //
    //             .flatMap(Frame::stream)
    //             .collect(toList());
    // }
    //
    //
    // //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // private static Stream<Frame> constraints(final Shape shape) {
    //     return Stream
    //
    //             .of(
    //
    //                     datatype(shape),
    //                     clazz(shape),
    //
    //                     minExclusive(shape),
    //                     maxExclusive(shape),
    //                     minInclusive(shape),
    //                     maxInclusive(shape),
    //
    //                     minLength(shape),
    //                     maxLength(shape),
    //                     pattern(shape),
    //                     languageIn(shape),
    //
    //                     minCount(shape),
    //                     maxCount(shape),
    //
    //                     in(shape),
    //                     hasValue(shape)
    //
    //             )
    //
    //             .flatMap(identity());
    // }
    //
    //
    // private static Stream<Frame> datatype(final Shape shape) {
    //     return shape.datatype().stream().flatMap(datatype -> Stream.of(
    //
    //             datatype.equals(VALUE) ? field(SHACL.NODE_KIND, Optional.empty())
    //
    //                     : datatype.equals(RESOURCE) ? field(SHACL.NODE_KIND, SHACL.BLANK_NODE_OR_IRI)
    //                     : datatype.equals(BNODE) ? field(SHACL.NODE_KIND, SHACL.BLANK_NODE)
    //                     : datatype.equals(IRI) ? field(SHACL.NODE_KIND, SHACL.IRI)
    //                     : datatype.equals(LITERAL) ? field(SHACL.NODE_KIND, SHACL.LITERAL)
    //
    //                     : datatype.equals(RDF.LANGSTRING) ? frame(
    //                     field(SHACL.DATATYPE, RDF.LANGSTRING),
    //                     field(SHACL.UNIQUE_LANG, literal(shape.maxCount().filter(v -> v == 1).isPresent()))
    //             )
    //
    //                     : field(SHACL.DATATYPE, datatype)
    //
    //     ));
    // }
    //
    // private static Stream<Frame> clazz(final Shape shape) {
    //     return shape.clazz().stream().flatMap(clazz -> Stream.of(
    //
    //             field(SHACL.CLASS, clazz)
    //
    //     ));
    // }
    //
    //
    // private static Stream<Frame> minExclusive(final Shape shape) {
    //     return shape.minExclusive().stream().flatMap(limit -> Stream.of(
    //
    //             field(SHACL.MIN_EXCLUSIVE, limit)
    //
    //     ));
    // }
    //
    // private static Stream<Frame> maxExclusive(final Shape shape) {
    //     return shape.maxExclusive().stream().flatMap(limit -> Stream.of(
    //
    //             field(SHACL.MAX_EXCLUSIVE, limit)
    //
    //     ));
    // }
    //
    // private static Stream<Frame> minInclusive(final Shape shape) {
    //     return shape.minInclusive().stream().flatMap(limit -> Stream.of(
    //
    //             field(SHACL.MIN_INCLUSIVE, limit)
    //
    //     ));
    // }
    //
    // private static Stream<Frame> maxInclusive(final Shape shape) {
    //     return shape.maxExclusive().stream().flatMap(limit -> Stream.of(
    //
    //             field(SHACL.MAX_INCLUSIVE, limit)
    //
    //     ));
    // }
    //
    //
    // private static Stream<Frame> minLength(final Shape shape) {
    //     return shape.minLength().stream().flatMap(limit -> Stream.of(
    //
    //             field(SHACL.MIN_LENGTH, literal(integer(limit)))
    //
    //     ));
    // }
    //
    // private static Stream<Frame> maxLength(final Shape shape) {
    //     return shape.maxLength().stream().flatMap(limit -> Stream.of(
    //
    //             field(SHACL.MAX_LENGTH, literal(integer(limit)))
    //
    //     ));
    // }
    //
    // private static Stream<Frame> pattern(final Shape shape) {
    //     return shape.pattern().stream().flatMap(pattern -> Stream.of(
    //
    //             field(SHACL.PATTERN, literal(pattern))
    //
    //     ));
    // }
    //
    // private static Stream<Frame> languageIn(final Shape shape) {
    //     return shape.languageIn().stream().map(locales ->
    //
    //             field(SHACL.LANGUAGE_IN, list(locales.stream().map(Frame::literal).collect(toList())))
    //
    //     );
    // }
    //
    //
    // private static Stream<Frame> minCount(final Shape shape) {
    //     return shape.localized() ? Stream.empty() : shape.minCount().stream().map(limit ->
    //
    //             field(SHACL.MIN_COUNT, literal(integer(limit)))
    //
    //     );
    // }
    //
    // private static Stream<Frame> maxCount(final Shape shape) {
    //     return shape.localized() ? Stream.empty() : shape.maxCount().stream().map(limit ->
    //
    //             field(SHACL.MAX_COUNT, literal(integer(limit)))
    //
    //     );
    // }
    //
    //
    // private static Stream<Frame> in(final Shape shape) {
    //     return shape.in().stream().flatMap(values -> Stream.of(
    //
    //             field(SHACL.IN, list(values))
    //
    //     ));
    // }
    //
    //
    // private static Stream<Frame> hasValue(final Shape shape) {
    //     return shape.hasValue().stream().flatMap(values -> Stream.of(
    //
    //             field(SHACL.HAS_VALUE, values)
    //
    //     ));
    // }
    //
    //
    // private static Stream<Frame> properties(final Shape shape) {
    //     return shape.predicates().entrySet().stream().flatMap(e -> {
    //
    //         final IRI uri=e.getKey();
    //         final Shape nested=e.getValue().getValue().get();
    //
    //         if ( uri.equals(ID) ) {
    //
    //             return Stream.empty();
    //
    //         } else {
    //
    //             return Stream.of(field(SHACL.PROPERTY, merge(
    //
    //                     Stream.of(
    //                             field(SHACL.PATH, path(uri))
    //                     ),
    //
    //                     constraints(nested),
    //
    //                     nested.composite() ? properties(nested) : Stream.empty()
    //
    //             )));
    //
    //         }
    //
    //     });
    // }
    //
    //
    // //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // @SafeVarargs
    // private static Frame merge(final Stream<Frame>... fields) {
    //     return frame(Arrays
    //             .stream(fields)
    //             .flatMap(identity())
    //             .collect(toList())
    //     );
    // }
    //
    //
    // private static Value path(final IRI uri) {
    //     return forward(uri) ? uri
    //             : frame(field(SHACL.INVERSE_PATH, reverse(uri)));
    // }
    //
    // private static Value list(final Value... values) {
    //     return list(Frame.list(values));
    // }
    //
    // private static Value list(final Collection<Value> values) {
    //
    //     final List<Value> items=new ArrayList<>(values);
    //
    //     Collections.reverse(items);
    //
    //     Value list=RDF.NIL;
    //
    //     for (final Value item : items) {
    //         list=frame(
    //                 field(RDF.FIRST, item),
    //                 field(RDF.REST, list)
    //         );
    //     }
    //
    //     return list;
    // }
    //
    //
    // //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // private _ShapeEncoder() { }
    //
}
