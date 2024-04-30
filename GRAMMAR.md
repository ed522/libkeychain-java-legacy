# `TYPES` file grammar

Each extension (in all languages) defines a `TYPES` file to use. This defines:
- The group name
- The extension name
- All messages
    - Also defines their fields
- Routines
    - *Note*: The routines are **not** portable. Each language will have a different entrypoint.
    - Some reserved names have special functions

## Basics

The `TYPES.xml` file is based upon custom XML. You may find the DTD in `src/main/resources/grammar.dtd`.

!!IMPORTANT!!: The grammar file uses a different license. All other files are licensed under Apache 2.0

## `group`
Defines the group name. Does not need to be unique across extensions.

Extensions may have the same group name *or* the same extension name, but not both. Extensions are considered unique if `<group>.<extension>` (referred to as the *combined name*) is unique.

Only defined once. More than one definition results in an error.

Duplicated combined names result in undefined behaviour.

```
group example # This belongs to the example group.
```

## `ext`
Defines the extension name. Must be unique in a group, but may be repeated in a different group.

Only defined once. More than one definition results in an error.

Duplicated combined names result in undefined behaviour.

```
group example
ext exampleext # The combined name is something like example.exampleext
```

## `msg`
Defines a message.

Must include a name and one or more fields in square brackets.

The following names may not be used, as they interfere with the routine interface:
- `none`
- `preinit`
- `postinit`
- `baseinit`

```
msg ex1 [
    ...
]
```

## `field`
Defines a field in a message.

Must include a name and type. Names must be unique.

Valid types are: 
- `u8`: Unsigned 8-bit integer
- `u16`: Unsigned 16-bit integer
- `u32`: Unsigned 32-bit integer
- `u64`: Unsigned 64-bit integer
- `i8`: Signed 8-bit integer
- `i16`: Signed 16-bit integer
- `i32`: Signed 32-bit integer
- `i64`: Signed 64-bit integer
- `f32`: 32-bit floating point value
- `f64`: 64-bit floating point value
- `str`: Variable-length UTF-8 string value
- `bin`: Freeform variable-length binary value
- `bool`: Boolean value (true/false, stored as 8 bits)

### Arrays

Fields may declare an array type. This can store up to (2 ^ 31) - 1 items.

These are specified by placing an opening square bracket `[` after the field.

```
<field>
    <name>test1</name>
    <type>u8</type> <!-- Unsigned 8-bit integer -->
</field>
<field>
    <name>test2</name>
    <type>str[</type> <!-- Array of strings -->
</field>
```

## `routine`
Defines a routine.

Includes a name, language, trigger and a language-specific reference.

- Java: Full class and method names
- C++: RTTR class and method reference
- Rust: OSO module and method reference

Triggers:
- `none`: On-demand
- `preinit`: Run before base but after handshake
- `postinit`: Run after initialization
- `baseinit`: Reserved for base module
- `<message name>`: Run on message received

```
<routine>
```
