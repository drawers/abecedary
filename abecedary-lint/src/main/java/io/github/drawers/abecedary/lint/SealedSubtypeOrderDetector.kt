// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.detector.api.AnnotationInfo
import com.android.tools.lint.detector.api.AnnotationUsageInfo
import com.android.tools.lint.detector.api.AnnotationUsageType
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Context
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import com.intellij.psi.PsiClass
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.kotlin.KotlinUTypeReferenceExpression
import java.util.EnumSet

class SealedSubtypeOrderDetector : Detector(), SourceCodeScanner {
    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type == AnnotationUsageType.EXTENDS
    }

    override fun applicableAnnotations(): List<String> = listOf("Alphabetical")

    private val classToSealedSubTypes = hashMapOf<SealedTypeInfo, MutableList<UClass>>()

    override fun afterCheckFile(context: Context) {
        for (entry in classToSealedSubTypes.entries) {
            val zipped =
                entry.value.sortedBy { it.name }.zip(entry.value) { sorted, unsorted ->
                    Order(expected = sorted, actual = unsorted)
                }
            val firstOutOfOrder =
                zipped.firstOrNull { it.expected.name != it.actual.name } ?: continue

            context.report(
                issue = ISSUE,
                location = context.getLocation(firstOutOfOrder.actual as UElement),
                message =
                    buildMessage(
                        sealedType = entry.key.sealedOuter,
                        annotated = entry.key.annotatedClass,
                        expectedActual = firstOutOfOrder,
                    ),
            )
        }
        classToSealedSubTypes.clear()
    }

    @Suppress("UnstableApiUsage", "ktlint:standard:no-consecutive-comments")
    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {
        /**
         *     Consider the following code:
         *
         *     @Alphabetical
         *     interface Edible
         *
         *     sealed class Fruit: Edible {
         *         object Banana: Fruit()
         *         object Apple: Fruit()
         *     }
         *
         */

        // `Fruit` in the example
        val typeReference = element as? KotlinUTypeReferenceExpression ?: return

        // `Banana` in the example
        val sealedSubType = typeReference.getParentOfType<UClass>() ?: return

        // `Edible` in the example
        val annotatedClass =
            annotationInfo.annotation.getParentOfType<UClass>() ?: return

        // `Fruit` in the example
        val outerClass = sealedSubType.getParentOfType<UClass>() ?: return
        if (!context.evaluator.isSealed(outerClass)) return

        classToSealedSubTypes.getOrPut(
            SealedTypeInfo(
                sealedOuter = outerClass,
                annotatedClass = annotatedClass,
            ),
        ) { mutableListOf() }
            .add(sealedSubType)
    }

    private fun buildMessage(
        sealedType: PsiClass,
        annotated: PsiClass,
        expectedActual: Order,
    ) = buildString {
        append("`${sealedType.name}` should declare its entries in alphabetical order ")
        if (sealedType == annotated) {
            append("since it ")
        } else {
            append("since its super type `${annotated.name}` ")
        }
        append("is annotated with `@Alphabetical`. ")
        append("Rearrange so that ${expectedActual.expected.name} ")
        append("is before ${expectedActual.actual.name}.")
    }

    private data class SealedTypeInfo(
        /**
         * The sealed parent that has its declarations out of order.
         *
         * In the following example, it would be `Fruit`:
         *
         *     @Alphabetical
         *     sealed class Fruit {
         *         object Banana: Fruit()
         *         object Apple: Fruit()
         *     }
         */
        val sealedOuter: UClass,
        /**
         * Class or interface annotated with the explicit @Alphabetical annotation.
         *
         * In the following example, it would be `Edible`:
         *
         *     @Alphabetical
         *     interface Edible
         *
         *     sealed class Fruit: Edible {
         *         object Banana: Fruit()
         *         object Apple: Fruit()
         *     }
         */
        val annotatedClass: PsiClass,
    )

    private data class Order(
        val expected: UClass,
        val actual: UClass,
    )

    companion object {
        @JvmField
        val ISSUE =
            Issue.create(
                id = "SealedSubtypeOrder",
                briefDescription = "Sealed subtype order",
                explanation =
                    "Keeping sealed subtype declarations in alphabetical order, where appropriate, " +
                        "enables quick scanning of a file containing sealed types and prevents " +
                        "merge conflicts.",
                implementation =
                    Implementation(
                        SealedSubtypeOrderDetector::class.java,
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
