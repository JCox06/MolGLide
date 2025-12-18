# Contributing And Project Documentation

All contributions are welcome! 


## Documentation

All source code of MolGLide is fount in [src/main/kotlin/uk/co/jcox/chemvis](https://github.com/JCox06/MolGLide/tree/master/src/main/kotlin/uk/co/jcox/chemvis) - At this point the project is spit into two distinct layers.

|Layer|Description|
| ---- | ---- |
|Application| Contains the MolGLide specific code - This is the most important folder|
|CVEngine| Contains code for OpenGL rendering and resource management |


### Application Layer

This contains a few important classes and packages that are summarised in the table below:

|Java Class/Package| Description |
|---|---|
|`main.kt`|Contains the entry point to the program which creates the `MolGLide.kt` and CVEngine object|
|`MolGLide.kt`| Directly communicates with the CVEngine object and initialises `MainState.kt` and `ApplicationUI.kt` |
|`mainstate/MainState.kt`|Manages the `ToolRegistry.kt` and creating and closing documents (OrganicEditorStates)|
|`ToolRegistry.kt`|Manages tools that can be used in documents|
|`chemengine`|A CDK wrapper that manages that actual chemical data and structure information|
|`graph`|Manages the appearance of the chemical data|
|`moleditorstate/OrganicEditorState.kt`|Represents a chemistry document where you can draw molecules|


## Application States and the OrganicEditorState.kt

An ApplicationState is a place where you can draw something to the screen using OpenGL. All ApplicationStates are associated with a RenderTarget (which internally is an OpenGL FrameBuffer Wrapper), which by default is left as null, which represents the GLFW main window. 

ImGui is always drawn over the null RenderTarget (The GLFW main window). When the user selects `file -> New Project`, ApplicationUI.kt tells the MainState to create a new document/OrganicEditorState and bind it to a new RenderTarget. This RenderTarget is then loaded as a texture in an external ImGui window.

OrganicEditorState.kt is a sublcass of Application State. It manages the Level and level rendering through direct interaction of CVEngine. Each separate project/document has a unique OrganicEditorState.

Level information in each document is stored within the LevelContainer object of the OrganicEditorState. Level Manipulation is achieved through **tools**.


### Tools

Tools group like actions together. For instance, adding molecules, adding atoms, adding bonds, is all achieved through using the AtomBondTool. Each OrganicEditorState manages its own set of tools. For example, document A, and document B, will use different instances of the same AtomBondTool.

Despite this, each specific tool will all share some partial states. 

For instance, the AtomBondTool allows you to select what atom you want to be placed/activated - And this setting is shared across through all instances and saved/stored in `ui/tool/ToolViewUI`.

A list of the tools in MolGLide is provided below:

|Tool|Description|
|--|--|
|Atom Bond Tool | Allows you to add bonds and atoms|
|Template Tool | Allows you to add rings |
|Implicit Move Tool| Changes the orientation of implicit hydrogens|

### ActionManager.kt

This keeps track of all actions the user has made. An Action is defined by an execute action, and an undo action. This essentially allows undo and redo support.

## Development Guide - Adding a new tool

The easiest way to extend the functionality of MolGLide is to create a new tool to interact with the OrganicEditorState. The basic outline for tool creation is:

1) Subclass ui/tool/ToolViewUI to draw the UI and manage tool options and for your tool. (If your tool has no UI you can skip this step and use `ToolViewUI.kt`)
2) Sublcass moleditorstate/tool/Tool and override the available methods to implement your tool.
3) Create actions your tool can execute by subclassing moleditorstate/tool/IAction
4) Register your tool to the ToolRegistry so it appears in the Menu Dropdown - This is done in the MainState class

## CVEngine package

The CVEngine package which contains key OpenGL code mostly manages resource loading, text rendering, batch rendering, and instance rendering.

The CVEngine exposes the ICVServices interface to allow the Application Layer to communicate back to the engine when required. At the moment CVEngine is mostly bug free. 
