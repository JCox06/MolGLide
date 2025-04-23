package uk.co.jcox.chemvis.application.editor;

import jdk.dynalink.linker.LinkerServices;
import org.joml.Vector2f;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import uk.ac.ebi.beam.Element;
import uk.co.jcox.chemvis.cvengine.IApplication;

import java.util.*;

public class CDKManager implements IMoleculeManager{

    private final String PROPERTY_MOL_POSITION = "molpos";
    private final String PROPERTY_ATOM_POSITION = "atompos";

    private final Map<UUID, MolInfo> molecules;
    private final Map<UUID, IAtom> atoms;
    private final Map<UUID, IBond> bonds;

    public CDKManager() {
        this.molecules = new HashMap<>();
        this.atoms = new HashMap<>();
        this.bonds = new HashMap<>();
    }

    @Override
    public UUID createMolecule() {
        UUID molID = UUID.randomUUID();
        this.molecules.put(molID, new MolInfo());
        return molID;
    }

    @Override
    public UUID addAtom(UUID molecule, String element) {
        IAtom atom = new Atom(element);

        //Add the atom to the molecule
        MolInfo molInfo = molecules.get(molecule);
        molInfo.cdk.addAtom(atom);
        UUID uuid = UUID.randomUUID();
        molInfo.relAtoms.add(uuid);
        atoms.put(uuid, atom);
        return uuid;
    }

    @Override
    public UUID formBond(UUID moleculeID, UUID atom1, UUID atom2, int bondOrder) {
        IAtom cAtom1 = atoms.get(atom1);
        IAtom cAtom2 = atoms.get(atom2);
        IBond bond = new Bond(cAtom1, cAtom2, IBond.Order.SINGLE);
        UUID bondID = UUID.randomUUID();
        bonds.put(bondID, bond);

        IAtomContainer container = molecules.get(moleculeID).cdk;
        container.addBond(bond);

        molecules.get(moleculeID).relBonds.add(bondID);
        return bondID;
    }

    @Override
    public void setMoleculePosition(UUID moleculeID, Vector2f position) {
        IAtomContainer container = molecules.get(moleculeID).cdk;
        container.setProperty(PROPERTY_MOL_POSITION, position);
    }

    @Override
    public void setAtomOffsetPosition(UUID atomID, Vector2f position) {
        IAtom atom = atoms.get(atomID);
        atom.setProperty(PROPERTY_ATOM_POSITION, position);
    }

    @Override
    public Vector2f getMoleculePosition(UUID moleculeID) {
        return molecules.get(moleculeID).cdk.getProperty(PROPERTY_MOL_POSITION);
    }

    @Override
    public Vector2f getAtomOffsetPosition(UUID atomID) {
        return atoms.get(atomID).getProperty(PROPERTY_ATOM_POSITION);
    }

    @Override
    public String getAtomSymbol(UUID atom) {
        return atoms.get(atom).getSymbol();
    }

    @Override
    public Iterator<UUID> molecules() {
        return molecules.keySet().iterator();
    }

    @Override
    public Iterator<UUID> atoms(UUID molecule) {
        return this.molecules.get(molecule).relAtoms.iterator();
    }

    private static class MolInfo {
        public final IAtomContainer cdk;
        public final List<UUID> relAtoms;
        public final List<UUID> relBonds;

        public MolInfo() {
            this.cdk = new AtomContainer();
            this.relAtoms = new ArrayList<>();
            this.relBonds = new ArrayList<>();
        }
    }
}
