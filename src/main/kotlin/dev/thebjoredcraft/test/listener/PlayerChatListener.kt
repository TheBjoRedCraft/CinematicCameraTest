package dev.thebjoredcraft.test.listener

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import dev.thebjoredcraft.test.CamPath
import dev.thebjoredcraft.test.CamPhase
import dev.thebjoredcraft.test.plugin
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

object PlayerChatListener : Listener {

    @EventHandler
    fun onChat(event: AsyncChatEvent) {
        val p = event.player
        val w = p.world
        val base = p.location.clone()

        // Phase 1: Schneller Push nach vorne oben
        val forward = base.direction.clone().multiply(15.0)
        val upward = 10.0

        val phase1 = CamPhase(
            start = base.clone(),
            end = base.clone().add(forward.x, upward, forward.z),
            durationTicks = 40,    // 2 Sekunden
            stayTicks = 10
        )

        // Phase 2: Orbit um den Spieler
        val radius = 10.0
        val center = base.clone()
        val orbitStart = center.clone().add(radius, 3.0, 0.0)
        val orbitEnd = center.clone().add(-radius, 3.0, 0.0)

        val phase2 = CamPhase(
            start = orbitStart,
            end = orbitEnd,
            durationTicks = 100,   // 5 Sekunden
            stayTicks = 20
        )

        // Phase 3: Dramatischer Rückwärts-Zoom
        val backward = base.direction.clone().multiply(-1).normalize().multiply(25.0)

        val phase3 = CamPhase(
            start = center.clone(),
            end = center.clone().add(backward),
            durationTicks = 60,    // 3 Sekunden
            stayTicks = 10
        )

        val cam = CamPath(
            playerUUID = p.uniqueId,
            phases = listOf(phase1, phase2, phase3)
        )

        plugin.launch(plugin.entityDispatcher(p)) {
            cam.start()
        }
    }
}
