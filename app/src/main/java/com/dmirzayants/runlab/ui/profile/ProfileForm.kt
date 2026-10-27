package com.dmirzayants.runlab.ui.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dmirzayants.runlab.R

@Composable
fun ProfileForm(
    input: ProfileInput,
    onInputChange: (ProfileInput) -> Unit,
    showErrors: Boolean,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val nameError = showErrors && !input.isNameValid
        OutlinedTextField(
            value = input.name,
            onValueChange = { onInputChange(input.copy(name = it)) },
            label = { Text(stringResource(R.string.field_name)) },
            singleLine = true,
            isError = nameError,
            supportingText = if (nameError) {
                { Text(stringResource(R.string.error_name_required)) }
            } else null,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Next
            ),
            modifier = Modifier.fillMaxWidth()
        )

        val weightError = showErrors && input.parsedWeight == null
        OutlinedTextField(
            value = input.weight,
            onValueChange = { onInputChange(input.copy(weight = it)) },
            label = { Text(stringResource(R.string.field_weight)) },
            suffix = { Text(stringResource(R.string.unit_kg)) },
            singleLine = true,
            isError = weightError,
            supportingText = if (weightError) {
                { Text(stringResource(R.string.error_weight_invalid)) }
            } else null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

fun formatWeight(weightKg: Float): String =
    if (weightKg % 1f == 0f) weightKg.toInt().toString() else weightKg.toString()
