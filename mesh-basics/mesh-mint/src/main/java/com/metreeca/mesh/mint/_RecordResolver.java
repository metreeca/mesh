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

package com.metreeca.mesh.mint;

import com.metreeca.mesh.meta.jsonld.Base;
import com.metreeca.mesh.meta.jsonld.Namespace;
import com.metreeca.mesh.mint._RecordModel.Meta;
import com.metreeca.mesh.mint._RecordModel.Tray;
import com.metreeca.mesh.mint._RecordModel.Tree;
import com.metreeca.mesh.util.URIs;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint._RecordGlass.*;
import static com.metreeca.mesh.util.Collections.entry;
import static com.metreeca.mesh.util.Exceptions.error;
import static com.metreeca.mesh.util.URIs.term;
import static com.metreeca.mesh.util.URIs.uri;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableMap;
import static java.util.function.Function.identity;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.*;

public final class _RecordResolver {

    private static final Pattern NAME_PATTERN=Pattern.compile(
            "[_a-zA-Z][-._a-zA-Z0-9]*"
    );

    private static final Pattern DEFAULT_PATTERN=Pattern.compile(
            "\\[(?<prefix>"+NAME_PATTERN+")]"
    );

    /**
     * Simplified version of <a href="https://www.w3.org/TR/2010/NOTE-curie-20101216/#s_syntax">CURIE Syntax 1.0</a>:
     */
    private static final Pattern CURIE_PATTERN=Pattern.compile(
            "(?:(?<prefix>"+NAME_PATTERN+")?:)?(?<name>"+NAME_PATTERN+")?"
    );


    private static Optional<Tray<URI>> base(final Tree metas) {
        return metas.metas().stream()

                .filter(meta -> meta.is(Base.class))
                .map(meta -> new Tray<>(uri(value(meta)), meta.source()))
                .peek(meta -> {

                    if ( !meta.value().isAbsolute() ) {
                        throw new IllegalArgumentException(format(
                                "relative @%s URIs %s", simple(Base.class), meta
                        ));
                    }

                })
                .reduce((x, y) -> x.value().equals(y.value()) ? x : error(new IllegalArgumentException(format(
                        "conflicting @%s URIs %s / %s", simple(Base.class), x, y
                ))))

                .or(() -> metas.ancestors().stream()
                        .map(_RecordResolver::base)
                        .flatMap(Optional::stream)
                        .reduce((x, y) -> x.value().equals(y.value()) ? x : error(new IllegalArgumentException(format(
                                "conflicting @%s URIs %s / %s", simple(Base.class), x, y
                        ))))
                );
    }

    private static Stream<Tray<Entry<String, URI>>> namespaces(final Tree metas, final URI base) {

        // identify the default [prefix] declaration

        final Optional<String> dflt=metas.metas().stream()
                .filter(meta -> meta.is(Namespace.class))
                .flatMap(meta -> Optional.of(value(meta))
                        .map(DEFAULT_PATTERN::matcher)
                        .filter(Matcher::matches)
                        .map(m -> new Tray<>(entry(m.group(PREFIX), uri()), meta.source()))
                        .stream()
                )
                .reduce((x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format(
                        "multiple default @%s declarations <%s> / <%s>", simple(Namespace.class), x, y
                ))))
                .map(t -> t.value().getKey());

        final Collection<Tray<Entry<String, URI>>> declared=metas.metas().stream()

                .filter(meta -> meta.is(Namespace.class))

                // default [prefix] declarations will be resolved after inspecting ancestors

                .filter(not(meta -> DEFAULT_PATTERN.matcher(value(meta)).matches()))

                .map(meta -> {
                    try {

                        return guard(() -> new Tray<>(
                                entry(prefix(meta), base.resolve(value(meta))),
                                meta.source()
                        ));

                    } catch ( final URISyntaxException e ) {

                        throw new IllegalArgumentException(format(
                                "malformed @%s URI <%s>: %s", simple(Namespace.class), value(meta), e.getMessage()
                        ));

                    }
                })

                .flatMap(namespace -> {

                    final String prefix=namespace.value().getKey();

                    return Optional.of(prefix)

                            .map(DEFAULT_PATTERN::matcher)
                            .filter(Matcher::matches)

                            .map(m -> Stream.of(

                                    new Tray<>(
                                            entry("", namespace.value().getValue()),
                                            namespace.source()
                                    ),

                                    new Tray<>(
                                            entry(m.group(PREFIX), namespace.value().getValue()),
                                            namespace.source()
                                    )

                            ))

                            .orElseGet(() -> Stream.of(namespace));

                })

                .collect(groupingBy(t -> t.value().getKey(), collectingAndThen(

                        reducing((x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format(
                                "conflicting @%s declarations <%s> / <%s>", simple(Namespace.class), x, y
                        )))),

                        binding -> binding.orElseThrow(() ->
                                new AssertionError("undefined binding")
                        )

                )))

                .values();

        final Collection<Tray<Entry<String, URI>>> inherited=metas.ancestors().stream()

                .flatMap(tree -> namespaces(tree, base))

                // local declarations override inherited ones

                .filter(namespace -> declared.stream().noneMatch(t ->
                        t.value().getKey().equals(namespace.value().getKey())
                ))

                // ignore default namespaces if a default [prefix] declaration is present

                .filter(not(namespace -> dflt.isPresent() && namespace.value().getKey().isEmpty()))

                .toList();

        return Stream

                .of(

                        declared.stream(),
                        inherited.stream(),

                        // resolve default [prefix]] declarations wrt to inherited namespaces

                        metas.metas().stream()
                                .filter(meta -> meta.is(Namespace.class))
                                .flatMap(meta -> Optional.of(value(meta))
                                        .map(DEFAULT_PATTERN::matcher)
                                        .filter(Matcher::matches)
                                        .map(m -> m.group(PREFIX))
                                        .map(prefix -> inherited.stream()
                                                .filter(namespace -> namespace.value().getKey().equals(prefix))
                                                .findFirst()
                                                .map(namespace -> new Tray<>(entry("", namespace.value().getValue()), namespace.source()))
                                                .orElseThrow(() -> new IllegalArgumentException(format(
                                                        "undefined prefix <%s> in @%s declaration @ <%s>",
                                                        prefix, simple(Namespace.class), meta.source()
                                                )))
                                        )
                                        .stream()
                                )
                )

                .flatMap(identity())

                .collect(groupingBy(t -> t.value().getKey(), collectingAndThen(

                        reducing((x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format(
                                "conflicting @%s declarations %s / %s", simple(Namespace.class), x, y
                        )))),

                        binding -> binding.orElseThrow(() ->
                                new AssertionError("undefined binding")
                        )

                )))

                .values()
                .stream();

    }


    private static String value(final Meta meta) {
        return Optional.ofNullable(meta.string(VALUE)).orElseThrow(() ->
                new AssertionError(format("missing annotation value() <%s>", meta))
        );
    }

    private static String prefix(final Meta meta) {
        return Optional.ofNullable(meta.string(PREFIX)).orElse("");
    }


    private static <V> V guard(final Supplier<V> supplier) throws URISyntaxException {
        try {

            return supplier.get();

        } catch ( final RuntimeException e ) {

            switch ( e.getCause() ) {

                case final URISyntaxException cause -> throw cause;
                case null, default -> throw e;
            }

        }
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final URI base;
    private final Map<String, URI> namespaces;


    public _RecordResolver(final Tree tree) {
        this.base=base(tree).map(Tray::value).orElseGet(URIs::base);
        this.namespaces=namespaces(tree, base).map(Tray::value).collect(toUnmodifiableMap(Entry::getKey, Entry::getValue));
    }

    _RecordResolver(final URI base, final Map<String, URI> namespaces) {
        this.base=base;
        this.namespaces=unmodifiableMap(namespaces);
    }


    public URI base() {
        return base;
    }

    Map<String, URI> namespaces() {
        return namespaces;
    }


    URI resolve(final String uri, final String fallback) throws URISyntaxException {
        return guard(() -> Optional.of(uri)

                .map(CURIE_PATTERN::matcher)
                .filter(Matcher::matches)

                .map(m -> {

                    final String prefix=m.group(PREFIX);
                    final String name=Optional.ofNullable(m.group(NAME)).orElse(fallback);

                    if ( prefix == null ) {

                        return Optional.ofNullable(namespaces.get(""))
                                .map(namespace -> uri(namespace+name))
                                .orElseGet(() -> term(name));

                    } else {

                        return Optional.ofNullable(namespaces.get(prefix))
                                .map(namespace -> uri(namespace+name))
                                .orElseThrow(() -> new RuntimeException(new URISyntaxException(uri, format(
                                        "undefined prefix <%s>", prefix
                                ))));

                    }

                })

                .orElseGet(() -> base.resolve(uri.isEmpty() ? fallback : uri))
        );
    }

}
