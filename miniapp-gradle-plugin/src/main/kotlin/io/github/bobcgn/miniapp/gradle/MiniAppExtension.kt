package io.github.bobcgn.miniapp.gradle

import org.gradle.api.Action
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * `miniapp { }` — Mini App platform build integration.
 *
 * The platform owns the Kotlin sources and the runtime SDK the consumer compiles against. A host is
 * the runtime that loads the bundle, and it is configured under this extension rather than beside
 * it, so the hierarchy reads Mini App Platform → Host → WeChat and stays that way when a second host
 * exists:
 *
 * ```kotlin
 * miniapp {
 *     wechat {
 *         bundleDirectory.set(layout.buildDirectory.dir("miniapp/bundle"))
 *     }
 * }
 * ```
 *
 * Declaring a host is optional. Applying the plugin already targets the current host, so a consumer
 * that configures nothing keeps working; the block exists to name the host explicitly and to change
 * what genuinely belongs to the build.
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
     * The current host.
     *
     * One property per host rather than a string map: a host's build configuration is typed, and a
     * consumer should not be able to name a host that no code implements.
     */
    public val wechat: WeChatHostConfiguration =
        objects.newInstance(WeChatHostConfiguration::class.java).apply {
            bundleDirectory.convention(layout.buildDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_BUNDLE_DIRECTORY))
        }

    /** Configures the current host. */
    public fun wechat(action: Action<in WeChatHostConfiguration>): Unit = action.execute(wechat)

    public companion object {
        /** The extension's name, and therefore the `miniapp { }` accessor. */
        public const val NAME: String = "miniapp"
    }
}
