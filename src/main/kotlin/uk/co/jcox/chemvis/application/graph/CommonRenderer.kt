package uk.co.jcox.chemvis.application.graph

import org.joml.Vector3f
import org.joml.minus
import org.joml.plus
import org.joml.times
import org.joml.unaryMinus
import org.openscience.cdk.interfaces.IBond
import kotlin.math.min


/**
 * This class takes all the chemistry information from the LevelContainer and turns it into "primitives"
 * Primitives are lines, text, symbols, etc
 *
 * The (OpenGL) LevelRenderer and in the future the SVGRenderer then use the CommonRenderer to draw stuff
 */


class CommonRenderer (
    private val levelContainer: LevelContainer,
    private val themeStyleManager: ThemeStyleManager,
) {

    private val normalLines = mutableListOf<LineElement>()
    private val wedgedLines = mutableListOf<LineElement>()
    private val dashedLines = mutableListOf<LineElement>()
    private val textElements = mutableListOf<FormulaStringElement>()

    private val doubleBondOffset = themeStyleManager.lineThickness * 2.0f

    fun calculatePrimitiveData() {

        normalLines.clear()
        wedgedLines.clear()
        dashedLines.clear()
        textElements.clear()

        val atomEntities: MutableList<ChemAtom> = mutableListOf()
        val normalBonds: MutableList<ChemBond> = mutableListOf()
        val wedgedBonds: MutableList<ChemBond> = mutableListOf()
        val dashedBonds: MutableList<ChemBond> = mutableListOf()

        traverseAndCollect(atomEntities, normalBonds, wedgedBonds, dashedBonds)
        prepareBondData(normalBonds) { normalLines += it }
        prepareBondData(wedgedBonds) { wedgedLines += it }
        prepareBondData(dashedBonds) { dashedLines += it }

        prepareAtomGroupData(atomEntities)

    }

    private fun traverseAndCollect(
        atomsFound: MutableList<ChemAtom>,
        normalBondsFound: MutableList<ChemBond>,
        wedgedBondsFound: MutableList<ChemBond>,
        dashedBondsFound: MutableList<ChemBond>,
    ) {
        //Go through every molecule, noting down the molecule position
        levelContainer.sceneMolecules.forEach { mol ->
            //Now go through every atom
            mol.atoms.forEach { atom ->
                atomsFound.add(atom)
            }
            mol.bonds.forEach { bond ->
                val order = bond.iBond.order
                if (order == IBond.Order.DOUBLE || order == IBond.Order.TRIPLE) {
                    normalBondsFound.add(bond)
                    return@forEach
                }
                val stereochem = bond.getStereo()
                when (stereochem) {
                    IBond.Display.Solid -> normalBondsFound.add(bond)
                    IBond.Display.WedgeEnd -> wedgedBondsFound.add(bond)
                    IBond.Display.WedgedHashEnd -> dashedBondsFound.add(bond)
                    else -> normalBondsFound.add(bond)
                }
            }
        }
    }


    private fun prepareBondData(normalBondsFound: MutableList<ChemBond>, submit: (line: LineElement) -> Unit) {
        normalBondsFound.forEach { bond ->
            val atomA = bond.atomA
            val atomB = bond.atomB
            val start = getEffectiveBond(atomA, atomB)
            val end = getEffectiveBond(atomB, atomA)

            val modVector = getCentreBondTransform(bond)


            submit(applyBondTransform(start, end, modVector))

            //Double bond - Triple bond logic
            val vec = bond.getOrth() * doubleBondOffset
            if (bond.flipDoubleBond) vec.negate()


            val valueA = getExtraShorteningModifier(bond, atomA)
            val valueB = getExtraShorteningModifier(bond, atomB)
            val highStart = getEffectiveBond(start, end, valueA)
            val highEnd = getEffectiveBond(end, start, valueB)

            if (bond.iBond.order == IBond.Order.DOUBLE || bond.iBond.order == IBond.Order.TRIPLE) {
                normalLines += applyBondTransform(highStart, highEnd, vec + modVector)
            }
            if (bond.iBond.order == IBond.Order.TRIPLE) {
                normalLines += applyBondTransform(highStart, highEnd, -vec + modVector)
            }

        }
    }


    private fun getExtraShorteningModifier(bond: ChemBond, changingAtom: ChemAtom) : Float {
        if (bond.centredBond) {
            return 1.0f
        }

        var minimumValue = 1.0f

        val bondLength = (bond.atomA.getWorldPosition() - bond.atomB.getWorldPosition()).length()

        //Now shorten based on single bonds around this one
        //This kind of works, but is more qualitative than quantitative method
        val molecule = changingAtom.parent
        val bonds = molecule.getConnectedBonds(changingAtom)
        bonds.forEach { adjacentBond ->
            val randomAtom = adjacentBond.getOtherAtom(changingAtom)
            val distanceAtom = bond.getOtherAtom(changingAtom)
            if (randomAtom != distanceAtom && randomAtom != null && distanceAtom != null) {
                val distance = randomAtom.getWorldPosition() - distanceAtom.getWorldPosition()
                //divide further by two, as the line segment considered is 2 full bond lengths, and so we want a proportion of that
                val newValue =  distance.length() / bondLength * 0.5f
                if (newValue < minimumValue) {
                    minimumValue = newValue
                }
            }
        }

        return minimumValue
    }


    //todo Cant remember how this works, need to create proper system
    private fun getCentreBondTransform(bond: ChemBond) : Vector3f {

        if (!bond.centredBond) {
            return Vector3f()
        }

        val flip = if (!bond.flipDoubleBond) -1.0f else 1.0f
        val orth = bond.getOrth() * doubleBondOffset * flip
        val bisectorNudge = bond.bisectorNudge
        if (! (bisectorNudge.x == 0f && bisectorNudge.y == 0f && bisectorNudge.z == 0f)) {
            orth.div(2f)
        }
        val newNudge = Vector3f(bisectorNudge)
        val walk = bond.getVector()
        if (walk.y > 0) {
            newNudge.y = -newNudge.y
        }
        return orth + newNudge
    }

    private fun applyBondTransform(start: Vector3f, end: Vector3f, translate: Vector3f) : LineElement {
        val newStart =  start + translate
        val newEnd = end + translate
        return LineElement(newStart, newEnd)
    }

    fun getLineData() : List<LineElement> {
        return normalLines
    }

    fun getWedgeData() : List<LineElement> {
        return wedgedLines
    }

    fun getDashedData(): List<LineElement> {
        return dashedLines
    }

    fun getElements() : List<FormulaStringElement> {
        return textElements
    }


    /**
     * Although bonds have a length from atom X to atom Y
     * They need to be cut off at the end to ensure the atom level text is not being drawn over
     * The effective length takes into account, and takes a bond from the benchmark to the changing atom minus a modifier, usually 0.70
     */
    private fun getEffectiveBond(changingAtom: ChemAtom, benchmark: ChemAtom) : Vector3f {
        if (changingAtom.visible) {
            return getEffectiveBond(changingAtom.getWorldPosition(), benchmark.getWorldPosition())
        }
        return changingAtom.getWorldPosition()
    }

    private fun getEffectiveBond(changingAtom: Vector3f, benchmark: Vector3f, factor: Float = 0.70f) : Vector3f {
        val vector = changingAtom - benchmark
        val newPoint = benchmark + (vector * factor)
        return newPoint

    }

    private fun prepareAtomGroupData(atomEntities: MutableList<ChemAtom>) {
        atomEntities.forEach { atom ->
            if (! atom.visible) {
                return@forEach
            }
            atom.priorityName?.let { overrideAtomString(atom, it) } ?: run {createNormalAtomString(atom)}
        }
    }


    private fun createNormalAtomString(atom: ChemAtom) {
        val symbol = atom.getSymbol()
        val position = atom.getWorldPosition()

        var relationalElement: RelationalFormulaStringElement? = null
        val groupString = createProtonFormulaString(atom)
        val groupPos = atom.implicitHydrogenPos
        if (groupString != "") {
            relationalElement = RelationalFormulaStringElement(groupString, groupPos)
        }
        val textElement = FormulaStringElement(symbol, position, relationalElement)
        textElements += textElement
    }

    private fun overrideAtomString(atom: ChemAtom, label: String) {
        val textElement = FormulaStringElement(label, atom.getWorldPosition(), null)
        textElements += textElement
    }

    private fun createProtonFormulaString(chemAtom: ChemAtom) : String {
        val protons = chemAtom.iAtom.implicitHydrogenCount
        if (protons == 0) {
            return ""
        }
        val string = if (protons == 1) "H" else "H${protons}"
        return string
    }


    data class LineElement (val start: Vector3f, val end: Vector3f)
    data class FormulaStringElement(val text: String, val position: Vector3f, val group: RelationalFormulaStringElement?)
    data class RelationalFormulaStringElement(val text: String, val relationalPos: ChemAtom.RelationalPos)


    companion object {
        const val DIGIT_SCALE = 0.6f
    }
}