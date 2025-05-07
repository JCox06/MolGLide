package uk.co.jcox.chemvis.application.chemengine;


import java.util.Iterator;
import java.util.UUID;

public interface IMoleculeManager {

    UUID createMolecule();

    UUID addAtom(UUID molecule, String element);

    UUID formBond(UUID moleculeID, UUID atom1, UUID atom2, int bondOrder);

    UUID relatedMolecule(UUID atomID);

    String getAtomSymbol(UUID atom);

    Iterator<UUID> molecules();

    Iterator<UUID> relatedAtoms(UUID molecule);

    Iterator<UUID> allAtoms();
}
