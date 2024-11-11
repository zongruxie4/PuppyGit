package com.catpuppyapp.puppygit.settings

import kotlinx.serialization.Serializable

@Serializable
data class SshSettings(
    /**
     * if enable, will allow when ssh connect to a host doesn't exists in the `known_hosts` file;
     * if disable, only allow hosts in the `known_hosts`
     */
    var allowUnknownHosts:Boolean = true
)
