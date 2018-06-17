package playwrigkt.skript.application

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import arrow.core.Try
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class ApplicationRegistryTest: StringSpec() {
    init {
        "A registry should register a loader" {
            val registry = ApplicationRegistry()
            val loader = mock(ApplicationResourceLoader::class.java)
            val name = "myLoader"
            val dependencies = emptyList<String>()

            `when`(loader.name()).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should register a loader and its dependencies" {
            val registry = ApplicationRegistry()
            val loader = mock(ApplicationResourceLoader::class.java)
            val name = "myLoader"
            val dependencies = listOf("dep1", "dep2")

            `when`(loader.name()).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should not register a loader with a duplicate name" {
            val registry = ApplicationRegistry()
            val name = "myLoader"
            val loader = mock(ApplicationResourceLoader::class.java)
            val dependencies = listOf("dep1", "dep2")
            val duplicateLoader = mock(ApplicationResourceLoader::class.java)
            val duplicateDependencies = listOf("dupe1", "dupe2")

            `when`(loader.name()).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)
            `when`(duplicateLoader.name()).thenReturn(name)
            `when`(duplicateLoader.dependencies).thenReturn(duplicateDependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            registry.register(duplicateLoader) shouldBe
                    Try.Failure(ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.DuplicateStageManagerLoader(name)))
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }
    }
}