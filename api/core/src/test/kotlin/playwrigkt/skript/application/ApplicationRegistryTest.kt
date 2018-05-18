package playwrigkt.skript.application

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.funktionale.tries.Try
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue
import playwrigkt.skript.troupe.FileTroupe
import playwrigkt.skript.troupe.SerializeTroupe

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

        //TODO move tests below here to ApplicationLoaderSpec
        "A registry should fail to build a stage manager for a manager it has no reference to" {
            val registry = ApplicationRegistry()
            val name = "nosuch"
            val applicationLoader = SkriptApplicationLoader(mock(FileTroupe::class.java), mock(SerializeTroupe::class.java), registry)
            val appConfig = AppConfig(emptyList(), listOf(ApplicationResourceLoaderConfig(name, emptyMap(), ConfigValue.Empty.Null)))
            val result = applicationLoader.buildApplication(appConfig)
            result.error() shouldBe ApplicationRegistry.RegistryException(ApplicationRegistry.RegistryError.NotFound(name))
            result.result() shouldBe null
        }

        "A registry should build a stage manager for a manager it has a reference to" {
            val resource = mock(ApplicationResource::class.java)
            val name = "myLoader"
            val dependencies = emptyList<String>()

            val registry = ApplicationRegistry()
            val applicationLoader = SkriptApplicationLoader(mock(FileTroupe::class.java), mock(SerializeTroupe::class.java), registry)

            val config = ApplicationResourceLoaderConfig(name, emptyMap(), ConfigValue.Empty.Null)
            val appConfig = AppConfig(emptyList(), listOf(config))
            val loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = dependencies
                override fun name(): String = name
                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map { resource }
            }
            registry.register(loader) shouldBe Try.Success(Unit)

            val result = applicationLoader.buildApplication(appConfig)
            result.error() shouldBe null
            result.result() shouldBe SkriptApplication(mapOf(name to resource))
        }

        "A registry should build a stage manager for a manager and its dependencies" {
            val registry = ApplicationRegistry()
            val parentName = "parent"
            val child1Name = "child1"
            val child2Name = "child2"
            val grandChild1Name = "grandChild1"
            val grandChild2Name = "grandChild2"
            
            val parentResource = mock(ApplicationResource::class.java)
            val parentDependencies= listOf(child1Name, child2Name)
            val parentConfig = ApplicationResourceLoaderConfig(parentName, emptyMap(), ConfigValue.Empty.Null)
            
            val child1Resource = mock(ApplicationResource::class.java)
            val child1Dependencies= listOf(grandChild1Name, grandChild2Name)
            val child1Config = ApplicationResourceLoaderConfig(child1Name, emptyMap(), ConfigValue.Empty.Null)

            val child2Resource = mock(ApplicationResource::class.java)
            val child2Dependencies= emptyList<String>()
            val child2Config = ApplicationResourceLoaderConfig(child2Name, emptyMap(), ConfigValue.Empty.Null)

            val grandChild1Resource = mock(ApplicationResource::class.java)
            val grandChild1Dependencies= emptyList<String>()
            val grandChild1Config = ApplicationResourceLoaderConfig(grandChild1Name, emptyMap(), ConfigValue.Empty.Null)

            val grandChild2Resource = mock(ApplicationResource::class.java)
            val grandChild2Dependencies= emptyList<String>()
            val grandChild2Config = ApplicationResourceLoaderConfig(grandChild2Name, emptyMap(), ConfigValue.Empty.Null)

            val parentLoader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = parentDependencies
                override fun name(): String = parentName
                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript
                                .map {
                                    it.applicationResourceLoaderConfig shouldBe parentConfig
                                    it.existingApplicationResources shouldBe mapOf(child1Name to child1Resource, child2Name to child2Resource, grandChild1Name to grandChild1Resource, grandChild2Name to grandChild2Resource)
                                    parentResource
                                }
            }

            val child1Loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = child1Dependencies
                override fun name(): String = child1Name

                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingApplicationResources shouldBe mapOf(child2Name to child2Resource, grandChild1Name to grandChild1Resource, grandChild2Name to grandChild2Resource)
                            it.applicationResourceLoaderConfig shouldBe child1Config
                            child1Resource
                        }
            }

            val child2Loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = child2Dependencies
                override fun name(): String = child2Name
                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingApplicationResources shouldBe emptyMap<String, ApplicationResource>()
                            it.applicationResourceLoaderConfig shouldBe child2Config
                            child2Resource
                        }

            }

            val grandChild1Loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = grandChild1Dependencies
                override fun name(): String = grandChild1Name

                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingApplicationResources shouldBe emptyMap<String, ApplicationResource>()
                            it.applicationResourceLoaderConfig shouldBe grandChild1Config
                            grandChild1Resource
                        }
            }

            val grandChild2Loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = grandChild2Dependencies
                override fun name(): String = grandChild2Name
                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingApplicationResources shouldBe emptyMap<String, ApplicationResource>()
                            it.applicationResourceLoaderConfig shouldBe grandChild2Config
                            grandChild2Resource
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
                    parentName to parentResource,
                    child1Name to child1Resource,
                    child2Name to child2Resource,
                    grandChild1Name to grandChild1Resource,
                    grandChild2Name to grandChild2Resource))
        }
    }
}