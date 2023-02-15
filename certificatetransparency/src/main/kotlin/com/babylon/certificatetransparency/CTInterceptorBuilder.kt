/*
 * Copyright 2019 Babylon Partners Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.babylon.certificatetransparency

import com.babylon.certificatetransparency.cache.DiskCache
import com.babylon.certificatetransparency.chaincleaner.CertificateChainCleanerFactory
import com.babylon.certificatetransparency.datasource.DataSource
import com.babylon.certificatetransparency.internal.verifier.CertificateTransparencyInterceptor
import com.babylon.certificatetransparency.internal.verifier.model.Host
import com.babylon.certificatetransparency.loglist.LogListResult
import com.babylon.certificatetransparency.loglist.LogServer
import okhttp3.Interceptor
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

/**
 * Builder to create an OkHttp network interceptor that will verify a host is trusted using
 * certificate transparency
 */
@Suppress("TooManyFunctions")
public class CTInterceptorBuilder {
    private var certificateChainCleanerFactory: CertificateChainCleanerFactory? = null
    private var trustManager: X509TrustManager? = null
    private var logListDataSource: DataSource<LogListResult>? = null
    private val includeHosts = mutableSetOf<Host>()
    private val excludeHosts = mutableSetOf<Host>()
    // test
    /**
     * Determine if a failure to pass certificate transparency results in the connection being closed. A value of true ensures the connection is
     * closed on errors
     * Default: true
     */
    public var failOnError: Boolean = true
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CTLogger] which will be called with all results
     * Default: none
     */
    public var logger: CTLogger? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CTPolicy] which will verify correct number of SCTs are present
     * Default: [CTPolicy] which follows rules of https://github.com/chromium/ct-policy/blob/master/ct_policy.md
     */
    public var policy: CTPolicy? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [DiskCache] which will cache the log list
     * Default: none
     */
    public var diskCache: DiskCache? = null
        @JvmSynthetic get
        @JvmSynthetic set

    /**
     * [CertificateChainCleanerFactory] used to provide the cleaner of the certificate chain
     * Default: null
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setCertificateChainCleanerFactory(certificateChainCleanerFactory: CertificateChainCleanerFactory): CTInterceptorBuilder =
        apply { this.certificateChainCleanerFactory = certificateChainCleanerFactory }

    /**
     * [CertificateChainCleanerFactory] used to provide the cleaner of the certificate chain
     * Default: null
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun certificateChainCleanerFactory(init: () -> CertificateChainCleanerFactory) {
        setCertificateChainCleanerFactory(init())
    }

    /**
     * [X509TrustManager] used to clean the certificate chain
     * Default: Platform default [X509TrustManager] created through [TrustManagerFactory]
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setTrustManager(trustManager: X509TrustManager): CTInterceptorBuilder =
        apply { this.trustManager = trustManager }

    /**
     * [X509TrustManager] used to clean the certificate chain
     * Default: Platform default [X509TrustManager] created through [TrustManagerFactory]
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun trustManager(init: () -> X509TrustManager) {
        setTrustManager(init())
    }

    /**
     * A [DataSource] providing a list of [LogServer]
     * Default: In memory cached log list loaded from https://www.gstatic.com/ct/log_list/log_list.json
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun setLogListDataSource(logListDataSource: DataSource<LogListResult>): CTInterceptorBuilder =
        apply {
            this.logListDataSource = logListDataSource
        }

    /**
     * A [DataSource] providing a list of [LogServer]
     * Default: In memory cached log list loaded from https://www.gstatic.com/ct/log_list/log_list.json
     */
    @JvmSynthetic
    @Suppress("unused")
    public fun logListDataSource(init: () -> DataSource<LogListResult>) {
        setLogListDataSource(init())
    }

    /**
     * Determine if a failure to pass certificate transparency results in the connection being closed. [failOnError] set to true closes the
     * connection on errors
     * Default: true
     */
    @Suppress("unused")
    public fun setFailOnError(failOnError: Boolean): CTInterceptorBuilder = apply { this.failOnError = failOnError }

    /**
     * [CTLogger] which will be called with all results
     * Default: none
     */
    @Suppress("unused")
    public fun setLogger(logger: CTLogger): CTInterceptorBuilder = apply { this.logger = logger }

    /**
     * [CTPolicy] which will verify correct number of SCTs are present
     * Default: [CTPolicy] which follows rules of https://github.com/chromium/ct-policy/blob/master/ct_policy.md
     */
    @Suppress("unused")
    public fun setPolicy(policy: CTPolicy): CTInterceptorBuilder = apply { this.policy = policy }

    /**
     * [DiskCache] which will cache the log list
     * Default: none
     */
    @Suppress("unused")
    public fun setDiskCache(diskCache: DiskCache): CTInterceptorBuilder = apply { this.diskCache = diskCache }

    /**
     * Verify certificate transparency for hosts that match [pattern].
     *
     * @property pattern lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun includeHost(pattern: String): CTInterceptorBuilder = apply {
        includeHosts.add(Host(pattern))
    }

    /**
     * Verify certificate transparency for hosts that match [this].
     *
     * @receiver lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun String.unaryPlus() {
        includeHost(this)
    }

    /**
     * Verify certificate transparency for hosts that match one of [this].
     *
     * @receiver [List] of lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun List<String>.unaryPlus() {
        forEach { includeHost(it) }
    }

    /**
     * Ignore certificate transparency for hosts that match [pattern].
     *
     * @property pattern lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public fun excludeHost(vararg pattern: String): CTInterceptorBuilder = apply {
        pattern.forEach { excludeHosts.add(Host(it)) }
    }

    /**
     * Ignore certificate transparency for hosts that match [this].
     *
     * @receiver lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun String.unaryMinus() {
        excludeHost(this)
    }

    /**
     * Ignore certificate transparency for hosts that match one of [this].
     *
     * @receiver [List] of lower-case host name or wildcard pattern such as `*.example.com`.
     */
    @JvmSynthetic
    public operator fun List<String>.unaryMinus() {
        forEach { excludeHost(it) }
    }

    /**
     * Build the network [Interceptor]
     */
    public fun build(): Interceptor = CertificateTransparencyInterceptor(
        includeHosts.toSet(),
        excludeHosts.toSet(),
        certificateChainCleanerFactory,
        trustManager,
        logListDataSource,
        policy,
        diskCache,
        failOnError,
        logger
    )
}
