package dev.thebjoredcraft.test.listener

import dev.thebjoredcraft.test.CamPath
import dev.thebjoredcraft.test.CamPhase
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object PlayerJoinListener : Listener {
    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val world = event.player.world
        val phase1 = CamPhase(
            start = Location(world, 0.0, 80.0, 0.0, 0f, 0f),
            end = Location(world, 50.0, 85.0, -20.0, 90f, 0f),
            durationTicks = 100,
            stayTicks = 20
        )

        val phase2 = CamPhase(
            start = Location(world, 50.0, 85.0, -20.0, 90f, 0f),
            end = Location(world, 50.0, 90.0, 50.0, 180f, -10f),
            durationTicks = 120,
            stayTicks = 10
        )

        val phases = listOf(phase1, phase2)
        val uuid = event.player.uniqueId
        val cam = CamPath(uuid, phases)

        cam.start()


    }
}