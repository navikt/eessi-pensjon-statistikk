package no.nav.eessi.pensjon

import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.statistikk.listener.SedHendelseRina
import no.nav.eessi.pensjon.utils.mapJsonToAny

class ResourceHelper {
    companion object {
        fun getResourceBucMetadata(resourcePath: String): BucMetadata {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json)
        }

        fun getResourceSed(resourcePath: String) : Sed {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json)
        }

        fun getResourceSedHendelseRina(resourcePath: String) : SedHendelseRina {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json)
        }
    }
}