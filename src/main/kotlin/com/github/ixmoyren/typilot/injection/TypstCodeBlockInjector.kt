package com.github.ixmoyren.typilot.injection

import com.github.ixmoyren.typilot.psi.TypstRawBlockPsiElement
import com.intellij.lang.Language
import com.intellij.lang.injection.MultiHostInjector
import com.intellij.lang.injection.MultiHostRegistrar
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import kotlin.jvm.java

class TypstCodeBlockInjector : MultiHostInjector {
    private val languageAliases: Map<String, String> =
        mapOf(
            "java" to "JAVA",
            "kotlin" to "kotlin",
            "kt" to "kotlin",
            "groovy" to "Groovy",
            "scala" to "Scala",
            "clojure" to "Clojure",
            "c" to "ObjectiveC",
            "cpp" to "ObjectiveC",
            "c++" to "ObjectiveC",
            "csharp" to "C#",
            "c#" to "C#",
            "javascript" to "JavaScript",
            "js" to "JavaScript",
            "typescript" to "TypeScript",
            "ts" to "TypeScript",
            "html" to "HTML",
            "css" to "CSS",
            "scss" to "SCSS",
            "less" to "LESS",
            "astro" to "Astro",
            "jsx" to "JSX Harmony",
            "tsx" to "TypeScript JSX",
            "vue" to "Vue",
            "python" to "Python",
            "py" to "Python",
            "ruby" to "ruby",
            "rb" to "ruby",
            "perl" to "Perl",
            "php" to "PHP",
            "lua" to "Lua",
            "r" to "R",
            "bash" to "Shell Script",
            "sh" to "Shell Script",
            "shell" to "Shell Script",
            "zsh" to "Shell Script",
            "powershell" to "PowerShell",
            "ps1" to "PowerShell",
            "go" to "go",
            "golang" to "go",
            "rust" to "Rust",
            "rs" to "Rust",
            "swift" to "Swift",
            "dart" to "Dart",
            "zig" to "Zig",
            "json" to "JSON",
            "xml" to "XML",
            "yaml" to "yaml",
            "yml" to "yaml",
            "toml" to "TOML",
            "ini" to "Ini",
            "properties" to "Properties",
            "sql" to "SQL",
            "plsql" to "PLSQL",
            "mysql" to "MySQL",
            "postgresql" to "PostgreSQL",
            "markdown" to "Markdown",
            "md" to "Markdown",
            "latex" to "LaTeX",
            "tex" to "LaTeX",
            "dockerfile" to "Dockerfile",
            "docker" to "Dockerfile",
            "makefile" to "Makefile",
            "cmake" to "CMake",
            "haskell" to "Haskell",
            "hs" to "Haskell",
            "erlang" to "Erlang",
            "elixir" to "Elixir",
            "ocaml" to "OCaml",
            "protobuf" to "Protocol Buffer",
            "proto" to "Protocol Buffer",
            "graphql" to "GraphQL",
            "regex" to "RegExp",
            "diff" to "Diff",
            "patch" to "Diff",
            "plantuml" to "PlantUML",
            "nix" to "Nix")

    fun findLanguage(langName: String): Language? {
        languageAliases[langName]?.let { alias ->
            Language.findLanguageByID(alias)?.let {
                return it
            }
        }
        Language.getRegisteredLanguages()
            .firstOrNull { it.id.equals(langName, ignoreCase = true) }
            ?.let {
                return it
            }
        Language.getRegisteredLanguages()
            .firstOrNull { it.displayName.equals(langName, ignoreCase = true) }
            ?.let {
                return it
            }
        return null
    }

    override fun getLanguagesToInject(registrar: MultiHostRegistrar, context: PsiElement) {
        val rawBlock = context as? TypstRawBlockPsiElement ?: return
        if (!rawBlock.isValidHost()) return

        val langTag = rawBlock.langTag() ?: return
        val language = findLanguage(langTag) ?: return

        val hostStart = rawBlock.textRange.startOffset
        val textNodes = rawBlock.textChildren()
        if (textNodes.isEmpty()) return

        registrar.startInjecting(language)

        textNodes.forEachIndexed { index, textNode ->
            val relativeRange = TextRange(textNode.textRange.startOffset - hostStart, textNode.textRange.endOffset - hostStart)
            val prefix = if (index == 0) "" else "\n"
            val suffix = ""
            registrar.addPlace(prefix, suffix, rawBlock, relativeRange)
        }

        registrar.doneInjecting()
    }

    override fun elementsToInjectIn(): List<Class<out PsiElement>> = listOf(TypstRawBlockPsiElement::class.java)
}
