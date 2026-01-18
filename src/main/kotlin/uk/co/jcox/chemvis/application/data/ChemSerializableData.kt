package uk.co.jcox.chemvis.application.data

import org.joml.Vector3f
import org.openscience.cdk.interfaces.IBond
import uk.co.jcox.chemvis.application.graph.ChemAtom
import uk.co.jcox.chemvis.application.graph.ChemBond
import uk.co.jcox.chemvis.application.graph.ChemMolecule
import java.io.Serializable
import javax.vecmath.Point2d

data class DataMolecule (
    val id: Int,
    val offset: Vector3f,
    val atoms: MutableList<Int> = mutableListOf(),
    val bonds: MutableList<Int> = mutableListOf()
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

data class DataAtom (
    val id: Int,
    val symbol: String,
    val isVisible: Boolean,
    val labelPosition: ChemAtom.RelationalPos,
    val internalPosition: Point2d,
    val visible: Boolean,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}


data class DataBond(
    val id: Int,
    val atomA: Int,
    val atomB: Int,
    val centred: Boolean,
    val nudge: Vector3f,
    val bondFlip: Boolean,
    val offset: Vector3f,
    val order: IBond.Order,
    val stereo: IBond.Display,
    val aromatic: Boolean,
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

data class DataSaveFile(
    val molecules: MutableList<DataMolecule> = mutableListOf(),
    val bonds: MutableMap<Int, DataBond> = mutableMapOf(),
    val atoms: MutableMap<Int, DataAtom> = mutableMapOf(),
) : Serializable {
    companion object {
        private const val serialVersionUID: Long = 1L
    }
}

data class DataObjectIDMap(
    val containers: MutableMap<ChemMolecule, Int> = mutableMapOf(),
    val atoms: MutableMap<ChemAtom, Int> = mutableMapOf(),
    val bonds: MutableMap<ChemBond, Int> = mutableMapOf(),
    )