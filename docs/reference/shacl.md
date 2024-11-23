| Constraint                             | Object                         | Collection<T>                  | Local<T>                                                           | Notes                                        |
|----------------------------------------|--------------------------------|--------------------------------|--------------------------------------------------------------------|----------------------------------------------|
| Min/MaxInclusive<br />Min/MaxExclusive | value                          | items                          | values                                                             | ✗ requires  value/item type to be comparable |
| Min/MaxLength                          | string representation of value | string representation of items | string representation of  values                                   |                                              |
| Pattern                                | string representation of value | string representation of items | string representation of values                                    |                                              |
| MinCount                               | MinCount(1) implies not null   | collection size                | size of value collections                                          |                                              |
| MaxCount                               | ✗ only MaxCount(1)             | collection size                | size of value collections<br />✗ only MaxCount(1) on unique values |                                              |
| In                                     |                                |                                |                                                                    |                                              |
| hasValue                               |                                |                                |                                                                    |                                              |
| LanguageIn                             | ✗                              | ✗                              | locale set                                                         |                                              |

- sh:class / sh:datatype / sh:nodeKind
    - implied by Java typing
- sh:in
    - use Java enums
    - **useful for Set<String>?**
- sh:hasValue
    - limited value taking into account annotation value() limitations
    - **useful for Set<String>?**

