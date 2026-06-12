import com.dylibso.chicory.compiler.InterpreterFallback
import com.dylibso.chicory.build.time.compiler.Config
import com.dylibso.chicory.build.time.compiler.Generator
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*

abstract class ChicoryCompilerTask : DefaultTask() {

    @get:InputFile
    abstract val wasmFile: RegularFileProperty

    @get:Input
    abstract val moduleName: Property<String>

    @get:OutputDirectory
    abstract val targetClassFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val targetSourceFolder: DirectoryProperty

    @get:OutputDirectory
    abstract val targetWasmFolder: DirectoryProperty

    @get:Input
    abstract val interpreterFallback: Property<InterpreterFallback>

    @get:Input
    abstract val interpretedFunctions: SetProperty<Int>

    @get:Optional
    @get:Input
    abstract val moduleInterface: Property<String>

    @TaskAction
    fun compile() {
        val wasmPath = wasmFile.get().asFile.toPath()
        val config = Config.builder()
            .withWasmFile(wasmPath)
            .withName(moduleName.get())
            .withTargetClassFolder(targetClassFolder.get().asFile.toPath())
            .withTargetSourceFolder(targetSourceFolder.get().asFile.toPath())
            .withTargetWasmFolder(targetWasmFolder.get().asFile.toPath())
            .withInterpreterFallback(interpreterFallback.get())
            .withInterpretedFunctions(interpretedFunctions.get())
            .withModuleInterface(moduleInterface.orNull)
            .build()

        val generator = Generator(config)
        val finalInterpretedFunctions = generator.generateResources()
        generator.generateMetaWasm(finalInterpretedFunctions)
        generator.generateSources()

        if (moduleInterface.isPresent && moduleInterface.get().isNotEmpty()) {
            generator.generateModuleInterface(moduleInterface.get())
        }

        if (interpreterFallback.get() == InterpreterFallback.WARN && finalInterpretedFunctions.isNotEmpty()) {
            val message = buildString {
                appendLine("// Copy-paste into your build.gradle.kts")
                appendLine("interpretedFunctions = setOf(")
                finalInterpretedFunctions.sorted().forEach { funcId ->
                    appendLine("    $funcId,")
                }
                append(")")
            }
            logger.warn(message)
        }
    }
}