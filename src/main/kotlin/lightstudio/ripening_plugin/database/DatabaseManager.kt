package lightstudio.ripening_plugin.database

import lightstudio.ripening_plugin.data.JarData
import org.bukkit.Location
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.sql.Types
import java.util.UUID

class DatabaseManager(private val dataFolder: File) {
    private var connection: Connection? = null

    fun connect() {
        if (!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val dbFile = File(dataFolder, "ripening.db")
        val url = "jdbc:sqlite:${dbFile.absolutePath}"
        connection = DriverManager.getConnection(url)
        createTable()
    }

    fun disconnect() {
        connection?.close()
    }

    private fun createTable() {
        val sql = """
            CREATE TABLE IF NOT EXISTS jars (
                world TEXT NOT NULL,
                x INTEGER NOT NULL,
                y INTEGER NOT NULL,
                z INTEGER NOT NULL,
                owner_uuid TEXT NOT NULL,
                recipe_id TEXT,
                start_time INTEGER,
                PRIMARY KEY (world, x, y, z)
            );
        """.trimIndent()
        connection?.createStatement()?.execute(sql)
    }

    fun getJarData(location: Location): JarData? {
        val sql = "SELECT * FROM jars WHERE world = ? AND x = ? AND y = ? AND z = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, location.world.name)
            pstmt.setInt(2, location.blockX)
            pstmt.setInt(3, location.blockY)
            pstmt.setInt(4, location.blockZ)
            val rs = pstmt.executeQuery()
            if (rs.next()) {
                val recipeId = rs.getString("recipe_id")
                val startTime = rs.getLong("start_time")
                return JarData(
                    rs.getString("world"),
                    rs.getInt("x"),
                    rs.getInt("y"),
                    rs.getInt("z"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    if (rs.wasNull()) null else recipeId,
                    if (rs.wasNull()) null else startTime
                )
            }
        }
        return null
    }

    fun saveJarData(jarData: JarData) {
        val sql = "INSERT OR REPLACE INTO jars (world, x, y, z, owner_uuid, recipe_id, start_time) VALUES (?, ?, ?, ?, ?, ?, ?)"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, jarData.world)
            pstmt.setInt(2, jarData.x)
            pstmt.setInt(3, jarData.y)
            pstmt.setInt(4, jarData.z)
            pstmt.setString(5, jarData.owner.toString())
            if (jarData.recipeId == null) pstmt.setString(6, null) else pstmt.setString(6, jarData.recipeId)
            if (jarData.startTime == null) pstmt.setObject(7, null) else pstmt.setLong(7, jarData.startTime)
            pstmt.executeUpdate()
        }
    }

    fun deleteJarData(location: Location) {
        val sql = "DELETE FROM jars WHERE world = ? AND x = ? AND y = ? AND z = ?"
        connection?.prepareStatement(sql)?.use { pstmt ->
            pstmt.setString(1, location.world.name)
            pstmt.setInt(2, location.blockX)
            pstmt.setInt(3, location.blockY)
            pstmt.setInt(4, location.blockZ)
            pstmt.executeUpdate()
        }
    }
}
