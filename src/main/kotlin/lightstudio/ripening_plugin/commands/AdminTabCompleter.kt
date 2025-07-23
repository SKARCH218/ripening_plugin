package lightstudio.ripening_plugin.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter

class AdminTabCompleter : TabCompleter {

    override fun onTabComplete(sender: CommandSender, command: Command, alias: String, args: Array<out String>): MutableList<String>? {
        if (args.size == 1) {
            return mutableListOf("reload").filter { it.startsWith(args[0], ignoreCase = true) }.toMutableList()
        }
        return null
    }
}