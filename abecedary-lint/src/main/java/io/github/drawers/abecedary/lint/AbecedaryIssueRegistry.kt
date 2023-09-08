// Copyright (C) 2023 David Rawson
// SPDX-License-Identifier: Apache-2.0
package io.github.drawers.abecedary.lint

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.Issue

class AbecedaryIssueRegistry : IssueRegistry() {

    override val vendor: Vendor =
        Vendor(
            vendorName = "abecedary",
            identifier = "abecedary-lint",
            feedbackUrl = "https://github.com/drawers/abecedary",
            contact = "https://github.com/drawers/abecedary",
        )

    override val issues: List<Issue> = listOf(
        EnumEntryOrderDetector.ISSUE,
        SealedSubtypeOrderDetector.ISSUE,
        VarargOrderDetector.ISSUE,
    )
}
