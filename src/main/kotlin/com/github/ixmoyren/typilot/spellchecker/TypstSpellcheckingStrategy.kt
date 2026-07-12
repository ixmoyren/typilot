package com.github.ixmoyren.typilot.spellchecker

import com.github.ixmoyren.typalize.TypstSyntaxKind
import com.github.ixmoyren.typilot.language.TypstLanguage
import com.github.ixmoyren.typilot.psi.TypstBlockCommentPsiElement
import com.github.ixmoyren.typilot.psi.TypstLineCommentPsiElement
import com.github.ixmoyren.typilot.psi.getTypstSyntaxKind
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer

class TypstSpellcheckingStrategy : SpellcheckingStrategy(), DumbAware {

    override fun isMyContext(element: PsiElement): Boolean {
        return element.language.isKindOf(TypstLanguage.INSTANCE)
    }

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        val kind = element.getTypstSyntaxKind() ?: return super.getTokenizer(element)
        return when {
            element is TypstLineCommentPsiElement || element is TypstBlockCommentPsiElement -> myCommentTokenizer

            kind is TypstSyntaxKind.Str || kind is TypstSyntaxKind.Label || kind is TypstSyntaxKind.RefMarker || kind is TypstSyntaxKind.MathText || kind is TypstSyntaxKind.Text ->
                TEXT_TOKENIZER

            else -> super.getTokenizer(element)
        }
    }
}
