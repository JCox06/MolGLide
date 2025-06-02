package uk.co.jcox.chemvis.application.chemengine;

import org.checkerframework.checker.units.qual.A;
import org.openscience.cdk.Atom;
import org.openscience.cdk.AtomContainer;
import org.openscience.cdk.Bond;
import org.openscience.cdk.CDK;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import java.util.HashMap;
import java.util.List;
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

}
