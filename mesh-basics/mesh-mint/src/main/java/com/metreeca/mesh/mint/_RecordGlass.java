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

import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.jsonld.Id;
import com.metreeca.mesh.meta.jsonld.Internal;
import com.metreeca.mesh.meta.jsonld.Type;
import com.metreeca.mesh.meta.shacl.*;
import com.metreeca.mesh.mint.ast.Reference;
import com.metreeca.mesh.shapes.Shape;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.time.*;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.metreeca.mesh.mint._RecordModel.*;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.util.Collections.*;
import static com.metreeca.mesh.util.Exceptions.error;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * Record introspection utility ;-)
 */
public final class _RecordGlass {

    static final String NAME="name";
    public static final String VALUE="value";
    public static final String PREFIX="prefix";
    static final String REVERSE="reverse";


    private static final Pattern CANONICAL_PATTERN=Pattern.compile("^(.*)\\.([^.]*)");

    private static final Class<?> CLASS=com.metreeca.mesh.meta.jsonld.Class.class; // ;( avoid repeated FQNs


    static String simple(final Class<?> clazz) {
        return Reference.simple(clazz);
    }

    public static String canonical(final Class<?> clazz) {
        return clazz.getCanonicalName();
    }

    private static String canonical(final AnnotationMirror annotation) {
        return ((QualifiedNameable)annotation.getAnnotationType().asElement()).getQualifiedName().toString();
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private final Types typeUtils;
    private final Elements elementUtils;

    private final TypeElement object;

    private final Map<Class<?>, TypeMirror> types=new ConcurrentHashMap<>();


    public _RecordGlass(final ProcessingEnvironment environment) {

        this.typeUtils=environment.getTypeUtils();
        this.elementUtils=environment.getElementUtils();

        this.object=elementUtils.getTypeElement(canonical(Object.class));
    }


    //̸/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public Info info(final TypeElement clazz) {

        if ( !isFrame(clazz) ) {
            throw new IllegalArgumentException(format(
                    "missing %s annotation on <%s>", simple(Frame.class), clazz
            ));
        }

        if ( !isInterface(clazz) ) {
            throw new IllegalArgumentException(format(
                    "concrete class <%s>", clazz
            ));
        }

        if ( !isTopLevel(clazz) ) {
            throw new IllegalArgumentException(format(
                    "nested interface <%s>", clazz
            ));
        }

        if ( isGeneric(clazz) ) {
            throw new IllegalArgumentException(format(
                    "generic interface <%s>", clazz
            ));
        }


        final Tree tree=tree(clazz);

        final _RecordResolver resolver=new _RecordResolver(tree);
        final _RecordShaper shaper=new _RecordShaper(tree);

        final Set<Slot> slots=slots(clazz);

        final Shape id=slots.stream()
                .filter(Slot::id)
                .reduce((x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format(
                        "multiple @%s methods <%s> / <%s>", simple(Id.class), x.name(), y.name()
                ))))
                .map(Slot::name)
                .map(i -> shape().id(i))
                .orElseGet(Shape::shape);

        final Shape type=slots.stream()
                .filter(Slot::type)
                .reduce((x, y) -> x.equals(y) ? x : error(new IllegalArgumentException(format(
                        "multiple @%s methods <%s> / <%s>", simple(Type.class), x.name(), y.name()
                ))))
                .map(Slot::name)
                .map(t -> shape().type(t))
                .orElseGet(Shape::shape);

        return new Info(

                resolver.base(),

                pkg(clazz),
                cls(clazz),

                slots,

                shape()
                        .merge(id)
                        .merge(type)
                        .merge(shaper.virtual())
                        .merge(shaper.clazz()),

                shaper.constraints()

        );
    }


    private String pkg(final QualifiedNameable clazz) {
        return CANONICAL_PATTERN.matcher(clazz.getQualifiedName().toString()).replaceAll("$1");
    }

    private String cls(final Element clazz) {
        return clazz.getSimpleName().toString();
    }


    private Set<Slot> slots(final TypeElement clazz) {
        return set(methods(clazz).map(method -> slot(clazz, method)));
    }

    private Slot slot(final TypeElement clazz, final ExecutableElement method) {

        final String name=method.getSimpleName().toString();

        if ( isGeneric(method) ) {
            throw new IllegalArgumentException(format(
                    "generic return type <%s>", method
            ));
        }

        if ( method.getReturnType().getKind() == VOID ) {
            throw new IllegalArgumentException(format(
                    "void return type <%s>", method
            ));
        }

        if ( name.equals(method.getEnclosingElement().getSimpleName().toString()) ) {
            throw new IllegalArgumentException(format(
                    "reserved method name <%s>", method
            ));
        }

        if ( !method.getParameters().isEmpty() ) {
            throw new IllegalArgumentException(format(
                    "unexpected arguments on method <%s.%s>", clazz, method
            ));
        }


        final TypeMirror type=method.getReturnType();

        // comparing textually to avoid issues on incremental compilation…
        // classes retrieved through elementUtils are apparently different across builds

        if ( isId(method) && !type.toString().equals(canonical(URI.class)) ) {
            throw new IllegalArgumentException(format(
                    "unexpected return type <%s> for @Id method <%s.%s>", type, clazz, method
            ));
        }

        if ( isType(method) && !type.toString().equals(canonical(String.class)) ) {
            throw new IllegalArgumentException(format(
                    "unexpected return type <%s> for @Type method <%s.%s>", type, clazz, method
            ));
        }

        final boolean internal=hasAnnotation(method, Internal.class);

        final String base=internal ? type.toString() : base(type);
        final String item=internal ? null : item(type, clazz);


        final boolean multiple=base.equals(SET) || base.equals(LIST)
                               || base.equals(TEXTS) || base.equals(TEXTSETS)
                               || base.equals(DATAS) || base.equals(DATASETS);

        final Tree c=tree(clazz);
        final Tree m=tree(clazz, method);

        final _RecordShaper shaper=new _RecordShaper(m);

        return new Slot(

                isDefault(method),

                isId(method),
                isType(method),
                isInternal(method),

                base,
                item,
                name,

                shaper.property(c, m)
                        .embedded(shaper.embedded())
                        .shape(shape()
                                .merge(shaper.virtual())
                                .merge(shaper.datatype(item != null ? item : base))
                                .merge(shaper.minExclusive(item != null ? item : base))
                                .merge(shaper.maxExclusive(item != null ? item : base))
                                .merge(shaper.minInclusive(item != null ? item : base))
                                .merge(shaper.maxInclusive(item != null ? item : base))
                                .merge(shaper.minLength())
                                .merge(shaper.maxLength())
                                .merge(shaper.pattern())
                                .merge(shaper.in(item != null ? item : base))
                                .merge(shaper.languageIn(item != null ? item : base))
                                .merge(shaper.uniqueLang(item != null ? item : base))
                                .merge(shaper.minCount(multiple))
                                .merge(shaper.maxCount(multiple))
                                .merge(shaper.hasValue(item != null ? item : base))
                        )

        );
    }


    public Stream<ExecutableElement> methods(final TypeElement clazz) { // !!! private
        return elementUtils.getAllMembers(clazz).stream()

                .filter(member -> member.getKind() == METHOD)
                .filter(not(method -> method.getEnclosingElement().equals(object)))
                .map(ExecutableElement.class::cast)

                .filter(not(this::isStatic))
                .filter(not(this::isUtility));
    }


    //̸// Types ////////////////////////////////////////////////////////////////////////////////////////////////////////

    private TypeMirror type(final Class<?> clazz) {
        return types.computeIfAbsent(clazz, key ->
                elementUtils.getTypeElement(canonical(key)).asType()
        );
    }


    private String base(final TypeMirror type) {
        return type(type) instanceof final String base ? base

                : isSame(raw(type), Set.class) ? SET
                : isSame(raw(type), List.class) ? LIST
                : isSame(raw(type), Collection.class) ? COLLECTION

                : isText(type) ? TEXT
                : isTexts(type) ? TEXTS
                : isTextsets(type) ? TEXTSETS

                : isData(type) ? DATA
                : isDatas(type) ? DATAS
                : isDatasets(type) ? DATASETS

                : error(new UnsupportedOperationException(format("unsupported type <%s>", type)));
    }

    private String item(final TypeMirror type, final TypeElement clazz) {
        if ( isCollection(type) ) {

            return Stream.of(type)
                    .filter(DeclaredType.class::isInstance)
                    .map(DeclaredType.class::cast)
                    .map(DeclaredType::getTypeArguments)
                    .flatMap(Collection::stream)
                    .findFirst()
                    .map(t -> t.getKind() == TYPEVAR ? resolve(t, clazz) : t)
                    .map(this::type)
                    .orElseThrow(() -> new UnsupportedOperationException(format(
                            "unsupported collection item type <%s>", type
                    )));

        } else {

            return null;

        }
    }

    private String type(final TypeMirror type) {
        return type instanceof final TypeVariable var ? var.asElement().getSimpleName().toString()

                : type.getKind() == VOID ? simple(void.class)
                : type.getKind() == TypeKind.BOOLEAN ? simple(boolean.class)
                : type.getKind() == TypeKind.BYTE ? simple(byte.class)
                : type.getKind() == TypeKind.SHORT ? simple(short.class)
                : type.getKind() == TypeKind.INT ? simple(int.class)
                : type.getKind() == TypeKind.LONG ? simple(long.class)
                : type.getKind() == TypeKind.FLOAT ? simple(float.class)
                : type.getKind() == TypeKind.DOUBLE ? simple(double.class)

                : isSame(type, Boolean.class) ? simple(Boolean.class)
                : isSame(type, Number.class) ? simple(Number.class)
                : isSame(type, Byte.class) ? simple(Byte.class)
                : isSame(type, Short.class) ? simple(Short.class)
                : isSame(type, Integer.class) ? simple(Integer.class)
                : isSame(type, Long.class) ? simple(Long.class)
                : isSame(type, Float.class) ? simple(Float.class)
                : isSame(type, Double.class) ? simple(Double.class)
                : isSame(type, BigInteger.class) ? simple(BigInteger.class)
                : isSame(type, BigDecimal.class) ? simple(BigDecimal.class)
                : isSame(type, String.class) ? simple(String.class)
                : isSame(type, URI.class) ? simple(URI.class)
                : isSame(type, Temporal.class) ? simple(Temporal.class)
                : isSame(type, Year.class) ? simple(Year.class)
                : isSame(type, YearMonth.class) ? simple(YearMonth.class)
                : isSame(type, LocalDate.class) ? simple(LocalDate.class)
                : isSame(type, LocalTime.class) ? simple(LocalTime.class)
                : isSame(type, OffsetTime.class) ? simple(OffsetTime.class)
                : isSame(type, LocalDateTime.class) ? simple(LocalDateTime.class)
                : isSame(type, OffsetDateTime.class) ? simple(OffsetDateTime.class)
                : isSame(type, ZonedDateTime.class) ? simple(ZonedDateTime.class)
                : isSame(type, Instant.class) ? simple(Instant.class)
                : isSame(type, TemporalAmount.class) ? simple(TemporalAmount.class)
                : isSame(type, Period.class) ? simple(Period.class)
                : isSame(type, Duration.class) ? simple(Duration.class)

                // !!! enums

                : type instanceof final DeclaredType declared
                  && declared.asElement() instanceof final TypeElement element
                  && isFrame(element) ? element.getQualifiedName().toString()

                : null;
    }


    private TypeMirror raw(final TypeMirror type) {
        return typeUtils.erasure(type);
    }

    private TypeMirror resolve(final TypeMirror variable, final TypeElement clazz) {

        final Map<TypeMirror, TypeMirror> bindings=new HashMap<>();
        final Queue<TypeElement> queue=new ArrayDeque<>(list(clazz));

        while ( !queue.isEmpty() ) {

            final TypeElement current=queue.remove();

            for (final TypeMirror base : current.getInterfaces()) {
                if ( base instanceof final DeclaredType declared ) {
                    if ( declared.asElement() instanceof final TypeElement type ) {

                        final List<? extends TypeParameterElement> parameters=type.getTypeParameters();
                        final List<? extends TypeMirror> arguments=declared.getTypeArguments();

                        for (int i=0; i < parameters.size(); ++i) {

                            final TypeMirror parameter=parameters.get(i).asType();
                            final TypeMirror argument=arguments.get(i);

                            bindings.put(parameter, bindings.getOrDefault(argument, argument));
                        }

                        queue.add(type);

                    }
                }
            }
        }

        return bindings.getOrDefault(variable, variable);
    }


    //̸// Annotations //////////////////////////////////////////////////////////////////////////////////////////////////

    public Tree tree(final TypeElement clazz) {

        final String name=clazz.getSimpleName().toString();

        final List<Meta> metas=clazz.getAnnotationMirrors().stream()
                .flatMap(a -> tree(a, source(clazz)))
                .flatMap(this::expand)
                .toList();

        final List<Tree> trees=clazz.getInterfaces().stream()
                .map(i -> tree(((TypeElement)((DeclaredType)i).asElement())))
                .toList();

        return new Tree(name, metas, trees);
    }

    public Tree tree(final TypeElement clazz, final Element method) { // !!!

        final ExecutableType baseMethodType=(ExecutableType)typeUtils.asMemberOf((DeclaredType)clazz.asType(), method);

        return tree(clazz, method, baseMethodType);
    }

    private Tree tree(final TypeElement clazz, final Element method, final ExecutableType baseMethodType) {

        final String name=method.getSimpleName().toString();

        final List<Meta> metas=new ArrayList<>();

        // search for a matching method in the current interface

        for (final Element enclosed : clazz.getEnclosedElements()) {
            if ( enclosed instanceof final ExecutableElement candidate
                 && clazz.asType() instanceof final DeclaredType declared
                 && typeUtils.asMemberOf(declared, candidate) instanceof final ExecutableType candidateMethodType
            ) {

                if ( method.getSimpleName().equals(candidate.getSimpleName())
                     && typeUtils.isSubsignature(candidateMethodType, baseMethodType)
                ) {

                    for (final AnnotationMirror annotation : candidate.getAnnotationMirrors()) {
                        tree(annotation, source(clazz, method))
                                .flatMap(this::expand)
                                .forEach(metas::add);
                    }

                }
            }
        }

        return new Tree(name, metas, clazz.getInterfaces().stream()
                .map(i -> tree(
                        (TypeElement)((DeclaredType)i).asElement(),
                        method,
                        baseMethodType
                ))
                .toList()
        );
    }


    public List<Meta> expand(final List<Meta> metas) { // expand @Aliased annotations
        return metas.stream()
                .flatMap(this::expand)
                .toList();

    }

    private Stream<Meta> expand(final Meta meta) {
        try { // expand as a pre-compiled annotation

            final Class<?> type=Class.forName(meta.type());

            return expand(meta, type);

        } catch ( final ClassNotFoundException ignored ) { // expand as a source annotation

            final TypeElement annotation=elementUtils.getTypeElement(meta.type());

            if ( annotation == null || annotation.getKind() != ANNOTATION_TYPE ) {
                throw new NoSuchElementException(format("unknown annotation class <%s>", meta.type()));
            }

            return expand(meta, annotation);

        }
    }

    private Stream<Meta> expand(final Meta meta, final Class<?> annotation) {
        return Optional.ofNullable(annotation.getDeclaredAnnotation(Alias.class))
                .map(alias -> Stream.concat(

                        Stream.of(

                                new Meta(
                                        MinExclusive.class,
                                        map(entry(VALUE, alias.MinExclusive().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        MaxExclusive.class,
                                        map(entry(VALUE, alias.MaxExclusive().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        MinInclusive.class,
                                        map(entry(VALUE, alias.MinInclusive().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        MaxInclusive.class,
                                        map(entry(VALUE, alias.MaxInclusive().value())),
                                        meta.source()
                                ),


                                new Meta(
                                        MinLength.class,
                                        map(entry(VALUE, alias.MinLength().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        MaxLength.class,
                                        map(entry(VALUE, alias.MaxLength().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        com.metreeca.mesh.meta.shacl.Pattern.class,
                                        map(entry(VALUE, alias.Pattern().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        In.class,
                                        map(entry(VALUE, list(alias.In().value()))),
                                        meta.source()
                                ),

                                new Meta(
                                        LanguageIn.class,
                                        map(entry(VALUE, list(alias.LanguageIn().value()))),
                                        meta.source()
                                ),

                                new Meta(
                                        MinCount.class,
                                        map(entry(VALUE, alias.MinCount().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        MaxCount.class,
                                        map(entry(VALUE, alias.MaxCount().value())),
                                        meta.source()
                                ),

                                new Meta(
                                        HasValue.class,
                                        map(entry(VALUE, list(alias.HasValue().value()))),
                                        meta.source()
                                )

                        ),

                        Arrays.stream(alias.Constraints().value()).map(constraint -> new Meta(
                                Constraint.class,
                                map(entry(VALUE, canonical(constraint.value()))),
                                meta.source()
                        ))

                ))
                .orElseGet(() -> Stream.of(meta));
    }

    private Stream<Meta> expand(final Meta meta, final AnnotatedConstruct annotation) {
        return annotation.getAnnotationMirrors().stream()
                .filter(a -> canonical(a).equals(canonical(Alias.class)))
                .findFirst()
                .map(alias -> alias.getElementValues().values().stream()
                        .map(v -> (AnnotationMirror)v.getValue())
                        .flatMap(value -> {

                            if ( canonical(value).equals(canonical(Constraints.class)) ) {

                                return value.getElementValues().values().stream()
                                        .map(AnnotationValue::getValue)
                                        .map(List.class::cast)
                                        .<Object>flatMap(List::stream)
                                        .map(a -> meta(((AnnotationMirror)a), meta.source()));

                            } else {

                                return Stream.of(meta(value, meta.source()));


                            }

                        })
                )
                .orElseGet(() -> Stream.of(meta));
    }


    private Stream<Meta> tree(final AnnotationMirror annotation, final String source) {
        return Optional.of(annotation.getElementValues())

                .filter(methods -> methods.size() == 1)

                .stream()
                .map(Map::entrySet)
                .flatMap(Collection::stream)
                .findFirst()

                .filter(e -> isAnnotationContainer(e.getKey()))

                .map(Entry::getValue)

                .map(annotationValue -> ((Collection<?>)annotationValue.getValue()).stream()
                        .filter(AnnotationValue.class::isInstance)
                        .map(v -> (AnnotationMirror)((AnnotationValue)v).getValue())
                        .map(annotationMirror -> meta(annotationMirror, source))
                )

                .orElseGet(() -> Stream.of(meta(annotation, source)));

    }

    private Meta meta(final AnnotationMirror annotation, final String source) {

        final String type=((QualifiedNameable)annotation.getAnnotationType().asElement()).getQualifiedName().toString();

        final Map<String, Object> arguments=new LinkedHashMap<>();

        for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotation.getElementValues().entrySet()
        ) {

            final String param=entry.getKey().getSimpleName().toString();
            final Object value=entry.getValue().getValue();

            arguments.put(param, value(value));
        }

        return new Meta(type, arguments, source);
    }


    private Object value(final Object value) {
        if ( value instanceof final List<?> list ) {

            return list.stream()
                    .map(v -> value(((AnnotationValue)v).getValue()))
                    .toList();

        } else if ( value instanceof final TypeMirror typeMirror
                    && typeMirror instanceof final DeclaredType declaredType
                    && declaredType.asElement() instanceof final TypeElement typeElement
        ) {

            return typeElement.getQualifiedName().toString();

        } else {

            return value;

        }
    }


    private static String source(final Element current) {
        return current.getSimpleName().toString();
    }

    private static String source(final Element type, final Element method) {
        return "%s.%s()".formatted(
                type.getSimpleName().toString(), method.getSimpleName()
        );
    }


    //̸// Checks ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isGeneric(final Parameterizable parameterizable) {
        return !parameterizable.getTypeParameters().isEmpty();
    }


    private boolean isFrame(final Element element) {
        return isTopLevel(element)
               && isInterface(element)
               && hasAnnotation(element, Frame.class);
    }

    private boolean isInterface(final Element element) {
        return element.getKind() == INTERFACE;
    }

    private boolean isTopLevel(final Element element) {
        return element.getEnclosingElement().getKind() == PACKAGE;
    }

    private boolean isStatic(final Element element) {
        return element.getModifiers().contains(STATIC);
    }

    private boolean isDefault(final Element method) {
        return method.getModifiers().contains(DEFAULT);
    }

    private boolean isUtility(final ExecutableElement method) {
        return isInternal(method) && isDefault(method) && !method.getParameters().isEmpty();
    }

    private boolean isId(final AnnotatedConstruct method) {
        return hasAnnotation(method, Id.class);
    }

    private boolean isType(final AnnotatedConstruct method) {
        return hasAnnotation(method, Type.class);
    }

    private boolean isInternal(final AnnotatedConstruct method) {
        return hasAnnotation(method, Internal.class);
    }


    private boolean isCollection(final TypeMirror type) {
        return typeUtils.isSubtype(raw(type), raw(type(Collection.class)));
    }

    private boolean isSame(final TypeMirror type, final Class<?> clazz) {
        return typeUtils.isSameType(type, raw(type(clazz)));
    }


    private boolean isText(final TypeMirror type) {
        return type instanceof final DeclaredType declared
               && isSame(raw(declared), Entry.class)
               && isSame(declared.getTypeArguments().get(0), Locale.class)
               && isSame(declared.getTypeArguments().get(1), String.class);
    }

    private boolean isTexts(final TypeMirror type) {
        return type instanceof final DeclaredType declare
               && isSame(raw(declare), Map.class)
               && isSame(declare.getTypeArguments().get(0), Locale.class)
               && isSame(declare.getTypeArguments().get(1), String.class);
    }

    private boolean isTextsets(final TypeMirror type) {
        return type instanceof final DeclaredType declared
               && isSame(raw(declared), Map.class)
               && isSame(declared.getTypeArguments().get(0), Locale.class)
               && declared.getTypeArguments().get(1) instanceof final DeclaredType value
               && isSame(raw(value), Set.class)
               && isSame(value.getTypeArguments().getFirst(), String.class);
    }


    private boolean isData(final TypeMirror type) {
        return type instanceof final DeclaredType declared
               && isSame(raw(declared), Entry.class)
               && isSame(declared.getTypeArguments().get(0), URI.class)
               && isSame(declared.getTypeArguments().get(1), String.class);
    }

    private boolean isDatas(final TypeMirror type) {
        return type instanceof final DeclaredType declare
               && isSame(raw(declare), Map.class)
               && isSame(declare.getTypeArguments().get(0), URI.class)
               && isSame(declare.getTypeArguments().get(1), String.class);
    }

    private boolean isDatasets(final TypeMirror type) {
        return type instanceof final DeclaredType declared
               && isSame(raw(declared), Map.class)
               && isSame(declared.getTypeArguments().get(0), URI.class)
               && declared.getTypeArguments().get(1) instanceof final DeclaredType value
               && isSame(raw(value), Set.class)
               && isSame(value.getTypeArguments().getFirst(), String.class);
    }


    private boolean isAnnotationContainer(final ExecutableElement method) {

        if ( !method.getSimpleName().toString().contentEquals(VALUE) ) {
            return false;
        }

        final TypeMirror returnType=method.getReturnType();

        if ( returnType.getKind() != TypeKind.ARRAY ) {
            return false;
        }

        final ArrayType arrayType=(ArrayType)returnType;
        final TypeMirror componentType=arrayType.getComponentType();

        return typeUtils.asElement(componentType).getKind() == ANNOTATION_TYPE;
    }

    private boolean hasAnnotation(final AnnotatedConstruct element, final Class<?> annotation) {
        return element.getAnnotationMirrors().stream()
                .map(AnnotationMirror::getAnnotationType)
                .anyMatch(type -> isSame(type, annotation));
    }

}
