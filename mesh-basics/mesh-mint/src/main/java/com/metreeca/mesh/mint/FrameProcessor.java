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

import com.metreeca.mesh.mint.tools.Generator;
import com.metreeca.mesh.mint.tools.Introspector;

import com.google.auto.service.AutoService;

import javax.annotation.processing.*;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import static com.metreeca.mesh.mint.tools.Generator.frame;

import static java.lang.String.format;
import static javax.lang.model.SourceVersion.RELEASE_21;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.NOTE;

@SupportedAnnotationTypes("com.metreeca.mesh.meta.jsonld.Frame")
@SupportedSourceVersion(RELEASE_21)
@AutoService(Processor.class)
public class FrameProcessor extends AbstractProcessor {

    private final Set<String> generated=new HashSet<>();


    @Override public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment environment) {

        final Filer filer=processingEnv.getFiler();
        final Messager messager=processingEnv.getMessager();

        messager.printMessage(NOTE, format(
                "running <%s>", FrameProcessor.class.getSimpleName()
        ));

        if ( annotations.isEmpty() ) { return false; } else {

            final Introspector introspector=new Introspector(processingEnv);

            for (final TypeElement annotation : annotations) {
                for (final Element element : environment.getElementsAnnotatedWith(annotation)) {
                    if ( element instanceof final TypeElement type ) {

                        final String name=type.getQualifiedName().toString();

                        if ( generated.add(name) ) {

                            messager.printMessage(NOTE, format(
                                    "<%s.java>: generating frame", type.getSimpleName()
                            ));

                            final String frame=frame(name);

                            try ( final Writer writer=filer.createSourceFile(frame, type).openWriter() ) {

                                writer.write(new Generator(introspector.clazz(type)).generate());

                            } catch ( final FrameException e ) {

                                messager.printMessage(ERROR, format(
                                        "%s.java: %s", type.getSimpleName(), e.getMessage()
                                ));

                            } catch ( final RuntimeException|IOException e ) {

                                final StringWriter writer=new StringWriter();

                                e.printStackTrace(new PrintWriter(writer));

                                messager.printMessage(ERROR, format(
                                        "%s.java: internal error: %s", type.getSimpleName(), writer
                                ));

                            }

                        } else {

                            messager.printMessage(NOTE, format(
                                    "skipping frame generation for <%s>", name
                            ));

                        }

                    }
                }
            }

            return true;

        }
    }

}