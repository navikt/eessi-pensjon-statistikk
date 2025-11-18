package no.nav.eessi.pensjon.statistikk.rapporter

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.sequences.forEach

class GeneringAvRapportForDepartement {

    @Test
    @Disabled
    fun `read numbers from UFORE_SUBSET csv, include only lines where number has exactly one unique col2 in funnet_landkoder and save as csv`() {
        val tenList = this::class.java.getResourceAsStream("/funnet_landkoder.csv")
            ?.bufferedReader()
            ?.useLines { lines ->
                lines.map { line ->
                    val parts = line.split(";").map { it.trim(' ', '"') }
                    parts + List(4 - parts.size) { "" }
                }.map { it.take(4) }.toList()
            } ?: emptyList()

        val validNumbers = tenList
            .groupBy { it[0] }
            .filterValues { rows -> rows.map { it[1] }.distinct().size == 1 }

        val uniqueMap: Map<String, List<List<String>>> = validNumbers.mapValues { (_, rows) ->
            rows.distinct()
        }
        val inputStream = this::class.java.getResourceAsStream("/UFORE_SUBSET.csv") ?: throw IllegalArgumentException("File not found")

        var counter  = 0
        File("result.csv").bufferedWriter().use { writer ->
            inputStream.bufferedReader().useLines { lines ->
                lines.forEach { line ->
                    val columns = line.split(",")
                    val number = columns.last().trim()
                    uniqueMap[number]?.forEach { match ->
                        val resultLine = (line + "," + match.subList(1, 4).joinToString(",")).replace(",1,", ",").replace(",,", ",")
                        println(resultLine)
                        writer.write(resultLine)
                        writer.newLine()
                        counter++
                    }
                }
            }
        }
        println(counter)
    }
}