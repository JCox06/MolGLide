package uk.co.jcox.chemvis.application.moleditorstate

enum class RingInsert (val size: Int, val friendlyName: String) {

    BENZENE(6, "Benzene"),
    CYCLOHEXANE(6, "Cyclohexane"),
    CYCLOPENTANE(5, "Cyclopentane"),
    CYCLOPROPANE(4, "Cyclopropane"),
    CYCLOBUTANE(3, "Cyclobutane"),
    CYCLOHEPTANE(7, "Cycloheptane"),
    CYCLOOCTANE(8, "Cyclooctane")
}