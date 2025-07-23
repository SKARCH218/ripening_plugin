package lightstudio.ripening_plugin

import lightstudio.ripening_plugin.commands.AdminCommand
import lightstudio.ripening_plugin.commands.AdminTabCompleter
import lightstudio.ripening_plugin.database.DatabaseManager
import lightstudio.ripening_plugin.listeners.JarListener
import lightstudio.ripening_plugin.manager.ConfigManager
import lightstudio.ripening_plugin.manager.GuiManager
import lightstudio.ripening_plugin.manager.RecipeManager
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import org.bukkit.Bukkit

class Ripening_plugin : JavaPlugin() {

    companion object {
        lateinit var instance: Ripening_plugin
            private set
    }

    lateinit var configManager: ConfigManager
    lateinit var databaseManager: DatabaseManager
    lateinit var recipeManager: RecipeManager
    lateinit var guiManager: GuiManager

    override fun onEnable() {
        instance = this

        // Initialize managers
        configManager = ConfigManager(this)
        configManager.loadConfigs()

        databaseManager = DatabaseManager(dataFolder)
        databaseManager.connect()

        recipeManager = RecipeManager(configManager)
        guiManager = GuiManager(configManager, recipeManager, databaseManager)

        // Register listeners
        server.pluginManager.registerEvents(JarListener(databaseManager, guiManager, recipeManager, configManager), this)

        // Register commands
        getCommand("ripening")?.setExecutor(AdminCommand(this))
        getCommand("ripening")?.setTabCompleter(AdminTabCompleter())

        // Copy ItemsAdder configs
        copyItemsAdderConfigs()

        
    }

    override fun onDisable() {
        databaseManager.disconnect()
        
    }

    private fun copyItemsAdderConfigs() {
        val pluginItemsAdderFolder = File(dataFolder, "itemsadder")
        if (!pluginItemsAdderFolder.exists()) {
            pluginItemsAdderFolder.mkdirs()
        }
        copyResourceFolder("itemsadder", pluginItemsAdderFolder)
    }

    private fun copyResourceFolder(resourcePath: String, destinationFolder: File) {
        try {
            val jarUrl = javaClass.protectionDomain.codeSource.location
            val jarFile = java.util.jar.JarFile(jarUrl.toURI().path)
            val entries = jarFile.entries()

            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                val entryName = entry.name

                if (entryName.startsWith(resourcePath) && !entry.isDirectory) {
                    val relativePath = entryName.substring(resourcePath.length)
                    val destinationFile = File(destinationFolder, relativePath)

                    if (!destinationFile.exists()) {
                        destinationFile.parentFile.mkdirs()
                        jarFile.getInputStream(entry).use { inputStream ->
                            FileOutputStream(destinationFile).use { outputStream ->
                                inputStream.copyTo(outputStream)
                            }
                        }
                        logger.info("Copied resource: $entryName to ${destinationFile.absolutePath}")
                    }
                }
            }
        } catch (e: Exception) {
            logger.warning("Failed to copy resources from $resourcePath: ${e.message}")
        }
    }
}