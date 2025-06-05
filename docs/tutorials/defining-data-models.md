---
title: "Defining JSON-LD Data Models"
---

In this tutorial you will learn how to use shapes to define declarative data models that will drive data validation and
other linked data processing tools.

Focusing on the *Office* and *Employees* data tables of the BIRT sample dataset, we will build step by step a complete
data model, according to the following high‑level UML model (see also
the [standard datatypes](../handbooks/datatypes.md) reference for types used in the diagram).

![BIRT UML Model](toys.png#100)

# What Are Shapes?

Shapes are declarative definitions that serve as the foundation for data modelling, validation and other model-driven
services. They represent comprehensive specifications for what valid data looks like, combining structural rules, type
constraints, and business logic validation into cohesive data blueprints.

# JSON-LD Context

The Metreeca/Mesh framework builds upon the [JSON-LD 1.1](https://www.w3.org/TR/json-ld11/) specification to provide
semantic data modelling capabilities. JSON-LD enables the expression of linked data using familiar JSON syntax while
maintaining full compatibility with RDF and semantic web technologies. The shapes framework extends this foundation by
providing declarative validation and structural constraints that ensure data consistency and semantic integrity.

Shapes serve as blueprints that define how JSON-LD documents should be structured, what properties they may contain, and
how these properties relate to RDF vocabularies and ontologies. This approach bridges the gap between developer-friendly
JSON and semantically rich linked data.

## Initial Shape Definitions

The simplest shape accepts any value:

```java
import static com.metreeca.mesh.shapes.Shape.shape;

final Shape emptyShape = shape();
```

Add constraints to make shapes more specific:

```java
import static com.metreeca.mesh.Value.*;

final Shape nameShape = shape()
    .datatype(String())
    .minLength(1)
    .maxLength(100)
    .required();
```

## Properties

Properties connect field names to RDF predicates and define semantic relationships.

### Property Names and Forward URIs

Properties map JSON field names to RDF predicates:

```java
import static com.metreeca.mesh.shapes.Property.property;

// Automatic URI generation from property name
property("name")
    .forward(true)  // Creates URI based on property name
    .shape(nameShape);

// Explicit URI specification
property("title")
    .forward(term("http://purl.org/dc/terms/title"))
    .shape(nameShape);
```

## Class Constructs

Object shapes require class information and identity properties.

### Id and Type Properties

Define identity and type fields for objects:

```java
import static com.metreeca.mesh.shapes.Type.type;

final Type EmployeeType = type("Employee");

final Shape employeeShape = shape()
    .id("id")           // Identity field name
    .type("type")       // Type field name
    .clazz(EmployeeType); // Class constraint
```

Setting `id()` and `type()` automatically configures the shape's datatype to `Object()`.

### Virtual Properties

Virtual properties exist only in serialization without corresponding RDF statements:

```java
property("displayName")
    .virtual(true)
    .shape(shape()
        .datatype(String())
        .optional());
```

## Property Constructs

Properties support various behavioral modifiers and relationship directions.

### Hidden Properties

Hidden properties are excluded from default serialization:

```java
property("internalId")
    .hidden(true)
    .forward(true)
    .shape(shape()
        .datatype(String())
        .optional());
```

### Foreign and Embedded Properties

Control cascade behavior during updates:

```java
// Foreign: reference external resources, not updated in cascade
property("reports")
    .foreign(true)
    .reverse(term("supervisor"))
    .shape(shape()
        .datatype(Object())
        .multiple());

// Embedded: updated in cascade, serialized inline
property("career")
    .embedded(true)
    .forward(true)
    .shape(shape()
        .datatype(Object())
        .multiple());
```

### Forward and Reverse URIs

Define relationship directions:

```java
// Forward relationship: Employee -> Office
property("office")
    .forward(true)
    .shape(shape()
        .datatype(Object())
        .required());

// Reverse relationship: Office <- Employee
property("employees")
    .reverse(term("office"))
    .shape(shape()
        .datatype(Object())
        .multiple());
```

#### Simultaneous Forward and Reverse

Properties can define bidirectional relationships:

```java
property("colleague")
    .forward(term("knows"))
    .reverse(term("knownBy"))
    .shape(shape()
        .datatype(Object())
        .multiple());
```

# SHACL Constraints

The Metreeca/Mesh framework implements a subset of
the [Shapes Constraint Language (SHACL)](https://www.w3.org/TR/shacl/) W3C recommendation for validating RDF graphs.
SHACL provides a standardized vocabulary for describing structural and value constraints on linked data, enabling
precise validation rules that ensure data quality and consistency across distributed systems.

By leveraging SHACL constraints, the framework ensures that JSON-LD data conforms to defined schemas while maintaining
semantic interoperability. These constraints operate at both the syntactic level (data types, cardinality) and the
semantic level (relationships, business rules), providing comprehensive validation capabilities for linked data
applications.

## Class Constraints

### Single Class Constraints

Define the primary type for a resource:

```java
final Shape employeeShape=shape()
        .clazz(type("Employee"))
        .property(property("name").forward(true).shape(nameShape));
```

### Multiple Class Constraints

Assign multiple types to a resource:

```java
final Shape managerShape=shape()
        .clazz(type("Manager"), type("Employee"), type("Person"))
        .property(property("department").forward(true).shape(nameShape));
```

The first type is the explicit class; subsequent types are implicit parent classes.

## Property Constraints

### Value Constraints

Constrain property values to specific sets or patterns:

```java
// Enumeration constraints
final Shape statusShape=shape()
                .datatype(String())
                .in(value("ACTIVE"), value("INACTIVE"), value("PENDING"))
                .required();

// Required values (at least one must be present)
final Shape departmentShape=shape()
        .datatype(String())
        .hasValue(value("Engineering"), value("Sales"))
        .multiple();
```

### Numeric Range Constraints

Define bounds for numeric values:

```java
// Inclusive and exclusive bounds
final Shape salaryShape=shape()
                .datatype(Decimal())
                .minInclusive(value(new BigDecimal("0")))
                .maxExclusive(value(new BigDecimal("1000000")))
                .optional();

// Age constraints
final Shape ageShape=shape()
        .datatype(Integral())
        .minInclusive(value(18))
        .maxInclusive(value(65))
        .required();
```

### String Constraints

Validate string format and length:

```java
// Pattern and length validation
final Shape phoneShape=shape()
                .datatype(String())
                .pattern("^\\+?[1-9]\\d{1,14}$")
                .minLength(10)
                .maxLength(15)
                .optional();

// Email format validation
final Shape emailShape=shape()
        .datatype(String())
        .pattern("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
        .required();
```

### Cardinality Constraints

Control the number of property values:

```java
// Explicit count constraints
final Shape skillsShape=shape()
                .datatype(String())
                .minCount(1)
                .maxCount(10);

// Convenience methods for common cardinalities
shape().

required()    // exactly 1 (minCount=1, maxCount=1)

shape().

optional()    // 0 or 1 (minCount=0, maxCount=1)

shape().

repeatable()  // at least 1 (minCount=1, no maxCount)

shape().

multiple()    // 0 or more (no minCount, no maxCount)

shape().

exactly(3)    // exactly 3 (minCount=3, maxCount=3)
```

### Language Constraints

Validate multilingual text properties:

```java
// Language tag constraints
final Shape descriptionShape=shape()
                .datatype(Text())
                .languageIn("en", "fr", "de")
                .uniqueLang(true)  // Each language at most once
                .multiple();
```

### Node Shape Constraints

Reference other shapes for complex validation:

```java
// Reference to another shape
final Shape officeRefShape=shape()
                .datatype(Object())
                .nodeShape(() -> officeShape())
                .required();

// Recursive references
final Shape employeeShape=shape()
        .property(property("supervisor")
                .forward(true)
                .shape(() -> employeeShape().optional()));
```

## Custom Class Constraints

Implement domain-specific validation logic:

```java
final Function<Value, Value> employeeValidator=value -> {
    final Optional<Value> birthdate=value.get("birthdate");
    final Optional<Value> hireDate=value.get("hireDate");

    if ( birthdate.isPresent() && hireDate.isPresent() ) {
        final LocalDate birth=birthdate.get().as(LocalDate.class).orElse(null);
        final LocalDate hire=hireDate.get().as(LocalDate.class).orElse(null);

        if ( birth != null && hire != null && birth.plusYears(16).isAfter(hire) ) {
            return error("Employee must be at least 16 years old at hire date");
        }
    }

    return value;
};

final Shape employeeShape=shape()
        .clazz(type("Employee"))
        .constraints(employeeValidator)
        .property(property("birthdate").forward(true).shape(
                shape().datatype(LocalDate()).required()))
        .property(property("hireDate").forward(true).shape(
                shape().datatype(LocalDate()).required()));
```

## Sample Validation

Validate data against defined shapes:

```java
final Shape shape=employeeShape();
final Value employee=object(
        entry("id", value("emp123")),
        entry("type", value("Employee")),
        entry("code", value("12345")),
        entry("forename", value("John")),
        entry("surname", value("Doe")),
        entry("birthdate", value(LocalDate.of(1985, 5, 15))),
        entry("email", value("john.doe@company.com")),
        entry("seniority", value(3))
);

final Value result=shape.validate(employee);
if(result.

error().

isPresent()){
        System.out.

println("Validation failed: "+result.error().

get());
        }else{
        System.out.

println("Employee data is valid");
}
```

### Validation Error Examples

```java
// Missing required field
final Value invalidEmployee=object(
                entry("id", value("emp124")),
                entry("forename", value("Jane"))
                // Missing required surname, email, etc.
        );

// Invalid data type
final Value typeErrorEmployee=object(
        entry("id", value("emp125")),
        entry("seniority", value("high"))  // Should be integer 1-5
);

// Constraint violation
final Value constraintErrorEmployee=object(
        entry("id", value("emp126")),
        entry("email", value("invalid-email"))  // Fails pattern constraint
);
```

# Employee and Office Model Examples

Combining all concepts into comprehensive data models:

```java
public static Shape employeeShape() {
    return shape()
            .id("id")
            .type("type")
            .clazz(type("Employee"), type("Resource"))

            // Basic information
            .property(property("code")
                    .forward(true)
                    .shape(shape()
                            .datatype(String())
                            .pattern("^\\d+$")
                            .required()))

            .property(property("forename")
                    .forward(true)
                    .shape(shape()
                            .datatype(String())
                            .minLength(1)
                            .maxLength(50)
                            .required()))

            .property(property("surname")
                    .forward(true)
                    .shape(shape()
                            .datatype(String())
                            .minLength(1)
                            .maxLength(50)
                            .required()))

            // Organizational relationships
            .property(property("office")
                    .forward(true)
                    .shape(() -> officeShape().required()))

            .property(property("supervisor")
                    .forward(true)
                    .shape(() -> employeeShape().optional()))

            .property(property("reports")
                    .foreign(true)
                    .reverse(term("supervisor"))
                    .shape(() -> employeeShape().multiple()));
}

public static Shape officeShape() {
    return shape()
            .id("id")
            .type("type")
            .clazz(type("Office"), type("Resource"))

            .property(property("code")
                    .forward(true)
                    .shape(shape()
                            .datatype(String())
                            .pattern("^\\d+$")
                            .required()))

            .property(property("city")
                    .forward(true)
                    .shape(shape()
                            .datatype(String())
                            .required()))

            .property(property("employees")
                    .foreign(true)
                    .reverse(term("office"))
                    .shape(() -> employeeShape().multiple()));
}
```

# Shape Composition and Reusability

Shapes support composition patterns for code reuse:

## Shape Extension

```java
// Base person shape
final Shape personShape=shape()
                .property(property("forename").forward(true).shape(nameShape))
                .property(property("surname").forward(true).shape(nameShape))
                .property(property("birthdate").forward(true).shape(birthdateShape));

// Employee extends person
final Shape employeeShape=personShape
        .extend(shape()
                .clazz(type("Employee"))
                .property(property("code").forward(true).shape(codeShape))
                .property(property("title").forward(true).shape(titleShape)));
```

## Extension vs Merge

- `extend()` - Properties are merged, but explicit classes are not inherited
- `merge()` - Everything is merged including explicit classes

# Practical Validation Usage

Validate JSON-LD data using defined shapes:

```java
final Shape shape=employeeShape();
final Value employee=object(
        entry("id", value("emp123")),
        entry("type", value("Employee")),
        entry("code", value("12345")),
        entry("forename", value("John")),
        entry("surname", value("Doe")),
        entry("email", value("john.doe@company.com"))
);

final Value result=shape.validate(employee);
if(result.

error().

isPresent()){
        System.out.

println("Validation failed: "+result.error().

get());
        }else{
        System.out.

println("Employee data is valid");
}
```

This tutorial demonstrated how to define comprehensive JSON-LD data models using the Mesh shapes framework. The
combination of SHACL constraints, semantic relationships, and validation capabilities provides a powerful foundation for
building robust linked data applications.
