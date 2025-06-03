package uk.co.jcox.chemvis.application.chemengine;


import java.util.Iterator;
import java.util.UUID;

public interface IMoleculeManager {

    UUID createMolecule();

    UUID addAtom(UUID molecule, String element);

    UUID formBond(UUID moleculeID, UUID atom1, UUID atom2, int bondOrder);

    String getMolecularFormula(UUID moleculeID);

    int getBonds(UUID molecule, UUID atom);

    boolean isOfElement(UUID molecule, UUID Atom, String element);

    IMoleculeManager clone();
}
