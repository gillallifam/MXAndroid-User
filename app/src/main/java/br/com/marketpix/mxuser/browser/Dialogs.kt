package br.com.marketpix.mxuser.browser

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.com.marketpix.mxuser.p2pNet.mainContext
import br.com.marketpix.mxuser.p2pNet.p2pViewModel
import br.com.marketpix.mxuser.p2pNet.updateFilter
import br.com.marketpix.mxuser.types.Category
import java.util.Locale

private fun changeCategory(category: Category) {
    p2pViewModel!!.filterCategories.toList()
        .forEach { it.selected = false }
    category.selected = true
    p2pViewModel!!.filterCategory.value = category
    val items = p2pViewModel!!.filterCategories.toList()
    p2pViewModel!!.filterCategories.clear()
    p2pViewModel!!.filterCategories.addAll(items)
    updateFilter()
}

@Composable
fun DialogCats(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                p2pViewModel!!.filterCategories.toList().forEach { category ->
                    val identif = mainContext!!.resources.getIdentifier(
                        category.name, "string",
                        mainContext!!.packageName
                    )
                    Text(
                        text = if (identif > 0) mainContext!!.resources.getString(identif)
                            .uppercase(Locale.ROOT)
                        else category.name.uppercase(Locale.ROOT),
                        color = if (category.selected) Color.White else Color.Gray,
                        fontSize = 24.sp,
                        modifier = Modifier.clickable {
                            changeCategory(category)
                            p2pViewModel!!.dialogCatState.value = false
                        })
                }
            }
        }
    }
}

@Composable
fun DialogProducts(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    bitmap = p2pViewModel!!.selectedProd.img!!.asImageBitmap(),
                    contentDescription = "image",
                    modifier = Modifier
                        .padding(8.dp)
                        .size(250.dp)
                        .clip(RoundedCornerShape(CornerSize(6.dp)))
                        .align(alignment = Alignment.CenterHorizontally)
                )
                Text(
                    text = p2pViewModel!!.selectedProd.nameSho,
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
                Row {
                    Button(
                        onClick = {
                            val hasProd =
                                p2pViewModel!!.cartItems[p2pViewModel!!.selectedProd.cod]
                            if (hasProd != null) {
                                hasProd.qnt = hasProd.qnt.plus(1)
                            } else {
                                val cp = p2pViewModel!!.selectedProd.copy()
                                cp.qnt = 1
                                cp.img = p2pViewModel!!.selectedProd.img
                                p2pViewModel!!.cartItems[p2pViewModel!!.selectedProd.cod] = cp
                            }

                        }) {
                        Text("+")
                    }
                    Button(
                        onClick = {
                            val hasProd =
                                p2pViewModel!!.cartItems[p2pViewModel!!.selectedProd.cod]
                            if (hasProd != null) {
                                if (hasProd.qnt > 0) hasProd.qnt = hasProd.qnt.minus(1)
                                else {
                                    p2pViewModel!!.cartItems.remove(p2pViewModel!!.selectedProd.cod)
                                }
                            }
                        }) {
                        Text("-")
                    }
                }

            }
        }
    }
}

@Composable
fun DialogUser(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Profile",
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun DialogCart(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                Text(
                    text = "Cart items",
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
                Button(onClick = {
                    p2pViewModel!!.dialogCartState.value = false
                    p2pViewModel!!.dialogPaymentState.value = true
                }) {
                    Text(text = "Payment")
                }
            }
        }
    }
}

@Composable
fun DialogPayment(onDismissRequest: () -> Unit) {
    Dialog(
        onDismissRequest = { onDismissRequest() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true,
        ),
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Payment",
                    modifier = Modifier
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}