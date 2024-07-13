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
import com.intellij.psi.PsiElement
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement
import org.jetbrains.uast.getParentOfType
import org.jetbrains.uast.kotlin.KotlinUEnumConstant
import java.util.EnumSet

@Suppress("UnstableApiUsage")
class EnumEntryOrderDetector : Detector(), SourceCodeScanner {
    override fun applicableAnnotations(): List<String> = listOf("Alphabetical")

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return true
    }

    private val classToEnumConstants = hashMapOf<UClass, MutableList<Entry>>()

    override fun afterCheckFile(context: Context) {
        for (uClass in classToEnumConstants.keys) {
            val entries = classToEnumConstants[uClass].orEmpty()
            val outOfOrder = entries.firstOutOfOrder() ?: continue
            context.report(
                issue = ISSUE,
                location = context.getLocation(outOfOrder.actual.node),
                message = buildMessage(uClass, outOfOrder),
                quickfixData = null,
            )
        }
        classToEnumConstants.clear()
    }

    @Suppress("UnstableApiUsage")
    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {
        val enumConstant = element as? KotlinUEnumConstant ?: return
        val parentClass = enumConstant.getParentOfType<UClass>(strict = true) ?: return

        classToEnumConstants.getOrPut(parentClass) { mutableListOf() }
            .add(Entry(enumConstant, name = enumConstant.name))
    }

    private fun buildMessage(
        enum: PsiClass,
        outOfOrder: Order,
    ) = buildString {
        append("`${enum.name}` should declare its entries in alphabetical order. ")
        append("Rearrange so that ${outOfOrder.expected.name} ")
        append("is before ${outOfOrder.actual.name}.")
    }

    private data class Entry(
        val node: PsiElement,
        val name: String,
    ) : Comparable<Entry> {
        override fun compareTo(other: Entry): Int {
            return name.compareTo(other.name)
        }
    }

    private data class Order(
        val expected: Entry,
        val actual: Entry,
    )

    private fun List<Entry>.firstOutOfOrder(): Order? {
        for (i in 1 until size) {
            if (get(i - 1).name > get(i).name) {
                return Order(expected = get(i), actual = get(i - 1))
            }
        }
        return null
    }

    companion object {
        @JvmField
        val ISSUE =
            Issue.create(
                id = "EnumEntryOrder",
                briefDescription = "Enum entry order",
                explanation =
                    "Keeping enum entries in alphabetical order, where appropriate, " +
                        "enables quick scanning of a file containing enums and prevents " +
                        "merge conflicts.",
                implementation =
                    Implementation(
                        EnumEntryOrderDetector::class.java,
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
