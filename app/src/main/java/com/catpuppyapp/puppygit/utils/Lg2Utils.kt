package com.catpuppyapp.puppygit.utils

import com.github.git24j.core.Libgit2
import java.io.File

object Lg2Utils {
    const val libgit2HomeDirName = "lg2home"
    const val sshKnownHostsPath = ".ssh/known_hosts"
    
    private lateinit var lg2Home: File
    private lateinit var knownHosts:File
    
    fun init(homeBaseDirPath:File) {
        createLg2Home(homeBaseDirPath)
        createKnownHostsIfNonExists()

        // make ssh can find the known_hosts file
        Libgit2.optsGitOptSetHomedir(lg2Home.canonicalPath)
    }
    
    fun getLg2Home():File {
        if(lg2Home.exists()) {
            lg2Home.mkdirs()
        }

        return lg2Home
    }

    fun createLg2Home(baseDir:File) {
        lg2Home=createDirIfNonexists(baseDir, libgit2HomeDirName)
    }    
    
    fun createKnownHostsIfNonExists() {
        knownHosts = File(lg2Home.canonicalPath+"/"+sshKnownHostsPath)
        if(knownHosts.exists().not()) {
            knownHosts.parentFile?.mkdirs()
        }
        knownHosts.createNewFile()
    }
    
    fun getKnownHostsFile():File {
        return knownHosts
    }
    
    fun addRulesToKnowHosts(rules:StringBuilder) {
        //TODO()
    }
}
