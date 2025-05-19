This package contains everything to do with the Molecule Editor
It contains the GameState (OrganicEditorState) (main class for this package) which
manages an EntityLevel (visual representation of the molecule) and an IMoleculeManager (chemical representation of the molecule)

The package also contains Actions which defines things the editor allows you to do (adding an atom)

    For instance, when the user wants to add an atom, an AtomInsertionAction object is created
    The GameState executes this action by passing in the Level and MoleculeManager

//The game state can update the level directly, but for transient UI, the game state should never update anything related to the chemical structure