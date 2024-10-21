package br.com.marketpix.mxuser.types

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

data class Category(
    val name: String,
    val transId: String?,
    val selected: Boolean,
    val fractionable: Boolean?,
)

//list -> {JsonArray@27849} "[{"name":"morango","transId":"prod-opt-lu4ltxc8-item-lu4lu9fs"},{"name":"menta","transId":"prod-opt-lu4ltxc8-item-lu4luiq0"}]"
data class Optional(
    val name: String,
    val transId: String?,
)

//{"name":"pizza","transId":"prod-adt-lu4ls79r","sizes":"prod-size-lu4lqz3r","list":[{"name":"queijo","transId":"prod-adt-lu4ls79r-item-lu4lv0pk","list":[{"name":"pequena","transId":"prod-size-lu4lqz3r-item-lu4lr7lc","price":"6"},{"name":"media","transId":"prod-size-lu4lqz3r-item-lu4lrbkg","price":"9"},{"name":"grande","transId":"prod-size-lu4lqz3r-item-lu4lrf89","price":"12"}]}]}


data class Additional(
    val name: String,
    val transId: String?,
    val sizes: String?,

)