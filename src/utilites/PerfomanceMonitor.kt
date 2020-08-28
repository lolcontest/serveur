package utilites

import com.sun.management.OperatingSystemMXBean
import java.lang.management.ManagementFactory.getOperatingSystemMXBean
import kotlin.math.round

object PerformanceMonitor {
    private val availableProcessors: Int = getOperatingSystemMXBean().availableProcessors
    private var lastSystemTime: Long = 0
    private var lastProcessCpuTime: Long = 0

    @get:Synchronized
    val cpuUsage: Double
        get() {
            if (lastSystemTime == 0L) {
                baselineCounters()
                return 0.0
            }
            val systemTime = System.nanoTime()
            var processCpuTime: Long = 0
            if (getOperatingSystemMXBean() is OperatingSystemMXBean) {
                processCpuTime = (getOperatingSystemMXBean() as OperatingSystemMXBean).processCpuTime
            }
            val cpuUsage = (processCpuTime - lastProcessCpuTime).toDouble() / (systemTime - lastSystemTime)
            lastSystemTime = systemTime
            lastProcessCpuTime = processCpuTime
            return (cpuUsage / availableProcessors).round(3)
        }

    fun Double.round(decimals: Int): Double {
        var multiplier = 1.0
        repeat(decimals) { multiplier *= 10 }
        return round(this * multiplier) / multiplier
    }

    private fun baselineCounters() {
        lastSystemTime = System.nanoTime()
        if (getOperatingSystemMXBean() is OperatingSystemMXBean) {
            lastProcessCpuTime = (getOperatingSystemMXBean() as OperatingSystemMXBean).processCpuTime
        }
    }
}