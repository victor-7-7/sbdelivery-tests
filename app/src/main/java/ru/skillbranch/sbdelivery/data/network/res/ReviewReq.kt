package ru.skillbranch.sbdelivery.data.network.res

import java.io.Serializable

data class ReviewReq(
    val date:Long,
    val rating:Int,
    val message:String
): Serializable