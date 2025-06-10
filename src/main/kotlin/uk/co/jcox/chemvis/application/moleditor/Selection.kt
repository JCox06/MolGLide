package uk.co.jcox.chemvis.application.moleditor

import java.util.UUID

sealed class Selection {
    object None: Selection()
    data class Active(val id: UUID) : Selection()
}