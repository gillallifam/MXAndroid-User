package com.example.myapplication1

import android.icu.text.NumberFormat
import android.os.Bundle
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.myapplication1.p2pNet.filterOptions
import com.example.myapplication1.p2pNet.p2pViewModel
import com.example.myapplication1.p2pNet.updateFilter
import com.example.myapplication1.types.Product
import com.example.myapplication1.ui.theme.MyApplication1Theme
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
                Row(modifier = Modifier.padding(5.dp)) {
                    if (p2pViewModel!!.selectedProducts[index].img != null) {
                        Image(
                            bitmap = p2pViewModel!!.selectedProducts[index].img!!.asImageBitmap(),
                            contentDescription = "image",
                            modifier = Modifier
                                .padding(8.dp)
                                .size(60.dp)
                                .clip(RoundedCornerShape(CornerSize(6.dp)))
                                .align(alignment = Alignment.CenterVertically)
                        )
                    }
                    Column {
                        Text(text = prod.nameSho, modifier = Modifier.padding(5.dp, 5.dp))
                        Text(
                            text = NumberFormat.getCurrencyInstance(Locale("PT", "br"))
                                .format(prod.price), modifier = Modifier.padding(5.dp, 5.dp)
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun RecyclerView(products: List<Product>) {
        LazyColumn {
            itemsIndexed(items = products) { index, prod ->
                ProdCard(index, prod)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        updateFilter()

        setContent {
            MyApplication1Theme {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Top,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Browser Activity",
                        modifier = Modifier.padding(20.dp)
                    )
                    Button(
                        //enabled = viewModel!!.p2pState == "offline",
                        onClick = {
                            val filterIndex = filterOptions.indexOf(p2pViewModel!!.filter)
                            val nextIndex = filterIndex + 1
                            if (nextIndex == filterOptions.size) {
                                p2pViewModel!!.filter = filterOptions[0]
                            } else {
                                p2pViewModel!!.filter = filterOptions[nextIndex]
                            }
                            updateFilter()
                        }) {
                        Text(p2pViewModel!!.filter)
                    }
                    RecyclerView(p2pViewModel!!.selectedProducts)
                }
            }
        }
    }
}
