package com.example.groovemaster.audio
import kotlin.math.log2
import kotlin.math.pow

object Tuning {
    const val C4 = 261.6255653
    val ET_MAJOR = listOf(0.0,200.0,400.0,500.0,700.0,900.0,1100.0,1200.0)
    val JI_MAJOR = listOf(0.0,203.91,386.31,498.04,701.96,884.36,1088.27,1200.0)
    val PYTHAGOREAN_MAJOR = listOf(0.0,203.91,407.82,498.04,701.96,905.87,1109.78,1200.0)
    val MEANTONE_MAJOR = listOf(0.0,193.16,386.31,503.42,696.58,889.74,1082.89,1200.0)
    val WERCKMEISTER_MAJOR = listOf(0.0,192.18,390.23,498.05,696.09,888.27,1092.18,1200.0)
    val RAST = listOf(0.0,200.0,350.0,500.0,700.0,900.0,950.0,1200.0)
    val NAHAWAND = listOf(0.0,200.0,300.0,500.0,700.0,800.0,1000.0,1200.0)
    val SLENDRO = listOf(0.0,238.0,478.0,722.0,962.0,1200.0)
    val PELOG = listOf(0.0,125.0,290.0,500.0,650.0,835.0,1010.0,1200.0)
    val SHRUTIS: List<Pair<String, Double>> = listOf(
        "Sa" to 0.0,"re (low)" to 90.0,"re (high)" to 112.0,"Re (low)" to 182.0,"Re (high)" to 204.0,
        "ga (low)" to 294.0,"ga (high)" to 316.0,"Ga" to 386.0,"Ga (high)" to 408.0,
        "ma" to 498.0,"ma (high)" to 520.0,"Ma (low)" to 590.0,"Ma (high)" to 612.0,
        "Pa" to 702.0,"dha (low)" to 792.0,"dha (high)" to 814.0,
        "Dha (low)" to 884.0,"Dha (high)" to 906.0,
        "ni (low)" to 996.0,"ni (high)" to 1018.0,"Ni" to 1088.0,"Ni (high)" to 1110.0)
    fun freq(centsFromC4: Double) = C4 * 2.0.pow(centsFromC4 / 1200.0)
    fun centsBetween(f1: Double, f2: Double) = 1200.0 * log2(f2 / f1)
}
