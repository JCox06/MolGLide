package uk.co.jcox.chemvis.application.moleditorstate

import imgui.ImGui
import imgui.type.ImString
import org.openscience.cdk.interfaces.IBond
import org.openscience.cdk.isomorphism.TransformOp
import org.tinylog.Level
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.LevelContainer
import uk.co.jcox.chemvis.application.moleditorstate.action.AtomVisibilityAction
import uk.co.jcox.chemvis.application.moleditorstate.action.CentreBondAction
import uk.co.jcox.chemvis.application.moleditorstate.action.ChangeBondOrderAction
import uk.co.jcox.chemvis.application.moleditorstate.action.ChangeStereoAction
import uk.co.jcox.chemvis.application.moleditorstate.action.FlipBondAction

class ClickMenu (
    private val selectionManager: SelectionManager,
    private val actionManager: ActionManager,
    private val levelContainer: LevelContainer
) {

    var showBondMenu = false
    var showAtomMenu = false


    var showCustomLabelInput = false

    fun renderMenu() {
        displayBondMenu()
        displayAtomMenu()

        if (showCustomLabelInput) {
            displayCustomInput()
        }
    }


    private fun displayCustomInput() {
        ImGui.begin("Custom Input")

        ImGui.text("Enter a custom group")

        ImGui.inputText("##", ImString())

        ImGui.sameLine()

        if (ImGui.button("Cancel")) {
            showCustomLabelInput = false
        }

        ImGui.sameLine()

        if (ImGui.button("Accept")) {
            showCustomLabelInput = false
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

                if (ImGui.menuItem("Aromatic", false)) {
                    TODO("Create Aromatic Bond Order")
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

            ImGui.endPopup()
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
                showCustomLabelInput = true
            }

            if (ImGui.menuItem("Group Visible", selection.atom.visible)) {
                val action = AtomVisibilityAction(selection.atom)
                actionManager.executeAction(action)
            }

            ImGui.separator()

            if (ImGui.menuItem("Delete")) {
                TODO("Create Delete Action")
            }

            ImGui.endMenu()
        }

    }

    fun closeWindows() {
        showAtomMenu = false
        showBondMenu = false
        showCustomLabelInput = false
    }
}