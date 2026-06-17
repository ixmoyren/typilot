package com.github.ixmoyren.typilot.psi

import com.github.ixmoyren.typalize.TypstSyntaxKind

val TypstSyntaxKind.name: String
    get() = this::class.java.simpleName

object TypstSyntaxKindUtils {
    val entriesMap: Map<String, TypstSyntaxKind> by lazy {
        TypstSyntaxKind::class.java
            .declaredClasses
            .filter { clazz ->
                val modifiers = clazz.modifiers
                java.lang.reflect.Modifier.isPublic(modifiers) &&
                        java.lang.reflect.Modifier.isStatic(modifiers) &&
                        TypstSyntaxKind::class.java.isAssignableFrom(clazz) &&
                        !java.lang.reflect.Modifier.isAbstract(modifiers)
            }
            .mapNotNull { clazz ->
                runCatching {
                    val builderClass = Class.forName($$"$${clazz.name}$Builder")
                    val builder = builderClass.getDeclaredConstructor().newInstance()
                    val buildMethod = builderClass.getDeclaredMethod("build")
                    buildMethod.invoke(builder) as TypstSyntaxKind
                }.getOrNull()
            }
            .associateBy { it::class.java.simpleName }
    }

    val entries: List<TypstSyntaxKind> by lazy {
        entriesMap.values.toList()
    }

    val keywordSet: Set<TypstSyntaxKind> by lazy {
        setOf(
            TypstSyntaxKind.Not::class.java.simpleName,
            TypstSyntaxKind.And::class.java.simpleName,
            TypstSyntaxKind.Or::class.java.simpleName,
            TypstSyntaxKind.None::class.java.simpleName,
            TypstSyntaxKind.Auto::class.java.simpleName,
            TypstSyntaxKind.Let::class.java.simpleName,
            TypstSyntaxKind.Set::class.java.simpleName,
            TypstSyntaxKind.Show::class.java.simpleName,
            TypstSyntaxKind.Context::class.java.simpleName,
            TypstSyntaxKind.If::class.java.simpleName,
            TypstSyntaxKind.Else::class.java.simpleName,
            TypstSyntaxKind.For::class.java.simpleName,
            TypstSyntaxKind.In::class.java.simpleName,
            TypstSyntaxKind.While::class.java.simpleName,
            TypstSyntaxKind.Break::class.java.simpleName,
            TypstSyntaxKind.Continue::class.java.simpleName,
            TypstSyntaxKind.Return::class.java.simpleName,
            TypstSyntaxKind.Import::class.java.simpleName,
            TypstSyntaxKind.Include::class.java.simpleName,
            TypstSyntaxKind.As::class.java.simpleName,
        ).mapNotNull { entriesMap[it] }.toSet()
    }

    val identSet: Set<TypstSyntaxKind> by lazy {
        setOf(TypstSyntaxKind.Ident::class.java.simpleName, TypstSyntaxKind.MathIdent::class.java.simpleName).mapNotNull { entriesMap[it] }.toSet()
    }

    val commentSet: Set<TypstSyntaxKind> by lazy {
        setOf(
            TypstSyntaxKind.LineComment::class.java.simpleName,
            TypstSyntaxKind.BlockComment::class.java.simpleName,
            TypstSyntaxKind.Shebang::class.java.simpleName,
        ).mapNotNull { entriesMap[it] }.toSet()
    }

    val spaceSet: Set<TypstSyntaxKind> by lazy {
        setOf(
            TypstSyntaxKind.Space::class.java.simpleName,
            TypstSyntaxKind.Parbreak::class.java.simpleName,
        ).mapNotNull { entriesMap[it] }.toSet()
    }

    val literalSet: Set<TypstSyntaxKind> by lazy {
        setOf(
            TypstSyntaxKind.Bool::class.java.simpleName,
            TypstSyntaxKind.Int::class.java.simpleName,
            TypstSyntaxKind.Float::class.java.simpleName,
            TypstSyntaxKind.Numeric::class.java.simpleName,
            TypstSyntaxKind.Str::class.java.simpleName,
            TypstSyntaxKind.Text::class.java.simpleName,
            TypstSyntaxKind.Link::class.java.simpleName,
            TypstSyntaxKind.Label::class.java.simpleName,
            TypstSyntaxKind.MathText::class.java.simpleName,
            TypstSyntaxKind.MathShorthand::class.java.simpleName
        ).mapNotNull { entriesMap[it] }.toSet()
    }

    val operatorSet: Set<TypstSyntaxKind> by lazy {
        setOf(
            TypstSyntaxKind.Hash::class.java.simpleName,
            TypstSyntaxKind.LeftBrace::class.java.simpleName,
            TypstSyntaxKind.RightBrace::class.java.simpleName,
            TypstSyntaxKind.LeftBracket::class.java.simpleName,
            TypstSyntaxKind.RightBracket::class.java.simpleName,
            TypstSyntaxKind.LeftParen::class.java.simpleName,
            TypstSyntaxKind.RightParen::class.java.simpleName,
            TypstSyntaxKind.Comma::class.java.simpleName,
            TypstSyntaxKind.Semicolon::class.java.simpleName,
            TypstSyntaxKind.Colon::class.java.simpleName,
            TypstSyntaxKind.Star::class.java.simpleName,
            TypstSyntaxKind.Underscore::class.java.simpleName,
            TypstSyntaxKind.Dollar::class.java.simpleName,
            TypstSyntaxKind.Plus::class.java.simpleName,
            TypstSyntaxKind.Minus::class.java.simpleName,
            TypstSyntaxKind.Slash::class.java.simpleName,
            TypstSyntaxKind.Hat::class.java.simpleName,
            TypstSyntaxKind.Dot::class.java.simpleName,
            TypstSyntaxKind.Eq::class.java.simpleName,
            TypstSyntaxKind.EqEq::class.java.simpleName,
            TypstSyntaxKind.ExclEq::class.java.simpleName,
            TypstSyntaxKind.Lt::class.java.simpleName,
            TypstSyntaxKind.LtEq::class.java.simpleName,
            TypstSyntaxKind.Gt::class.java.simpleName,
            TypstSyntaxKind.GtEq::class.java.simpleName,
            TypstSyntaxKind.PlusEq::class.java.simpleName,
            TypstSyntaxKind.HyphEq::class.java.simpleName,
            TypstSyntaxKind.StarEq::class.java.simpleName,
            TypstSyntaxKind.SlashEq::class.java.simpleName,
            TypstSyntaxKind.Dots::class.java.simpleName,
            TypstSyntaxKind.Arrow::class.java.simpleName,
            TypstSyntaxKind.Root::class.java.simpleName,
            TypstSyntaxKind.MathAlignPoint::class.java.simpleName
        ).mapNotNull { entriesMap[it] }.toSet()
    }
}
