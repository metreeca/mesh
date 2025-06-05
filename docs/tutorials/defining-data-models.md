---
title: "Defining JSON-LD Data Models"
---

In this tutorial you will learn how to use shapes to define declarative data models that drive data validation and
other linked data processing tools.

Focusing on the *Employees* data tables of the BIRT sample dataset, as outlined in the following high‑level UML model,
we will build a complete
data model step by step.

![BIRT UML Model](toys.png#100)

By the end of this tutorial, you will understand how to:

- Configure JSON-LD properties and RDF relationships
- Apply SHACL validation constraints for data quality
- Build complete resource shapes with multiple properties
- Validate values and access validation reports

# What Are Shapes?

Shapes are declarative definitions that serve as the foundation for data modelling, validation and other model-driven
services. They represent comprehensive specifications for what valid data looks like, combining structural rules, type
constraints, and business logic validation into cohesive data blueprints.

Think of shapes as templates that define:

- What properties an object can have
- What datatypes those properties must use
- What validation rules apply to property values
- How properties relate to RDF predicates for semantic web integration

The simplest shape accepts any value without constraints. This serves as the starting point for all shape definitions:

```java
import static com.metreeca.mesh.shapes.Shape.shape;

public static Shape emptyShape() {
    return shape();
}
```

# JSON-LD Structures

The Metreeca/Mesh framework builds upon the [JSON for Linked Data (JSON-LD)](https://www.w3.org/TR/json-ld/)
specification for linked data
serialisation.

JSON-LD provides a method for encoding linked data using JSON syntax. Key capabilities include:

- **Context mapping** - Define vocabularies and map JSON properties to RDF predicates and types
- **Type coercion** - Automatically convert JSON values to appropriate RDF datatypes
- **Language tagging** - Support multilingual content with language-specific literals
- **Graph structures** - Represent complex relationships and nested objects as RDF graphs
- **API compatibility** - Maintain standard JSON syntax for seamless web service integration

## Class Constructs

Class constructs transform shapes into object definitions with identity and type information. This enables
JSON-LD serialisation with proper `@id` and `@type` fields.

### Id and Type Properties

The `id()` and `type()` methods designate which properties supply identity and type information for JSON-LD contexts:

```java
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.shapes.Type.type;

public static Shape basicEmployeeShape() {
    return shape()
        .id("id")           // @id field name
        .type("type");      // @type field name
}
```

Setting `id()` and `type()` automatically configures the shape's datatype to `Object()`, ensuring the shape
validates object values rather than primitive values.

### Class Constraints

Class constraints specify which RDF classes a resource belongs to. This enables type checking and semantic
interoperability with other RDF systems:

```java
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.shapes.Type.type;

public static Shape classConstraintsExample() {
    return shape()
        .clazz(type("Employee", URI.create("https://data.example.net/#Employee")));
}
```

## Property Constructs

Properties define the structure of objects by mapping JSON field names to RDF predicates, enabling seamless
translation between JSON-LD serialisation and RDF graph storage. These relationships form the foundation of
linked data by connecting resources into semantic graphs through typed predicates.

### Property Names

Every property needs a name that appears in JSON serialisation. The `Property` factory creates property definitions
that link names to shapes and RDF predicates:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;

public static Property forenameProperty() {
    return property("forename")
            .shape(shape()
                    .datatype(String()));
}
```

### Property URIs

Property URIs uniquely identify relationships from a resource to its property values. You can either use
automatic URI generation or specify explicit URIs for precise control over RDF serialisation:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;

public static Property automaticForenameProperty() {
    return property("forename")
            .forward(true)
            .shape(shape()
                    .datatype(String()));
}

public static Property explicitForenameProperty() {
    return property("forename")
            .forward(URI.create("https://data.example.net/#forename"))
            .shape(shape()
                    .datatype(String()));
}
```

### Forward Relationships

Forward relationships represent outgoing connections from a resource to related objects or values:

```java
import static com.metreeca.mesh.shapes.Property.property;

// Employee -> Office relationship

public static Property forwardRelationshipExample() {
    return property("office")
            .forward(URI.create("https://data.example.net/#office"));
}
```

### Reverse Relationships

Reverse relationships represent incoming connections from other resources. These are particularly useful for
navigating hierarchical or bidirectional relationships:

```java
import static com.metreeca.mesh.shapes.Property.property;

// Employee <- reports relationship (reverse of supervisor)

public static Property reverseRelationshipExample() {
    return property("reports")
            .reverse(URI.create("https://data.example.net/#supervisor"));
}
```

## Property Ownership

The framework provides two distinct ownership models for managing relationships between resources, each optimised
for different data architecture patterns and lifecycle requirements.

| Aspect               | Embedded Properties                       | Foreign Properties                                  |
|----------------------|-------------------------------------------|-----------------------------------------------------|
| **Ownership**        | Owned by parent resource                  | Independent resources                               |
| **Update behaviour** | Cascade updates and deletions from parent | No modification during parent updates and deletions |
| **Lifecycle**        | Managed with parent                       | Autonomous lifecycle                                |
| **Use cases**        | Composition, owned data                   | References, shared resources                        |

- **Use embedded properties** for data that logically belongs to and should be managed with the parent resource
- **Use foreign properties** for references to independent resources that have their own management lifecycle

> [!IMPORTANT]
>
> Embedded and foreign annotations are mutually exclusive to prevent ambiguous ownership semantics

### Embedded Properties

Embedded relationships define properties that represent owned objects updated in cascade during resource operations.
When a resource is updated or deleted, embedded properties are automatically updated or deleted alongside the parent
resource, ensuring data consistency for tightly coupled object hierarchies.

For example, an employee's career events are embedded objects that belong exclusively to the employee and should
be updated together:

```java
import static com.metreeca.mesh.shapes.Property.property;

public static Property embeddedRelationshipExample() {
    return property("career")
            .forward(URI.create("https://data.example.net/#career"))
            .embedded(true);
}
```

### Foreign Properties

Foreign relationships define properties that reference independent resources managed elsewhere in the system.
When a resource is updated or deleted, foreign properties are not modified, preserving the autonomy of the
linked resources and preventing unintended side effects across resource boundaries.

For example, an employee's reports relationship is foreign because the reporting employees are independent
resources with their own lifecycle:

```java
import static com.metreeca.mesh.shapes.Property.property;

public static Property foreignRelationshipExample() {
    return property("reports")
            .reverse(URI.create("https://data.example.net/#supervisor"))
            .foreign(true);
}
```

# SHACL Constraints

The Metreeca/Mesh framework implements core [Shapes Constraint Language (SHACL)](https://www.w3.org/TR/shacl/)
constraints for validating RDF graphs.

SHACL provides a declarative approach to validate RDF data by defining shapes that describe
expected structures and constraints. Key capabilities include:

- **Structural validation** - Define required and optional properties with cardinality constraints
- **Datatype validation** - Ensure values conform to specific RDF datatypes like strings, numbers, and dates
- **Value constraints** - Implement business rules through enumeration, pattern matching, and range validation
- **Relationship validation** - Validate connections between resources and enforce referential integrity
- **Custom validation** - Apply domain-specific business logic through custom constraint functions

## Datatype Constraints

Datatype constraints ensure values conform to specific RDF datatypes, enabling type safety and proper
serialisation across different formats.

### String Datatypes

String properties are the most common datatype for textual information:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;

public static Property employeeSurnameProperty() {
    return property("surname")
            .forward(URI.create("https://data.example.net/#surname"))
            .shape(shape()
                    .datatype(String()));
}
```

### Numeric Datatypes

Numeric properties enable mathematical operations and range validation. The Integral datatype represents
whole numbers like counts, rankings, and identifiers:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.Integral;

public static Property employeeSeniorityProperty() {
    return property("seniority")
            .forward(URI.create("https://data.example.net/#seniority"))
            .shape(shape()
                    .datatype(Integral()));
}
```

### Date and Time Datatypes

Temporal properties capture time-based information using standard RDF datatypes. LocalDate represents
calendar dates without time zones:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.LocalDate;

// Employee birthdate
public static Property employeeBirthdateProperty() {
    return property("birthdate")
            .forward(URI.create("https://data.example.net/#birthdate"))
            .shape(shape()
                    .datatype(LocalDate()));
}
```

## Value Range Constraints

Range constraints define acceptable value boundaries for numeric and ordered datatypes. These constraints
implement business rules like "seniority must be between 1 and 5" or "sales figures cannot be negative".

### Minimum Inclusive Bounds

Minimum inclusive bounds ensure values are greater than or equal to a specified limit:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.Integral;
import static com.metreeca.mesh.Value.value;

// Employee seniority with minimum value
public static Property seniorityWithMinimumExample() {
    return property("seniority")
            .forward(URI.create("https://data.example.net/#seniority"))
            .shape(shape()
                    .datatype(Integral())
                    .minInclusive(value(1)));
}
```

### Maximum Inclusive Bounds

Maximum inclusive bounds ensure values are less than or equal to a specified limit. Combining minimum and
maximum bounds creates valid ranges:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.Integral;
import static com.metreeca.mesh.Value.value;

// Employee seniority with both bounds (1-5 scale)
public static Property seniorityWithBoundsExample() {
    return property("seniority")
            .forward(URI.create("https://data.example.net/#seniority"))
            .shape(shape()
                    .datatype(Integral())
                    .minInclusive(value(1))
                    .maxInclusive(value(5)));
}
```

## Text Constraints

Text constraints validate string properties beyond basic datatype checking. These constraints implement
business rules about data quality and user interface requirements.

### Length Constraints

Length constraints ensure text values fall within acceptable size limits, preventing both empty values
and excessively long text:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;

// Employee label with length constraints (5-80 characters)
public static Property labelProperty() {
    return property("label")
            .forward(URI.create("http://www.w3.org/2000/01/rdf-schema#label"))
            .shape(shape()
                    .datatype(String())
                    .minLength(5)
                    .maxLength(80));
}
```

### Pattern Matching

Regular expression patterns validate text format according to specific rules. This enables enforcement
of business formats like codes, email addresses, and structured identifiers:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;

public static Property numericCodeProperty() {
    return property("code")
            .forward(URI.create("https://data.example.net/#code"))
            .shape(shape()
                    .datatype(String())
                    .pattern("^\\d+$"));
}

public static Property emailValidationProperty() {
    return property("email")
            .forward(URI.create("https://data.example.net/#email"))
            .shape(shape()
                    .datatype(String())
                    .pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$"));
}
```

## Value Enumeration Constraints

Enumeration constraints restrict values to predefined sets, implementing controlled vocabularies and
closed lists. These constraints ensure consistency and enable validation against known options.

### Closed Value Sets

The `in()` constraint limits values to a specific closed set of allowed options, implementing controlled vocabularies:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;
import static com.metreeca.mesh.Value.value;

public static Property statusEnumerationExample() {
    return property("status")
            .forward(URI.create("https://data.example.net/#status"))
            .shape(shape()
                    .datatype(String())
                    .in(value("ACTIVE"), value("INACTIVE"), value("PENDING")));
}
```

### Required Values

The `hasValue()` constraint ensures specific values are present in multi-valued properties. This implements
rules like "every employee must belong to at least one of these departments":

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;
import static com.metreeca.mesh.Value.value;

public static Property departmentRequirementExample() {
    return property("department")
            .forward(URI.create("https://data.example.net/#department"))
            .shape(shape()
                    .datatype(String())
                    .hasValue(value("Engineering"), value("Sales")));
}
```

## Language Constraints

Language constraints enable multilingual data support by controlling language tags on literal values.
These constraints are essential for international applications and semantic web interoperability.

### Language Tag Specification

The `languageIn()` constraint restricts text values to specific languages, enabling controlled multilingual content:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.Text;

public static Property multilingualDescriptionExample() {
    return property("description")
            .forward(URI.create("https://data.example.net/#description"))
            .shape(shape()
                    .datatype(Text())
                    .languageIn("en", "fr", "de"));
}
```

### Unique Language Requirements

The `uniqueLang()` constraint ensures each language appears at most once in multi-valued text properties,
preventing duplicate translations:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.Text;

public static Property uniqueLanguageDescriptionExample() {
    return property("description")
            .forward(URI.create("https://data.example.net/#description"))
            .shape(shape()
                    .datatype(Text())
                    .languageIn("en", "fr", "de")
                    .uniqueLang(true));
}
```

## Cardinality Constraints

Cardinality constraints control how many values a property can have. These constraints implement business
rules about required information and relationship multiplicity.

### Explicit Count Constraints

Explicit cardinality constraints define precise bounds on value counts, implementing rules like
"employees must have 1-10 skills":

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.Value.String;

public static Property skillsCardinalityExample() {
    return property("skills")
            .forward(URI.create("https://data.example.net/#skills"))
            .shape(shape()
                    .datatype(String())
                    .minCount(1)
                    .maxCount(10));
}
```

### Cardinality Shorthands

Common cardinality patterns have convenient shorthand methods that make code more readable and express
intent clearly:

```java
import static com.metreeca.mesh.shapes.Shape.shape;

public static void cardinalityShorthandsExamples() {

    shape().required();    // minCount=1, maxCount=1
    shape().optional();    // minCount=0, maxCount=1
    shape().multiple();    // no minCount, no maxCount
    shape().repeatable();  // minCount=1, no maxCount
    shape().exactly(3);    // minCount=3, maxCount=3

}
```

# A Complete Model

Now we'll assemble all the concepts covered in this tutorial into a complete Employee shape definition.
This demonstrates how individual constraints combine to create comprehensive data models.

The complete Employee shape combines identity, class, and property constraints into a cohesive definition
that validates employee data according to business rules:

```java
import static com.metreeca.mesh.shapes.Property.property;
import static com.metreeca.mesh.shapes.Shape.shape;
import static com.metreeca.mesh.shapes.Type.type;
import static com.metreeca.mesh.Value.*;

public static Shape employeeShape() {
    return shape()

            .id("id")
            .type("type")

            .clazz(type("Employee", URI.create("https://data.example.net/#Employee")),
                    type("Resource", URI.create("https://data.example.net/#Resource")))

            .property(property("code")
                    .forward(URI.create("https://data.example.net/#code"))
                    .shape(shape()
                            .datatype(String())
                            .pattern("^\\d+$")
                            .required()))

            .property(property("forename")
                    .forward(URI.create("https://data.example.net/#forename"))
                    .shape(shape()
                            .datatype(String())
                            .required()))

            .property(property("surname")
                    .forward(URI.create("https://data.example.net/#surname"))
                    .shape(shape()
                            .datatype(String())
                            .required()))

            .property(property("email")
                    .forward(URI.create("https://data.example.net/#email"))
                    .shape(shape()
                            .datatype(String())
                            .pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
                            .required()))

            .property(property("seniority")
                    .forward(URI.create("https://data.example.net/#seniority"))
                    .shape(shape()
                            .datatype(Integral())
                            .minInclusive(value(1))
                            .maxInclusive(value(5))
                            .required()))

            .property(property("birthdate")
                    .forward(URI.create("https://data.example.net/#birthdate"))
                    .shape(shape()
                            .datatype(LocalDate())
                            .required()));
}
```

# Validating Values

Validation examples demonstrate how shapes enforce data quality rules in practice. Understanding both
valid and invalid examples helps developers build robust data processing applications.

### Valid Data Examples

Valid data satisfies all shape constraints, enabling successful processing and storage:

```java
import static com.metreeca.mesh.Value.*;

public static void validDataExample() {
    final Value validEmployee=object(
            entry("id", value("emp123")),
            entry("type", value("Employee")),
            entry("code", value("12345")),
            entry("forename", value("John")),
            entry("surname", value("Doe")),
            entry("email", value("john.doe@company.com")),
            entry("seniority", value(3)),
            entry("birthdate", value(LocalDate.of(1985, 5, 15)))
    );

    final Value result=employeeShape().validate(validEmployee);

    // result will be empty (that is a JSON null value) for valid data indicating successful validation
}
```

### Common Validation Errors

Validation failures occur when data violates shape constraints. Understanding common error patterns
helps developers debug data quality issues:

```java
import static com.metreeca.mesh.Value.*;

public static void validationErrorExamples() {
    // Missing required field
    final Value missingField=object(
            entry("id", value("emp124")),
            entry("forename", value("Jane"))
            // Missing required surname, email, etc.
    );

    // Invalid pattern
    final Value invalidEmail=object(
            entry("id", value("emp125")),
            entry("email", value("invalid-email"))  // Fails pattern constraint
    );

    // Out of range value
    final Value invalidSeniority=object(
            entry("id", value("emp126")),
            entry("seniority", value(10))  // Outside 1-5 range
    );
}
```

# Beyond This Tutorial

This tutorial covered the core concepts needed for most data modelling scenarios. For advanced use cases
and validation requirements, refer to the [shapes reference](../handbooks/shapes.md):

- **Virtual shapes** - Shapes generated on the fly during retrieval operations even when not present in storage
- **Custom constraints** - Domain-specific validation functions for complex business rules
- **Shape composition** - Extending and merging shapes for reusability and inheritance
- **Hidden properties** - Properties excluded from default serialisation but stored internally

The reference provides complete coverage of all shape constructs and their usage patterns.
