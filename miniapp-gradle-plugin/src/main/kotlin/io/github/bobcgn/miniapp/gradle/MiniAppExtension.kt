package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.provider.Property
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * `miniapp { }` — Mini App platform build integration.
 *
 * The platform owns the Kotlin sources and the runtime SDK the consumer compiles against. A host is
 * the runtime that loads the result, and it is configured under this extension rather than beside
 * it, so the hierarchy reads Mini App Platform → Host → WeChat and stays that way when a second host
 * exists:
 *
 * ```kotlin
 * miniapp {
 *     wechat {
 *         bundleDirectory.set(layout.buildDirectory.dir("miniapp/bundle"))
 *         sourceDirectory.set(layout.projectDirectory.dir("miniApp"))
 *         generatedLibraryDirectory.set(
 *             sourceDirectory.dir("miniprogram/libs"),
 *         )
 *         distributionDirectory.set(layout.buildDirectory.dir("miniapp-dist"))
 *     }
 * }
 * ```
 *
 * Every one of those has a default, so the block is optional twice over: applying the plugin already
 * targets the current host, and a consumer that configures nothing gets the documented layout.
 *
 * Only producer selection and build paths are configurable. Declaring a host is not a business configuration surface, and
 * nothing about the mini program itself — its identifier, its pages, its capabilities, its markup —
 * is reachable from here.
 *
 * Deliberately absent, because Gradle is not where they belong: application identifiers and secrets,
 * merchant and payment material, order or signature data, API tokens, user identity, subscription
 * template identifiers, permission state, presentation state, page routes, markup, view definitions,
 * and any other business or backend configuration.
 */
public abstract class MiniAppExtension @Inject constructor(
    objects: ObjectFactory,
    layout: ProjectLayout,
) {

    /**
     * Explicit existing KMP project to compile when this plugin is applied to the build root.
     *
     * Unset for the existing module-local workflow. A root coordinator selects one subproject,
     * applies the Mini App plugin there and exposes its host tasks at the root. Names are arbitrary;
     * no project, source migration or UI dependency exclusion is inferred or created.
     * The root declares the Kotlin Multiplatform plugin with apply false to share its classpath.
     */
    public abstract val logicProject: Property<Project>

    /**
     * The current host.
     *
     * One property per host rather than a string map: a host's build configuration is typed, and a
     * consumer should not be able to name a host that no code implements.
     */
    public val wechat: WeChatHostConfiguration =
        objects.newInstance(WeChatHostConfiguration::class.java).apply {
            rootHost.convention(false)
            bundleDirectory.convention(layout.buildDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_BUNDLE_DIRECTORY))
            sourceDirectory.convention(layout.projectDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_SOURCE_DIRECTORY))
            distributionDirectory.convention(
                layout.buildDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_DISTRIBUTION_DIRECTORY),
            )
            // Derived from the source host's *convention* rather than from its current value, so a
            // consumer that moves the source host moves the generated area with it unless it says
            // otherwise.
            generatedLibraryDirectory.convention(
                sourceDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_GENERATED_LIBRARY_DIRECTORY),
            )
        }

    /** Configures the current host. */
    public fun wechat(action: Action<in WeChatHostConfiguration>): Unit = action.execute(wechat)

    public companion object {
        /** The extension's name, and therefore the `miniapp { }` accessor. */
        public const val NAME: String = "miniapp"
    }
}
