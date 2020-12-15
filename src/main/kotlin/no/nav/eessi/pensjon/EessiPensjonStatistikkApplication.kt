package no.nav.eessi.pensjon

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class EessiPensjonStatistikkApplication

fun main(args: Array<String>) {
	runApplication<EessiPensjonStatistikkApplication>(*args)
}
