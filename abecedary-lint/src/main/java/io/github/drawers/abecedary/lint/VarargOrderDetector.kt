// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.uast.UCallExpression
import org.jetbrains.uast.UElement
import org.jetbrains.uast.UExpression
import org.jetbrains.uast.UMethod
import org.jetbrains.uast.UQualifiedReferenceExpression
import org.jetbrains.uast.isUastChildOf
import org.jetbrains.uast.resolveToUElement
import java.util.EnumSet

class VarargOrderDetector : Detector(), SourceCodeScanner {
    override fun getApplicableUastTypes(): List<Class<out UElement>> =
        listOf(UCallExpression::class.java, UQualifiedReferenceExpression::class.java)

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            /**
             * Qualified reference expressions with the annotation where need
             * to search for children with vararg arguments e.g.,
             *
             *     @Alphabetical listOf("a", "b", "c").forEach { println(it) }`
             *
             */
            private val annotatedQualifiedExpressions = mutableSetOf<UQualifiedReferenceExpression>()

            override fun visitQualifiedReferenceExpression(node: UQualifiedReferenceExpression) {
                node.findAnnotation(Annotation.FULLY_QUALIFIED_NAME) ?: return
                annotatedQualifiedExpressions.add(node)
            }

            override fun visitCallExpression(node: UCallExpression) {
                val explicitAnnotation = node.findAnnotation(Annotation.FULLY_QUALIFIED_NAME)
                val annotatedParent = annotatedQualifiedExpressions.find { node.isUastChildOf(it) }

                if (explicitAnnotation == null && annotatedParent == null) return

                val method = node.resolveToUElement() as? UMethod ?: return

                if (method.parameterList.parametersCount != 1 ||
                    method.parameterList.getParameter(0)?.isVarArgs == false
                ) {
                    return
                }

                // we've found the first match in our parent UQualifiedReferenceExpression
                // so we can remove it from the set to prevent further matches
                if (annotatedParent != null) {
                    annotatedQualifiedExpressions.remove(annotatedParent)
                }

                val zipped =
                    node.valueArguments.sortedBy { it.sourcePsi?.text.orEmpty() }
                        .zip(
                            node.valueArguments,
                        ) { sorted, unsorted ->
                            Entry(expected = sorted, actual = unsorted)
                        }

                val outOfOrder =
                    zipped.firstOrNull { it.expectedText != it.actualText }
                        ?: return

                context.report(
                    issue = ISSUE,
                    location = context.getLocation(node),
                    message =
                        "Vararg out of alphabetical order. Rearrange so that " +
                            "`${outOfOrder.expectedText}` is before `${outOfOrder.actualText}`",
                )
            }
        }
    }

    private data class Entry(
        val expected: UExpression,
        val actual: UExpression,
    ) {
        val expectedText = expected.sourcePsi?.text.orEmpty()
        val actualText = actual.sourcePsi?.text.orEmpty()

        override fun toString(): String {
            return "Entry(expected='$expectedText', actual='$actualText')"
        }
    }

    companion object {
        @JvmField
        val ISSUE =
            Issue.create(
                id = "VarargOrder",
                briefDescription = "Vararg order",
                explanation =
                    "Keeping vararg in alphabetical order, where appropriate, " +
                        "enables quick scanning of the arguments.",
                implementation =
                    Implementation(
                        VarargOrderDetector::class.java,
                        EnumSet.of(
                            Scope.JAVA_FILE,
                            Scope.TEST_SOURCES,
                        ),
                        EnumSet.of(Scope.JAVA_FILE),
                        EnumSet.of(Scope.TEST_SOURCES),
                    ),
                category = Category.PRODUCTIVITY,
                priority = 5,
                severity = Severity.ERROR,
            )
    }
}
