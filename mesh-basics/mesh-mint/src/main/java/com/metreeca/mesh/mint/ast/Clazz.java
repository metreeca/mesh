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

package com.metreeca.mesh.mint.ast;

import com.metreeca.mesh.meta.jsonld.*;
import com.metreeca.mesh.meta.jsonld.Class;
import com.metreeca.mesh.meta.shacl.Constraint;
import com.metreeca.mesh.mint.FrameException;
import com.metreeca.mesh.shapes.Type;
import com.metreeca.shim.URIs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint.ast.Annotation.*;
import static com.metreeca.mesh.mint.ast.Reference.simple;
import static com.metreeca.shim.Collections.*;
import static com.metreeca.shim.URIs.term;
import static com.metreeca.shim.URIs.uri;

import static java.lang.Math.max;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;


/**
 * Represents a Java interface class in the AST model.
 *
 * <p>Encapsulates the structure of a Java interface that is annotated with
 * {@link com.metreeca.mesh.meta.jsonld.Frame}. Captures annotations, methods, parent interfaces, and provides
 * functionality for namespace management, URI resolution, and inheritance flattening.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Managing class hierarchy and inheritance relationships</li>
 *   <li>Resolving base URIs and namespace prefixes for property references</li>
 *   <li>Identifying class-level annotations and constraints</li>
 *   <li>Handling method flattening to merge overridden methods from parent interfaces</li>
 *   <li>Processing CURIE syntax for compact URI representation</li>
 * </ul>
 *
 * <p>This class implements complex namespace resolution rules including default prefixes,
 * inherited prefixes, and conflict handling to ensure consistent URI mapping.</p>
 */
@SuppressWarnings("FieldNotUsedInToString")
public final class Clazz {

    private static final Pattern NAME_PATTERN=Pattern.compile(
            "[_a-zA-Z][-._a-zA-Z0-9]*"
    );

    private static final Pattern DEFAULT_PATTERN=Pattern.compile(
            "\\[(?<prefix>"+NAME_PATTERN+")]"
    );

    /**
     * Simplified version of <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/#s_syntax">CURIE Syntax 1.0</a> :
     */
    private static final Pattern CURIE_PATTERN=Pattern.compile(
            "(?:(?<prefix>"+NAME_PATTERN+")?:)?(?<name>"+NAME_PATTERN+")?"
    );


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final String canonical;

    private final List<Annotation> annotations;
    private final List<Method> methods;
    private final List<Clazz> parents;

    private URI base;
    private Map<String, URI> namespaces;
    private Clazz flattened;


    public Clazz(final String canonical) {
        this(
                canonical,
                list(),
                list(),
                list()
        );
    }

    public Clazz(

            final String canonical,

            final List<Annotation> annotations,
            final List<Method> methods,
            final List<Clazz> parents

    ) {

        this.canonical=requireNonNull(canonical, "null canonical name");

        this.annotations=list(requireNonNull(annotations, "null annotations"));
        this.methods=requireNonNull(methods, "null methods").stream().map(m -> m.clazz(this)).toList();
        this.parents=list(requireNonNull(parents, "null parents"));

    }


    public String pkg() {
        return canonical.substring(0, max(0, canonical.lastIndexOf('.')));
    }

    public String name() {
        return canonical.substring(canonical.lastIndexOf('.')+1);
    }

    public String canonical() {
        return canonical;
    }


    public URI base() {
        return base != null ? base : (base=_base()
                .map(Reference::value).orElseGet(URIs::base)
        );
    }

    public Map<String, URI> namespaces() {
        return namespaces != null ? namespaces : (namespaces=map(_namespaces()
                .map(reference -> entry(reference.value().getKey(), reference.value().getValue()))
        ));
    }

    public Optional<URI> namespace(final String prefix) {
        return Optional.ofNullable(namespaces().get(prefix));
    }

    public URI resolve(final String curie, final String fallback) throws URISyntaxException {
        return guard(() -> Optional.of(curie)

                .map(CURIE_PATTERN::matcher)
                .filter(Matcher::matches)

                .map(m -> {

                    final String prefix=Optional.ofNullable(m.group(PREFIX)).orElse("");
                    final String name=Optional.ofNullable(m.group(NAME)).filter(not(String::isEmpty)).orElse(fallback);

                    return namespace(prefix)
                            .map(namespace -> uri(namespace+name))
                            .orElseThrow(() -> new RuntimeException(new URISyntaxException(curie, format(
                                    "undefined prefix <%s>", prefix
                            ))));

                })

                .orElseThrow(() -> new RuntimeException(new URISyntaxException(curie, format(
                        "malformed CURIE <%s>", curie
                ))))
        );
    }


    public Clazz flattened() {
        return flattened != null ? flattened : (flattened=flatten());
    }


    public Stream<Annotation> annotations() {
        return annotations.stream();
    }

    public Clazz annotations(final Collection<Annotation> annotations) {
        return new Clazz(
                canonical,
                list(annotations),
                methods,
                parents
        );
    }


    public Clazz methods(final Collection<Method> methods) {
        return new Clazz(
                canonical,
                annotations,
                list(methods),
                parents
        );
    }

    public Stream<Method> methods() {
        return methods.stream();
    }


    public Stream<Clazz> parents() {
        return parents.stream();
    }

    public Clazz parents(final List<Clazz> parents) {
        return new Clazz(
                canonical,
                annotations,
                methods,
                parents
        );
    }


    public Stream<Clazz> lineage() { // top-down
        return Stream.concat(parents().flatMap(Clazz::lineage), Stream.of(this));
    }


    //̸// Shape ////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean virtual() {
        return lineage()
                .flatMap(Clazz::annotations)
                .anyMatch(annotation -> annotation.is(Virtual.class));
    }

    public Optional<String> id() {
        return lineage()
                .flatMap(Clazz::methods)
                .filter(Method::isId)
                .map(Method::name)
                .reduce((x, y) -> {

                    if ( x.equals(y) ) { return x; } else {
                        throw new FrameException(format(
                                "multiple @%s methods <%s> / <%s>",
                                simple(Id.class), x, y
                        ));
                    }

                });
    }

    public Optional<String> type() {
        return lineage()
                .flatMap(Clazz::methods)
                .filter(Method::isType)
                .map(Method::name)
                .reduce((x, y) -> {

                    if ( x.equals(y) ) { return x; } else {
                        throw new FrameException(format(
                                "multiple @%s methods <%s> / <%s>",
                                simple(com.metreeca.mesh.meta.jsonld.Type.class), x, y
                        ));
                    }

                });
    }

    public Optional<Type> clazz() {
        return annotations()
                .filter(annotation -> annotation.is(Class.class))
                .map(annotation -> {

                    try {

                        return new Reference<>(
                                Type.type(name(), resolve(annotation.string(VALUE), name())),
                                annotation.source()
                        );

                    } catch ( final URISyntaxException e ) {

                        throw new FrameException(format(
                                "malformed @%s URI <%s> : %s",
                                simple(Class.class), annotation.string(VALUE), e.getMessage()
                        ));
                    }

                })
                .reduce((x, y) -> {

                    if ( x.equals(y) ) { return x; } else {

                        throw new FrameException(format(
                                "conflicting @%s declarations <%s> / <%s>",
                                simple(Class.class), x, y
                        ));

                    }

                })
                .map(Reference::value);
    }

    public Stream<Type> clazzes() {
        return Stream

                .concat( // bottom-up
                        clazz().stream(),
                        parents().flatMap(Clazz::clazzes)
                )

                .distinct();
    }

    public Set<String> constraints() {
        return set(lineage()
                .flatMap(Clazz::annotations)
                .filter(annotation -> annotation.is(Constraint.class))
                .map(annotation1 -> annotation1.string(VALUE))
        );
    }


    //̸// Base and Namespaces  /////////////////////////////////////////////////////////////////////////////////////////

    private Optional<Reference<URI>> _base() {
        return annotations.stream()
                .filter(annotation -> annotation.is(Base.class))
                .map(annotation -> {

                    final String value=annotation.string(VALUE);

                    try {

                        return new Reference<>(uri(value), annotation.source());

                    } catch ( final IllegalArgumentException ignored ) {
                        throw new FrameException(format(
                                "malformed @%s URIs <%s> @ <%s>",
                                simple(Base.class), value, annotation.source()
                        ));
                    }

                })
                .peek(reference -> {

                    if ( !reference.value().isAbsolute() ) {
                        throw new FrameException(format(
                                "relative @%s URIs %s", simple(Base.class), reference
                        ));
                    }

                })
                .reduce((x, y) -> {
                    if ( x.value().equals(y.value()) ) { return x; } else {
                        throw new FrameException(format(
                                "conflicting @%s URIs %s / %s", simple(Base.class), x, y
                        ));
                    }
                })
                .or(() -> parents()
                        .map(Clazz::_base)
                        .flatMap(Optional::stream)
                        .reduce((x, y) -> {

                            if ( x.value().equals(y.value()) ) { return x; } else {
                                throw new FrameException(format(
                                        "conflicting @%s URIs %s / %s", simple(Base.class), x, y
                                ));
                            }

                        })
                );
    }

    private Stream<Reference<Entry<String, URI>>> _namespaces() {

        final Optional<String> dflt=dflt(); // identify the default [prefix] declaration

        final Collection<Reference<Entry<String, URI>>> declared=declared();
        final Collection<Reference<Entry<String, URI>>> inherited=inherited(dflt.isPresent(), declared);

        return Stream

                .of(

                        declared.stream(),
                        inherited.stream(),

                        // resolve default [prefix] declarations wrt to inherited namespaces

                        annotations.stream()
                                .filter(annotation -> annotation.is(Namespace.class))
                                .flatMap(annotation -> Optional.of(annotation.string(VALUE))
                                        .map(DEFAULT_PATTERN::matcher)
                                        .filter(Matcher::matches)
                                        .map(m -> m.group(PREFIX))
                                        .map(prefix -> inherited.stream()
                                                .filter(namespace -> namespace.value().getKey().equals(prefix))
                                                .findFirst()
                                                .map(namespace -> new Reference<>(entry(
                                                        "", namespace.value().getValue()), namespace.source()
                                                ))
                                                .orElseThrow(() -> new FrameException(format(
                                                        "undefined prefix <%s> in @%s declaration @ <%s>",
                                                        prefix, simple(Namespace.class), annotation.source()
                                                )))
                                        )
                                        .stream()
                                ),

                        // if no default namespace is defined, generate an implicit declaration

                        dflt.isPresent()
                        || declared.stream().anyMatch(r -> r.value().getKey().isEmpty())
                        || inherited.stream().anyMatch(r -> r.value().getKey().isEmpty())
                                ? Stream.<Reference<Entry<String, URI>>>empty()
                                : Stream.of(new Reference<>(entry("", term("")), name()))

                )

                .flatMap(identity())

                .collect(groupingBy(t -> t.value().getKey(), collectingAndThen(

                        reducing((x, y) -> {

                            if ( x.value().equals(y.value()) ) { return x; } else {
                                throw new FrameException(format(
                                        "conflicting @%s declarations %s / %s", simple(Namespace.class), x, y
                                ));
                            }

                        }),

                        reference -> reference.orElseThrow(() ->
                                new AssertionError("undefined reference")
                        )

                )))

                .values()
                .stream();
    }


    private Optional<String> dflt() {
        return annotations.stream()
                .filter(meta -> meta.is(Namespace.class))
                .flatMap(meta -> Optional.of(meta.string(VALUE))
                        .map(DEFAULT_PATTERN::matcher)
                        .filter(Matcher::matches)
                        .map(m -> new Reference<>(entry(m.group(PREFIX), uri()), meta.source()))
                        .stream()
                )
                .reduce((x, y) -> {

                    if ( x.equals(y) ) { return x; } else {
                        throw new FrameException(format(
                                "multiple default @%s declarations <%s> / <%s>", simple(Namespace.class), x, y
                        ));
                    }

                })
                .map(namespace -> namespace.value().getKey());
    }

    private Collection<Reference<Entry<String, URI>>> declared() {
        return annotations.stream()

                .filter(annotation -> annotation.is(Namespace.class))

                // default [prefix] declarations will be resolved after inspecting ancestors

                .filter(not(annotation -> DEFAULT_PATTERN.matcher(annotation.string(VALUE)).matches()))

                .map(annotation -> {

                    try {

                        return guard(() -> new Reference<>(
                                entry(annotation.string(PREFIX), base().resolve(annotation.string(VALUE))),
                                annotation.source()
                        ));

                    } catch ( final URISyntaxException e ) {

                        throw new FrameException(format(
                                "malformed @%s URI <%s> : %s",
                                simple(Namespace.class), annotation.string(VALUE), e.getMessage()
                        ));

                    }

                })

                .flatMap(namespace -> {

                    final String prefix=namespace.value().getKey();

                    return Optional.of(prefix)

                            .map(DEFAULT_PATTERN::matcher)
                            .filter(Matcher::matches)

                            .map(m -> Stream.of(

                                    new Reference<>(
                                            entry("", namespace.value().getValue()),
                                            namespace.source()
                                    ),

                                    new Reference<>(
                                            entry(m.group(PREFIX), namespace.value().getValue()),
                                            namespace.source()
                                    )

                            ))

                            .orElseGet(() -> Stream.of(namespace));

                })

                .collect(groupingBy(t -> t.value().getKey(), collectingAndThen(

                        reducing((x, y) -> {

                            if ( x.equals(y) ) { return x; } else {
                                throw new FrameException(format(
                                        "conflicting @%s declarations <%s> / <%s>",
                                        simple(Namespace.class), x, y
                                ));
                            }

                        }),

                        reference -> reference.orElseThrow(() ->
                                new AssertionError("undefined reference")
                        )

                )))

                .values();
    }

    private List<Reference<Entry<String, URI>>> inherited(
            final boolean dflt,
            final Collection<Reference<Entry<String, URI>>> declared
    ) {
        return list(parents()

                .flatMap(Clazz::_namespaces)

                // local declarations override inherited ones

                .filter(namespace -> declared.stream().noneMatch(e ->
                        e.value().getKey().equals(namespace.value().getKey())
                ))

                // ignore default namespaces if a default [prefix] declaration is present

                .filter(not(namespace -> dflt && namespace.value().getKey().isEmpty()))

        );
    }


    private <V> V guard(final Supplier<V> supplier) throws URISyntaxException {
        try {

            return supplier.get();

        } catch ( final RuntimeException e ) {

            switch ( e.getCause() ) {

                case final URISyntaxException cause -> throw cause;
                case null, default -> throw e;
            }

        }
    }


    //̸// Flattening ///////////////////////////////////////////////////////////////////////////////////////////////////

    private Clazz flatten() {
        return new Clazz(canonical) // create a virtual class declaring all methods in the hierarchy

                .annotations(annotations) // preserve annotations for metadata processing
                .parents(parents) // preserve hierarchy info

                .methods(lineage()
                        .flatMap(Clazz::methods)
                        .map(m -> m.annotations(list(Stream.concat(

                                m.annotations(),

                                // resolve @Forward/@Reverse wrt the declaring interface before flattening

                                Stream.of(property(m))

                        ))))
                        .collect(groupingBy(
                                Method::name,
                                LinkedHashMap::new,
                                reducing(null, (x, y) -> x == null ? y : new Method(

                                        // reassigned by the new flattened clazz

                                        null,

                                        x.isDefault() || y.isDefault(),

                                        // x and y are methods in the same hierarchy: the followings always match
                                        // use y to support covariant method return types

                                        y.isEnum(),
                                        y.isWild(),
                                        y.type(),
                                        y.item(),
                                        y.name(),

                                        list(Stream.concat(
                                                x.annotations(),
                                                y.annotations()
                                        ))

                                ))
                        ))
                        .values()
                );

    }


    private Annotation property(final Method method) {

        final String implied=Optional.of("")
                .map(empty -> {

                    try {

                        return method.clazz().resolve(empty, method.name()).toString();


                    } catch ( final URISyntaxException e ) {

                        throw new AssertionError(format(
                                "malformed default @%s URIs <%s>",
                                simple(Forward.class), method
                        ), e);

                    }

                })
                .orElse("");

        final String forward=method.annotations()
                .filter(a -> a.is(Forward.class))
                .map(a -> {

                    try {

                        return method.clazz().resolve(a.string(VALUE), method.name()).toString();

                    } catch ( final URISyntaxException e ) {

                        throw new FrameException(format(
                                "malformed @%s URIs <%s> @ <%s>",
                                simple(Forward.class), a.string(VALUE), a.source()
                        ));

                    }

                })
                .findFirst()
                .orElse("");

        final String reverse=method.annotations()
                .filter(a -> a.is(Reverse.class))
                .map(a -> {

                    try {

                        return method.clazz().resolve(a.string(VALUE), method.name()).toString();

                    } catch ( final URISyntaxException e ) {

                        throw new FrameException(format(
                                "malformed @%s URIs <%s> @ <%s>",
                                simple(Reverse.class), a.string(VALUE), a.source()
                        ));

                    }

                })
                .findFirst()
                .orElse("");

        return new Annotation(
                Property.class,
                Map.of(
                        IMPLIED, implied,
                        FORWARD, forward,
                        REVERSE, reverse
                ),
                "%s.%s()".formatted(method.clazz().name(), method.name())
        );
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    @Override public String toString() {
        return name();
    }

}
