/*
 * Copyright 2013-2026 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Provides the classes and interfaces necessary for API metrics analysis within
 * the Chronicle Test Framework. This package includes functionality for defining
 * metrics, accumulators, and builders, allowing for detailed analysis of code
 * structures, including classes, methods, and fields.
 *
 * <p>This package enables users to create customised metrics or utilise standard
 * metrics, aggregate the results through accumulators, and build and execute
 * analysis through the ApiMetricsBuilder.
 *
 * <p>The usual workflow involves defining a set of metrics, configuring one or
 * more accumulators to gather scores, and then building an {@code ApiMetrics}
 * instance with the builder. That instance can be applied to the chosen
 * packages.
 *
 * <p>Example usage might include creating customised {@link Metric} instances and
 * a tailored {@link Accumulator}, followed by the builder workflow to analyse
 * packages for compliance with specific coding standards, detecting patterns, or
 * generating reports for code quality assurance. Typical use cases include
 * maintaining API consistency across releases.
 *
 * @see net.openhft.chronicle.testframework.apimetrics.ApiMetrics
 * @see net.openhft.chronicle.testframework.apimetrics.Accumulator
 * @see net.openhft.chronicle.testframework.apimetrics.Metric
 */
package net.openhft.chronicle.testframework.apimetrics;
