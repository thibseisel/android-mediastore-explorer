package com.github.thibseisel.csv

fun CsvWriter.writeRow(vararg values: CharSequence) {
    newRow()
    values.forEach(this::writeField)
}