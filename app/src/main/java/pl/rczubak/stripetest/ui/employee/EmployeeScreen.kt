package pl.rczubak.stripetest.ui.employee

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.compose.CoffeeTheme
import org.koin.androidx.compose.koinViewModel
import pl.rczubak.stripetest.R
import pl.rczubak.stripetest.domain.model.Order
import pl.rczubak.stripetest.domain.model.OrderStatus

@Composable
fun EmployeeScreen(
    paddingValues: PaddingValues
) {
    val viewModel: EmployeeViewModel = koinViewModel()
    val uiState: EmployeeState by viewModel.uiState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    viewModel.setEvent(EmployeeEvent.RefreshOrders)
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
    EmployeeScreenContent(
        ordersList = {
            OrdersList(
                orders = uiState.orders,
                onServe = {
                    viewModel.setEvent(EmployeeEvent.ServeMeal(it))
                },
                isLoading = uiState.isOrderLoading
            )
        },
        modifier = Modifier.padding(paddingValues)
    )
}

@Composable
fun EmployeeScreenContent(
    ordersList: @Composable () -> Unit,
    modifier: Modifier,
) {
    Column(verticalArrangement = Arrangement.Center, modifier = modifier) {
        ordersList()
    }
}

@Composable
fun OrdersList(orders: List<Order>, onServe: (Int) -> Unit, isLoading: Boolean) {
    Text(
        text = "Zamówienia",
        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp)
    )
    if (!isLoading)
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 100.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(orders) { order ->
                OrderItem(order = order, onServe = onServe)
            }
        }
    else
        CircularProgressIndicator(modifier = Modifier.size(40.dp))
}

@Composable
fun OrderItem(order: Order, onServe: (Int) -> Unit) {
    var expanded by remember {
        mutableStateOf(false)
    }
    Box(Modifier.aspectRatio(1f)) {
        Card(
            elevation = CardDefaults.cardElevation(4.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp)
                .clickable { expanded = !expanded }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row {
                        Text(
                            "Zamówienie" +
                                    " ${order.id}",
                            style = TextStyle(
                                textAlign = TextAlign.Center,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(
                                id = when (order.status) {
                                    OrderStatus.PAID -> R.drawable.paid
                                    OrderStatus.ACCEPTED -> R.drawable.card
                                    OrderStatus.CANCELLED -> R.drawable.multiply
                                    OrderStatus.DELIVERED -> R.drawable.meal
                                }
                            ), contentDescription = "",
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "${order.price} zł",
                            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = expanded, modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Button(
                        onClick = { onServe.invoke(order.id) },
                        shape = RectangleShape,
                    ) {
                        Text(text = "Wydaj")
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun EmployeePreview() {
    CoffeeTheme {
        EmployeeScreenContent(ordersList = {
            OrdersList(
                orders = listOf(
                    Order("sda", 2, 13.95f, OrderStatus.ACCEPTED),
                    Order("sda3", 4, 13.95f, OrderStatus.PAID),
                    Order("sda4", 3, 13.95f, OrderStatus.ACCEPTED),
                ), onServe = {}, isLoading = false
            )
        }, modifier = Modifier)
    }
}