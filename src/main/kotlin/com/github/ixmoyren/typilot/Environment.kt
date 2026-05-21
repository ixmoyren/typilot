package com.github.ixmoyren.typilot

object Environment {
    const val UNKNOWN: String = "<unknown>"
    private var classifier: String = UNKNOWN

    init {
        val classifierBuilder = StringBuilder()
        val os = System.getProperty("os.name").lowercase()
        when {
            os.startsWith("windows") -> classifierBuilder.append("win32")
            else -> classifierBuilder.append("linux")
        }
        classifierBuilder.append("-")

        val arch = System.getProperty("os.arch").lowercase()
        if (arch == "aarch64") {
            classifierBuilder.append("aarch_64")
        } else {
            classifierBuilder.append("x86-64")
        }

        classifier = classifierBuilder.toString()
    }

    @JvmStatic
    fun getClassifier(): String {
        return classifier
    }
}