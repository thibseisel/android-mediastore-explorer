package com.github.thibseisel.csv

import com.google.common.truth.StringSubject
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.StringWriter

class CsvWriterTest {

    @Test
    fun whenInitialized_nothingIsWritten() {
        val csv = buildCsv { Unit }
        assertThat(csv).isEmpty()
    }

    @Test
    fun test_setColumnNames() {
        val csv = buildCsv {
            setColumnNames("ID", "First name", "Last name")
        }

        assertThat(csv).isEqualToCRLF("ID,First name,Last name")
    }

    @Test(expected = IllegalStateException::class)
    fun whenSettingColumnsTwice_failsWithException() {
        buildCsv {
            setColumnNames("ID", "First name", "Last name")
            setColumnNames("ID", "Title", "Description")
        }
    }

    @Test(expected = IllegalStateException::class)
    fun whenWritingFieldWithoutSettingColumns_failsWithException() {
        buildCsv {
            writeField("Hello World!")
        }
    }

    @Test
    fun withCustomDelimiter_separatesColumnsWithIt() {
        val csv = buildCsv(delimiterChar = ';') {
            setColumnNames("ID", "First name", "Last name")
        }

        assertThat(csv).isEqualToCRLF("ID;First name;Last name")
    }

    @Test(expected = IllegalStateException::class)
    fun whenWritingValuesBeforeNewRow_failsWithException() {
        buildCsv {
            setColumnNames()
            setColumnNames("ID", "First name", "Last name")
            writeField("42")
            writeField("John")
            writeField("Doe")
        }
    }

    @Test
    fun whenWritingOneRow_shouldBePrintedBelowColumnNames() {
        val csv = buildCsv {
            setColumnNames("ID", "First name", "Last name")
            newRow()
            writeField("42")
            writeField("John")
            writeField("Doe")
        }

        assertThat(csv).isEqualToCRLF("""
            ID,First name,Last name
            42,John,Doe
        """.trimIndent())
    }

    @Test(expected = IllegalStateException::class)
    fun whenWritingTooMuchValues_failsWithException() {
        buildCsv {
            setColumnNames("ID", "First name", "Last name")
            newRow()
            writeField("42")
            writeField("John")
            writeField("Doe")
            writeField("Extra value")
        }
    }

    @Test(expected = IllegalStateException::class)
    fun whenNotGivingValueForEachColumn_failsWithException() {
        buildCsv {
            setColumnNames("ID", "First name", "Last name")
            newRow()
            writeField("42")
            writeField("John")
            // Missing value for "Last name"
            newRow()
            writeField("101")
            writeField("Jean")
            writeField("Dupont")
        }
    }

    @Test
    fun whenValueContainsDelimiterOrLines_valueIsQuoted() {
        val csv = buildCsv {
            setColumnNames("Month", "Income")
            newRow()
            writeField("January")
            writeField("2145,00")
            newRow()
            writeField("February${LINE_ENDING}March")
            writeField("2200,00")
        }

        assertThat(csv).isEqualToCRLF("""
            Month,Income
            January,"2145,00"
            "February
            March","2200,00"
        """.trimIndent())
    }

    @Test
    fun whenValueContainsBothDelimiterAndEscapeChars_valueIsQuotedAndEscaped() {
        val csv = buildCsv {
            setColumnNames("Category", "Software")
            newRow()
            writeField("Browser")
            writeField("IE, aka \"Internet Explorer\"")
        }

        assertThat(csv).isEqualToCRLF("""
            Category,Software
            Browser,"IE, aka ""Internet Explorer""${'"'}
        """.trimIndent())
    }

}

private fun buildCsv(
    delimiterChar: Char = DEFAULT_DELIMITER_CHAR,
    escapeChar: Char = DEFAULT_ESCAPE_CHAR,
    block: CsvWriter.() -> Unit
): String {
    val stringWriter = StringWriter()
    CsvWriter(stringWriter, delimiterChar, escapeChar).use(block)
    return stringWriter.toString()
}

private fun StringSubject.isEqualToCRLF(expected: String?) {
    isEqualTo(expected?.replace("\n", LINE_ENDING))
}