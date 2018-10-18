package com.github.thibseisel.mediaxplore.mailing

operator fun Appendable.plus(cs: CharSequence): Appendable = append(cs)
operator fun Appendable.plus(c: Char): Appendable = append(c)

operator fun Int.times(ch: Char): String {
    val length = this.coerceAtLeast(0)
    val array = CharArray(length) { ch }
    return String(array)
}