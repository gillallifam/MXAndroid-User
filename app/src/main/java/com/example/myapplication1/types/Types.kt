package com.example.myapplication1.types

data class OfferExtras(
    val peerOwner: String,
    val peerProfType: String = "user",
    val shop: String,
    val userid: String,
    val deviceUUID: String,
    val username: String,
    val userpass: String,
    //val FBToken: String,
    val profile: String,
)

data class CustomSDPClass(
    val type: String,
    val sdp: String,
    val extras: OfferExtras,
)

data class PackedOffer(
    val offer: CustomSDPClass,
)

data class Cmd(
    var pid: String? = null,
    val cmd: String,
    val age: Long? = null,
    val from: String,
    val retry: Int = 1,
    var attempts: Int = 1,
    val load: Any? = null,
)

data class CmdResp(
    var pid: String? = null,
    val cmd: String,
    var status: String = "error",
    val content: String,
    val extras: String?,
)

data class ImageModel(
    var cod: String,
    val img: String,
)

data class ImgLoad(
    val cod: String? = null,
    val img: String? = null,
)
