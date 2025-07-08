package uk.co.jcox.chemvis.application.moleditor

data class ClickContext(
    val xPos: Float,
    val yPos: Float,
    val atomInsert: AtomInsert,
    val compoundInsert: CompoundInsert,
    )
