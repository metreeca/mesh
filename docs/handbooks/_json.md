---
title: "JSON-LD Wire Format"
---

> **⚠️**  WIP

---

Metreeca/Link [JSON codec]() (!!!) generates and consumes linked data serialised in
compacted/framed [JSON-LD](https://json-ld.org) format, streamlining resource descriptions on the basis of the
constraints specified by a target linked
data [shape](models.md#shapes).

> *ℹ️*  This serialisation format is intended to simplify front-end development by converting linked data descriptions
> to/from idiomatic JSON objects structured according to the conventions a JavaScript developer would expect from a
> typical
> REST/JSON API.

# JSON Data Model

```typescript
type Value = null | boolean | number | string | Frame | Tagged | Typed

type Frame = Partial<{

	"{label}": Value | Value[] | Local
	"{label}={expression}": Value | Value[] | Local

	"<{expression}": Value // less than
	">{expression}": Value // greater than

	"<={expression}": Value // less than or equal
	">={expression}": Value // greater than or equal

	"~{expression}": string // like (stemmed word search)

	"?{expression}": Value | Value[] | Local // any

	"^{expression}": "increasing" | "decreasing" | number // order
	"${expression}": Value | Value[] | Local // focus

	"@": number // offset
	"#": number // limit

}>

type Local=Partial<{
  
	"{locale}": string | string[]
	"*": string | string[] // shorthand for "mul" (multiple languages)
	"": string | string[] // shorthand for "zxx" (not applicable); represents plain strings in a localized contex

}>

type Tagged = {
	"@value": string
	"@language": "{locale}"
}

type Typed = {
	"@value": string
	"@type": "{iri}"
}
```

```
iri = as defined by https://www.rfc-editor.org/rfc/rfc3987.html
```

```
locale = as defined by https://www.rfc-editor.org/rfc/rfc4646.html
```

See also https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes

```
expression = *(transform ":") *(["."] field)
transform = label
field = label
label = 1*("_" / DIGIT / ALPHA) / "'" *(uchar / "''") "'"
uchar = %x20-26 / %x28-10FFFF ; all non-control Unicode chars minus "'" (single quote)
```

[ABNF](https://en.wikipedia.org/wiki/Augmented_Backus–Naur_form)

## Frames

> ❗️
>
> id/type (property names)
>
> property names
>
> Back-reference encoding

## IRI References

## Blank Nodes

## Typed Literals

## Tagged Literals

# Work

*JSON objects are deserialised to the corresponding RDF payload performing a depth-first visit of the JSON value*
*structure.*

*References to previously visited blank nodes and IRI references are represented as simplified*
*back-references to the complete representation, omitting predicate values.*

	<rdf> ::= <iri>

*The top-level object for the JSON serialization is a single RDF value describing the root resource.*

## *RDF Values*

```
<value> ::= <bnode> | <iri> | <literal>
```

*RDF values are serialized to different JSON value patterns according to their kind.*

## *Blank Nodes*

	<blank> ::= {  "@id" : "_:<id>" (, <property>)* }

*Blank nodes descriptions are serialized as JSON objects including a JSON field for the node identifier and a JSON
field*
*for each exposed property.*

```
<blank> ::= { [<property> (, <property>)*] }
```

*If there is no back-reference from a nested object, the `@id` id field may be left empty or omitted.*

### *Back-Links*

```
<blank> ::= { "@id": "_:<id>" }
```

*If the value is a back-link to an enclosing blank node, only the `@id` id field is included.*

```
<blank> ::= "_:<id>"
```

*If the value may be proved to be a back-reference to an enclosing resource, the node id may be inlined.*

## *IRI References*

```
<iri> ::= { "@id" : "<iri>" (, <property>)* }
```

*IRI reference descriptions are serialized as JSON objects including a JSON field for the resource IRI and a JSON field*
*for each exposed property.*

```
<iri> ::= { [<property> (, <property>)*] }
```

*If the value may be proved to be a constant known IRI reference, the `@id` id field may be omitted.*

```
<iri> ::= "<iri>"
```

*If the value may be proved to be an IRI reference without properties, the IRI may be inlined.*

### *Back-Links*

```
<iri> ::= { "@id": "<iri>" }
```

*If the value is a back-reference to an enclosing object, only the `@id` id field is included.*

```
<iri> ::= "<iri>"
```

*If the value may be proved to be a back-reference to an enclosing resource, the IRI may be inlined.*

## *Decoding*

*When decoding, relative `<iri>` references are resolved against the provided base URI, which for HTTP REST operations*
*equals the IRI of the request [item](../javadocs/com/metreeca/link/Message.html#item--).*

## *Encoding*

*When writing, local `<iri>` references are relativized as root-relative IRIs against the provide base URI, which for*
*HTTP*
*REST operations equals the root IRI of the response [item](../javadocs/com/metreeca/link/Message.html#item--).*

## *Properties*

```
<property> ::= <label>: [<value>(, <value>)*]
```

*Direct/inverse resource property values are serialized as JSON object fields including the property label and a JSON*
*array containing serialized property objects/subjects as value.*

```
<label> ::= <shape-defined label> | <system-inferred-label>
```

*Property labels are either explicitly in the [field](../javadocs/com/metreeca/json/shapes/Field.html) definition or*
*inferred by the system on the basis of the field IRI.*

*!!! warning JSON-LD keywords (i.e. object field names staring with `@`) are reserved for system use.*

*!!! warning Predicate IRIs with undefined or clashing labels are reported as errors.*

```
<property> ::= <label>: <value>
```

*If the property value may be proved to be non-repeatable, it may be included as a single JSON value, rather than a
JSON*
*array.*

## *Literals*

```
"<text>"^^<type> ::= { "@value": "<text>", "@type": "<type>" }
"<text>"@<lang>  ::= { "@value": "<text>", "@language": "<lang>" }
```

*In the more general form, literals are serialized as JSON objects including the literal lexical representation and*
*either*
*the literal datatype IRI or the literal language tag.*

## *Typed Literals*

```
"<text>"             ::= "<text>"
"<text>"^^xsd:string ::= "<text>
```

*Simple literals and typed `xsd:string` literals are serialized as JSON string values.*

```
"<integer>"^^xsd:integer ::= <integer> # no decimal part
"<decimal>"^^xsd:decimal ::= <decimal> # decimal part

"<number>"^^<type> ::= { "@value": "<number>", "@type": "<type>" } # explicit type
```

*Typed `xsd:integer` and `xsd:decimal` literals are serialized as JSON numeric values using type-specific number
formats.*
*Other typed numeric literals are serialized in the extended form.*

```
"boolean"^^xsd:boolean ::= <boolean>
```

*Typed `xsd:boolean` literals are serialized as JSON boolean values.*

	"<text>"^^<type> ::= "<text>"

*If the datatype of the literal may be proved to be a constant known value, the literal may be serialized as a JSON*
*string*
*value including its lexical representation, omitting datatype info.*

## *Tagged Literals*

```
"<text0>"@"<lang1>", "<text1>"@"<lang1>", "<text2>"@"<lang2>", … ::= { 
	"<lang1>" : ["<text0>", "<text1>"],
	"<lang2>" : ["<text2>"],
	…
} 
```

*If collection of literals may be proved to be `rdf:langString`, the collections may be serialized as a JSON object*
*mapping language tags to lists of string values.*

```
"<text1>"@"<lang1>", "<text2>"@"<lang2>", … ::= { 
	"<lang1>" : "<text1>",
	"<lang2>" : "<text2>",
	…
} 
```

*If language tags may be proved to be unique in the collection, string values may be included without wraping them in a*
*list.*

```
"<text1>"@"<lang>", "<text2>"@"<lang>", … ::= ["<text1>","<text2>", …]
```

*If the language tag may be proved to be a constant, string values may be serialized as a JSON list, omitting language*
*tags.*

```
"<text>"@"<lang>" ::= "<text"
```

*If the tagged literal may be proved to be non-repeatable and with a known language tag, its string value may be
included*
*directly.*

# Client-Driven Fetching

## Query String Encoding

# Collection Filtering

REST/JSON-LD APIs published with the Metreeca/Java framework support engine-managed faceted search capabilities, driven
by structural and typing constraints specified in the underlying linked data model.

## Facet Filters

```
<filter> ::= {

    "> <path>": <value>, // minExclusive
    "< <path>: <value>, // maxExclusive
    ">= <path>": <value>, // minInclusive
    "<= <path>": <value>, // maxInclusive
    
    "~ <path>": "keywords", // like (stemmed word search)
    "^ <path>": "stem", // stem (prefix search)
    
    "! <path>": <value> | [<value>(, <value>)*], // all
    "? <path>": <value> | [<value>(, <value>)*], // any
        
    "<path>": <value>, //  shorthand for "? <path": <value>
    "<path>": [<value>(, <value>)*] // shorthand for "? <path>": [<value>(, <value>)*]
    
}
```

## Property Paths

```
<path> ::= (<label> ('.' <label>)*)?
```

## Query Parameters

```
{expression}<={value} // less than or equal
{expression}>={value} // greater than or equal

~{expression}={string} // like (stemmed word search)

{expression}={value} // any option
{expression}=* // existential any option
{expression}= // non-existential any option

^{expression} // order
^{expression}=increasing // order
^{expression}=decreasing // order
^{expression}={number} // order

@={number} // offset
#={number} // limit
```

# Sample Queries

*[Queries](../javadocs/com/metreeca/json/Query.html) define what kind of results is expected from faceted searches*
*on [readable](../tutorials/consuming-jsonld-apis.md#read-operations) linked data REST/JSON-LD APIs.*

*JSON query serialization extends the idiomatic [JSON-LD](_jsonld-format.md) format with query-specific objects for*
*serializing facet [filters](#facet-filters) and property [paths](#property-paths). Standard JSON serialization applies*
*to*
*all values appearing in filters, including [shorthands](_jsonld-format.md#literals) for numeric values and literals
with*
*provable datatypes.*

*!!! warning Work in progress… specs to be improved and detailed.*

*JSON-based queries are appended to container IRIs using one of the following encodings:*

- *[URLEncoded](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/encodeURI) (*
  *e.g. `"http://example.com/container/?"+encodeURI({ <query> })`)*
- *[URLSearchParams](https://developer.mozilla.org/en-US/docs/Web/API/URLSearchParams) (*
  *e.g. `"http://example.com/container/?"+new URLSearchParams({ <query> })`)*

*The second form supports idiomatic container filtering (e.g. `http://example.com/container/?<property>=<value>&…`, but*
*requires:*

- *values to contain no comma;*
- *boolean, numeric and other literal properties to be specified as such in the driving shape.*

## Items

[Items](../javadocs/com/metreeca/json/queries/Items.html) queries return the description of collection items matching a
set of facet filters.

    <items query> ::= { // all fields are optional and nullable
    
        "<filter>": <value> | [<value>, …],
        
        ⋮
        
        ".order": <criterion> | [<criterion>,(<criterion>)*],
        ".offset": <integer>,
        ".limit": <integer>
        
    }
    
    <criterion> :;= "[-+]?<path>"

```
<items response> ::= {
    "@id": "<target-iri>"
    "contains": [<value>(, <value>)*]
}
```

```json
{
  "members": [
    {
      "id": "",
      "label": "",
      ">=seniority": 3,
            "@": 100,
            "#": 10
        }
    ]
}
```

```json
{
    "members": [
        {
            "id": "/employees/123",
            "label": "Tino Faussone"
        },
        {
            "id": "/employees/098",
            "label": "Memmo Cancelli"
        }
    ]
}
```

## Stats

[Stats](../javadocs/com/metreeca/json/queries/Stats.html) queries return a report detailing datatype, count and range
stats for a facet specified by a target property path, taking into account applied filters.

```
<stats query> ::= {
    
    "<filter>": <value> | [<value>, …],  // optional and nullable
    
    ⋮

    ".stats": "<path>",
    ".offset": <integer>,
    ".limit": <integer>
}
```

```
<stats response> ::= {

    "@id": "<target-iri>"
    
    // global stats 
    
    "count": <number>,
    "min": <value>,
    "max": <value>,
    
    // datatype-specific stats sorted by descending count
    
    "stats": [
        {
            "@id": "<datatype-iri>",
            "count": <number>,
            "min": <value>,
            "max": <value>
        }
    ]
}
```

```json
{
    "members": [
        {
            "lower=min:seniority": 0,
            "upper=max:seniority": 0
        }
    ]
}
```

```json
{
  "members": [
    {
      "lower": 1,
      "upper": 6
    }
  ]
}
```

## Terms

[Terms](../javadocs/com/metreeca/json/queries/Terms.html) queries return a report detailing option values and counts for
a facet specified by a target property path, taking into account applied filters.

    <terms query> ::= {
            
        "<filter>": <value> | [<value>, …],  // optional and nullable
        
        ⋮
    
        ".terms": "<path>",
        ".offset": <integer>,
        ".limit": <integer>
    }

```
<terms response> ::= {

    "@id": "<target-iri>"
        
    "terms": [ // sorted by descending count
        {
            "value": { "@id": <iri>[, "label": "<label>"]} | <literal>,
            "count": <number>
        }
    ]
}
```

```json
{
    "members": [
        {
            "~office.label": "US",
            "count=count:": 0,
            "office": {
                "id": "",
                "label": ""
            },
            "^count": -1,
          	"^office.label": 1,
            "#": 10
        }
    ]
}
```

```json
{
    "members": [
        {
            "count": 10,
            "office": {
                "id": "/offices/1",
                "label": "Paris"
            }
        },
        {
            "count": 3,
            "office": {
                "id": "/offices/1",
                "label": "Paris"
            }
        }
    ]
}
```

## Links

```json
{
    "members": [
        {
            "=count:*": "count",
            "*": {
                "id": "",
                "label": ""
            }
        }
    ]
}
```

```json
{
    "members": [
        {
            "count": 10,
            "office": {
                "id": "/offices/1",
                "label": "Paris"
            }
        },
        {
            "count": 3,
            "office": {
                "id": "/offices/1",
                "label": "Paris"
            }
        }
    ]
}
```

## Analytics

```json
{
    "members": [
        {
            "customers=count:*": 0,
            "country=customer.country": {
                "id": "",
                "label": ""
            },
            "^customers": "decreasing",
            "#": 10
        }
    ]
}
```

```json
{
    "members": [
        {
            "customers": 10,
            "country": {
                "id": "/countries/1",
                "label": "Italy"
            }
        },
        {
            "customers": 3,
            "country": {
                "id": "/countries/2",
                "label": "Germany"
            }
        }
    ]
}
```
