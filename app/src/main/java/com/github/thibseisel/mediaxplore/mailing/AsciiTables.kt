package com.github.thibseisel.mediaxplore.mailing

import org.jetbrains.annotations.TestOnly

@TestOnly
fun generateTable(columnHeaders: Array<out String>, tableValues: Array<Array<String>>) = buildString {
    appendTableInternal(columnHeaders, tableValues, this)
}

fun Appendable.appendTable(columnHeaders: Array<out String>, tableValues: Array<Array<String>>) {
    appendTableInternal(columnHeaders, tableValues, this)
}

private fun appendTableInternal(
        columnHeaders: Array<out String>,
        tableValues: Array<Array<String>>,
        output: Appendable
) {
    val columnWidths = IntArray(columnHeaders.size) { colIndex ->
        tableValues.fold(columnHeaders[colIndex].length) { acc, valueLine ->
            maxOf(acc, valueLine[colIndex].length)
        }
    }

    for (colIndex in columnHeaders.indices) {
        val header = columnHeaders[colIndex]
        val colWidth = columnWidths[colIndex]
        val emptySpaces = colWidth - header.length

        if (colIndex == 0) {
            output + '|'
        }

        output + (emptySpaces * ' ') + header + '|'
    }

    output.appendln()

    val totalWidth = columnWidths.sum() + columnHeaders.size + 1
    output + (totalWidth * '-')
    output.appendln()

    for (lineValues in tableValues) {

        for ((colIndex, value) in lineValues.withIndex()) {
            val colWidth = columnWidths[colIndex]
            val emptySpaces = colWidth - value.length

            if (colIndex == 0) {
                output + '|'
            }

            output + (emptySpaces * ' ') + value + '|'
        }

        output.appendln()
    }
}