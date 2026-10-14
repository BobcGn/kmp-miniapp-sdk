package io.github.bobcgn.miniapp.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project

/** A root configuration delegates to an explicitly selected existing logic module, never to a name heuristic. */
internal object MiniAppRootProjectSupport {
    public fun configure(root: Project, extension: MiniAppExtension) {
        if (root != root.rootProject) {
            throw GradleException("miniapp.logicProject may only be configured on the root project; apply the plugin locally in '${root.path}' instead.")
        }
        if (root.pluginManager.hasPlugin(MiniAppPluginDiagnostics.KOTLIN_MULTIPLATFORM_PLUGIN_ID)) {
            throw GradleException("A root selecting miniapp.logicProject must not also compile Kotlin Multiplatform. Declare the Kotlin plugin with apply false, or use the existing root-local KMP workflow without logicProject.")
        }
        extension.logicProject.finalizeValue()
        val logic = extension.logicProject.get()
        if (logic == root || logic.rootProject != root) {
            throw GradleException("miniapp.logicProject must select an existing subproject in this build, not '${logic.path}'.")
        }
        if (logic.state.executed) {
            throw GradleException("miniapp.logicProject '${logic.path}' has already been evaluated. Select it in the root build before evaluating subprojects.")
        }

        val host = extension.wechat
        host.rootHost.convention(true)
        if (!host.rootHost.get()) {
            throw GradleException("A root selecting miniapp.logicProject requires wechat.rootHost=true; omit the flag to use its coordinator default.")
        }
        // Only host defaults belong to root. The compiler bundle stays inside the selected module.
        host.bundleDirectory.convention(logic.layout.buildDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_BUNDLE_DIRECTORY))

        host.rootHost.finalizeValue()
        host.bundleDirectory.finalizeValue()
        host.sourceDirectory.finalizeValue()
        host.generatedLibraryDirectory.finalizeValue()
        host.distributionDirectory.finalizeValue()

        logic.pluginManager.apply(MiniAppGradlePlugin::class.java)
        val selected = logic.extensions.getByType(MiniAppExtension::class.java).wechat
        selected.rootHost.set(true)
        selected.bundleDirectory.set(host.bundleDirectory.get())
        selected.sourceDirectory.set(host.sourceDirectory.get())
        selected.generatedLibraryDirectory.set(host.generatedLibraryDirectory.get())
        selected.distributionDirectory.set(host.distributionDirectory.get())
        // One configuration owner. Root settings must not silently diverge from module settings.
        selected.rootHost.disallowChanges()
        selected.bundleDirectory.disallowChanges()
        selected.sourceDirectory.disallowChanges()
        selected.generatedLibraryDirectory.disallowChanges()
        selected.distributionDirectory.disallowChanges()
        // The module's normal callback validates KMP, registers compilations and claims root tasks.
        // No Project object is captured by task actions or execution-time providers here.
    }
}
