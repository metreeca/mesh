<!--- # Metreeca/Mesh --->

<!--[![Maven Central](https://img.shields.io/maven-central/v/com.metreeca/mesh)](https://central.sonatype.com/artifact/com.metreeca/metreeca-mesh) -->

> [!IMPORTANT]
>
> The edge development version is `4.0-SNAPSHOT`. We are actively working toward the first stable release: watch this
> repo or follow us
> on [LinkedIn](https://linkedin.com/company/metreeca) to keep up to date.
>

Metreeca/Mesh is a lightweight Java framework for rapid development of
[linked data](https://www.w3.org/2013/data/) services.

**Model-Driven** / Programmatically defined data models drive the automatic generation of complete read/write REST/JSON
APIs supporting:

- object validation against expected schemas;
- bidirectional idiomatic JSON serialisation;
- persistence to storage backends with built-in CRUD operations.

**Faceted Search** / Out-of-the-box support for both list and range facets, enabling powerful data exploration and
filtering capabilities without additional configuration.

**Analytics Queries** / Built-in support for analytical queries with custom data transformation and aggregation
operations, enabling complex reporting and business intelligence without external tools.

**Data Envelopes** / Custom client-defined data envelopes may be retrieved in a single request by specifying exactly
which
fields and
nested relationships to include, providing GraphQL-like efficiency while maintaining the simplicity of REST/JSON.

**Standards Compliance** / Built on established [JSON-LD](https://www.w3.org/TR/json-ld11/)
and [SHACL](https://www.w3.org/TR/shacl/) W3C standards for easy data interoperability.

**Developer Experience**  / A high-level abstraction layer allows defining JSON-LD models using annotated Java
interfaces, making the process quick, type-safe, and IDE-friendly, with all boilerplate code automatically generated at
compile-time.

# Modules

|      area | javadocs                                                     | description                                                   |
|----------:|:-------------------------------------------------------------|:--------------------------------------------------------------|
| framework | [mesh-core](https://javadoc.io/doc/com.metreeca/mesh-core)   | Core model and abstractions for linked data                   |
|           | [mesh-pipe](https://javadoc.io/doc/com.metreeca/mesh-pipe)   | Processing and storage services for linked data               |
|           | [mesh-meta](https://javadoc.io/doc/com.metreeca/mesh-meta)   | JSON-LD and validation annotations for interface-based models |
|           | [mesh-mint](https://javadoc.io/doc/com.metreeca/mesh-mint)   | Annotation-based code generator                               |
|    codecs | [mesh-json](https://javadoc.io/doc/com.metreeca/mesh-json)   | JSON-LD serialisation codec                                   |
|    stores | [mesh-rdf4j](https://javadoc.io/doc/com.metreeca/mesh-rdf4j) | [RDF4J](https://rdf4j.org) persistence store                  |

# Getting Started

1. Add the framework BOM and the relevant serialisation/persistence modules to your Maven dependencies, for instance
   using high-level annotated interfaces:

```xml 

<project>

    <dependencyManagement>

        <dependencies>

            <dependency>
                <groupId>com.metreeca</groupId>
                <artifactId>mesh</artifactId>
                <version>{{meta.version}}</version>
            </dependency>

        </dependencies>

    </dependencyManagement>

    <dependencies>

        <dependency>
            <groupId>com.metreeca</groupId>
            <artifactId>mesh-json</artifactId>
        </dependency>

        <dependency>
            <groupId>com.metreeca</groupId>
            <artifactId>mesh-rdf4j</artifactId>
        </dependency>

        <dependency> <!-- include to use high-level interface annotations -->
            <groupId>com.metreeca</groupId>
            <artifactId>mesh-meta</artifactId>
        </dependency>

    </dependencies>

    <build> <!-- include to activate annotation-based generation of frame objects -->

        <plugin>

            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.14.0</version>

            <configuration>

                <annotationProcessorPaths>

                    <path>
                        <groupId>com.metreeca</groupId>
                        <artifactId>mesh-mint</artifactId>
                    </path>

                </annotationProcessorPaths>

            </configuration>

        </plugin>

    </build>

</project>
```

2. Define a JSON-LD application data model, for instance:

```java
import com.metreeca.mesh.meta.jsonld.Frame;
import com.metreeca.mesh.meta.jsonld.Id;
import com.metreeca.mesh.meta.jsonld.Namespace;
import com.metreeca.mesh.meta.shacl.Required;

import java.net.URI;

@Frame
@Namespace("https://schema.org/")
public interface Person {

    @Id
    URI identifier();

   default String name() {
      return "%s %s".formatted(givenName(), familyName());
   }

    @Required
    String givenName();

    @Required
    String familyName();

   // more properties…

}
```

3. Create a store to persist your data to a storage backend, for instance:

```java
import com.metreeca.mesh.rdf4j.RDF4JStore;
import com.metreeca.mesh.pipe.Store;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;

import java.net.URI;

public final class Example {

    public static void main(final String... args) {

        final Store store=RDF4JStore.rdf4j(new SailRepository(new MemoryStore()));

        final URI id=URI.create("/persons/123");

        final Person person=new PersonFrame()
                .identifier(id)
                .givenName("Tino")
                .familyName("Faussone");

        store.create(person);

        final Person model=new PersonFrame()
                .familyName("")
                .givenName("");

        final Person retrieved=store.retrieve(model);

    }

}
```

5. Delve into the [tutorials](https://metreeca.github.io/mesh/tutorials/) to learn how to:

   - define and annotate data models;
   - convert data to / from serialisation formats;
   - persist data to storage backends;
   - publish model-driven REST/JSON-LD APIs;
   - consume model-driven REST/JSON-LD APIs.

# Support

- open an [issue](https://github.com/metreeca/mesh/issues) to report a problem or to suggest a new feature
- start a [discussion](https://github.com/metreeca/mesh/discussions) to ask a how-to question or to share an idea

# License

This project is licensed under the Apache 2.0 License –
see [LICENSE](https://github.com/metreeca/mesh?tab=Apache-2.0-1-ov-file) file for details.
