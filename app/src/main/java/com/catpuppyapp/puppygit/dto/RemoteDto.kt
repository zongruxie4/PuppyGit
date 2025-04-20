package com.catpuppyapp.puppygit.dto

import android.content.Context
import androidx.room.Ignore
import com.catpuppyapp.puppygit.constants.Cons
import com.catpuppyapp.puppygit.constants.SpecialCredential
import com.catpuppyapp.puppygit.play.pro.R

class RemoteDto {
    var remoteId=""
    var remoteName=""
    var remoteUrl=""

    /**
     * NOTE:
     * the `credentialId` and `pushCredentialId` maybe is match by domain, in that case, the `actuallyCredentialIdWhenCredentialIdIsMatchByDomain` and `actuallyPushCredentialIdWhenCredentialIdIsMatchByDomain`
     *  will save the actually using id, and the crenentialName/Pass/Type 's value is related about the actually credential/pushCredential id
     * if credentialId is match by domain, this will replace to the value of `actuallyCredentialIdWhenCredentialIdIsMatchByDomain` related credential
     */
    var credentialId:String?=""  // this id maybe match by domain,
    var credentialName:String?=""
    var credentialVal:String?=""
    var credentialPass:String?=""  // the pass is encrypted, will decrypt when using
    var credentialType:Int = Cons.dbCredentialTypeHttp
    var pushUrl=""
    var pushCredentialId:String?=""   // this id maybe match by domain,
    var pushCredentialName:String?=""
    var pushCredentialVal:String?=""
    var pushCredentialPass:String?=""  // the pass is encrypted, will decrypt when using
    var pushCredentialType:Int = Cons.dbCredentialTypeHttp

    var repoId=""
    var repoName=""

    @Ignore
    var actuallyCredentialIdWhenCredentialIdIsMatchByDomain:String=""
    @Ignore
    var actuallyPushCredentialIdWhenCredentialIdIsMatchByDomain:String=""

    //Ignore的作用是从数据库查dto的时候忽略此字段
    @Ignore
    var branchMode:Int= Cons.dbRemote_Fetch_BranchMode_All
    @Ignore
    var branchListForFetch:List<String> = listOf()


    /**
     * when push url empty, will use fetch url, that case should set this value to true*
     */
    @Ignore
    var pushUrlTrackFetchUrl:Boolean = false


    fun getLinkedFetchCredentialName():String {
        return getLinkedFetchOrPushCredentialName(true)
    }

    fun getLinkedPushCredentialName():String {
        return getLinkedFetchOrPushCredentialName(false)
    }

    private fun getLinkedFetchOrPushCredentialName(isFetch:Boolean):String {
        val id = if(isFetch) credentialId else pushCredentialId
        val name = if(isFetch) credentialName else pushCredentialName
        val scmbd = SpecialCredential.MatchByDomain
        val scnone = SpecialCredential.NONE

        //若id无效（已删除凭据或空字符串关联NONE），则name为null，这时显示凭据名NONE；反之，若name不为null，则id有效，这时显示凭据名
        return if(id == scmbd.credentialId) scmbd.name else (name ?: "[${scnone.name}]");
    }
}
