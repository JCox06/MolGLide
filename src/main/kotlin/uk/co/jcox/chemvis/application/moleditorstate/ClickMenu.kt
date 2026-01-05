package uk.co.jcox.chemvis.application.moleditorstate

import imgui.ImGui
import imgui.type.ImBoolean
import imgui.type.ImString
import jdk.internal.org.jline.keymap.KeyMap.display
import org.checkerframework.checker.units.qual.mol
import org.lwjgl.BufferUtils
import org.lwjgl.PointerBuffer
import org.lwjgl.system.MemoryStack
import org.lwjgl.util.nfd.NativeFileDialog
import org.openscience.cdk.depict.DepictionGenerator
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.io.MDLRXNWriter
import org.openscience.cdk.isomorphism.TransformOp
import org.tinylog.Level
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomVisibilityAction
import uk.co.jcox.chemvis.application.moleditorstate.action.CentreBondAction
import uk.co.jcox.chemvis.application.moleditorstate.action.ChangeAromacityAction
import uk.co.jcox.chemvis.application.moleditorstate.action.ChangeBondOrderAction
import uk.co.jcox.chemvis.application.moleditorstate.action.ChangeStereoAction
import uk.co.jcox.chemvis.application.moleditorstate.action.FlipBondAction
import java.awt.Desktop
import java.io.File
import java.nio.ByteBuffer

class ClickMenu (
    private val selectionManager: SelectionManager,
    private val actionManager: ActionManager,
    private val levelContainer: LevelContainer
) {

    var showBondMenu = false
    var showAtomMenu = false

    private var activePopup: ActivePopup = ActivePopup.None

    fun renderMenu() {
        displayBondMenu()
        displayAtomMenu()


        when (val popup = activePopup) {
            is ActivePopup.CDKSVGExporter -> displayCDKSVGExporter(popup)
            is ActivePopup.CustomInput -> displayCustomInput(popup)
            ActivePopup.None -> {}
        }
    }


    private fun displayCustomInput(popup: ActivePopup.CustomInput) {
        ImGui.begin("Custom Input")

        ImGui.text("Enter a custom group")

        ImGui.inputText("##", ImString())

        ImGui.sameLine()

        if (ImGui.button("Cancel")) {
            activePopup = ActivePopup.None
        }

        ImGui.sameLine()

        if (ImGui.button("Accept")) {
            activePopup = ActivePopup.None
            //todo actually add the group to the atom (The atom being the first letter in the input)
        }

        ImGui.end()
    }

    private fun displayBondMenu() {
        if (showBondMenu) {

            ImGui.openPopup("BondMenu")
            showBondMenu = false
        }

        val selection = selectionManager.primarySelection
        if (selection !is SelectionManager.Type.ActiveBond) {
            return
        }
        val bond = selection.bond
        val stereoChem = bond.getStereo()
        val bondOrder = bond.iBond.order

        if (ImGui.beginPopup("BondMenu")) {

            if (ImGui.menuItem("Flip Bond")) {
                val action = FlipBondAction(bond)
                actionManager.executeAction(action)
            }

            ImGui.separator()

            if (ImGui.beginMenu("Single")) {
                //todo these actions should really be grouped so one undo does two actions at once
                if (ImGui.menuItem("Plain", bondOrder == IBond.Order.SINGLE && stereoChem == IBond.Display.Solid)) {
                    val action = ChangeStereoAction(bond, IBond.Display.Solid)
                    actionManager.executeAction(action)
                    reduceToSingle(bond)
                }
                if (ImGui.menuItem("Wedged", bondOrder == IBond.Order.SINGLE && stereoChem == IBond.Display.WedgeEnd)) {
                    val action = ChangeStereoAction(bond, IBond.Display.WedgeEnd)
                    actionManager.executeAction(action)
                    reduceToSingle(bond)
                }
                if (ImGui.menuItem("Dashed", bondOrder == IBond.Order.SINGLE && stereoChem ==IBond.Display.WedgedHashEnd)) {
                    val action = ChangeStereoAction(bond, IBond.Display.WedgedHashEnd)
                    actionManager.executeAction(action)
                    reduceToSingle(bond)
                }

                ImGui.endMenu()
            }

            if (ImGui.beginMenu("Double")) {

                if (ImGui.menuItem("Plain", bondOrder == IBond.Order.DOUBLE)) {
                    val action = ChangeBondOrderAction(bond, IBond.Order.DOUBLE)
                    actionManager.executeAction(action)
                }

                ImGui.separator()

                if (ImGui.menuItem("Aromatic", bond.iBond.isAromatic)) {
                    val action = ChangeAromacityAction(bond)
                    actionManager.executeAction(action)
                }

                ImGui.separator()

                if (ImGui.menuItem("Centre/Carbonyl Bond", bond.centredBond)) {
                    val action = CentreBondAction(bond)
                    actionManager.executeAction(action)
                }
                ImGui.endMenu()
            }

            if (ImGui.menuItem("Triple Bond", bondOrder == IBond.Order.TRIPLE)) {
                val action = ChangeBondOrderAction(bond, IBond.Order.TRIPLE)
                actionManager.executeAction(action)
            }

            ImGui.separator()

            if (ImGui.menuItem("Delete")) {
                TODO("Create Delete Option")
            }

            ImGui.separator()

            displayGenericMoleculeMenu(selection.bond.atomA.parent)

            ImGui.endPopup()
        }
    }


    private fun displayGenericMoleculeMenu(molecule: ChemMolecule) {

        //todo Multi-thread CDK actions where required
        if (ImGui.beginMenu("CDK Tools")) {
            if (ImGui.menuItem("SVG Molecule Export")) {
                activePopup = ActivePopup.CDKSVGExporter(molecule)
                }
            ImGui.endMenu()
            }
        }

    private fun reduceToSingle(bond: ChemBond) {
        val action = ChangeBondOrderAction(bond, IBond.Order.SINGLE)
        actionManager.executeAction(action)
    }

    private fun displayAtomMenu() {
        if (showAtomMenu) {
            ImGui.openPopup("AtomMenu")
            showAtomMenu = false
        }

        val selection = selectionManager.primarySelection
        if (selection !is SelectionManager.Type.ActiveAtom) {
            return
        }

        if (ImGui.beginPopup("AtomMenu")) {

            if (ImGui.menuItem("Edit Label")) {
                activePopup = ActivePopup.CustomInput()
            }

            if (ImGui.menuItem("Group Visible", selection.atom.visible)) {
                val action = AtomVisibilityAction(selection.atom)
                actionManager.executeAction(action)
            }

            ImGui.separator()

            if (ImGui.menuItem("Delete")) {
                TODO("Create Delete Action")
            }

            ImGui.separator()

            displayGenericMoleculeMenu(selection.atom.parent)
            ImGui.endMenu()
        }



    }

    private fun displayCDKSVGExporter(popup: ActivePopup.CDKSVGExporter) {
        ImGui.begin("CDK SVG Exporter")

        ImGui.textWrapped("Export this molecule using CDK. Note: CDK might interpet your molecule slightly differently!")


        ImGui.checkbox("Terminal Carbon Display", popup.terminalCarbons)
        ImGui.checkbox("Aromatic Display", popup.aromaticDisplay)
        ImGui.checkbox("Carbon Symbols", popup.carbonDisplay)

        if (ImGui.button("Export")) {
            var dg = DepictionGenerator()
                .withSize(500.0, 500.0)
                .withFillToFit()

            if (popup.aromaticDisplay.get()) {
                dg = dg.withAromaticDisplay()
            }
            if (popup.carbonDisplay.get()) {
                dg = dg.withCarbonSymbols()
            }
            if (popup.terminalCarbons.get()) {
                dg = dg.withTerminalCarbons()
            }

            val depiction = dg.depict(popup.molecule.iContainer)
            val file = File("image.svg")

            val task = Runnable {
                file.writeText(depiction.toSvgStr())
                Desktop.getDesktop().open(file)
            }
            val thread = Thread(task)
            thread.start()

            activePopup = ActivePopup.None
        }

        ImGui.sameLine()
        if (ImGui.button("Close")) {
            activePopup = ActivePopup.None
        }
        ImGui.end()
    }


    fun closeWindows() {
        showAtomMenu = false
        showBondMenu = false
        activePopup = ActivePopup.None
    }

    private sealed class ActivePopup {
        object None: ActivePopup()

        class CustomInput() : ActivePopup()

        class CDKSVGExporter(val molecule: ChemMolecule, val terminalCarbons: ImBoolean = ImBoolean(false), val aromaticDisplay: ImBoolean = ImBoolean(false), val carbonDisplay: ImBoolean = ImBoolean(false)): ActivePopup()
    }
}