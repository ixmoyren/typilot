package com.github.ixmoyren.typilot.lsp

import com.github.ixmoyren.typilot.TypalizeUtils

class TinymistHelper : TinymistLocator {
    val tinymistBinary: String? by lazy {
        TypalizeUtils.findBinary("tinymist")
    }

    override fun locate(): String? = tinymistBinary
}