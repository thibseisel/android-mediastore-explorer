package com.github.thibseisel.csv

import java.io.Reader

class CsvParser(
    private val delimiterChar: Char = DEFAULT_DELIMITER_CHAR,
    private val escapeChar: Char = DEFAULT_ESCAPE_CHAR
) {
    private val _columnNames = mutableListOf<CharSequence>()

    fun parse(source: Reader) {
        TODO()
    }

    val columnNames: List<CharSequence> get() = TODO()

    val count: Int get() = 0

    fun getValueAt(lineIndex: Int, columnIndex: Int): CharSequence {
        TODO()
    }
}