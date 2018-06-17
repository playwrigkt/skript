package playwrigkt.skript.application

import arrow.core.Try
import io.kotlintest.matchers.contain
import io.kotlintest.matchers.should
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.mockito.Mockito.mock
import playwrigkt.skript.Skript
import playwrigkt.skript.config.ConfigValue

class SkriptApplicationTest: StringSpec() {
    init {

        "A skriptApplication can find resources that have no dependencies" {
            val registry = ApplicationRegistry()
            val parentName = "parent"
            val child1Name = "child1"
            val child1Implementation = "child1Implementation"
            val child2Name = "child2"
            val grandChild1Name = "grandChild1"
            val grandChild2Name = "grandChild2"

            val parentResource = mock(ApplicationResource::class.java)
            val parentDependencies= listOf(child1Name, child2Name)
            val parentConfig = ApplicationResourceLoaderConfig(parentName, mapOf(child1Name to child1Implementation), ConfigValue.Empty.Null)

            val child1Resource = mock(ApplicationResource::class.java)
            val child1Dependencies= listOf(grandChild1Name, grandChild2Name)
            val child1Config = ApplicationResourceLoaderConfig(child1Implementation, emptyMap(), ConfigValue.Empty.Null)

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
                override fun name(): String = child1Implementation

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
                            it.existingApplicationResources shouldBe emptyMap()
                            it.applicationResourceLoaderConfig shouldBe child2Config
                            child2Resource
                        }

            }

            val grandChild1Loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = grandChild1Dependencies
                override fun name(): String = grandChild1Name

                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingApplicationResources shouldBe emptyMap()
                            it.applicationResourceLoaderConfig shouldBe grandChild1Config
                            grandChild1Resource
                        }
            }

            val grandChild2Loader = object: ApplicationResourceLoader<ApplicationResource> {
                override val dependencies: List<String> = grandChild2Dependencies
                override fun name(): String = grandChild2Name
                override val loadResource: Skript<ApplicationResourceLoader.Input, ApplicationResource, SkriptApplicationLoader> =
                        Skript.map {
                            it.existingApplicationResources shouldBe emptyMap()
                            it.applicationResourceLoaderConfig shouldBe grandChild2Config
                            grandChild2Resource
                        }
            }

            registry.register(parentLoader) shouldBe Try.Success(Unit)
            registry.register(child1Loader) shouldBe Try.Success(Unit)
            registry.register(child2Loader) shouldBe Try.Success(Unit)
            registry.register(grandChild1Loader) shouldBe Try.Success(Unit)
            registry.register(grandChild2Loader) shouldBe Try.Success(Unit)

            val appConfig = AppConfig(emptyList(), listOf(parentConfig, child1Config, child2Config, grandChild1Config, grandChild2Config))

            val skriptApplication = SkriptApplication(
                    mapOf(
                            parentName to parentResource,
                            child1Implementation to child1Resource,
                            child2Name to child2Resource,
                            grandChild1Name to grandChild1Resource,
                            grandChild2Name to grandChild2Resource),
                    appConfig,
                    registry
            )
            val dependenciesMap = skriptApplication.dependencyMap(appConfig.applicationResourceLoaders)

            skriptApplication.hasNoDependents(dependenciesMap) shouldBe listOf(parentName)

            val secondRoundDependencies = skriptApplication.dependencyMap(appConfig.applicationResourceLoaders.filterNot { it.name == parentName })
            skriptApplication.hasNoDependents(secondRoundDependencies) should contain(child1Implementation)
            skriptApplication.hasNoDependents(secondRoundDependencies) should contain(child2Name)
            skriptApplication.hasNoDependents(secondRoundDependencies).size shouldBe 2

        }
    }
}