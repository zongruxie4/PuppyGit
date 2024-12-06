package com.catpuppyapp.puppygit.compose

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.catpuppyapp.puppygit.play.pro.R


@Composable
fun PasswordTextFiled(
    password: MutableState<String>,
    passwordVisible: MutableState<Boolean>,
    label:String,
    placeholder:String = "",
    singleLine:Boolean = true,
    focusRequest:FocusRequester? = null,
    errMsg:MutableState<String>,
    enabled:Boolean = true,
    paddingValues:PaddingValues = PaddingValues(10.dp),
    keyboardOptions: KeyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
    keyboardActions: KeyboardActions = KeyboardActions.Default,
) {
    TextField(
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .then(
                if(focusRequest != null) {
                    Modifier.focusRequester(focusRequest)
                }else {
                    Modifier
                }
            )
        ,
        singleLine = singleLine,
        value = password.value,
        onValueChange = {
            password.value = it
            errMsg.value = ""
        },
        label = { Text(label) },
        placeholder = { Text(placeholder) },


        isError = errMsg.value.isNotEmpty(),
        supportingText = {
            if(errMsg.value.isNotEmpty()) {
                MySelectionContainer {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = errMsg.value,
                        color = MaterialTheme.colorScheme.error
                    )
                }

            }
        },
        visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        trailingIcon = trailIcon@{
            //若输入框未启用则不允许切换是否显示密码
            if(enabled.not()) {
                return@trailIcon
            }

            val image = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff

            // Please provide localized description for accessibility services
            val description = if (passwordVisible.value) stringResource(R.string.hide_password) else stringResource(R.string.show_password)

            IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                // contentDescription is for accessibility
                Icon(imageVector = image, contentDescription = description)
            }
        }

    )
}
