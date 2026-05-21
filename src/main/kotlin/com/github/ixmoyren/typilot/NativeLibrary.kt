package com.github.ixmoyren.typilot

import java.io.File
import java.io.IOException
import java.io.UncheckedIOException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.concurrent.atomic.AtomicReference
import kotlin.jvm.java

object NativeLibrary {
    private enum class LoadingState {
        NOT_LOADED,
        LOADING,
        LOADED
    }

    private val state = AtomicReference(LoadingState.NOT_LOADED)

    init {
        loadLibrary()
    }

    @JvmStatic
    fun loadLibrary() {
        if (state.get() == LoadingState.LOADED) return

        if (state.compareAndSet(LoadingState.NOT_LOADED, LoadingState.LOADING)) {
            try {
                doLoadLibrary()
                state.set(LoadingState.LOADED)
            } catch (e: IOException) {
                state.set(LoadingState.NOT_LOADED)
                throw UncheckedIOException("Unable to load the typalize shared library", e)
            } catch (e: UnsatisfiedLinkError) {
                state.set(LoadingState.NOT_LOADED)
                throw e
            }
            return
        }

        while (state.get() == LoadingState.LOADING) {
            try {
                Thread.sleep(10)
            } catch (_: InterruptedException) {}
        }
    }

    @Throws(IOException::class)
    private fun doLoadLibrary() {
        val libname = System.getProperty("uniffi.component.Typalize.libraryOverride") ?: "typalize"
        try {
            System.loadLibrary(libname)
            return
        } catch (_: UnsatisfiedLinkError) {}

        doLoadBundledLibrary()
    }

    @Throws(IOException::class)
    private fun doLoadBundledLibrary() {
        val primaryPath = bundledLibraryPath()

        this::class.java.getResourceAsStream(primaryPath)?.use { inputStream ->
            val tmpFile = createTempFileFromResource(primaryPath, inputStream)
            try {
                System.load(tmpFile.absolutePath)
                return
            } catch (e: UnsatisfiedLinkError) {
                throw e
            }
        }
    }

    @Throws(IOException::class)
    private fun createTempFileFromResource(resourcePath: String, inputStream: java.io.InputStream): File {
        val dotIndex = resourcePath.lastIndexOf('.')
        val prefix = if (dotIndex >= 0) resourcePath.substring(0, dotIndex) else resourcePath
        val suffix = if (dotIndex >= 0) resourcePath.substring(dotIndex) else ""
        val tempFile = File.createTempFile(prefix, suffix)
        tempFile.deleteOnExit()
        Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        return tempFile
    }

    private fun bundledLibraryPath(): String {
        val classifier = Environment.getClassifier()
        val libraryName = System.mapLibraryName("typalize")
        return "$classifier/$libraryName"
    }
}
