/**
 * Provides utility classes for working with mapped files, specifically targeting
 * Linux systems where the "/proc/self/maps" file provides information about
 * virtual memory mappings.
 * <p>
 * This package contains utilities to parse the "/proc/self/maps" file, allowing
 * interaction with mapped files at a low level.
 * <p>
 * NOTE: The functionality provided in this package is likely to work only on
 * Linux or Linux-like environments where the "/proc/self/maps" file is available.
 */
package net.openhft.chronicle.testframework.mappedfiles;
