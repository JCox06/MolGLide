package uk.co.jcox.chemvis.application.editor;


import org.joml.Vector2f;

import java.util.Iterator;
import java.util.UUID;

public interface IMoleculeManager {

    UUID createMolecule();

    UUID addAtom(UUID molecule, String element);

    UUID formBond(UUID moleculeID, UUID atom1, UUID atom2, int bondOrder);

    void setMoleculePosition(UUID moleculeID, Vector2f position);

    void setAtomOffsetPosition(UUID atomID, Vector2f position);

    Vector2f getMoleculePosition(UUID moleculeID);

    Vector2f getAtomOffsetPosition(UUID atomID);

    String getAtomSymbol(UUID atom);

    Iterator<UUID> molecules();
    Iterator<UUID> atoms(UUID molecule);
}
