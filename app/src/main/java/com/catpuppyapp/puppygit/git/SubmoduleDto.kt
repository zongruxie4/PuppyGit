package com.catpuppyapp.puppygit.git

import android.content.Context
import androidx.compose.ui.graphics.Color
import com.catpuppyapp.puppygit.play.pro.R
import com.catpuppyapp.puppygit.style.MyStyleKt
import com.catpuppyapp.puppygit.utils.AppModel
import com.github.git24j.core.Submodule

data class SubmoduleDto (
    val name:String,
    val remoteUrl:String,
    val relativePathUnderParent:String,
    val fullPath:String,
    val cloned:Boolean,
    val targetHash:String,  //target commit hash recorded by parent repo

    val location:Set<Submodule.StatusT>,

    var tempStatus:String = "",  // cloning... etc

) {
    private fun getClonedText(activityContext:Context):String{

        return if(cloned) activityContext.getString(R.string.cloned) else activityContext.getString(R.string.not_clone)

    }

    fun getStatus(activityContext:Context):String {
        return tempStatus.ifBlank { getClonedText(activityContext) }
    }

    fun getStatusColor(): Color {
        return if(tempStatus.isNotBlank()) {
            MyStyleKt.TextColor.danger()
        }else if(cloned) {
            Color(0xFF4CAF50)
        }else {
            Color.Unspecified
        }
    }
}
