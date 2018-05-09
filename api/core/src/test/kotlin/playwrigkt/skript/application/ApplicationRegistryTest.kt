package playwrigkt.skript.application

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.funktionale.tries.Try
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

class ApplicationRegistryTest: StringSpec() {
    init {
        "A registry should register a loader" {
            val registry = ApplicationRegistry()
            val loader = mock(StageManagerLoader::class.java)
            val name = "myLoader"
            val dependencies = emptyList<String>()

            `when`(loader.name).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should register a loader and its dependencies" {
            val registry = ApplicationRegistry()
            val loader = mock(StageManagerLoader::class.java)
            val name = "myLoader"
            val dependencies = listOf("dep1", "dep2")

            `when`(loader.name).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should not register a loader with a duplicate name" {
            val registry = ApplicationRegistry()
            val name = "myLoader"
            val loader = mock(StageManagerLoader::class.java)
            val dependencies = listOf("dep1", "dep2")
            val duplicateLoader = mock(StageManagerLoader::class.java)
            val duplicateDependencies = listOf("dupe1", "dupe2")

            `when`(loader.name).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)
            `when`(duplicateLoader.name).thenReturn(name)
            `when`(duplicateLoader.dependencies).thenReturn(duplicateDependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            registry.register(duplicateLoader) shouldBe
                    Try.Failure(ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.DuplicateStageManagerLoader(name)))
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should fail to build a stage manager for a manager it has no reference to" {
            val registry = ApplicationRegistry()
            val name = "nosuch"
            val result = registry.buildStageManagers(listOf(StageManagerLoaderConfig(name, emptyMap(), ConfigValue.Empty.Null)))
            result.error() shouldBe ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.NotFound(name))
            result.result() shouldBe null
        }

        "A registry should build a stage manager for a manager it has a reference to" {
            val registry = ApplicationRegistry()
            val loader = mock(StageManagerLoader::class.java)
            val manager= mock(StageManager::class.java)
            val name = "myLoader"
            val dependencies = emptyList<String>()
            val config = StageManagerLoaderConfig(name, emptyMap(), ConfigValue.Empty.Null)
            `when`(loader.name).thenReturn(name)
            `when`(loader.dependencies).thenReturn(dependencies)

            registry.register(loader) shouldBe Try.Success(Unit)
            `when`(loader.loadManager(emptyMap(), config)).thenReturn(AsyncResult.succeeded(manager))

            val result = registry.buildStageManagers(listOf(config))
            result.error() shouldBe null
            result.result() shouldBe mapOf(name to manager)
        }

        "A registry should build a stage manager for a manager and its dependencies" {
            val registry = ApplicationRegistry()
            val parentName = "parent"
            val child1Name = "child1"
            val child2Name = "child2"
            val grandChild1Name = "grandChild1"
            val grandChild2Name = "grandChild2"

            val parentLoader = mock(StageManagerLoader::class.java)
            val parentManager = mock(StageManager::class.java)
            val parentDependencies= listOf(child1Name, child2Name)
            val parentConfig = StageManagerLoaderConfig(parentName, emptyMap(), ConfigValue.Empty.Null)

            val child1Loader = mock(StageManagerLoader::class.java)
            val child1Manager = mock(StageManager::class.java)
            val child1Dependencies= listOf(grandChild1Name, grandChild2Name)
            val child1Config = StageManagerLoaderConfig(child1Name, emptyMap(), ConfigValue.Empty.Null)

            val child2Loader = mock(StageManagerLoader::class.java)
            val child2Manager = mock(StageManager::class.java)
            val child2Dependencies= emptyList<String>()
            val child2Config = StageManagerLoaderConfig(child2Name, emptyMap(), ConfigValue.Empty.Null)

            val grandChild1Loader = mock(StageManagerLoader::class.java)
            val grandChild1Manager = mock(StageManager::class.java)
            val grandChild1Dependencies= emptyList<String>()
            val grandChild1Config = StageManagerLoaderConfig(grandChild1Name, emptyMap(), ConfigValue.Empty.Null)

            val grandChild2Loader = mock(StageManagerLoader::class.java)
            val grandChild2Manager = mock(StageManager::class.java)
            val grandChild2Dependencies= emptyList<String>()
            val grandChild2Config = StageManagerLoaderConfig(grandChild2Name, emptyMap(), ConfigValue.Empty.Null)

            `when`(parentLoader.name).thenReturn(parentName)
            `when`(parentLoader.dependencies).thenReturn(parentDependencies)
            `when`(child1Loader.name).thenReturn(child1Name)
            `when`(child1Loader.dependencies).thenReturn(child1Dependencies)
            `when`(child2Loader.name).thenReturn(child2Name)
            `when`(child2Loader.dependencies).thenReturn(child2Dependencies)
            `when`(grandChild1Loader.name).thenReturn(grandChild1Name)
            `when`(grandChild1Loader.dependencies).thenReturn(grandChild1Dependencies)
            `when`(grandChild2Loader.name).thenReturn(grandChild2Name)
            `when`(grandChild2Loader.dependencies).thenReturn(grandChild2Dependencies)

            registry.register(parentLoader) shouldBe Try.Success(Unit)
            registry.register(child1Loader) shouldBe Try.Success(Unit)
            registry.register(child2Loader) shouldBe Try.Success(Unit)
            registry.register(grandChild1Loader) shouldBe Try.Success(Unit)
            registry.register(grandChild2Loader) shouldBe Try.Success(Unit)

            `when`(grandChild1Loader.loadManager(emptyMap(), grandChild1Config)).thenReturn(AsyncResult.succeeded(grandChild1Manager))
            `when`(grandChild2Loader.loadManager(emptyMap(), grandChild2Config)).thenReturn(AsyncResult.succeeded(grandChild2Manager))
            `when`(child2Loader.loadManager(emptyMap(), child2Config)).thenReturn(AsyncResult.succeeded(child2Manager))
            `when`(child1Loader.loadManager(mapOf(child2Name to child2Manager, grandChild1Name to grandChild1Manager, grandChild2Name to grandChild2Manager), child1Config)).thenReturn(AsyncResult.succeeded(child1Manager))
            `when`(parentLoader.loadManager(mapOf(child1Name to child1Manager, child2Name to child2Manager, grandChild1Name to grandChild1Manager, grandChild2Name to grandChild2Manager), parentConfig)).thenReturn(AsyncResult.succeeded(parentManager))

            val result = registry.buildStageManagers(listOf(parentConfig, child1Config, child2Config, grandChild1Config, grandChild2Config))
            result.error() shouldBe null
            result.result() shouldBe mapOf(
                    parentName to parentManager,
                    child1Name to child1Manager,
                    child2Name to child2Manager,
                    grandChild1Name to grandChild1Manager,
                    grandChild2Name to grandChild2Manager)
        }
    }
}