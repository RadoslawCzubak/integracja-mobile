package pl.rczubak.stripetest.ui.order

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.CoffeeTheme
import org.koin.androidx.compose.koinViewModel
import pl.rczubak.stripetest.R
import pl.rczubak.stripetest.domain.model.MenuItem
import pl.rczubak.stripetest.domain.model.Order
import pl.rczubak.stripetest.domain.model.OrderStatus
import pl.rczubak.stripetest.ui.login.UserData
import pl.rczubak.stripetest.ui.order.model.OrderEvent

@Composable
fun OrderScreen(
    paddingValues: PaddingValues,
    userData: UserData,
    onLogout: () -> Unit,
    onPayOrder: (orderId: Int) -> Unit
) {
    val viewModel: OrderViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.setEvent(OrderEvent.RefreshOrder)
                    viewModel.setEvent(OrderEvent.RefreshLoyalty)
                    viewModel.setEvent(OrderEvent.RefreshMenu)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)

        // When the effect leaves the Composition, remove the observer
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    OrderScreenContent(
        userInfo = { UserInfoBar(userData = userData, onLogout = onLogout) },
        loyaltyRow = { Text("Punkty lojalnościowe: ${uiState.loyaltyPoints}") },
        orderMenu = {
            OrderMenu(
                menuItems = uiState.menuItems,
                chosenItemsIds = uiState.chosenMenuItemsIds,
                onOrder = { viewModel.setEvent(OrderEvent.CreateOrder) },
                onItemChosen = {
                    viewModel.setEvent(
                        OrderEvent.ChooseMenuItem(it)
                    )
                },
                onMenuRefresh = { viewModel.setEvent(OrderEvent.RefreshMenu) })
        },
        orderList = {
            OrderList(
                orders = uiState.orders,
                onOrderRefresh = { viewModel.setEvent(OrderEvent.RefreshOrder) },
                onPayOrder = onPayOrder
            )
        },
        modifier = Modifier.padding(paddingValues)
    )
}

@Composable
fun OrderScreenContent(
    userInfo: @Composable () -> Unit,
    loyaltyRow: @Composable () -> Unit,
    orderMenu: @Composable () -> Unit,
    orderList: @Composable () -> Unit,
    modifier: Modifier
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(scrollState)
    ) {
        userInfo()
        loyaltyRow()
        orderMenu()
        orderList()
    }
}

@Composable
fun OrderMenu(
    menuItems: List<MenuItem>,
    onMenuRefresh: () -> Unit,
    onItemChosen: (Int) -> Unit,
    chosenItemsIds: List<Int>,
    onOrder: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text("Menu", style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold))
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh Menu",
            modifier = Modifier.clickable {
                onMenuRefresh()
            })
    }
    menuItems.forEach {
        MenuItemView(
            item = it,
            onItemClick = onItemChosen,
            isChosen = it.id in chosenItemsIds
        )
    }
    AnimatedVisibility(visible = chosenItemsIds.isNotEmpty()) {
        Button(
            onClick = onOrder, modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(text = "Zamów")
        }
    }

}

@Composable
fun MenuItemView(item: MenuItem, onItemClick: (Int) -> Unit, isChosen: Boolean) {
    Card(elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChosen) Color(0xffbdffbd) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 4.dp)
            .clickable { onItemClick(item.id) }

    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.eat),
                contentDescription = "Menu Item",
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.weight(3f))
            Text(item.name, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.weight(3f))
            Text(item.price, modifier = Modifier.padding(16.dp))
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun OrderList(orders: List<Order>, onPayOrder: (orderId: Int) -> Unit, onOrderRefresh: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            "Zamówienia",
            style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 24.sp),
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Refresh Menu",
            modifier = Modifier.clickable {
                onOrderRefresh()
            })
    }

    if (orders.isNotEmpty())
        orders.forEach {
            OrderItem(order = it, onPayOrder = onPayOrder)
        }
    else
        Text(
            "Brak zamówień", modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        )
}

@Composable
fun OrderItem(order: Order, onPayOrder: (orderId: Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(),
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (expanded) Modifier.heightIn(min = 60.dp) else Modifier
            )
            .clip(RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp)
            .then(if (order.status == OrderStatus.ACCEPTED) Modifier.clickable {
                expanded = !expanded
            } else Modifier)

    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioHighBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(
                        Modifier.fillMaxSize()
                    )
            ) {
                Text(
                    text = order.id.toString(),
                    style = TextStyle(fontSize = 30.sp, fontWeight = FontWeight.SemiBold)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Cena", style = TextStyle(fontSize = 12.sp))
                    Text(text = "${order.price}")
                }
                Image(
                    painter = painterResource(
                        id = when (order.status) {
                            OrderStatus.PAID -> R.drawable.paid
                            OrderStatus.ACCEPTED -> R.drawable.card
                            OrderStatus.CANCELLED -> R.drawable.multiply
                            OrderStatus.DELIVERED -> R.drawable.meal
                        }
                    ), contentDescription = "",
                    modifier = Modifier.size(24.dp)
                )
            }
            if (expanded && order.status == OrderStatus.ACCEPTED) {
                Button(
                    onClick = { onPayOrder(order.id) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding()
                ) {
                    Text("Opłać zamówienie")
                }
            }
        }
    }

}


@Composable
fun UserInfoBar(
    userData: UserData, onLogout: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(userData.profilePictureUrl)
                .crossfade(true).build(),
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "user image",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = userData.username ?: "Anonymous", style = TextStyle(
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onLogout() }, modifier = Modifier.height(40.dp)
        ) {
            Text(text = "Wyloguj")
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun OrderScreenPreview() {
    CoffeeTheme() {
        OrderScreenContent(
            userInfo = {
                UserInfoBar(
                    userData = UserData(userId = "", "Radek", ""),
                    onLogout = {})
            },
            loyaltyRow = { Text("Punkty lojalnościowe: ${104}") },
            orderMenu = {
                OrderMenu(
                    menuItems = listOf(
                        MenuItem(id = 1, name = "Kawa", price = "15.95zł"),
                        MenuItem(id = 2, name = "Pierogi", price = "15.95zł"),
                        MenuItem(id = 3, name = "Chleb", price = "15.95zł"),
                        MenuItem(id = 4, name = "Frytki", price = "15.95zł"),
                    ),
                    chosenItemsIds = listOf(2),
                    onItemChosen = {},
                    onMenuRefresh = {},
                    onOrder = {}
                )
            },
            orderList = {
                OrderList(
                    orders = listOf(
                        Order(userId = "", id = 1, price = 42.90f, status = OrderStatus.ACCEPTED),
                        Order(userId = "", id = 2, price = 10.90f, status = OrderStatus.CANCELLED),
                        Order(userId = "", id = 3, price = 23.90f, status = OrderStatus.PAID),
                        Order(userId = "", id = 4, price = 23.90f, status = OrderStatus.DELIVERED),
                    ), onPayOrder = {}, onOrderRefresh = {}
                )
            },
            modifier = Modifier.padding()
        )
    }

}

