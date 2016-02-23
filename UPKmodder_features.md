# Introduction #

This page provides a general overview of the features in the UPKmodder tool.

![https://upk-modder.googlecode.com/svn/wiki/images/UPK_modder_v0_65_marked_up.png](https://upk-modder.googlecode.com/svn/wiki/images/UPK_modder_v0_65_marked_up.png)

# Details #

UPKmodder provides the following :
  * The project pane, on the left-hand side
    * Multiple projects can be open and are denoted **bold**
    * Projects may have subfolders, as well as mod\_files
    * Projects are essentially a mapping to a directory on disc, so changes to the folder will be reflected in the Project view once it refreshes

  * Each open modfile has a separate tab and is divided into 2 sub-panes
    * The main editor on the left hand side, which is open by default
      * The main editor provides highlighting of parseable unreal bytecode and is otherwise a general-purpose text editor
    * The tree view on the right-hand side, which is closed by default
      * Does not provide editing capability
      * Allows viewing of parsed unreal bytecode in a hierarchical fashion
      * Shows memory / file positions for each line
      * Shows memory sizes of each unreal token when line is expanded
      * Shows operand name and reference names when target upk is selected

File Actions :
  * Project Actions
    * New Project
    * Open Project
    * Close Project
  * ModFile Actions
    * New File
      * Creates a new modfile using the template file 'defaultModfileTemplate.upk\_mod'
    * Open File
      * File can also be opened by double-clicking on the file in the Project pane
    * Close File
    * Close All
    * Save
      * Save the active modfile
    * Save as
      * Save a copy of the active modfile
    * Export
      * Not implemented as of v0.65

Edit Actions :
  * Update References
    * Can also be accessed from the toolbar
    * Opens the Update References dialogue
    * Provides capability to update a modfile from one game version to another (i.e. fixing after patches)
  * Apply Hex Changes
    * Can also be accessed from the toolbar
    * Attempts to apply changes in the active ModFile to the targeted upk for that ModFile
      * Searches for BEFORE hex and replaces them with AFTER hex
    * If successful ModFile name will be displayed in _**Blue**_ italics (this means that the upk is modded)
    * If unsuccessful the Logger will contain info detailing why the application failed
  * Revert Hex Changes
    * Can also be accessed from the toolbar
    * Similar to apply, but searches for AFTER and replaces it with BEFORE
    * If successful the ModFile name will be displayed in **Green** non-italics (this means that the upk is restored to original state)

Other actions :
  * Set target upk
    * Accessed in lower left status bar by clicking on folder icon
    * Target upk performs the following roles :
      * Is the upk that apply and revert actions affect
      * Is the upk that reference names are drawn from in the Tree View

  * View log
    * The log is accessed via the button in the lower right corner of the status bar
    * Provides additional information about actions performed, including errors and timing information

# Installation #
  * Install anywhere.

  * Adjust the _UPKmodder v0.65\Config\upk\_config.ini_ to reflect location and GUID of some local UPK's.

  * Run **UPKmodder.jar** and to start a new Project.

# Usage #
Included are a few sample projects in the distribution (they should be in the _UPKmodderProjects_ subfolder within the decompressed folder).

You should be able to open the project files (which are the **.xml** files in the projects' folder) -- for example _UPKmodderProjects\Expanded Perk Tree EW\**Expanded Perk Tree EW.xml**_. From there you can open individual modfiles by double-clicking on the file in the project pane. Alternatively you can directly open a sample modfile with the "**Open File...**" menu item.

This will let you look at a variety of sample **upk\_mod** files (most of which are full function replacements).

There are some definite usability improvements we're still trying to make. The projects pane isn't as useful as it could be, for example. And associating target UPKs is still a file-by-file basis, which is a bit cumbersome.

When I'm creating a new modfile, I typically start off by copying the header from another function and then modifying it as necessary. I then open the function I'm going to change in [UE Explorer](http://eliotvu.com/portfolio/view/21/ue-explorer)'s Token view, copy the data into a temporary text editor document (I use [Notepad++](http://notepad-plus-plus.org/)) and use a macro to reformat it into something more readable by **UPKmodder** (stripping out the leading/trailing stuff around the hex, and the internal memory positions). I then copy the code into the _CODE_ block in the _BEFORE_ section. This triggers the limited parser in **UPKmodder**, which will color code the various hex parts accordingly, so I can double check that things are working as expected. I then fill out the memory/file positions (either directly from **UE Explore**'s buffer view or from the **UPKmodder** tree view)

The next steps involve either doing another copy/paste of the hex into the _AFTER / CODE_ section (if I'm primarly making modifications), using updated code from a previous version mod that I'm trying to fix up, or writing entirely new code, depending on the particulars.

# Application #
## To apply a **UPK Modfile**: ##
  1. Decompress your UPK files using [Gildor's Decompress tool](http://www.gildor.org/downloads) -- UPKmodder doesn't do anything or know anything about this (alternately ToolBoks will auto-decompress the UPKs and clean up the CookedPCConsole when launched).
  1. Open UPKmodder and create a new _Project_.
  1. Create a new file within the _Project_, paste the contents of your modfile into the new _Project_ file.
  1. Repeat step 3 for each modfile to be included in the Project.
  1. Set up the target UPKs for the project using the control in the lower left (click on the folder icon to bring up a chooser) -- select the appropriate UPK.
  1. Perform step 5 for each type of UPK (i.e. **XComStrategyGame.upk** and **XComGame.upk**) - the project stores the links from the "UPKFILE=" line in the _Project_ file and will automatically set the targets after that.
  1. Test the status of the UPK\_mod files, either by right-clicking the _Project_ and selecting "test status" or performing it for each file individually (_**green**_ = BEFORE, _**blue**_ = AFTER, _**red**_ = ERROR).
  1. All files should show as _**green**_ (unless you've already modded the UPK in same way).
  1. Open each file and select the "Apply" button -- if successful the color should change to _**blue**_.
  1. Launch the game.

## To change **object table entries**: ##
Each object has information stored in two different places.

Object table data is what is accessed with UE Explorer in the "Table Buffer". The actual object's hex itself is accessed via the "Buffer".

The UE Explorer "Table Buffer" is the table entry for the object (which has some basic descriptive information about the object, including the file position and file size of the object within the UPK). Typically each of the table entries is 68 bytes long. UPKmodder automatically parses the entire object table for each UPK it deals with. That's how it builds the reference ID-to-number mapping. The reference number is just the index within this table. XComGame.UPK for EW has about 55,000 (= D6D8) entries in the table, so that's why the numbers grow around that big.

UPKmodder extracts the "useful" information from each object using (this is pasted directly out of the Java source code for UPKmodder):

```
   private void parseData(int[] data) {
        this.iType = data[0];
        this.iParent = data[1];
        this.iOuter = data[2];
        this.iNamePtr = data[3];
        this.iHighFlags = data[6];
        this.iLowFlags = data[7];
        this.iUpkSize = data[8];
        this.iUpkPos = data[9];
        // TODO: parse/store other values, create getters and other convenience methods (e.g. getNameListIndex(), etc.)
   }
```

UPKmodder reads the object table data as 4-byte words instead of at the byte-level. Most entries contain 17 4-byte words, although the specifier at position 11 specifies additional 4 byte-words (this value is only non-zero for non-script objects, in my experience).

The stuff at the back end of each isn't of use for scripting (e.g. variables, enumerations, functions), and appears to be used to define art asset object info (e.g. 3D meshes, animations, textures, sound files). The 64 bits of flags are described in the downloadable [UPK Format Document](http://www.nexusmods.com/xcom/download/1000003820) developed by Wasteland Ghost (wghost81).

When you include a RESIZE= command for an object it automatically updates the UpkSize of the current object and the UpkPos of all subsequent objects (all objects that have a position later than the current one).


---


In UE Explorer the regular hex "Buffer" displays the hex for the object itself. The position and size of this object within the UPK file is defined via the UpkSize and UpkPos entries within the table entry for the object (i.e. if you looked up the table buffer, read words 8 and 9, opened the UPK with HxD and went to that position you'd find the object hex).

Functions contain both the run-time "byte-code" bytes as well as 48 bytes of header and 15 bytes of footer information. Typically the only bytes in the header/footer that are changed are the last two words, which contain the memory/file size of the byte-code. The preceding 40 bytes do contain some useful things, including some references (import, object, and/or name). This means that listing the full raw header hex isn't patch-safe, since those values may be updated. However, you can use the name of the object which UPKmodder will attempt to convert to hex at apply/revert-time, which does make it patch-safe. (e.g. {|iType@GetPossibleSpawns@XGDeployAI|})

I'm just not sure what the different header/footer bytes do specifically. I'm pretty sure that there are flags defining the function type (e.g. private, static, native, virtual) in the footer. And that some of the header information is used to in creating the linked list of local variables/parameters/return value for the function. Wghost is ahead of me in decoding that stuff.


---


By default UPKmodder changes the object entry itself (the "buffer dump"). It can change any bytes within the object hex, which is how we make changes to enumerations and variables, for example. For functions this includes the full function header (48 bytes) and footer (15 bytes).

UPKmodder will only change hex in the object table when using special commands. For example the RESIZE= command alters size/position of many object table entries.

Other commands include:

ACTION=typechange -- a specific command that requires changing both the iType and UpkSize fields of the object table entry. These are specified using OBJECT\_TYPE= and SIZE=, for example:

```
ACTION=typechange

//change object type parameter into int type

[BEFORE_HEX]
OBJECT_TYPE=Core:ObjectProperty@Core
SIZE=2C // hexadecimal
[/BEFORE_HEX]

[AFTER_HEX]
OBJECT_TYPE=Core:IntProperty@Core
SIZE=28 // hexadecimal
[/AFTER_HEX]
```

For more general-purpose changes to the object table entry for an object, there is the "ACTION=genericObjectTableChange" command.

This allows you to manually change most any entry within the object table entry. Note that this can easily corrupt the UPK, in particular if the size/position values are changed incorrectly.

For this command new values are specified via keywords, instead of via generic hex changes. This is because UPKmodder has to store copies of most of the table entry data in order to properly decode things, so changing these things is a little trickier and has to be under tighter control. Fortunately there aren't many 'useful' table values, so they can all be specified directly.

This command could actually supercede the "typechange" command, as it's more flexible. It allows any or all entries below to be changed --not all values have to be specified.

The keywords that allow changes to the object table entry are:
  * OBJECT\_TYPE : bytes 0-3 in the table buffer
  * OBJECT\_PARENT : bytes 4-7 in the table buffer
  * OBJECT\_OUTER : bytes 8-11 in the table buffer
  * OBJECT\_NAMEIDX : bytes 12-15 in the table buffer
  * OBJECT\_HIGHFLAGS : bytes 20-23 in the table buffer
  * OBJECT\_LOWFLAGS : bytes 24-27 in the table buffer
  * OBJECT\_SIZE : bytes 28-31 in the table buffer
  * OBJECT\_POSITION : bytes 32-35 in the table buffer

> A few notes about these.
  * OBJECT\_TYPE is almost always an import object (reference value is negative), so in UPKmodder would be specified in a patch-safe manner as something like Core:IntProperty@Core, or Core:FunctionProperty@Core.
  * OBJECT\_PARENT is only used for classes, and indicates the parent of a class (e.g. XGWeapon is the parent of XGWeapon\_AssaultRifle) This is typically a regular object reference
  * OBJECT\_OUTER indicates hierarchical ownership. For example in iType@GetPossibleSpawns@XGDeployAI, the iType object has as OUTER the GetPossibleSpawns object, while GetPossibleSpawns has as OUTER the XGDeployAI object. XGDeployAI has a null OUTER
  * OBJECT\_NAMEIDX is a reference to a name entry (an index into the name table). This is effectively how each object is named. Objects with the same name reference the same name entry without problem.

UPKmodder expects certain type values in each field:
  * OBJECT\_TYPE : must be a valid object (import or export) name (e.g. Core:Function@Core or Core:IntProperty@Core)
  * OBJECT\_PARENT : must be a valid object (import of export) name
  * OBJECT\_OUTER : must be a valid object (import of export) name
  * OBJECT\_NAMEIDX : must be a valid name on the namelist
  * OBJECT\_HIGHFLAGS : must be an integer, specified in hexadecimal
  * OBJECT\_LOWFLAGS : must be an integer, specifed in hexadecimal
  * OBJECT\_SIZE : must be an integer, specifed in hexadecimal
  * OBJECT\_POSITION : must be an integer, specifed in hexadecimal

Changing the OBJECT\_NAMEIDX for an object is problematic since the name is how the object is referenced in the first place. So changing the name results in UPKmodder being unable to properly locate the object after the change (to verify correct install and to be able to revert the change).

IMPORTANT!!!

When changing the NAMEIDX, leave the name of the current object out of the FUNCTION= (alternative usage OBJECT\_ENTRY=) specifier.

For example:
```
MODFILEVERSION=4
UPKFILE=XComGame.upk
GUID=1C 18 A1 1A 2B C3 34 4E 8B 2C 72 33 CD 16 7E 3E // XComGame_EW_patch3.upk
OBJECT_ENTRY=CheckpointRecord_XGWeapon@XGWeapon
ACTION=genericObjectTableChange

//iOverheatChance@CheckpointRecord_XGWeapon@XGWeapon
//alter CheckpointRecord_XGWeapon so that m_iTurnFired is the member, not iOverheatChance

[BEFORE_HEX]
OBJECT_NAMEIDX=iOverheatChance
[/BEFORE_HEX]

[AFTER_HEX]
OBJECT_NAMEIDX=m_iTurnFired
[/AFTER_HEX]
```

In this case the object _iOverheatChance@CheckpointRecord\_XGWeapon@XGWeapon_ is being transformed into the object _m\_iTurnFired@CheckpointRecord\_XGWeapon@XGWeapon_.

The _CheckpointRecord\_XGWeapon@XGWeapon_ object itself is not being changed. Instead if UPKmodder sees that the OBJECT\_NAMEIDX= field is being changed, it automatically prepends the value to what is specified in the OBJECT\_ENTRY field.

So when applying the change, it locates the object with the name _"iOverheatChance@CheckpointRecord\_XGWeapon@XGWeapon"_.

When reverting the change is locates the object with the name _"m\_iTurnFired@CheckpointRecord\_XGWeapon@XGWeapon"_.

Then when testing for apply/revert status, it tests both.

# FAQ #
(as of version 0.81)

Q&A: The "upk\_config.ini" is obsolete.  Just ignore it.

Q:  To replace old code by copy/paste: you need to delete it first, then paste a new code. Replacing selected text with text from clipboard doesn't work.

A: This appears to be a limitation of the built-in "DefaultStyledEditor" in Java, which was used instead of re-inventing that particular wheel.

Q: For Update References to work properly I needed to lookup/convert source references first, then close the dialog, re-open it and lookup/convert destination references. If I try to lookup/convert destination references right after lookup/convert source references nothing happens.

A: Are you converting to names as an intermediate step? The name conversion is more intended as a step for handling cases were some of the symbols don't match from one version to another. My usual steps are :
  1. Look up Source References
  1. Look up Destination References
  1. If everything mapped then the "Convert Source Refs to Dest Refs" button will activate, which directly converts the numerical source refs to dest refs.
> However, if one of the intermediate names doesn't map to a destination reference (like in one function where Firaxis renamed a field _**c**_lassName to _**C**_lassName), then that button doesn't activate, since doing such a conversion would result in a file with references to two different versions of UPK and no way to tell them apart.
> To handle that case is what I added the "Convert Hex References to Names" button for. It's there to convert everything to a name. It is a bit glitchy that you have to close and re-open the dialogue, but if you do so you can then convert the names that do match to dest refs back to references values, leaving just the names that didn't map to deal with manually.

Q: Converting destination references updates the GUID, but it doesn't updates the UPKFILE path.

A: That's a good point. I guess I assumed that the "UPKFILE=`<tag>`" wouldn't be changing. Behind the scenes it's a fully pathed filename, but I could extract the upk name and replace it if necessary.

Q: At some point GUID wasn't updated after updating destination references. That was my first attempt to work with this feature, so I messed up some things. After I learned how it works, I see GUID updated each time I convert destination references (but, again, UPKFILE is not updated).

A: I think if you convert to names the GUID is changed to "None", since a file with all names isn't necessarily tied (in the way numerical references are) to a particular version. Any time you convert to destination references it should be updating the GUID.
> I should also point out that the smallest unit for conversion is a single line, so it's quite possible to copy lines to another file and therefore convert a file "piecemeal". I sometimes do this when some lines don't convert properly.

Q: Code structure sometimes get messed up after references conversion: end of line gets "eaten", so some lines get merged into one. I suspect it can be related to different end of line standards: 0x0A without 0x0D or 0x0D followed by 0x0A.

A: I think the issue is that an earlier version of UPKmodder required a trailing space even after the final hex character (if the final hex was directly followed by a '\\n' it would be treated as non-parsable). This was fixed in v0.70, by which point the Reference Update function had already been written and tested.
> So I think that what is happening is that the Reference Update code is assuming that each hex line will have at least one trailing space, which it overwrites. However, if the final hex is instead immediately followed by a '\\n' character then instead that is overwritten by a space, resulting in the loss of the newline.
> Definitely a glitch I've noticed as well, thought I forgot to add it to the issues list.

Q: Experiencing considerable slowdowns when highlighting long code lines; i.e. copying/pasting an entire code block from HxD as one long line.

A: no response as yet.

Q: Only projects which have been opened via "Open Project" are saved in project view (leftmost window). If you create a project and then close UPKModder, the new project will not be displayed after reopening it. But after you manually open the project and close UPKModder, it will be displayed after reopening UPKModder.

A: no response as yet.