package com.github.thibseisel.csv

import java.io.Closeable
import java.io.IOException
import java.io.Writer

class CsvWriter(
    writer: Writer,
    private val delimiterChar: Char = DEFAULT_DELIMITER_CHAR,
    private val escapeChar: Char = DEFAULT_ESCAPE_CHAR
) : Closeable {

    private val writer = writer.buffered()
    private var columnCount = 0
    private var columnIndex = 0

    @Throws(IOException::class)
    fun setColumnNames(vararg headers: CharSequence) {
        check(columnCount == 0) { "Columns are already set." }

        columnCount = headers.size
        headers.forEachIndexed { index, header ->
            if (index > 0) {
                writer.append(delimiterChar)
            }

            writeEscaped(header)
        }

        columnIndex = headers.size
    }

    @Throws(IOException::class)
    fun writeField(value: CharSequence?) {
        check(columnCount > 0) { "Column names must be set before writing values." }
        check(columnIndex < columnCount) {
            "Writing a value at column $columnIndex but only $columnCount are available."
        }

        if (columnIndex > 0) {
            writer.append(delimiterChar)
        }

        if (value != null) {
            writeEscaped(value)
        }

        columnIndex++
    }

    @Throws(IOException::class)
    fun newRow() {
        check(columnIndex >= columnCount) {
            "Requested a new line before setting a value for each column."
        }

        writer.append(LINE_ENDING)
        columnIndex = 0
    }

    @Throws(IOException::class)
    private fun writeEscaped(value: CharSequence) {
        if (value.contains(delimiterChar) || value.contains(LINE_ENDING)) {
            writer.append(escapeChar)

            var position = 0
            while (position < value.length) {
                val indexOfEscapeChar = value.indexOf(escapeChar, position)
                position = if (indexOfEscapeChar >= position) {
                    writer.append(value, position, indexOfEscapeChar + 1)
                    writer.append(escapeChar)
                    indexOfEscapeChar + 1
                } else {
                    writer.append(value, position, value.length)
                    value.length
                }
            }

            writer.append(escapeChar)
        } else {
            writer.append(value)
        }
    }

    /**
     * Flushes the stream.
     */
    fun flush() {
        writer.flush()
    }

    @Throws(IOException::class)
    override fun close() {
        writer.close()
    }
}
