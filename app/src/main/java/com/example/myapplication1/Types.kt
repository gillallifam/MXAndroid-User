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
    val offer: CustomSDPClass
)

data class Cmd(
    var pid: String? = null,
    val cmd: String,
    val age: Int? = null,
    val from: String,
    val load: Any? = null,
)
data class CmdResp(
    var pid: String? = null,
    val cmd: String,
    var success: Boolean = false,
    val content: String,
)

data class ImageModel(
    var cod: String? = null,
    val img: String,
)

data class Product(
    val cod: String,
    val nameSho: String,
    val nameLon: String? = null,
    val qnt: Int,
    val min: Int,
    val cost: Int,
    val price: Double,
    val expiration: String,
    val expAlert: String,
    val provider: String,
    val categories: Array<String>,
    val notify: String,
    val active: Boolean,
    val showStock: Boolean,
    val hasOptionals: Boolean,
    val hasSizes: Boolean,
    val hasAdditionals: Boolean,
    val optionals: Any? = null,
    val additionals: Any? = null,
    val sizes: Any? = null,
    val fractionable: Boolean,
    val translatable: Boolean,
    val description: String,
    val fractionCalc: String,
    val lastUpdate: Long

)

data class Load(
    val cod: String? = null,
    val img: String? = null,
)