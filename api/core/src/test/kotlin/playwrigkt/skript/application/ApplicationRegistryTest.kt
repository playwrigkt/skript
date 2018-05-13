package playwrigkt.skript.application

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.funktionale.tries.Try
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.stagemanager.StageManager
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

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
            val applicationLoader = SkriptApplicationLoader(mock(FileTroupe::class.java), mock(SerializeTroupe::class.java), registry)
            val appConfig = AppConfig(emptyList(), listOf(StageManagerLoaderConfig(name, emptyMap(), ConfigValue.Empty.Null)))
            val result = applicationLoader.buildApplication(appConfig)
            result.error() shouldBe ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.NotFound(name))
            result.result() shouldBe null
        }

        "A registry should build a stage manager for a manager it has a reference to" {
            val manager: StageManager<Any> = mock(StageManager::class.java) as StageManager<Any>
            val name = "myLoader"
            val dependencies = emptyList<String>()

            val registry = ApplicationRegistry()
            val applicationLoader = SkriptApplicationLoader(mock(FileTroupe::class.java), mock(SerializeTroupe::class.java), registry)

            val config = StageManagerLoaderConfig(name, emptyMap(), ConfigValue.Empty.Null)
            val appConfig = AppConfig(emptyList(), listOf(config))
            val loader = object: StageManagerLoader<Any> {
                override val dependencies: List<String> = dependencies
                override val name: String = name
                override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Any>, SkriptApplicationLoader> =
                        Skript.map { manager }
            }
            registry.register(loader) shouldBe Try.Success(Unit)

            val result = applicationLoader.buildApplication(appConfig)
            result.error() shouldBe null
            result.result() shouldBe SkriptApplication(mapOf(name to manager))
        }

        "A registry should build a stage manager for a manager and its dependencies" {
            val registry = ApplicationRegistry()
            val parentName = "parent"
            val child1Name = "child1"
            val child2Name = "child2"
            val grandChild1Name = "grandChild1"
            val grandChild2Name = "grandChild2"
            
            val parentManager = mock(StageManager::class.java) as StageManager<Any>
            val parentDependencies= listOf(child1Name, child2Name)
            val parentConfig = StageManagerLoaderConfig(parentName, emptyMap(), ConfigValue.Empty.Null)
            
            val child1Manager = mock(StageManager::class.java) as StageManager<Any>
            val child1Dependencies= listOf(grandChild1Name, grandChild2Name)
            val child1Config = StageManagerLoaderConfig(child1Name, emptyMap(), ConfigValue.Empty.Null)

            val child2Manager = mock(StageManager::class.java) as StageManager<Any>
            val child2Dependencies= emptyList<String>()
            val child2Config = StageManagerLoaderConfig(child2Name, emptyMap(), ConfigValue.Empty.Null)

            val grandChild1Manager = mock(StageManager::class.java) as StageManager<Any>
            val grandChild1Dependencies= emptyList<String>()
            val grandChild1Config = StageManagerLoaderConfig(grandChild1Name, emptyMap(), ConfigValue.Empty.Null)

            val grandChild2Manager = mock(StageManager::class.java) as StageManager<Any>
            val grandChild2Dependencies= emptyList<String>()
            val grandChild2Config = StageManagerLoaderConfig(grandChild2Name, emptyMap(), ConfigValue.Empty.Null)

            val parentLoader = object: StageManagerLoader<Any> {
                override val dependencies: List<String> = parentDependencies
                override val name: String = parentName
                override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Any>, SkriptApplicationLoader> =
                        Skript
                                .map {
                                    it.stageManagerLoaderConfig shouldBe parentConfig
                                    it.existingManagers shouldBe mapOf(child1Name to child1Manager, child2Name to child2Manager, grandChild1Name to grandChild1Manager, grandChild2Name to grandChild2Manager)
                                    parentManager
                                }
            }

            val child1Loader = object: StageManagerLoader<Any> {
                override val dependencies: List<String> = child1Dependencies
                override val name: String = child1Name

                override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Any>, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingManagers shouldBe mapOf(child2Name to child2Manager, grandChild1Name to grandChild1Manager, grandChild2Name to grandChild2Manager)
                            it.stageManagerLoaderConfig shouldBe child1Config
                            child1Manager
                        }
            }

            val child2Loader = object: StageManagerLoader<Any> {
                override val dependencies: List<String> = child2Dependencies
                override val name: String = child2Name
                override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Any>, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingManagers shouldBe emptyMap()
                            it.stageManagerLoaderConfig shouldBe child2Config
                            child2Manager
                        }

            }

            val grandChild1Loader = object: StageManagerLoader<Any> {
                override val dependencies: List<String> = grandChild1Dependencies
                override val name: String = grandChild1Name

                override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Any>, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingManagers shouldBe emptyMap()
                            it.stageManagerLoaderConfig shouldBe grandChild1Config
                            grandChild1Manager
                        }
            }

            val grandChild2Loader = object: StageManagerLoader<Any> {
                override val dependencies: List<String> = grandChild2Dependencies
                override val name: String = grandChild2Name
                override val loadManager: Skript<StageManagerLoader.Input, out StageManager<Any>, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingManagers shouldBe emptyMap()
                            it.stageManagerLoaderConfig shouldBe grandChild2Config
                            grandChild2Manager
                        }
            }

            registry.register(parentLoader) shouldBe Try.Success(Unit)
            registry.register(child1Loader) shouldBe Try.Success(Unit)
            registry.register(child2Loader) shouldBe Try.Success(Unit)
            registry.register(grandChild1Loader) shouldBe Try.Success(Unit)
            registry.register(grandChild2Loader) shouldBe Try.Success(Unit)

            val applicationLoader = SkriptApplicationLoader(mock(FileTroupe::class.java), mock(SerializeTroupe::class.java), registry)
            val appConfig = AppConfig(emptyList(), listOf(parentConfig, child1Config, child2Config, grandChild1Config, grandChild2Config))

            val result = applicationLoader.buildApplication(appConfig)
            result.error() shouldBe null
            result.result() shouldBe SkriptApplication(mapOf(
                    parentName to parentManager,
                    child1Name to child1Manager,
                    child2Name to child2Manager,
                    grandChild1Name to grandChild1Manager,
                    grandChild2Name to grandChild2Manager))
        }
    }
}