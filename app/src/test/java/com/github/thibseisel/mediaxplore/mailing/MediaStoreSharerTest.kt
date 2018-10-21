package com.github.thibseisel.mediaxplore.mailing

import com.google.common.truth.Truth
import org.junit.Test

class MailerTest {

    @Test
    fun generateTable_withEmptyData_generatesEmptyTable() {
        val table = generateTable(arrayOf("ID", "Title"), emptyArray())
        Truth.assertThat(table).isEqualTo(EMPTY_TABLE)
    }

    @Test
    fun generateTable_withOneLine() {
        val headers = arrayOf("ID", "Title")
        val data = arrayOf(
                arrayOf("42", "Hello World!")
        )

        val table = generateTable(headers, data)
        Truth.assertThat(table).isEqualTo(ONE_LINE_TABLE)
    }

    @Test
    fun generateTable_withMultipleLines() {
        val headers = arrayOf("ID", "Title")
        val data = arrayOf(
                arrayOf("42", "Hello World!"),
                arrayOf("101", "Foo"),
                arrayOf("4", "Much longer line of text")
        )

        val table = generateTable(headers, data)
        Truth.assertThat(table).isEqualTo(MULTIPLE_LINE_TABLE)
    }

}

private val EMPTY_TABLE = """
    |ID|Title|
    ----------

""".trimIndent()

private val ONE_LINE_TABLE = """
    |ID|       Title|
    -----------------
    |42|Hello World!|

""".trimIndent()

private val MULTIPLE_LINE_TABLE = """
    | ID|                   Title|
    ------------------------------
    | 42|            Hello World!|
    |101|                     Foo|
    |  4|Much longer line of text|
    
""".trimIndent()