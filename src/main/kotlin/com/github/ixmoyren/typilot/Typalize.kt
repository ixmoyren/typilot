package com.github.ixmoyren.typilot

import com.github.ixmoyren.typalize.Core
import com.github.ixmoyren.typalize.Typalize
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

private val typalizerThreadLocal = ThreadLocal.withInitial {
    Core.builder().build().typalizer().build()
}

val typalizer: Typalize get() = typalizerThreadLocal.get()

fun <T : Any> lazyNonNull(initializer: () -> T?): ReadOnlyProperty<Any?, T?> =
    object : ReadOnlyProperty<Any?, T?> {
        @Volatile
        private var value: T? = null

        override fun getValue(thisRef: Any?, property: KProperty<*>): T? = value ?: initializer().also { value = it }
    }
