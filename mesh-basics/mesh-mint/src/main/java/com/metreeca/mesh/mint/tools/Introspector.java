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

package com.metreeca.mesh.mint.tools;

import com.metreeca.mesh.meta.jsonld.Embedded;
import com.metreeca.mesh.meta.jsonld.Foreign;
import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.jsonld.Internal;
import com.metreeca.mesh.meta.shacl.*;
import com.metreeca.mesh.mint.FrameException;
import com.metreeca.mesh.mint.ast.Annotation;
import com.metreeca.mesh.mint.ast.Clazz;
import com.metreeca.mesh.mint.ast.Method;

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
import java.util.stream.Stream;

import static com.metreeca.mesh.mint.ast.Annotation.VALUE;
import static com.metreeca.mesh.mint.ast.Method.*;
import static com.metreeca.mesh.mint.ast.Reference.simple;
import static com.metreeca.shim.Collections.*;
import static com.metreeca.shim.Exceptions.error;

import static java.lang.String.format;
import static java.util.function.Predicate.not;
import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.DEFAULT;
import static javax.lang.model.element.Modifier.STATIC;
import static javax.lang.model.type.TypeKind.TYPEVAR;
import static javax.lang.model.type.TypeKind.VOID;

/**
 * Analyzes Java interface classes to extract JSON-LD frame models.
 *
 * <p>Processes Java interface classes annotated with {@link com.metreeca.mesh.meta.jsonld.Frame} to extract
 * information about their structure, type hierarchy, and property constraints. The extracted information is used to
 * generate data binding code and shape validation constraints.</p>
 *
 * <p>Key responsibilities include:</p>
 * <ul>
 *   <li>Validating that target classes meet frame interface requirements (non-generic, top-level interfaces)</li>
 *   <li>Extracting annotations and resolving annotationsaliases to standard constraint forms</li>
 *   <li>Managing type resolutions and inheritance hierarchies</li>
 *   <li>Identifying and validating property types including specialized collection and entry types</li>
 *   <li>Processing method constraints including cardinality, value ranges, and pattern constraints</li>
 * </ul>
 */
public final class Introspector {

    private static final Set<String> RESERVED=set("toToken", "toValue", "toFrame", "get");


    private static final Map<String, Clazz> CACHE=new ConcurrentHashMap<>();


    private static String canonical(final Class<?> clazz) {
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


    public Introspector(final ProcessingEnvironment environment) {

        this.typeUtils=environment.getTypeUtils();
        this.elementUtils=environment.getElementUtils();

        this.object=elementUtils.getTypeElement(canonical(Object.class));
    }


    public Clazz clazz(final TypeElement type) {

        final Clazz clazz=lookup(type);

        // check for reference loops in embedded frame values

        final Deque<String> pending=new ArrayDeque<>();
        final Set<String> visited=new LinkedHashSet<>();

        pending.add(clazz.canonical());

        while ( !pending.isEmpty() ) {

            final Clazz next=lookup(elementUtils.getTypeElement(pending.remove()));

            if ( visited.add(next.canonical()) ) {

                next.methods()
                        .filter(not(Method::isInternal))
                        .filter(not(Method::isLiteral))
                        .filter(Method::embedded)
                        .map(Method::core)
                        .distinct()
                        .forEach(pending::add);

            } else {

                throw new FrameException(format(
                        "self-referencing embedded frame value %s", clazz.name()
                ));

            }
        }


        return clazz;

    }

    private Clazz lookup(final TypeElement type) {
        return CACHE.computeIfAbsent(type.getQualifiedName().toString(), key -> clazz(type, type));
    }


    //̸// Elements /////////////////////////////////////////////////////////////////////////////////////////////////////

    private Clazz clazz(final TypeElement clazz, final TypeElement root) {

        if ( clazz.equals(root) && isGeneric(clazz) ) {
            throw new FrameException(format(
                    "generic interface <%s>", clazz
            ));
        }

        if ( clazz.equals(root) && !isFrame(clazz) ) {
            throw new FrameException(format(
                    "missing %s annotation on <%s>", simple(Frame.class), clazz
            ));
        }

        if ( !isInterface(clazz) ) {
            throw new FrameException(format(
                    "concrete class <%s>", clazz
            ));
        }

        if ( !isTopLevel(clazz) ) {
            throw new FrameException(format(
                    "nested interface <%s>", clazz
            ));
        }

        return new Clazz(clazz.getQualifiedName().toString())

                .annotations(list(annotations(clazz, source(clazz))))

                .parents(list(clazz.getInterfaces().stream()
                        .map(i -> (TypeElement)typeUtils.asElement(i))
                        .map(i -> clazz(i, root))
                ))

                .methods(list(clazz.getEnclosedElements().stream()

                        .filter(member -> member.getKind() == METHOD)
                        .filter(not(mmethod -> mmethod.getEnclosingElement().equals(object)))
                        .map(ExecutableElement.class::cast)

                        .filter(not(this::isStatic))
                        .filter(not(this::isUtility))

                        .map(method -> method(clazz, root, method))
                ));
    }

    private Method method(final TypeElement clazz, final TypeElement root, final ExecutableElement method) {

        final String name=method.getSimpleName().toString();

        if ( isGeneric(method) ) {
            throw new FrameException(format(
                    "generic return type on method <%s.%s>", clazz, method
            ));
        }

        if ( method.getReturnType().getKind() == VOID ) {
            throw new FrameException(format(
                    "void return type on method <%s.%s>", clazz, method
            ));
        }

        if ( name.startsWith("$") || RESERVED.contains(name)
                || name.equals(method.getEnclosingElement().getSimpleName().toString())
        ) {
            throw new FrameException(format(
                    "reserved method name on method <%s.%s>", clazz, method
            ));
        }

        if ( !method.getParameters().isEmpty() ) {
            throw new FrameException(format(
                    "unexpected arguments on method <%s.%s>", clazz, method
            ));
        }

        if ( isForeign(method) && isEmbedded(method) ) {
            throw new IllegalArgumentException(format(
                    "conflicting @Foreign and @Embedded annotations on method <%s.%s>", clazz, method
            ));
        }


        final boolean internal=hasAnnotation(method, Internal.class);

        final TypeMirror type=method.getReturnType();
        final String source=source(clazz, method);

        return new Method(

                null, // set by parent class on assignment

                isDefault(method),
                parameter(type, root).map(this::isEnum).orElseGet(() -> isEnum(type)),
                wild(type, root),

                internal ? type.toString() : type(type, root),
                internal ? "" : item(type, root),
                name,

                list(Stream.concat(

                        // virtual @In annotation on enumerated properties

                        parameter(type, root).orElse(type) instanceof final DeclaredType declared
                                && declared.asElement() instanceof final TypeElement element
                                && isEnum(element)

                                ? Stream.of(new Annotation(In.class,

                                Map.of(VALUE, list(element.getEnclosedElements().stream()
                                        .filter(e -> e.getKind() == ENUM_CONSTANT)
                                        .map(e -> e.getSimpleName().toString()))
                                ),

                                source

                        ))
                                : Stream.empty(),

                        // real annotations

                        annotations(method, source)

                ))

        );
    }


    //̸// Types ////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Retrieves a TypeMirror for a given Java class.
     * <p>
     * Uses a cache to avoid repeated lookups for the same class type.
     */
    private TypeMirror type(final Class<?> clazz) {
        return types.computeIfAbsent(clazz, key ->
                elementUtils.getTypeElement(canonical(key)).asType()
        );
    }


    /**
     * Maps a TypeMirror to its string representation used in method signature processing.
     * <p>
     * Handles basic types, collection types, and specialized map types for text and data entries. Returns predefined
     * constants for common collection types or throws an exception for unsupported types.
     */
    private String type(final TypeMirror type, final TypeElement root) {

        final TypeMirror resolved=type.getKind() == TYPEVAR ? resolve(type, root) : type;

        return base(resolved) instanceof final String base ? base

                : isSame(raw(resolved), Set.class) ? SET
                : isSame(raw(resolved), List.class) ? LIST
                : isSame(raw(resolved), Collection.class) ? COLLECTION

                : isText(resolved) ? TEXT
                : isTexts(resolved) ? TEXTS
                : isTextsets(resolved) ? TEXTSETS

                : isData(resolved) ? DATA
                : isDatas(resolved) ? DATAS
                : isDatasets(resolved) ? DATASETS

                : error(new FrameException(format("unsupported type <%s>", resolved)));
    }


    private boolean wild(final TypeMirror type, final TypeElement root) {
        return isCollection(type) && parameter(type, root)
                .filter(p -> p instanceof final WildcardType wild && wild.getExtendsBound() != null)
                .isPresent();
    }

    /**
     * Extracts the item type from a collection type.
     * <p>
     * For collection types, retrieves the generic type argument, resolving type variables as needed. For non-collection
     * types, returns an empty string.
     *
     * @param type The type to examine
     * @param root The root type element for resolving type variables
     *
     * @return The string representation of the collection item type or empty string
     */
    private String item(final TypeMirror type, final TypeElement root) {
        if ( isCollection(type) ) {

            return parameter(type, root)
                    .map(this::base)
                    .orElseThrow(() -> new FrameException(format(
                            "unsupported collection item type <%s>", type
                    )));

        } else {

            return "";

        }
    }

    /**
     * Maps a TypeMirror to its base representation as a string.
     * <p>
     * Handles: - Type variables (returning their simple name) - Primitive types - Boxing types (Boolean, Number, etc.)
     * - Common Java types (String, URI, etc.) - Temporal types (LocalDate, Instant, etc.) - Frame-annotated interface
     * types (returning their qualified name)
     * <p>
     * Returns null for types that don't match any of the supported categories.
     */
    private String base(final TypeMirror type) {
        return type instanceof final TypeVariable var ? var.asElement().getSimpleName().toString()
                : type instanceof final WildcardType wild && wild.getExtendsBound() instanceof final TypeMirror bound ? base(bound)

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

                : type instanceof final DeclaredType declared
                && declared.asElement() instanceof final TypeElement element
                && isEnum(element) ? element.getQualifiedName().toString()

                : type instanceof final DeclaredType declared
                && declared.asElement() instanceof final TypeElement element
                && isFrame(element) ? element.getQualifiedName().toString()

                : null;
    }


    /**
     * Returns the raw type for a given type mirror by erasing generic type information.
     */
    private TypeMirror raw(final TypeMirror type) {
        return typeUtils.erasure(type);
    }

    private Optional<TypeMirror> parameter(final TypeMirror type, final TypeElement root) {
        return Stream.of(type)
                .filter(this::isCollection)
                .filter(DeclaredType.class::isInstance)
                .map(DeclaredType.class::cast)
                .map(DeclaredType::getTypeArguments)
                .flatMap(Collection::stream)
                .findFirst()
                .map(t -> t.getKind() == TYPEVAR ? resolve(t, root) : t);
    }

    /**
     * Resolves a type variable to its concrete type by analyzing the inheritance hierarchy.
     * <p>
     * Builds a map of type bindings by traversing the interface hierarchy of the given class. For each interface, maps
     * its type parameters to the actual type arguments used in the implementing class, capturing all type variable
     * bindings in the inheritance chain.
     *
     * @param variable The type variable to resolve
     * @param clazz    The class context for resolving the variable
     *
     * @return The resolved concrete type or the original variable if no binding is found
     */
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

                        if ( arguments.size() < parameters.size() ) {
                            throw new FrameException(format(
                                    "raw usage of @%s interface <%s>", Frame.class.getSimpleName(), type.getSimpleName()
                            ));
                        }

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

    /**
     * Extracts and processes annotations from an element.
     * <p>
     * Retrieves all annotation mirrors from the element, unwraps any container annotations, and expands any annotation
     * aliases into their constituent constraints.
     *
     * @param element The annotated element to process
     * @param source  The source context for tracking annotation provenance
     *
     * @return Stream of processed annotations
     */
    private Stream<Annotation> annotations(final AnnotatedConstruct element, final String source) {
        return element.getAnnotationMirrors().stream()
                .flatMap(a -> unwrap(a, source))
                .flatMap(this::unalias);
    }


    /**
     * Expands annotation aliases into their constituent constraints.
     * <p>
     * First attempts to resolve the annotation as a pre-compiled class, falling back to source-based resolution if the
     * class is not available at runtime. Handles both reflection-based alias expansion for compiled classes and
     * model-based expansion for source-only annotations.
     *
     * @param meta The annotation to potentially expand
     *
     * @return Stream of expanded annotations or the original if not an alias
     */
    Stream<Annotation> unalias(final Annotation meta) { // !!! private
        try { // expand as a pre-compiled annotation

            final Class<?> type=Class.forName(meta.type());

            return unalias(meta, type);

        } catch ( final ClassNotFoundException ignored ) { // expand as a source annotation

            final TypeElement annotation=elementUtils.getTypeElement(meta.type());

            if ( annotation == null || annotation.getKind() != ANNOTATION_TYPE ) {
                throw new FrameException(format("unknown annotation class <%s>", meta.type()));
            }

            return unalias(meta, annotation);

        }
    }

    private Stream<Annotation> unalias(final Annotation meta, final Class<?> annotation) {
        return Optional.ofNullable(annotation.getDeclaredAnnotation(Alias.class))
                .map(alias -> Stream.concat(

                        Stream.of(

                                new Annotation(
                                        MinExclusive.class,
                                        map(entry(VALUE, alias.MinExclusive().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        MaxExclusive.class,
                                        map(entry(VALUE, alias.MaxExclusive().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        MinInclusive.class,
                                        map(entry(VALUE, alias.MinInclusive().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        MaxInclusive.class,
                                        map(entry(VALUE, alias.MaxInclusive().value())),
                                        meta.source()
                                ),


                                new Annotation(
                                        MinLength.class,
                                        map(entry(VALUE, alias.MinLength().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        MaxLength.class,
                                        map(entry(VALUE, alias.MaxLength().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        com.metreeca.mesh.meta.shacl.Pattern.class,
                                        map(entry(VALUE, alias.Pattern().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        In.class,
                                        map(entry(VALUE, list(alias.In().value()))),
                                        meta.source()
                                ),

                                new Annotation(
                                        LanguageIn.class,
                                        map(entry(VALUE, list(alias.LanguageIn().value()))),
                                        meta.source()
                                ),

                                new Annotation(
                                        MinCount.class,
                                        map(entry(VALUE, alias.MinCount().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        MaxCount.class,
                                        map(entry(VALUE, alias.MaxCount().value())),
                                        meta.source()
                                ),

                                new Annotation(
                                        HasValue.class,
                                        map(entry(VALUE, list(alias.HasValue().value()))),
                                        meta.source()
                                )

                        ),

                        Arrays.stream(alias.Constraints().value()).map(constraint -> new Annotation(
                                Constraint.class,
                                map(entry(VALUE, canonical(constraint.value()))),
                                meta.source()
                        ))

                ))
                .orElseGet(() -> Stream.of(meta));
    }

    private Stream<Annotation> unalias(final Annotation meta, final AnnotatedConstruct annotation) {
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
                                        .map(a -> annotation(((AnnotationMirror)a), meta.source()));

                            } else {

                                return Stream.of(annotation(value, meta.source()));


                            }

                        })
                )
                .orElseGet(() -> Stream.of(meta));
    }


    /**
     * Unwraps container annotations that hold multiple annotations.
     * <p>
     * Detects and processes repeatable annotation containers, which use a single value method to hold an array of
     * actual annotations. If the annotation is a container, extracts and processes all contained annotations.
     * Otherwise, processes the annotation directly.
     *
     * @param annotation The annotation mirror to potentially unwrap
     * @param source     The source context for tracking annotation provenance
     *
     * @return Stream of unwrapped annotations
     */
    private Stream<Annotation> unwrap(final AnnotationMirror annotation, final String source) { // annotation containers
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
                        .map(annotationMirror -> annotation(annotationMirror, source))
                )

                .orElseGet(() -> Stream.of(annotation(annotation, source)));

    }


    /**
     * Converts an AnnotationMirror to an Annotation model object.
     * <p>
     * Extracts the annotation type and all its parameter values, converting them to appropriate Java objects. Handles
     * special value types like Class references by converting them to their qualified names.
     *
     * @param annotation The annotation mirror to convert
     * @param source     The source context for tracking annotation provenance
     *
     * @return The converted Annotation model object
     */
    private Annotation annotation(final AnnotationMirror annotation, final String source) {

        final String type=((QualifiedNameable)annotation.getAnnotationType().asElement()).getQualifiedName().toString();

        final Map<String, Object> arguments=new LinkedHashMap<>();

        for (final Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                annotation.getElementValues().entrySet()
        ) {

            final String param=entry.getKey().getSimpleName().toString();
            final Object value=entry.getValue().getValue();

            arguments.put(param, value(value));
        }

        return new Annotation(type, arguments, source);
    }

    /**
     * Converts annotation values to appropriate Java objects.
     * <p>
     * Handles: - Lists of annotation values (recursively processing each element) - Type references (converting to
     * their qualified names) - Other values (returning them as is)
     *
     * @param value The annotation value to convert
     *
     * @return The converted Java object
     */
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


    /**
     * Creates a source identifier for a class element.
     *
     * @param clazz The class element
     *
     * @return The simple name of the class as a source identifier
     */
    private String source(final Element clazz) {
        return clazz.getSimpleName().toString();
    }

    /**
     * Creates a source identifier for a method element with its containing class.
     *
     * @param clazz  The containing class element
     * @param method The method element
     *
     * @return A formatted string with class and method names in "Class.method()" format
     */
    private String source(final Element clazz, final Element method) {
        return "%s.%s()".formatted(clazz.getSimpleName().toString(), method.getSimpleName());
    }


    //̸// Checks ///////////////////////////////////////////////////////////////////////////////////////////////////////

    private boolean isGeneric(final Parameterizable parameterizable) {
        return !parameterizable.getTypeParameters().isEmpty();
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

    private boolean isFrame(final Element element) {
        return isTopLevel(element)
                && isInterface(element)
                && hasAnnotation(element, Frame.class);
    }


    private boolean isDefault(final Element method) {
        return method.getModifiers().contains(DEFAULT);
    }

    private boolean isUtility(final ExecutableElement method) {
        return isInternal(method) && isDefault(method) && !method.getParameters().isEmpty();
    }

    private boolean isInternal(final AnnotatedConstruct method) {
        return hasAnnotation(method, Internal.class);
    }

    public boolean isForeign(final AnnotatedConstruct method) {
        return hasAnnotation(method, Foreign.class);
    }

    public boolean isEmbedded(final AnnotatedConstruct method) {
        return hasAnnotation(method, Embedded.class);
    }


    private boolean isEnum(final TypeMirror type) {
        return type instanceof final DeclaredType declared
                && declared.asElement() instanceof final TypeElement element
                && isEnum(element);
    }

    private boolean isEnum(final Element element) {
        return element.getKind() == ENUM;
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


    /**
     * Checks if an annotation method represents a container for repeatable annotations.
     * <p>
     * A method is considered an annotation container if: 1. It's named "value" 2. It returns an array type 3. The array
     * component type is an annotation type
     * <p>
     * This pattern is used by the Java compiler to implement @Repeatable annotations.
     *
     * @param method The method element to check
     *
     * @return true if the method is an annotation container, false otherwise
     */
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

    /**
     * Checks if an element has a specific annotation.
     *
     * @param element    The element to check
     * @param annotation The annotation class to look for
     *
     * @return true if the element has the annotation, false otherwise
     */
    private boolean hasAnnotation(final AnnotatedConstruct element, final Class<?> annotation) {
        return element.getAnnotationMirrors().stream()
                .map(AnnotationMirror::getAnnotationType)
                .anyMatch(type -> isSame(type, annotation));
    }

}
