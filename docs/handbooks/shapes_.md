---
title: "Linked Data Modelling"
---

## Value Constraints

Primitive shapes specifying constraints to be individually satisfied by each value in the focus set.

| shape                                                                         | constraint                                                                                                                                                                                                                                                                              |
|:------------------------------------------------------------------------------|:----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [datatype](../javadocs/com/metreeca/json/shapes/Datatype.html)(IRI)           | each value in the focus set has a given extended RDF datatype IRI; IRI references and blank nodes are considered to be respectively of [Values.IRI](../javadocs/com/metreeca/json/Values.html#IRIType) and [Values.BNode](../javadocs/com/metreeca/json/Values.html#BNodeType) datatype |
| [class](../javadocs/com/metreeca/json/shapes/Clazz.html)(IRI)                 | each value in the focus set is an instance of a given RDF class or one of its superclasses                                                                                                                                                                                              |
| [range](../javadocs/com/metreeca/json/shapes/Range.html)(value, …)            | each value in the focus set is included in a given set of target values                                                                                                                                                                                                                 |
| [lang](../javadocs/com/metreeca/json/shapes/Lang.html)(tag, …)                | each value in the focus set is a tagged literal in a given set of target languages                                                                                                                                                                                                      |
| [minExclusive](../javadocs/com/metreeca/json/shapes/MinExclusive.html)(value) | each value in the focus set is strictly greater than a given minum value, according to <a href="https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#modOrderBy">SPARQL ordering</a> rules                                                                                           |
| [maxExclusive](../javadocs/com/metreeca/json/shapes/MaxExclusive.html)(value) | each value in the focus set is strictly less than a given maximum value, according to <a href="https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#modOrderBy">SPARQL ordering</a> rules                                                                                            |
| [minInclusive](../javadocs/com/metreeca/json/shapes/MinInclusive.html)(value) | each value in the focus set is greater than or equal to a given minimum value, according to <a href="https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#modOrderBy">SPARQL ordering</a> rules                                                                                      |
| [maxInclusive](../javadocs/com/metreeca/json/shapes/MaxInclusive.html)(value) | each value in the focus set is less than or equal to a given maximum value, according to <a href="https://www.w3.org/TR/2013/REC-sparql11-query-20130321/#modOrderBy">SPARQL ordering</a> rules                                                                                         |
| [minLength](../javadocs/com/metreeca/json/shapes/MinLength.html)(length)      | the length of the lexical representation of each value in the focus set is greater than or equal to the given minimum value                                                                                                                                                             |
| [maxLength](../javadocs/com/metreeca/json/shapes/MaxLength.html)(length)      | the length of the lexical representation of each value in the focus set is less than or equal to the given maximum value                                                                                                                                                                |
| [pattern](../javadocs/com/metreeca/json/shapes/Pattern.html)("pattern")       | the lexical representation of each value in the focus set matches a given [regular expression](https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum) pattern                                                                                                      |
| [like](../javadocs/com/metreeca/json/shapes/Like.html)("keywords")            | the lexical representation of each value in the focus set matches the given full-text keywords                                                                                                                                                                                          |
| [stem](../javadocs/com/metreeca/json/shapes/Stem.html)("prefix")              | the lexical representation of each value in the focus set starts with the given prefix                                                                                                                                                                                                  |

## Set Constraints

Primitive shapes specifying constraints to be collectively satisfied by the values in the focus set.

| shape                                                                 | constraint                                                                               |
|:----------------------------------------------------------------------|:-----------------------------------------------------------------------------------------|
| [minCount](../javadocs/com/metreeca/json/shapes/MinCount.html)(count) | the size of the focus set is greater than or equal to the given minimum value            |
| [maxCount](../javadocs/com/metreeca/json/shapes/MaxCount.html)(count) | the size of the focus set is less than or equal to the given maximum value               |
| [all](../javadocs/com/metreeca/json/shapes/All.html)(value, …)        | the focus set includes all values from a given set of target values                      |
| [any](../javadocs/com/metreeca/json/shapes/Any.html)(value, …)        | the focus set includes at least one value from a given set of target values              |
| [localized](../javadocs/com/metreeca/json/shapes/Localized.html)()    | the focus set contains only tagged literals with at most one value for each language tag |

Common combinations of set constraints are directly available as shorthand shapes.

| shorthand shape                                                                                         | equivalent shape                      | constraint                  |
|:--------------------------------------------------------------------------------------------------------|:--------------------------------------|-----------------------------|
| [required()](../javadocs/com/metreeca/json/Shape.html#required--)                                       | `and(minCount(1), maxCount(1))`       | exactly one                 |
| [optional()](../javadocs/com/metreeca/json/Shape.html#optional--)                                       | `maxCount(1)`                         | at most one                 |
| [repeatable](../javadocs/com/metreeca/json/Shape.html#repeatable--)                                     | `minCount(1)`                         | at least one                |
| [multiple()](../javadocs/com/metreeca/json/Shape.html#multiple--)                                       | `and()`                               | any number                  |
| [exactly](../javadocs/com/metreeca/json/Shape.html#exactly-org.eclipse.rdf4j.model.Value...-)(value, …) | `and(all(value, …), range(value, …))` | constant pre-defined values |

## Structural Constraints

Composite shapes specifying constraints to be satisfied by a derived focus set generated by a path.

| shape                                                                                                                           | constraint                                                                                                                                                  |
|:--------------------------------------------------------------------------------------------------------------------------------|:------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [field](../javadocs/com/metreeca/json/shapes/Field.html)(["label", ] IRI, [shape](../javadocs/com/metreeca/json/Shape.html), …) | the derived focus set generated by traversing a single step path is consistent with a given set of shapes                                                   |
| [link](../javadocs/com/metreeca/json/shapes/Link.html)(IRI, [shape](../javadocs/com/metreeca/json/Shape.html), …)               | the derived focus set generated by optionally traversing a single step path linking a resource alias to its target is consistent with a given set of shapes |

## Logical Constraints

Composite shapes specifying logical combinations of shapes.

| shape                                                                                                                                                                                                          | constraint                                                                                                                                                                                                                                  |
|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [guard](../javadocs/com/metreeca/json/shapes/Guard.html)(axis, value, …)                                                                                                                                       | the focus set is consistent with this shape only if the value of an externally assigned [axis variable](../javadocs/com/metreeca/json/Shape.html#redact-java.lang.String-java.util.Collection-) is included in a given set of target values |
| [when](../javadocs/com/metreeca/json/shapes/When.html)([test](../javadocs/com/metreeca/json/Shape.html),[pass](../javadocs/com/metreeca/json/Shape.html) [, [fail](../javadocs/com/metreeca/json/Shape.html)]) | the focus set is consistent either with a `pass` shape, if consistent also with a `test` shape, or with a `fail` shape, otherwise; if omitted, the `fail` shape defaults to `and()`, that is it's always meet                               |
| [and](../javadocs/com/metreeca/json/shapes/And.html)([shape](../javadocs/com/metreeca/json/Shape.html), …)                                                                                                     | the focus set is consistent with all shapes in a given target set                                                                                                                                                                           |
| [or](../javadocs/com/metreeca/json/shapes/Or.html)([shape](../javadocs/com/metreeca/json/Shape.html), …)                                                                                                       | the focus set is consistent with at least one shape in a given target set                                                                                                                                                                   |
