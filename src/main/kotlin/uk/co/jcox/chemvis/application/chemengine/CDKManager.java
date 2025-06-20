package uk.co.jcox.chemvis.application.chemengine;

import org.openscience.cdk.*;
import org.openscience.cdk.atomtype.CDKAtomTypeMatcher;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.*;
import org.openscience.cdk.tools.CDKHydrogenAdder;
import org.openscience.cdk.tools.manipulator.AtomTypeManipulator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;
import uk.co.jcox.chemvis.application.moleditor.AtomInsert;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class CDKManager implements IMoleculeManager{

    private final Map<UUID, MoleculeHolder> molecules;

    public CDKManager() {
        this.molecules = new HashMap<>();
    }

    public CDKManager(Map<UUID, MoleculeHolder> molecules) {
        this.molecules = molecules;
    }

    private static class MoleculeHolder {
        final UUID id;
        final IAtomContainer mol;
        final Map<UUID, IAtom> atoms;
        final Map<UUID, IBond> bonds;

        public MoleculeHolder(UUID id, IAtomContainer mol, Map<UUID, IAtom> atoms, Map<UUID, IBond> bonds) {
            this.id = id;
            this.mol = mol;
            this.atoms = atoms;
            this.bonds = bonds;
        }

        public MoleculeHolder() {
            this.id = UUID.randomUUID();
            this.mol = new AtomContainer();
            this.atoms = new HashMap<>();
            this.bonds = new HashMap<>();
        }

        public MoleculeHolder clone() {
            try {
                UUID idCopy = id;
                IAtomContainer molCopy = mol.clone();

                // Map original atoms to their UUIDs
                Map<IAtom, UUID> atomToUUID = new HashMap<>();
                for (Map.Entry<UUID, IAtom> entry : atoms.entrySet()) {
                    atomToUUID.put(entry.getValue(), entry.getKey());
                }

                Map<UUID, IAtom> atomsCopy = new HashMap<>();
                for (int i = 0; i < mol.getAtomCount(); i++) {
                    IAtom origAtom = mol.getAtom(i);
                    IAtom clonedAtom = molCopy.getAtom(i);
                    UUID uuid = atomToUUID.get(origAtom);
                    if (uuid != null) {
                        atomsCopy.put(uuid, clonedAtom);
                    }
                }

                // Map original bonds to their UUIDs
                Map<IBond, UUID> bondToUUID = new HashMap<>();
                for (Map.Entry<UUID, IBond> entry : bonds.entrySet()) {
                    bondToUUID.put(entry.getValue(), entry.getKey());
                }

                Map<UUID, IBond> bondsCopy = new HashMap<>();
                for (int i = 0; i < mol.getBondCount(); i++) {
                    IBond origBond = mol.getBond(i);
                    IBond clonedBond = molCopy.getBond(i);
                    UUID uuid = bondToUUID.get(origBond);
                    if (uuid != null) {
                        bondsCopy.put(uuid, clonedBond);
                    }
                }

                return new MoleculeHolder(idCopy, molCopy, atomsCopy, bondsCopy);

            } catch (
                    CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }

        }
    }

    @Override
    public UUID createMolecule() {
        final MoleculeHolder newMolecule = new MoleculeHolder();
        this.molecules.put(newMolecule.id, newMolecule);
        return newMolecule.id;
    }

    @Override
    public UUID addAtom(UUID molecule, String element) {
        MoleculeHolder molHolder = getMolHolder(molecule);

        Atom cdkAtom = new Atom(element);
        UUID atomID = UUID.randomUUID();

        molHolder.mol.addAtom(cdkAtom);
        molHolder.atoms.put(atomID, cdkAtom);

        return atomID;
    }

    @Override
    public UUID formBond(UUID moleculeID, UUID atom1, UUID atom2, int bondOrder) {
        MoleculeHolder molHolder = getMolHolder(moleculeID);

        IAtom cdkAtom1 = molHolder.atoms.get(atom1);
        IAtom cdkAtom2 = molHolder.atoms.get(atom2);

        if (cdkAtom1 == null || cdkAtom2 == null) {
            throw new NullPointerException("Atoms are null");
        }


        Bond newBond = new Bond(cdkAtom1, cdkAtom2, IBond.Order.SINGLE);
        UUID bondID = UUID.randomUUID();

        molHolder.mol.addBond(newBond);

        molHolder.bonds.put(bondID, newBond);

        return bondID;
    }

    @Override
    public String getMolecularFormula(UUID moleculeID) {
        MoleculeHolder moleculeHolder = getMolHolder(moleculeID);

        IMolecularFormula formula = MolecularFormulaManipulator.getMolecularFormula(moleculeHolder.mol);
        return MolecularFormulaManipulator.getString(formula);
    }

    @Override
    public int getBonds(UUID molecule, UUID atom) {
        MoleculeHolder moleculeHolder = getMolHolder(molecule);
        IAtom cdkAtom = moleculeHolder.atoms.get(atom);

        return moleculeHolder.mol.getConnectedBondsCount(cdkAtom) + cdkAtom.getImplicitHydrogenCount();
    }

    @Override
    public boolean isOfElement(UUID molecule, UUID Atom, String element) {
        MoleculeHolder moleculeHolder = getMolHolder(molecule);
        IAtom cdkAtom = moleculeHolder.atoms.get(Atom);

        return cdkAtom.getSymbol().equals(element);
    }

    @Override
    public void recalculate(UUID molecule) {
        MoleculeHolder moleculeHolder = getMolHolder(molecule);

        try {
            CDKAtomTypeMatcher matcher = CDKAtomTypeMatcher.getInstance(moleculeHolder.mol.getBuilder());

            for (IAtom atom: moleculeHolder.mol.atoms()) {

                atom.setAtomTypeName(null);
                atom.setValency(null);
                atom.setHybridization(null);
                atom.setFormalNeighbourCount(null);
                atom.setBondOrderSum(null);
                atom.setImplicitHydrogenCount(null);

                IAtomType type = matcher.findMatchingAtomType(moleculeHolder.mol, atom);
                AtomTypeManipulator.configure(atom, type);
            }
            CDKHydrogenAdder adder = CDKHydrogenAdder.getInstance(moleculeHolder.mol.getBuilder());
            adder.addImplicitHydrogens( moleculeHolder.mol);


            for (IAtom atom : moleculeHolder.mol.atoms()) {
                if (! AtomInsert.Companion.fromSymbol(atom.getSymbol()).getHydrogenable()) {
                    atom.setImplicitHydrogenCount(0);
                }
            }


        } catch (
                CDKException e) {
            //Error!
        }
    }

    @Override
    public IMoleculeManager clone() {
        Map<UUID, MoleculeHolder> copyMolecules = new HashMap<>();
        molecules.forEach((molID, mol) -> {
            copyMolecules.put(molID, mol.clone());
        });

        return new CDKManager(copyMolecules);
    }


    private MoleculeHolder getMolHolder(UUID molID) {
        MoleculeHolder holder = molecules.get(molID);

        if (holder == null) {
            throw new NullPointerException("Molecule not found");
        }

        return holder;
    }

    @Override
    public int getImplicitHydrogens(UUID molecule, UUID atom) {
        MoleculeHolder moleculeHolder = getMolHolder(molecule);
        IAtom cdkAtom = moleculeHolder.atoms.get(atom);
        return cdkAtom.getImplicitHydrogenCount();
    }

    @Override
    public void updateBondOrder(UUID molecule, UUID bond, int newBondOrder) {
        MoleculeHolder moleculeHolder = getMolHolder(molecule);
        IBond cdkBond = moleculeHolder.bonds.get(bond);
        cdkBond.setOrder(IBond.Order.DOUBLE);
    }

    @Override
    public UUID getJoiningBond(UUID molecule, UUID atomA, UUID atomB) {
        MoleculeHolder moleculeHolder = getMolHolder(molecule);
        IAtom cdkAtomA =  moleculeHolder.atoms.get(atomA);
        IAtom cdkAtomB =  moleculeHolder.atoms.get(atomB);

        IBond bond = moleculeHolder.mol.getBond(cdkAtomA, cdkAtomB);
        for (UUID value : moleculeHolder.bonds.keySet()) {
            if (moleculeHolder.bonds.get(value) == bond) {
                return value;
            }
        }
        return null;
    }
}
