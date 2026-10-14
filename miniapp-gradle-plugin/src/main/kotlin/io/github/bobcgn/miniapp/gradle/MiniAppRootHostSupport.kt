package io.github.bobcgn.miniapp.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project

/** Connects one module's existing host pipeline to the consumer root without moving Kotlin sources. */
internal object MiniAppRootHostSupport {
    private const val OWNER: String = "io.github.bobcgn.miniapp.rootHostOwner"
    private val hostTasks: List<String> = listOf(
        MiniAppPluginDiagnostics.INIT_MINIAPP_TASK_NAME,
        MiniAppPluginDiagnostics.SYNC_MINIAPP_TASK_NAME,
        MiniAppPluginDiagnostics.ASSEMBLE_MINIAPP_TASK_NAME,
        MiniAppPluginDiagnostics.VERIFY_MINIAPP_TASK_NAME,
    )

    public fun configure(project: Project) {
        val wechat = project.extensions.getByType(MiniAppExtension::class.java).wechat
        wechat.rootHost.finalizeValue()
        if (!wechat.rootHost.get()) return

        val root = project.rootProject
        val properties = root.extensions.extraProperties
        if (properties.has(OWNER)) {
            throw GradleException(
                "Only one module may set wechat.rootHost=true: ${properties.get(OWNER)} and ${project.path} both opted in.",
            )
        }
        if (root != project) {
            val conflicts = hostTasks.filter { root.tasks.findByName(it) != null }
            if (conflicts.isNotEmpty()) {
                throw GradleException(
                    "Root Mini App task names are already owned: $conflicts. Select one host producer; existing tasks are not replaced.",
                )
            }
        }
        properties.set(OWNER, project.path)
        val coordinator = root.extensions.findByType(MiniAppExtension::class.java)
        if (coordinator?.logicProject?.orNull != project) {
            wechat.sourceDirectory.convention(root.layout.projectDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_SOURCE_DIRECTORY))
            wechat.distributionDirectory.convention(root.layout.buildDirectory.dir(MiniAppPluginDiagnostics.MINIAPP_DISTRIBUTION_DIRECTORY))
        }

        if (root != project) {
            // Root clean owns root build outputs; source host remains outside that directory.
            root.pluginManager.apply("base")
            hostTasks.forEach { name ->
                val producer = project.tasks.named(name)
                root.tasks.register(name) { task ->
                    task.group = "mini app"
                    task.description = "Runs ${project.path}:$name for the root Mini App host."
                    task.dependsOn(producer)
                }
            }
        }
    }
}
