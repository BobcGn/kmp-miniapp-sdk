package io.github.bobcgn.miniapp.gradle

import org.gradle.api.file.DirectoryProperty

/**
 * Build configuration for one Mini App host.
 *
 * A Mini App platform is not a host: the platform is what the Kotlin sources and the runtime SDK are
 * written against, and a host is the runtime that loads the resulting bundle. This type is the
 * boundary between the two, so a future host is added by adding a subtype rather than by widening
 * this one or by adding root-level `wechatXxx` properties.
 *
 * It carries only what a build must know to produce and place a host's bundle. Application
 * identifiers, secrets, merchant material, order data, template identifiers and any other business
 * or backend configuration are not build inputs and do not belong here — a Gradle extension is not
 * a second copy of a host's console.
 */
public abstract class MiniAppHostConfiguration {

    /**
     * Where this host's bundle is assembled.
     *
     * Defaults to `build/miniapp/bundle`. A host integration reads the bundle from here, so this is
     * the one path the build must agree with the host about.
     */
    public abstract val bundleDirectory: DirectoryProperty
}
