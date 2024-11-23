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

final class _TraceDecoder {

    // private static final Map<IRI, IRI> CONSTRAINTS=Map.ofEntries(
    //
    //         entry(SHACL.CLASS_CONSTRAINT_COMPONENT, SHACL.CLASS),
    //         entry(SHACL.DATATYPE_CONSTRAINT_COMPONENT, SHACL.DATATYPE),
    //         entry(SHACL.NODE_KIND_CONSTRAINT_COMPONENT, SHACL.NODE_KIND),
    //
    //         entry(SHACL.MIN_COUNT_CONSTRAINT_COMPONENT, SHACL.MIN_COUNT),
    //         entry(SHACL.MAX_COUNT_CONSTRAINT_COMPONENT, SHACL.MAX_COUNT),
    //
    //         entry(SHACL.MIN_EXCLUSIVE_CONSTRAINT_COMPONENT, SHACL.MIN_EXCLUSIVE),
    //         entry(SHACL.MIN_INCLUSIVE_CONSTRAINT_COMPONENT, SHACL.MIN_INCLUSIVE),
    //         entry(SHACL.MAX_EXCLUSIVE_CONSTRAINT_COMPONENT, SHACL.MAX_EXCLUSIVE),
    //         entry(SHACL.MAX_INCLUSIVE_CONSTRAINT_COMPONENT, SHACL.MAX_EXCLUSIVE),
    //
    //         entry(SHACL.MIN_LENGTH_CONSTRAINT_COMPONENT, SHACL.MIN_LENGTH),
    //         entry(SHACL.MAX_LENGTH_CONSTRAINT_COMPONENT, SHACL.MAX_LENGTH),
    //         entry(SHACL.PATTERN_CONSTRAINT_COMPONENT, SHACL.PATTERN),
    //         entry(SHACL.LANGUAGE_IN_CONSTRAINT_COMPONENT, SHACL.LANGUAGE_IN),
    //         entry(SHACL.UNIQUE_LANG_CONSTRAINT_COMPONENT, SHACL.UNIQUE_LANG),
    //
    //         entry(SHACL.HAS_VALUE_CONSTRAINT_COMPONENT, SHACL.HAS_VALUE),
    //         entry(SHACL.IN_CONSTRAINT_COMPONENT, SHACL.IN)
    //
    // );
    //
    //
    // //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // static Map<Value, Trace> decode(final Collection<Statement> report) {
    //     return focus(SHACL.VALIDATION_REPORT, report)
    //
    //             .shift(reverse(RDF.TYPE), SHACL.RESULT)
    //
    //             .split()
    //
    //             .filter(result -> result
    //                     .shift(SHACL.RESULT_SEVERITY)
    //                     .values(asIRI())
    //                     .anyMatch(SHACL.VIOLATION::equals)
    //             )
    //
    //             .collect(groupingBy(
    //                     TraceDecoder::node,
    //                     mapping(TraceDecoder::property, collectingAndThen(toList(), Trace::trace))
    //             ));
    // }
    //
    //
    // //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //
    // private static Value node(final _Focus result) {
    //     return result.shift(SHACL.FOCUS_NODE).value().orElseThrow(() ->
    //             new IllegalArgumentException("sh:ValidationResult without sh:focusNode")
    //     );
    // }
    //
    // private static Trace property(final _Focus result) {
    //     return trace(path(result), trace(constraint(result)));
    // }
    //
    // private static IRI path(final _Focus result) { // !!! reverse
    //
    //     final _Focus path=result.shift(SHACL.RESULT_PATH);
    //
    //     if ( path.empty() ) {
    //         throw new IllegalArgumentException("sh:ValidationResult without sh:resultPath");
    //     }
    //
    //     return path.value(asIRI())
    //             .or(() -> path.shift(SHACL.INVERSE_PATH).value(asIRI()).map(Frame::reverse))
    //             .orElseThrow(() -> new UnsupportedOperationException("unsupported complex result path"));
    // }
    //
    // private static String constraint(final _Focus result) {
    //
    //     final IRI component=component(result);
    //     final IRI constraint=CONSTRAINTS.getOrDefault(component, component);
    //
    //     final _Focus parameter=result.shift(SHACL.SOURCE_SHAPE, constraint);
    //
    //     final Optional<Value> value=result.shift(SHACL.VALUE).value();
    //
    //     // !!! sh:message
    //     // !!! sh:details
    //
    //
    //     final StringBuilder builder=new StringBuilder(100);
    //
    //     value.ifPresent(v -> builder.append(v).append(' ')); // !!! to turtle
    //
    //     builder.append(constraint.getLocalName()).append('(')
    //
    //             .append(Optional
    //
    //                     .of(parameter.items())
    //                     .filter(not(List::isEmpty))
    //                     .map(Collection::stream)
    //                     .orElseGet(() -> parameter.value().stream())
    //
    //                     .map(Object::toString) // !!! to turtle
    //                     .collect(joining(", ")))
    //
    //             .append(')');
    //
    //     return builder.toString();
    // }
    //
    // private static IRI component(final _Focus result) {
    //     return result.shift(SHACL.SOURCE_CONSTRAINT_COMPONENT).value(asIRI()).orElseThrow(() ->
    //             new IllegalArgumentException("sh:ValidationResult without sh:sourceConstraintComponent")
    //     );
    // }
    //

    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private _TraceDecoder() { }

}
