package com.github.ixmoyren.typilot

import com.github.ixmoyren.typalize.Core
import com.github.ixmoyren.typalize.Typalize

val typalizer: Typalize by lazy { Core.builder().build().typalizer().build() }
