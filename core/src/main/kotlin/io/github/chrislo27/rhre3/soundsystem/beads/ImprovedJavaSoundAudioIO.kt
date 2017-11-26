package io.github.chrislo27.rhre3.soundsystem.beads

import net.beadsproject.beads.core.io.JavaSoundAudioIO
import kotlin.concurrent.thread

/**
 * The default JavaSoundAudioIO thread is non-daemon. This class uses reflection to make it a daemon thread.
 */
class ImprovedJavaSoundAudioIO : JavaSoundAudioIO() {

    override fun start(): Boolean {
        fun runRealTime() {
            JavaSoundAudioIO::class.java.getDeclaredMethod("runRealTime").apply {
                isAccessible = true
                invoke(this@ImprovedJavaSoundAudioIO)
            }
        }

        val audioThread = thread(start = false, isDaemon = true) {
            create()
            runRealTime()
            destroy()
        }
        audioThread.priority = threadPriority
        audioThread.start()

        JavaSoundAudioIO::class.java.getDeclaredField("audioThread").apply {
            isAccessible = true
            set(this@ImprovedJavaSoundAudioIO, audioThread)
        }

        return true
    }

}