package com.catpuppyapp.puppygit.dto

import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.SpecialCredential
import com.catpuppyapp.puppygit.data.entity.RepoEntity
import com.catpuppyapp.puppygit.git.BranchNameAndTypeDto
import com.catpuppyapp.puppygit.git.CommitDto
import com.catpuppyapp.puppygit.git.FileHistoryDto
import com.catpuppyapp.puppygit.git.SubmoduleDto
import com.catpuppyapp.puppygit.git.TagDto
import com.catpuppyapp.puppygit.service.http.server.ApiDto
import com.catpuppyapp.puppygit.service.http.server.ConfigDto
import com.catpuppyapp.puppygit.settings.AppSettings
import com.catpuppyapp.puppygit.utils.AppModel
import com.catpuppyapp.puppygit.utils.Libgit2Helper
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.getParentRecordedTargetHashForSubmodule
import com.catpuppyapp.puppygit.utils.Libgit2Helper.Companion.isValidGitRepo
import com.catpuppyapp.puppygit.utils.genHttpHostPortStr
import com.github.git24j.core.Commit
import com.github.git24j.core.Oid
import com.github.git24j.core.Repository
import com.github.git24j.core.Submodule

fun createCommitDto(
    commitOid: Oid,
    allBranchList: List<BranchNameAndTypeDto>,
    allTagList:List<TagDto>,
    commit: Commit,
    repoId: String,
    repoIsShallow:Boolean,
    shallowOidList:List<String>,
    settings:AppSettings
): CommitDto {
    val c = CommitDto()
    /*
             var oidStr: String="",
             var branchShortNameList: MutableList<String> = mutableListOf(),  //分支名列表，master origin/master 之类的，能通过看这个判断出是否把分支推送到远程了
             var parentOidStrList: MutableList<String> = mutableListOf(),  //父提交id列表，需要的时候可以根据这个oid取出父提交，然后可以取出父提交的树，用来diff
             var dateTime: String="",
             var author: String="",
             var email: String="",
             var shortMsg:String="", //只包含第一行
             var msg: String="",  //完整commit信息
             var repoId:String="",  //数据库的repoId，用来判断当前是在操作哪个仓库
             var treeOidStr:String="",  //提交树的oid和commit的oid不一样哦
             */
    c.oidStr = commitOid.toString()  // next.toString() or commit.id() ，两者相同，但用 next.toString() 性能更好，因为Oid纯java实现，不需要jni
    c.shortOidStr = Libgit2Helper.getShortOidStrByFull(c.oidStr)
    val commitOidStr = commit.id().toString()
    //添加分支列表
    for (b in allBranchList) {
        if (b.oidStr == commitOidStr) {
            c.branchShortNameList.add(b.shortName)
        }
    }
    //添加tag列表
    for(t in allTagList) {
        if(t.targetFullOidStr == commitOidStr) {
            c.tagShortNameList.add(t.shortName)
        }
    }

    //添加parent列表，合并的提交就会有多个parent，一般都是1个
    val parentCount = commit.parentCount()
    if (parentCount > 0) {
        var pc = 0
        while (pc < parentCount) {
            val parentOidStr = commit.parentId(pc).toString()
            c.parentOidStrList.add(parentOidStr)
            c.parentShortOidStrList.add(Libgit2Helper.getShortOidStrByFull(parentOidStr))
            pc++
        }
    }
    c.dateTime = Libgit2Helper.getDateTimeStrOfCommit(commit, settings)
    c.originTimeOffsetInMinutes = commit.timeOffset()

    val commitSignature = commit.author()  // git log 命令默认输出的author
    c.author = commitSignature.name
    c.email = commitSignature.email

    val committer = commit.committer()  //实际提交的人
    c.committerUsername = committer.name
    c.committerEmail = committer.email

    c.shortMsg = commit.summary()
    c.msg = commit.message()
    c.repoId = repoId
    c.treeOidStr = commit.treeId().toString()

    if(repoIsShallow && shallowOidList.contains(c.oidStr)) {
        c.isGrafted=true  //当前提交是shallow root
    }

    return c
}


suspend fun updateRemoteDtoList(repo: Repository, remoteDtoList: List<RemoteDto>, onErr:(errRemote: RemoteDto, e:Exception)->Unit={r,e->}) {
    remoteDtoList.forEach {
        try {
            updateRemoteDto(repo, it)

            val credDb = AppModel.dbContainer.credentialRepository
            val matchByDomainId = SpecialCredential.MatchByDomain.credentialId
            if(it.credentialId == matchByDomainId) {
                val actuallyCred = credDb.getByIdAndMatchByDomain(matchByDomainId, it.remoteUrl)
                if(actuallyCred!=null) {
                    it.actuallyCredentialIdWhenCredentialIdIsMatchByDomain = actuallyCred.id
                    it.credentialName = actuallyCred.name
                    it.credentialVal = actuallyCred.value
                    it.credentialPass = actuallyCred.pass
                    it.credentialType = actuallyCred.type
                }
            }

            if(it.pushCredentialId == matchByDomainId) {
                // when reached here, the pushUrl is set to actually using url by `updateRemoteDto()`
                val actuallyCred = credDb.getByIdAndMatchByDomain(matchByDomainId, it.pushUrl)
                if(actuallyCred!=null) {
                    it.actuallyPushCredentialIdWhenCredentialIdIsMatchByDomain = actuallyCred.id
                    it.pushCredentialName = actuallyCred.name
                    it.pushCredentialVal = actuallyCred.value
                    it.pushCredentialPass = actuallyCred.pass
                    it.pushCredentialType = actuallyCred.type
                }
            }
        }catch (e:Exception) {
            onErr(it, e)
        }
    }
}

fun updateRemoteDto(repo: Repository, remoteDto: RemoteDto) {
    val remoteName = remoteDto.remoteName
    val remote = Libgit2Helper.resolveRemote(repo, remoteName) ?: return

    //更新remoteUrl(即git config文件中的url)、pushUrl
    remoteDto.remoteUrl = remote.url()
    remoteDto.pushUrl = remote.pushurl()?:""

    // if push url not set, use same as fetch url(remoteUrl)
    // use isBlank() check the pushUrl is fine, if pushurl exists but has blank value, still will using fetch url
    if(remoteDto.pushUrl.isBlank()) {
        remoteDto.pushUrl = remoteDto.remoteUrl
        remoteDto.pushUrlTrackFetchUrl = true
    }

    //更新branchMode
    val (isAll, branchNameList) = Libgit2Helper.getRemoteFetchBranchList(remote)
    if(isAll) {
        remoteDto.branchMode = Cons.dbRemote_Fetch_BranchMode_All
        remoteDto.branchListForFetch = emptyList()  //fetch refspec为所有分支的时候用不到分支列表，设个空列表即可
    }else {
        remoteDto.branchMode = Cons.dbRemote_Fetch_BranchMode_CustomBranches
        remoteDto.branchListForFetch = branchNameList  //自定义分支列表，列表值是指定的分支的名字
    }
}


fun createSubmoduleDto(
    sm: Submodule,
    smName: String,
    parentWorkdirPathNoSlashSuffix: String,
    invalidUrlAlertText: String
): SubmoduleDto {
    val smRelativePath = sm.path()
    val smFullPath = parentWorkdirPathNoSlashSuffix + Cons.slash + smRelativePath.removePrefix(Cons.slash)

    // [fixed, the reason was pass NULL to jni StringUTF method in c codes] if call submodule.url() it will crashed when url invalid
    val smUrl = sm.url() ?: ""
    //another way to get url from .gitsubmodules, is read info by kotlin, 100% safe
    //                val smUrl = getValueFromGitConfig(parentDotGitModuleFile, "submodule.$name.url")
    val smDto = SubmoduleDto(
        name = smName,
        relativePathUnderParent = smRelativePath,
        fullPath = smFullPath,
        cloned = isValidGitRepo(smFullPath),
        remoteUrl = smUrl,
        targetHash = getParentRecordedTargetHashForSubmodule(sm),
        tempStatus = if (smUrl.isBlank()) invalidUrlAlertText else "",
        location = Libgit2Helper.getSubmoduleLocation(sm)
    )

    return smDto
}



fun createFileHistoryDto(
    commitOidStr: String,
    treeEntryOidStr:String,
    commit: Commit,
    repoId: String,
    fileRelativePathUnderRepo:String,
    settings: AppSettings
): FileHistoryDto {
    val obj = FileHistoryDto()

    obj.filePathUnderRepo = fileRelativePathUnderRepo
    obj.treeEntryOidStr = treeEntryOidStr
    obj.commitOidStr = commitOidStr
    obj.dateTime = Libgit2Helper.getDateTimeStrOfCommit(commit, settings)
    obj.originTimeOffsetInMinutes = commit.timeOffset()

    val commitSignature = commit.author()  // git log 命令默认输出的author
    obj.authorUsername = commitSignature.name
    obj.authorEmail = commitSignature.email

    val committer = commit.committer()  //实际提交的人
    obj.committerUsername = committer.name
    obj.committerEmail = committer.email

    obj.shortMsg = commit.summary()
    obj.msg = commit.message()
    obj.repoId = repoId

    return obj
}

fun genConfigDto(
    repoEntity: RepoEntity,
    settings: AppSettings
):ConfigDto {
    val host = settings.httpService.listenHost
    val port = settings.httpService.listenPort
    val token = settings.httpService.tokenList.let { if(it.isEmpty()) "" else it.first() }

    return ConfigDto(
        repoName = repoEntity.repoName,
        repoId = repoEntity.id,
        api = ApiDto(
            protocol = "http",
            host = host,
            port = port,
            token = token,
            pull = "/pull",
            push = "/push",
            //少加点参数，少写少错
            pull_example = "${genHttpHostPortStr(host, port.toString())}/pull?token=$token&repoNameOrId=${repoEntity.repoName}",
            push_example = "${genHttpHostPortStr(host, port.toString())}/push?token=$token&repoNameOrId=${repoEntity.repoName}",
        )
    )
}
