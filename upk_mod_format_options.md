# Introduction #

This page provides a brief overview of the upk\_mod file format and the various tags / options available.


# Details #

## Header ##

The header must contain at least four lines for the file to be considered a valid ModFile.

  * MODFILEVERSION=`<Integer>`
    * Current version (as of v0.65) is 4
    * This is used by UPKmodder to help maintain backwards compatibility with older file versions
    * Value must be an integer, in decimal format

  * UPKFILE=`<String>`
    * Describes the "generic" upkfile the ModFile is intended to be applied to (e.g. XComGame.upk, XComStrategyGame.upk)
    * User-archived versions (for retaining patch information) should start with this same value (e.g. XComGame\_EW\_patch1.upk)

  * GUID=`<String>` _or_ `<byte[16]>` in hex
    * Describes the specific version of the upk the file is intended to be applied to
    * Drawn from a 16 byte array located at file position 0x45 in the upk
      * example : 5B 06 B8 18 67 22 12 44 85 9B A8 5B 9D 57 1D 4B // XComGame\_EW\_patch1.upk
      * This GUID is unique for every upk created
    * May be UNKNOWN (if the file contains no hex or its source is unknown)

  * FUNCTION=`<String>` or OBJECT\_ENTRY=`<String>`
    * Defines the name of the function or object entry that the modfile is changing
    * Function names are in the format :
      * `<function>@\<class>` for regular functions
      * `<function>@<state>@<class>` for state functions
    * Object entry variables can be in the format:
      * `<variable>@<class>` for class variable
      * `<variable>@<function>@<class>` for local/return/parameter variables


## Optional Tags ##

  * ACTION=`<String>`
    * Describes a specific type of upk change to be made
    * If not specified then a function change is assumed
    * Currently valid Action Keys :
      * typechange (v0.65)
        * key that indicates the modfile is changing the type of a variable

  * KEYWORD=`<String>`
    * Defines searchable keywords for finding the modfile
      * Not implemented as of v0.65

  * RESIZE=`<HexadecimalInteger>`
    * Indicates that the modfile will be resizing the target function defined in FUNCTION=
    * Only a single BEFORE and AFTER block are allowed for this case
    * Contains the change in size in hexadecimal
    * example : 293 is read as 0x293 = 659 in decimal
    * negative values are allowed to make the function smaller
    * Omitting this tag indicates that no resizing is to take place
      * Having different sized BEFORE and AFTER sizes will generate an error if this tag is not present


## Hex Change Tags ##

  * `[BEFORE_HEX]  [/BEFORE_HEX]`
    * Used to delimit the extent of a BEFORE block
    * A file may contain multiple BEFORE blocks (in general)
    * Must match the number of AFTER blocks

  * `[AFTER_HEX]   [/AFTER_HEX]`
    * Used to delimit the extent of an AFTER block
    * A file may contain multiple AFTER blocks (in general)
    * Must match the number of BEFORE blocks

  * `[CODE]    [/CODE]`
    * Used within BEFORE or AFTER block to designate hex bytes that should be parsable unreal bytecode

  * `[HEADER]    [/HEADER]`
    * Used within BEFORE or AFTER block to designate hex bytes that are part of a function header. These bytes are automatically considered to be non-parseable

  * <| name |>
    * These delimiters indicate that this is a virtual function name
    * Virtual function names are looked up on the namelist
    * Typically created by and interpreted by the Reference Update utility

  * {| name |}
    * These delimiters indicate that this is an object or import name
    * These are looked up in the objectlist or import list (depending on value)
    * Typically created by and interpreted by the Reference Update utility

## Variable Typechange Tags ##
These tags are only used when ACTION=typechange is specified. Each of these tags must fall with a BEFORE or AFTER block.

  * OBJECT\_TYPE=`<String>`
    * Designates the object type
    * Valid examples include :
      * BJECT\_TYPE=`Core:ObjectProperty@Core`
      * BJECT\_TYPE=`Core:IntProperty@Core`

  * SIZE=`<HexadecimalValue>`
    * Designates the size of the object the ObjectEntry refers to
    * Valid examples include :
      * SIZE=`2C`
      * SIZE=`28`