package no.nav.eessi.pensjon.statistikk.rapporter

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.io.File
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Disabled("Kun for engangsbruk ved generering av rapport for departementet")
class GeneringAvRapportForDepartement {
    val dateFormatter = DateTimeFormatter.ofPattern("M/d/yy H:mm")
    val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    @Test
    fun `read numbers from UFORE_SUBSET csv, include only lines where number has exactly one unique col2 in funnet_landkoder and save as csv`() {

        // henter alle rader fra landkoder og grupperer på kolonne 5 (landkode)
        val tenListMap: Map<String, List<List<String>>> = this::class.java.getResourceAsStream("/landkoder_take3_1.csv")
            ?.bufferedReader()
            ?.useLines { lines ->
                lines.map { it.split(",").map { it.trim(' ', '"') }.toMutableList() }
                    .filter { it.size >= 7 }
                    .onEach { row ->
                        try {
                            val parsed = LocalDateTime.parse(row[1], dateFormatter)
                            row[1] = parsed.format(outputFormatter)
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                    .filter { row -> row[5].all { it.isDigit() } }
                    .groupBy { it[5] }
            } ?: emptyMap()

        // henter alle rader fra UFORE_SUBSET og grupperer på kolonne 5 (landkode)
        val mapList: Map<String, List<List<String>>> = this::class.java.getResourceAsStream("/UFORE_SUBSET.csv")
            ?.bufferedReader()
            ?.useLines { lines ->
                lines.map { it.split(",").map { it.trim(' ', '"') }.toMutableList() }
                    .filter { it.size >= 5 }
                    .onEach { row ->
                        try {
                            val parsed = YearMonth.parse(row[0], DateTimeFormatter.ofPattern("yyyyMM"))
                            val outputDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM")
                            row[0] = parsed.atDay(1).format(outputDateFormatter)
                        } catch (e: Exception) {
                            println(e)
                        }
                    }
                    .groupBy { it[5] }
            } ?: emptyMap()

        val tenListKeys = tenListMap.keys
        val mapListKeys = mapList.keys
        val matchingKeys = tenListKeys.intersect(mapListKeys)

        println("Number of matching keys: ${matchingKeys.size}")
        val joinedRows = mutableListOf<List<String>>()

        for (key in tenListMap.keys.intersect(mapList.keys)) {
            val tenRows = tenListMap[key] ?: emptyList()
            val mapRows = mapList[key] ?: emptyList()
            for (tenRow in tenRows) {
                for (mapRow in mapRows) {
                    val condition1 = tenRow[6] == "2" && mapRow[1] == "AVSL"
                    val condition2 = tenRow[6] in listOf("1", "3", "4") && mapRow[1] == "INNV"
                    val condition3 = tenRow[4] == "RECEIVED"

                    if (condition1 || condition2 || condition3) {
                        val newRow = tenRow + mapRow.dropLast(2)
                        if (!joinedRows.contains(newRow)) {
                            joinedRows.add(newRow)
                            break // går ut av indre løkke når en match er funnet
                        }
                    }
                }
            }
        }

        File("joined_result.csv").bufferedWriter().use { writer ->
            joinedRows.forEach { row ->
                writer.write(row.joinToString(","))
                writer.newLine()
            }
        }

        joinedRows.take(20).forEach { println(it) }
    }
}