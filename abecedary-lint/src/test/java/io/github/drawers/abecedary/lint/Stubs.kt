// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin

object Stubs {
    val ALPHABETICAL =
        kotlin(
            """
                package io.github.drawers.abecedary

                @Retention(AnnotationRetention.SOURCE)
                @Target(AnnotationTarget.CLASS, AnnotationTarget.EXPRESSION)
                annotation class Alphabetical
            """,
        )
}
