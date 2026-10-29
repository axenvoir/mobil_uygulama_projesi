package com.axenvoir.expira.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.axenvoir.expira.R

@Composable
fun OnboardingOverlay(
    visible: Boolean,
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    if (!visible) return
    var step by remember { mutableIntStateOf(0) }
    val pages = listOf(
        OnboardingPage(
            icon = Icons.Filled.DateRange,
            titleRes = R.string.onboarding_title_1,
            textRes = R.string.onboarding_text_1,
        ),
        OnboardingPage(
            icon = Icons.Filled.Settings,
            titleRes = R.string.onboarding_title_2,
            textRes = R.string.onboarding_text_2,
        ),
        OnboardingPage(
            icon = Icons.Filled.Edit,
            titleRes = R.string.onboarding_title_3,
            textRes = R.string.onboarding_text_3,
            actionLabelRes = R.string.open_settings,
        ),
    )
    val page = pages[step]

    val pageContentDescription = when (page.icon) {
        Icons.Filled.DateRange -> stringResource(R.string.onboarding_icon_date_range)
        Icons.Filled.Settings -> stringResource(R.string.onboarding_icon_settings)
        Icons.Filled.Edit -> stringResource(R.string.onboarding_icon_edit)
        else -> null
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                page.icon,
                contentDescription = pageContentDescription,
                tint = MaterialTheme.colorScheme.primary,
            )
        },
        title = { Text(stringResource(page.titleRes), fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 320.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(stringResource(page.textRes))
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (step > 0) {
                    TextButton(onClick = { step-- }) {
                        Text(stringResource(R.string.onboarding_back))
                    }
                }
                Button(
                    onClick = {
                        if (step < pages.lastIndex) {
                            step++
                        } else {
                            if (page.actionLabelRes != null) {
                                onOpenSettings()
                            }
                            onDismiss()
                        }
                    },
                ) {
                    Text(
                        stringResource(
                            if (step < pages.lastIndex) R.string.onboarding_next
                            else page.actionLabelRes ?: R.string.onboarding_done
                        )
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.onboarding_skip))
            }
        },
    )
}

private data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val titleRes: Int,
    val textRes: Int,
    val actionLabelRes: Int? = null,
)
