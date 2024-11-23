# JSON Format Specification

This document describes the JSON format accepted by the Metreeca/Mesh JSONDecoder.

## Basic Values

### Null Value

```json
null
```

Decoded as a nil value.

### Boolean Values

```json
true
false
```

Decoded as boolean values.

### Numeric Values

#### Integers

```json
0
10
-10
+10
```

Decoded as integer values.

#### Decimals

```json
0.0
10.234
-10.234
+10.234
```

Decoded as decimal values.

#### Floating Point

```json
0.0E0
1.0E1
1.234E0
1.0234E1
-1.0234E1
+1.0234E1
```

Decoded as floating point values.

### String Values

```json
""
"string"
```

Decoded as string values.

## Structured Values

### Arrays

```json
[]
[
  1
]
[
  1,
  2
]
```

Decoded as arrays containing the decoded values.

### Objects

```json
{}
{
  "x": 1
}
{
  "x": 1,
  "y": 2,
  "z": 3
}
```

Decoded as objects with the specified properties.

#### Object IDs

```json
{
  "@id": "path"
}
```

Decoded as an object with the specified ID. Relative paths are resolved against the base URI.

Empty IDs (`{"@id": ""}`) are preserved as default model values.

#### Type Properties

```json
{
  "@type": "Type"
}
```

Type properties are ignored in the decoded object.

## Special Values

### URI Values

When a shape with URI datatype is provided:

```json
"path"
""
```

Decoded as URI values. Relative paths are resolved against the base URI. Empty strings are handled specially.

### Text Values

Text values can be represented in several formats:

#### Single Text

```json
"value"
```

When a shape with Text datatype is provided, this is decoded as a text value with default locale.

#### Detailed Text

```json
{
  "@value": "value",
  "@language": "en"
}
```

Decoded as a text value with the specified language.

#### Shorthand for Multiple Localized Texts

```json
{
  "en": "one",
  "it": "uno"
}
{
  "en": [
    "one",
    "two"
  ],
  "it": [
    "uno",
    "due"
  ]
}
```

When a shape with Text datatype is provided, these are decoded as arrays of text values with the specified languages.

### Typed Data

```json
{
  "@value": "value",
  "@type": "test:t"
}
```

Decoded as a typed data value with the specified datatype URI.

## Query Format

Queries must be enclosed in arrays:

```json
[
  {
    "@": 0,
    "#": 10,
    "<x": "value",
    ">y": 100,
    "~z": "keyword"
  }
]
```

### Comparison Constraints

- `<x`: Less than
- `>x`: Greater than
- `<=x`: Less than or equal
- `>=x`: Greater than or equal
- `~x`: Text contains/matches the provided string

### Collection Constraints

- `?x`: Value must be any of the values in the array

```json
[
  {
    "?x": [
      null,
      true,
      1,
      "value"
    ]
  }
]
```

### Sorting and Pagination

- `^x`: Sort order - can be a number, or "increasing" (+1) or "decreasing" (-1)
- `@`: Offset for pagination
- `#`: Limit for pagination

```json
[
  {
    "^x": 1
  }
]
[
  {
    "^x": "increasing"
  }
]
[
  {
    "^x": "decreasing"
  }
]
[
  {
    "@": 100
  }
]
[
  {
    "#": 100
  }
]
```

### Focus Constraint

- `$x`: Focus on specified values

```json
[
  {
    "$x": [
      null
    ]
  }
]
```

## Table/Specs Format

Table/specs format is used for defining structured queries:

```json
[
  {
    "field=expression": value
  }
]
```

Example:

```json
[
  {
    "count=count:": 0
  }
]
```

This creates a Specs object with a probe named "count" using the expression "count:" with the value 0.

## Pruning

The decoder can operate in two modes:

- With pruning (default): Ignores null values, empty arrays, and empty objects
- Without pruning: Includes all values

The pruning behavior can be configured through the JSONCodec's `prune()` method.