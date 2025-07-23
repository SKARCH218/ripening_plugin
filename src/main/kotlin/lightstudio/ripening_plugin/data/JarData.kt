package lightstudio.ripening_plugin.data

import java.util.UUID

data class JarData(
    val world: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val owner: UUID,
    val recipeId: String?,
    val startTime: Long?
)
