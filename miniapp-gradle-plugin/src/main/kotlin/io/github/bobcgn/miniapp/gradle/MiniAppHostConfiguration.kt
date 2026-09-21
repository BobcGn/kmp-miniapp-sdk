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
 * It carries only what a build must know to produce and place a host's artifacts. Application
 * identifiers, secrets, merchant material, order data, template identifiers and any other business
 * or backend configuration are not build inputs and do not belong here — a Gradle extension is not
 * a second copy of a host's console.
 *
 * The three directories name three different things, and keeping them apart is the point:
 *
 * ```text
 * sourceDirectory            the host project a developer edits by hand (source)
 * generatedLibraryDirectory  the compiler's modules inside it (plugin-owned, disposable)
 * distributionDirectory      the complete standalone project (build output, disposable)
 * ```
 *
 * A `Sync` deletes files in its destination that its sources do not contain, so every one of these
 * is validated before it is written to. See [MiniAppPathDiagnostics].
 */
public abstract class MiniAppHostConfiguration {

    /**
     * Where this host's bundle is assembled.
     *
     * Defaults to `build/miniapp/bundle`. A host integration reads the bundle from here, so this is
     * the one path the build must agree with the host about.
     */
    public abstract val bundleDirectory: DirectoryProperty

    /**
     * The host project a developer maintains by hand.
     *
     * Defaults to `miniApp` in the project directory. It is source: `clean` does not remove it, the
     * plugin never writes over a file that is already there, and `assembleMiniApp` reads it rather
     * than changing it.
     */
    public abstract val sourceDirectory: DirectoryProperty

    /**
     * The complete standalone host project, assembled from [sourceDirectory] and the compiled
     * modules.
     *
     * Defaults to `build/miniapp-dist`. It is build output: safe to delete, safe to rebuild, and
     * never edited.
     */
    public abstract val distributionDirectory: DirectoryProperty
}
