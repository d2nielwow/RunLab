package com.dmirzayants.runlab.ui.common

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.dmirzayants.runlab.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RunLabTopBar(
    userName: String,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val title = if (userName.isBlank()) {
        stringResource(R.string.app_name)
    } else {
        stringResource(R.string.toolbar_lets_go, userName)
    }
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}
