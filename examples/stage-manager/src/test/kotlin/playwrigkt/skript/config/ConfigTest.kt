package playwrigkt.skript.config

import io.kotlintest.fail
import io.kotlintest.shouldBe
import io.kotlintest.specs.AbstractBehaviorSpec
import io.kotlintest.specs.StringSpec
import org.funktionale.tries.Try
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import playwrigkt.skript.result.AsyncResult
import playwrigkt.skript.stagemanager.StageManager

class ApplicationRegistryTest: StringSpec() {
    init {
        "A registry should register a loader" {
            val registry = ApplicationRegistry()
            val loader = mock(StageManagerLoader::class.java)
            val name = "myLoader"
            val dependencies = emptyList<String>()

            registry.register(name, loader, dependencies) shouldBe Try.Success(Unit)
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should register a loader and its dependencies" {
            val registry = ApplicationRegistry()
            val loader = mock(StageManagerLoader::class.java)
            val name = "myLoader"
            val dependencies = listOf("dep1", "dep2")

            registry.register(name, loader, dependencies) shouldBe Try.Success(Unit)
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

            registry.register(name, loader, dependencies) shouldBe Try.Success(Unit)
            registry.register(name, duplicateLoader, duplicateDependencies) shouldBe
                    Try.Failure(ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.DuplicateStageManagerLoader(name)))
            registry.getDependencies(name) shouldBe Try.Success(dependencies)
            registry.getLoader(name) shouldBe Try.Success(loader)
        }

        "A registry should fail to build a stage manager for a manager it has no reference to" {
            val registry = ApplicationRegistry()
            val name = "nosuch"
            val result = registry.buildStageManagers(listOf(StageManagerLoaderConfig(name, emptyMap(), ConfigValue.Empty)))
            result.error() shouldBe ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.NotFound(name))
            result.result() shouldBe null
        }

        "A registry should build a stage manager for a manager it has a reference to" {
            val registry = ApplicationRegistry()
            val loader = mock(StageManagerLoader::class.java)
            val manager= mock(StageManager::class.java)
            val name = "myLoader"
            val dependencies = emptyList<String>()
            val config = StageManagerLoaderConfig(name, emptyMap(), ConfigValue.Empty)
            registry.register(name, loader, dependencies) shouldBe Try.Success(Unit)
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
            val parentConfig = StageManagerLoaderConfig(parentName, emptyMap(), ConfigValue.Empty)

            val child1Loader = mock(StageManagerLoader::class.java)
            val child1Manager = mock(StageManager::class.java)
            val child1Dependencies= listOf(grandChild1Name, grandChild2Name)
            val child1Config = StageManagerLoaderConfig(child1Name, emptyMap(), ConfigValue.Empty)

            val child2Loader = mock(StageManagerLoader::class.java)
            val child2Manager = mock(StageManager::class.java)
            val child2Dependencies= emptyList<String>()
            val child2Config = StageManagerLoaderConfig(child2Name, emptyMap(), ConfigValue.Empty)

            val grandChild1Loader = mock(StageManagerLoader::class.java)
            val grandChild1Manager = mock(StageManager::class.java)
            val grandChild1Dependencies= emptyList<String>()
            val grandChild1Config = StageManagerLoaderConfig(grandChild1Name, emptyMap(), ConfigValue.Empty)

            val grandChild2Loader = mock(StageManagerLoader::class.java)
            val grandChild2Manager = mock(StageManager::class.java)
            val grandChild2Dependencies= emptyList<String>()
            val grandChild2Config = StageManagerLoaderConfig(grandChild2Name, emptyMap(), ConfigValue.Empty)
            
            registry.register(parentName, parentLoader, parentDependencies) shouldBe Try.Success(Unit)
            registry.register(child1Name, child1Loader, child1Dependencies) shouldBe Try.Success(Unit)
            registry.register(child2Name, child2Loader, child2Dependencies) shouldBe Try.Success(Unit)
            registry.register(grandChild1Name, grandChild1Loader, grandChild1Dependencies) shouldBe Try.Success(Unit)
            registry.register(grandChild2Name, grandChild2Loader, grandChild2Dependencies) shouldBe Try.Success(Unit)
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