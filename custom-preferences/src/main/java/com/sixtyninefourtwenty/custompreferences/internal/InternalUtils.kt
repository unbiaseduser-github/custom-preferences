package com.sixtyninefourtwenty.custompreferences.internal

import android.content.res.TypedArray
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
internal inline fun <R> TypedArray.useCompat(block: (TypedArray) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return if (this is AutoCloseable) {
        use(block)
    } else try {
        block(this)
    } finally {
        recycle()
    }
}
