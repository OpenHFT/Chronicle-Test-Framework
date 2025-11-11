/*
 * Copyright 2013-2025 chronicle.software; SPDX-License-Identifier: Apache-2.0
 */
/**
 * Provides a lightweight TCP proxy for tests.
 *
 * <p>The {@link net.openhft.chronicle.testframework.internal.network.proxy.TcpProxy}
 * listens on a port and forwards bytes to an upstream address. Each connection
 * is managed by a {@link net.openhft.chronicle.testframework.internal.network.proxy.ProxyConnection},
 * which can pause or stop forwarding without closing the sockets.
 *
 * <p>Classes in this package are for internal use and may change without notice.
 */
package net.openhft.chronicle.testframework.internal.network.proxy;
