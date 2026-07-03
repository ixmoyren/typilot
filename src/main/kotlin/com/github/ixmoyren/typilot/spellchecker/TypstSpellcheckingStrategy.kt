package com.github.ixmoyren.typilot.spellchecker

import com.github.ixmoyren.typilot.language.TypstLanguage
import com.github.ixmoyren.typilot.psi.*
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy
import com.intellij.spellchecker.tokenizer.Tokenizer

class TypstSpellcheckingStrategy : SpellcheckingStrategy(), DumbAware {

    override fun isMyContext(element: PsiElement): Boolean {
        return element.language.isKindOf(TypstLanguage.INSTANCE)
    }

    override fun getTokenizer(element: PsiElement): Tokenizer<*> {
        return when (element) {
            is TypstTextPsiElement,
            is TypstStrPsiElement,
            is TypstLabelPsiElement,
            is TypstRefMarkerPsiElement,
            is TypstMathTextPsiElement -> TEXT_TOKENIZER

            is TypstLineCommentPsiElement,
            is TypstBlockCommentPsiElement -> myCommentTokenizer

            else -> super.getTokenizer(element)
        }
    }
}
