package ru.skillbranch.sbdelivery.screens.dishes.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberImagePainter
import ru.skillbranch.sbdelivery.R
import ru.skillbranch.sbdelivery.screens.dishes.data.DishItem
import ru.skillbranch.sbdelivery.screens.root.ui.AppTheme


@OptIn(ExperimentalCoilApi::class)
@Composable
fun DishItem(
    dish: DishItem,
    onClick: (dish: DishItem) -> Unit,
    addToCart: (dish: DishItem) -> Unit
) {
    Card(Modifier.requiredHeight(250.dp)) {
        ConstraintLayout(Modifier.clickable { onClick(dish) }) {

            val (fab, title, poster, price) = createRefs()

            // https://google.github.io/accompanist/coil/
            val painter = rememberImagePainter(
                    data = dish.image,
                    builder = {
                        crossfade(true)
                        placeholder(R.drawable.img_empty_place_holder)
                })

            Image(
                painter = painter,
                contentDescription = "My content description",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxSize()
                    .constrainAs(poster) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            )

            Text(
                fontSize = 16.sp,
                color = MaterialTheme.colors.secondary,
                text = "${dish.price} руб",
                modifier = Modifier
                    .constrainAs(price) {
                        top.linkTo(poster.bottom, margin = 16.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth()

            )

            Text(
                fontSize = 14.sp,
                color = MaterialTheme.colors.onSurface,
                text = dish.title,
                modifier = Modifier
                    .constrainAs(title) {
                        top.linkTo(price.bottom, margin = 8.dp)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
                    .padding(start = 8.dp, end = 8.dp)
                    .fillMaxWidth(),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            FloatingActionButton(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .constrainAs(fab) {
                        top.linkTo(poster.bottom)
                        bottom.linkTo(poster.bottom)
                        end.linkTo(parent.end, margin = 16.dp)
                    },
                onClick = { addToCart(dish) }
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_add_product),
                    contentDescription = "Add Icon",
                    tint = Color.White
                )
            }
        }
    }

}


@Composable
fun <T> LazyGrid(
    items: List<T> = listOf(),
    itemsInRow: Int = 2,
    contentPadding: Dp = 8.dp,
    verticalPadding: Dp = 8.dp,
    itemContent: @Composable LazyItemScope.(T) -> Unit
) {
    // List<List<T>>. Делим список на списки по два блюда и затем
    // собираем двухблюдные списки в новый список
    val chunkedList = items.chunked(itemsInRow)
    LazyColumn(
        contentPadding = PaddingValues(contentPadding),
        verticalArrangement = Arrangement.spacedBy(verticalPadding),
    ) {
        items(chunkedList) { rowList ->
            // Для каждого двухблюдного списка строим горизонтальный ряд из его блюд
            Row {
                repeat(rowList.size) {
                    Box(modifier = Modifier.weight(0.5f)) {
                        itemContent(rowList[it])
                    }
                    // К блюдам в ряду (кроме последнего) добавляем вертикальный спейсер
                    if (it < rowList.size.dec()) Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }
    }
}

@Preview
@Composable
fun DishItemPreview(){
    val dish = DishItem(
        "0",
        "https://www.delivery-club.ru/media/cms/relation_product/32350/312372888_m650.jpg",
        "100",
        "test"
    )
    AppTheme {
        DishItem(dish = dish, onClick = { /*TODO*/ }, addToCart = { /*TODO*/ })
    }
}
