package no.nav.eessi.pensjon.statistikk.architecture

import com.tngtech.archunit.core.importer.ClassFileImporter
import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.core.importer.ImportOptions
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods
import com.tngtech.archunit.library.Architectures.layeredArchitecture
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices
import no.nav.eessi.pensjon.EessiPensjonStatistikkApplication
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ArchitectureTest {

    private val root = EessiPensjonStatistikkApplication::class.qualifiedName!!
        .replace("." + EessiPensjonStatistikkApplication::class.simpleName, "")

    private val classesToAnalyze = ClassFileImporter()
        .importClasspath(
            ImportOptions()
                .with(ImportOption.DoNotIncludeJars())
                .with(ImportOption.DoNotIncludeArchives())
                .with(ImportOption.DoNotIncludeTests())
        )

    @BeforeAll
    fun beforeAll() {
        // Validate number of classes to analyze
        assertTrue(classesToAnalyze.size > 50, "Sanity check on no. of classes to analyze")
        assertTrue(classesToAnalyze.size < 800, "Sanity check on no. of classes to analyze")
    }

    @Test

    fun `Packages should not have cyclic depenedencies`() {
        slices().matching("$root.(*)..").should().beFreeOfCycles().check(classesToAnalyze)
    }


    @Test
    fun `Services should not depend on eachother`() {
        slices().matching("..$root.services.(**)").should().notDependOnEachOther().check(classesToAnalyze)
    }

    @Test
    fun `Check architecture`() {
        val ROOT = "statistikk"
        val Config = "statistikk.Config"
        val Health = "statistikk.Health"
        val Listeners = "statistikk.listener"
        val JSON = "statistikk.json"
        val Services = "statistikk.services"

        layeredArchitecture()
            //Define components
            .layer(ROOT).definedBy(root)
            .layer(Config).definedBy("$root.config")
            .layer(Health).definedBy("$root.health")
            .layer(JSON).definedBy("$root.json")
            .layer(Listeners).definedBy("$root.statistikk.listener")
            .layer(Services).definedBy("$root.statistikk.services")

            //define rules
            .whereLayer(ROOT).mayNotBeAccessedByAnyLayer()
            .whereLayer(Health).mayNotBeAccessedByAnyLayer()
            .whereLayer(Listeners).mayNotBeAccessedByAnyLayer()
            //Verify rules
            .check(classesToAnalyze)
    }

    @Test
    fun `avoid JUnit4-classes`() {
        val junitReason = "We use JUnit5 (but had to include JUnit4 because spring-kafka-test needs it to compile)"

        noClasses()
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                "org.junit",
                "org.junit.runners",
                "org.junit.experimental..",
                "org.junit.function",
                "org.junit.matchers",
                "org.junit.rules",
                "org.junit.runner..",
                "org.junit.validator",
                "junit.framework.."
            ).because(junitReason)
            .check(classesToAnalyze)

        noClasses()
            .should()
            .beAnnotatedWith("org.junit.runner.RunWith")
            .because(junitReason)
            .check(classesToAnalyze)

        noMethods()
            .should()
            .beAnnotatedWith("org.junit.Test")
            .orShould().beAnnotatedWith("org.junit.Ignore")
            .because(junitReason)
            .check(classesToAnalyze)
    }
}
