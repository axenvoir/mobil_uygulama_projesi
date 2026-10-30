package com.axenvoir.expira.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val windowHeightPx = LocalWindowInfo.current.containerSize.height
    val bodyMaxHeight = with(LocalDensity.current) { windowHeightPx.toDp() * 0.34f }
        .coerceAtLeast(112.dp)
        .coerceAtMost(240.dp)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(page.icon, contentDescription = null, modifier = Modifier.size(28.dp))
                }
            }
        },
        title = {
            Text(
                text = stringResource(page.titleRes),
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = bodyMaxHeight)
                    .verticalScroll(rememberScrollState()),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    pages.indices.forEach { index ->
                        if (index > 0) Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier.size(
                                width = if (index == step) 24.dp else 8.dp,
                                height = 8.dp,
                            ),
                            shape = CircleShape,
                            color = if (index == step) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                        ) { }
                    }
                }
                Text(stringResource(page.textRes))
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (step > 0) {
                    TextButton(
                        onClick = { step-- },
                        modifier = Modifier.heightIn(min = 48.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.onboarding_back),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
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
                        text = stringResource(
                            if (step < pages.lastIndex) R.string.onboarding_next
                            else page.actionLabelRes ?: R.string.onboarding_done
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.heightIn(min = 48.dp),
            ) {
                Text(
                    text = stringResource(R.string.onboarding_skip),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
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
