package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class DevSettings (
    var singleDiffOn: Boolean = false,

)
