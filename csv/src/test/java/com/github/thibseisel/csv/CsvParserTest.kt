package com.github.thibseisel.csv

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.StringReader

class CsvParserTest {

    @Test
    fun whenEmptyStream_parseNothing() {
        givenParsed("") {
            assertThat(columnNames).isEmpty()
            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun whenParsingColumnsWithoutData_isEmptyAndHasColumns() {
        givenParsed("ID,First name,Last name") {
            assertThat(columnNames).containsExactly("ID", "First name", "Last name").inOrder()
            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun whenParsingColumnsAfterEmptySpaces_parseAsColumns() {
        givenParsed(LINE_ENDING + "ID,First name,Last name") {
            assertThat(columnNames).containsExactly("ID", "First name", "Last name").inOrder()
            assertThat(count).isEqualTo(0)
        }
    }

    @Test
    fun parseValues() {
        givenParsed("""
            ID,First name,Last name
            102,John,Doe
        """.trimIndentCRLF()) {
            assertThat(count).isEqualTo(1)

            assertThat(getValueAt(0, 0)).isEqualTo("102")
            assertThat(getValueAt(0, 1)).isEqualTo("John")
            assertThat(getValueAt(0, 2)).isEqualTo("Doe")
        }
    }

    @Test
    fun parseEscapedValues() {
        givenParsed("""
            ID,First name,Last name
            102,"John, ""Johnny""${'"'},Doe
        """.trimIndentCRLF()) {
            assertThat(count).isEqualTo(1)

            assertThat(getValueAt(0, 0)).isEqualTo("102")
            assertThat(getValueAt(0, 1)).isEqualTo("John, \"Johnny\"")
            assertThat(getValueAt(0, 2)).isEqualTo("Doe")
        }
    }

    private fun givenParsed(
        input: String,
        delimiterChar: Char = DEFAULT_DELIMITER_CHAR,
        escapeChar: Char = DEFAULT_ESCAPE_CHAR,
        block: CsvParser.() -> Unit
    ) {
        val reader = StringReader(input)
        val parser = CsvParser(delimiterChar, escapeChar)
        parser.parse(reader)
        parser.block()
    }
}

private fun String.trimIndentCRLF() = trimIndent().replace("\n", LINE_ENDING)