package no.nav.eessi.pensjon

import no.nav.eessi.pensjon.eux.BucMetadata
import no.nav.eessi.pensjon.eux.Sed
import no.nav.eessi.pensjon.json.mapJsonToAny
import no.nav.eessi.pensjon.json.typeRefs
import no.nav.eessi.pensjon.statistikk.listener.SedHendelseRina

class ResourceHelper {
    companion object {
        fun getResourceBucMetadata(resourcePath: String): BucMetadata {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json, typeRefs())
        }

        fun getResourceSed(resourcePath: String) : Sed {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json, typeRefs())
        }

        fun getResourceSedHendelseRina(resourcePath: String) : SedHendelseRina {
            val json = this::class.java.classLoader.getResource(resourcePath)!!.readText()
            return mapJsonToAny(json, typeRefs())
        }
    }
}