package no.nav.eessi.pensjon

import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.eux.model.SedHendelse
import no.nav.eessi.pensjon.eux.model.buc.BucMetadata
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

        fun getResourceSedHendelseRina(resourcePath: String) : SedHendelse {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json)
        }
    }
}