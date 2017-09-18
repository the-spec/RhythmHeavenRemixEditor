package io.github.chrislo27.rhre3.soundsystem.beads

import io.github.chrislo27.rhre3.soundsystem.Sound
import net.beadsproject.beads.core.Bead
import net.beadsproject.beads.ugens.SamplePlayer
import java.util.concurrent.ConcurrentHashMap


class BeadsSound(val audio: BeadsAudio) : Sound {

    val players: MutableMap<Long, GainedSamplePlayer> = ConcurrentHashMap()

    private fun obtainPlayer(): Pair<Long, GainedSamplePlayer> {
        val id = BeadsSoundSystem.obtainSoundID()
        val result = id to GainedSamplePlayer(
                SamplePlayer(BeadsSoundSystem.audioContext, audio.sample).apply {
                    killOnEnd = true
                    killListener = object : Bead() {
                        override fun messageReceived(message: Bead?) {
                            if (message == this) {
                                players.remove(id)
                            }
                        }
                    }
                })

        players.put(result.first, result.second)

        return result
    }

    override fun play(loop: Boolean, pitch: Float, volume: Float): Long {
        val result = obtainPlayer()
        val player = result.second.player

        player.loopType = if (loop) SamplePlayer.LoopType.LOOP_FORWARDS else SamplePlayer.LoopType.NO_LOOP_FORWARDS

        result.second.gain.gain = volume
        result.second.pitch.value = pitch
        result.second.addToContext()

        return result.first
    }

    override fun setPitch(id: Long, pitch: Float) {
        val player = players[id] ?: return
        player.pitch.value = pitch
    }

    override fun setVolume(id: Long, vol: Float) {
        val player = players[id] ?: return
        player.gain.gain = vol
    }

    override fun stop(id: Long) {
        val player = players[id] ?: return
        players.remove(id)

        player.player.kill()
    }

    override fun dispose() {
        players.forEach { stop(it.key) }
    }
}