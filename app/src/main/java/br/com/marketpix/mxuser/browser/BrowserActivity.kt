package br.com.marketpix.mxuser.browser

import android.icu.text.NumberFormat
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import br.com.marketpix.mxuser.p2pNet.mainContext
import br.com.marketpix.mxuser.p2pNet.p2pViewModel
import br.com.marketpix.mxuser.p2pNet.targetShop
import br.com.marketpix.mxuser.p2pNet.updateFilter
import br.com.marketpix.mxuser.types.Product
import br.com.marketpix.mxuser.ui.theme.MXUserTheme
import java.util.Locale

class BrowserActivity : ComponentActivity() {

    @Composable
    fun ProdCard(index: Int, prod: Product) {
        //var visible by remember { mutableStateOf(true) }
        val density = LocalDensity.current
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically {
                with(density) { -40.dp.roundToPx() }
            } + expandVertically(
                expandFrom = Alignment.Top
            ) + fadeIn(
                initialAlpha = 0.3f
            ),
            exit = slideOutVertically() + shrinkVertically() + fadeOut()
        ) {
            Card(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 5.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(CornerSize(10.dp)),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )
            ) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
                    .clickable(enabled = true) {
                        p2pViewModel!!.selectedProd =
                            p2pViewModel!!.selectedProducts[index]
                        p2pViewModel!!.dialogProdState.value = true
                    }) {
                    if (p2pViewModel!!.selectedProducts[index].img != null) {
                        BadgedBox(
                            badge = {
                                val cartItem = p2pViewModel!!.cartItems[prod.cod]
                                if (cartItem != null) {
                                    Badge(
                                        Modifier
                                            .offset(y = 90.dp, x = (-16).dp)
                                            .size(24.dp),
                                        containerColor = Color.Red,
                                        contentColor = Color.White,
                                    ) { Text(cartItem.qnt.toString()) }
                                } else {
                                    Badge(
                                        Modifier.size(0.dp),
                                    )
                                }

                            }
                        ) {
                            Image(
                                bitmap = p2pViewModel!!.selectedProducts[index].img!!.asImageBitmap(),
                                contentDescription = "image",
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(96.dp)
                                    .clip(RoundedCornerShape(CornerSize(6.dp)))
                            )
                        }

                    }
                    Column {
                        Text(text = prod.nameSho, modifier = Modifier.padding(5.dp, 5.dp))
                        Text(
                            text = "${
                                NumberFormat.getCurrencyInstance(Locale("PT", "br"))
                                    .format(prod.price)
                            } ${if (prod.qnt > 0) "x ${prod.qnt}" else ""}",
                            modifier = Modifier.padding(5.dp, 5.dp)
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(end = 8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(modifier = Modifier.size(40.dp),
                                onClick = {
                                    val hasProd = p2pViewModel!!.cartItems[prod.cod]
                                    if (hasProd != null) {
                                        hasProd.qnt = hasProd.qnt.plus(1)
                                    } else {
                                        val cp = prod.copy()
                                        cp.qnt = 1
                                        cp.img = prod.img
                                        p2pViewModel!!.cartItems[prod.cod] = cp
                                    }
                                    updateFilter()
                                }) {
                                Icon(
                                    modifier = Modifier.size(36.dp),
                                    imageVector = Icons.Filled.Add,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RecyclerView(products: List<Product>) {
        LazyColumn(Modifier.padding(bottom = 50.dp)) {
            itemsIndexed(items = products) { index, prod ->
                ProdCard(index, prod)
            }
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateFilter()
        setContent {
            MXUserTheme {
                Scaffold(
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick = {
                                p2pViewModel!!.dialogCartState.value = true
                                if (p2pViewModel!!.cartItems.isNotEmpty()) {
                                    p2pViewModel!!.dialogCartState.value = true
                                } else {
                                    Toast.makeText(
                                        mainContext,
                                        "No items in cart",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            },
                            content = {
                                BadgedBox(
                                    badge = {
                                        if (p2pViewModel!!.itemsInCart.intValue > 0) {
                                            Badge {
                                                Text(text = p2pViewModel!!.itemsInCart.intValue.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.ShoppingBasket,
                                        contentDescription = "ShoppingBasket"
                                    )
                                }
                            },
                        )
                    }
                ) { innerPadding ->
                    when {
                        p2pViewModel!!.dialogProdState.value -> {
                            DialogProducts(
                                onDismissRequest = {
                                    p2pViewModel!!.dialogProdState.value = false
                                    updateFilter()
                                },
                            )
                        }

                        p2pViewModel!!.dialogCatState.value -> {
                            DialogCats(
                                onDismissRequest = { p2pViewModel!!.dialogCatState.value = false },
                            )
                        }

                        p2pViewModel!!.dialogCartState.value -> {
                            DialogCart2(
                                onDismissRequest = { p2pViewModel!!.dialogCartState.value = false },
                            )
                        }

                        p2pViewModel!!.dialogUserState.value -> {
                            DialogUser(
                                onDismissRequest = { p2pViewModel!!.dialogUserState.value = false },
                            )
                        }

                        p2pViewModel!!.dialogPaymentState.value -> {
                            DialogPayment(
                                onDismissRequest = {
                                    p2pViewModel!!.dialogPaymentState.value = false
                                },
                            )
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            IconButton(modifier = Modifier.then(Modifier.size(40.dp)),
                                onClick = { p2pViewModel!!.dialogCatState.value = true }) {
                                Icon(
                                    modifier = Modifier.size(36.dp),
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Menu"
                                )
                            }

                            Text(text = targetShop)

                            IconButton(modifier = Modifier.size(40.dp),
                                onClick = { p2pViewModel!!.dialogUserState.value = true }) {
                                Icon(
                                    modifier = Modifier.size(36.dp),
                                    imageVector = Icons.Filled.Person,
                                    contentDescription = "Menu"
                                )
                            }
                        }
                        RecyclerView(p2pViewModel!!.selectedProducts)
                    }

                }
            }
        }
    }
}
