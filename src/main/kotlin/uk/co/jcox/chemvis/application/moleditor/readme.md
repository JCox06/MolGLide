# Mol Editor Package Info
___

This document explains the main components to the molecule editor. The major component is the OrganicEditorState. 


| Class              | Usage                                                                                                                                                                                                                                                                            |
|--------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| OrganicEditorState | Is an application state that recieves events from CVEngine such as update, render, etc. It contains the active tool being used, the main workstate, and other major components.                                                                                                  |
| WorkState          | Contains data about the current state/ChemLevelPair. Previous state data is also saved to allow for undo and redo                                                                                                                                                                |
| ApplicationUI      | Uses Dear ImGui to render the application UI (menu bar and atom selection)                                                                                                                                                                                                       |
| ChemLevelPair      | A bundle of the IMoleculeManager and a related EntityLevel                                                                                                                                                                                                                       |
| Tool               | A tool saves a temp copy of the workstate and performs editor actions on it. A tool also has a local workstate object to manage internal state restoration. Once an action has fully completed the latest state is commited to the main workstate held in the OrganicEditorState |
| EditorAction       | Different actions modify the state by using the molecule manager and the entitylevel. Actions ensure the two components never get out of sync                                                                                                                                    |