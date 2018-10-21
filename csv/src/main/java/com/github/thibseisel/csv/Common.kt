package com.github.thibseisel.csv

/**
 * Line endings used by MS-DOS.
 * This is the line endings used by RFC-4180 compliant CSV files.
 */
internal const val LINE_ENDING = "\r\n"

/** The default character to be used as a delimiter between values in CSV files. */
internal const val DEFAULT_DELIMITER_CHAR = ','

/** The default character to be used when escaping values in CSV files. */
internal const val DEFAULT_ESCAPE_CHAR = '"'