package dev.thebjoredcraft.test

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.UUID
import java.util.concurrent.TimeUnit

data class CamPhase(
    val start: Location,
    val end: Location,
    val durationTicks: Int,
    val stayTicks: Int
)

class CamPath(
    private val playerUUID: UUID,
    private val phases: List<CamPhase>
) {

    private var currentPhase = 0
    private var tick = 0
    private var stay = 0

    private lateinit var oldGamemode: GameMode
    private lateinit var oldLocation: Location
    private lateinit var task: ScheduledTask
    private var oldFlying = false

    fun start() {
        val player = Bukkit.getPlayer(playerUUID) ?: return
        oldGamemode = player.gameMode
        oldLocation = player.location.clone()
        oldFlying = player.isFlying
        player.gameMode = GameMode.SPECTATOR
        player.teleport(phases.first().start)
        run()
    }

    private fun run() {
        task = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, {
            val player = Bukkit.getPlayer(playerUUID) ?: run {
                task.cancel();
                stop();
                return@runAtFixedRate
            }

            if (currentPhase >= phases.size) {
                task.cancel()
                stop()
                return@runAtFixedRate
            }

            val phase = phases[currentPhase]

            if (tick <= phase.durationTicks) {
                val start = phase.start
                val end = phase.end
                val progress = tick.toDouble() / phase.durationTicks

                val x = lerp(start.x, end.x, progress)
                val y = lerp(start.y, end.y, progress)
                val z = lerp(start.z, end.z, progress)
                val yaw = lerpAngle(start.yaw, end.yaw, progress)
                val pitch = lerp(start.pitch, end.pitch, progress)

                val loc = Location(start.world, x, y, z, yaw, pitch)

                player.teleport(loc)

                val nextProgress = (tick + 1).toDouble() / phase.durationTicks
                val nx = lerp(start.x, end.x, nextProgress)
                val ny = lerp(start.y, end.y, nextProgress)
                val nz = lerp(start.z, end.z, nextProgress)
                val nloc = Location(start.world, nx, ny, nz)

                val vec = nloc.toVector().subtract(loc.toVector())
                player.velocity = vec

                tick++
                return@runAtFixedRate
            }

            if (stay < phase.stayTicks) {
                stay++
                return@runAtFixedRate
            }

            currentPhase++
            tick = 0
            stay = 0
        }, 0, 50, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        task::cancel

        val player = Bukkit.getPlayer(playerUUID) ?: return
        player.gameMode = oldGamemode
        player.teleport(oldLocation)
        player.isFlying = oldFlying
    }

    private fun lerp(a: Double, b: Double, t: Double) = a + (b - a) * t
    private fun lerp(a: Float, b: Float, t: Double) = a + (b - a) * t.toFloat()

    private fun lerpAngle(a: Float, b: Float, t: Double): Float {
        var diff = b - a
        diff = (diff + 540) % 360 - 180
        return a + diff * t.toFloat()
    }
}