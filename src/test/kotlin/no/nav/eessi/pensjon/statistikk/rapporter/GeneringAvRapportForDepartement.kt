package no.nav.eessi.pensjon.statistikk.rapporter

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class GeneringAvRapportForDepartement {

    @Test
    @Disabled
    fun `read numbers from UFORE_SUBSET csv, include only lines where number has exactly one unique col2 in funnet_landkoder and save as csv`() {
        val tenList = this::class.java.getResourceAsStream("/funnet_landkoder.csv")
            ?.bufferedReader()
            ?.useLines { lines ->
                lines.map {
                    val parts = it.split(";").map { it.trim(' ', '"') }
                    val number = if (parts[0].length == 10) "0${parts[0]}" else parts[0]
                    val col2 = parts.getOrNull(1) ?: ""
                    val col3 = parts.getOrNull(2) ?: ""
                    val col4 = parts.getOrNull(3) ?: ""
                    listOf(number, col2, col3, col4)
                }.toList()
            } ?: emptyList()

        // Only keep numbers where there is exactly one unique col2 value
        val validNumbers = tenList
            .groupBy { it[0] }
            .filter { (_, rows) -> rows.map { it[1] }.distinct().size == 1 }
            .mapValues { it.value }
        val uniqueMap = validNumbers.mapValues { entry ->
            entry.value.distinctBy { listOf(it[1], it[2], it[3]) }
        }
        println("uniqueMap contents:")
        uniqueMap.forEach { (number, rows) ->
            println("Number: $number")
            rows.forEach { row ->
                println("  ${row.joinToString(", ")}")
            }
        }
        val inputStream = this::class.java.getResourceAsStream("/UFORE_SUBSET.csv")
            ?: throw IllegalArgumentException("File not found")
        val outputFile = File("result.csv")
        outputFile.bufferedWriter().use { writer ->
            BufferedReader(InputStreamReader(inputStream)).useLines { lines ->
                lines.forEach { line ->
                    val columns = line.split(",")
                    val number = columns.last().trim()
                    val matches = uniqueMap[number]
                    if (matches != null && matches.isNotEmpty()) {
                        matches.forEach { match ->
                            writer.write(line + "," + match[1] + "," + match[2] + "," + match[3])
                            writer.newLine()
                        }
                    }
                }
            }
        }
    }
}