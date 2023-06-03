package pl.rczubak.stripetest.ui.home

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.compose.CoffeeTheme
import pl.rczubak.stripetest.R
import pl.rczubak.stripetest.domain.model.Reservation
import pl.rczubak.stripetest.domain.model.Table
import pl.rczubak.stripetest.ui.home.model.HomeContract
import pl.rczubak.stripetest.ui.login.UserData
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    navController: NavController,
    userData: UserData?,
    padding: PaddingValues,
    viewModel: HomeViewModel,
    onLogout: () -> Unit
) {
    val dateTime = remember { mutableStateOf(LocalDateTime.now()) }
    val uiState by viewModel.uiState.collectAsState()
    HomeScreenContent(
        padding = padding,
        availableTables = uiState.availableTables,
        userData = userData,
        onTableCheckBtnClicked = { time ->
            viewModel.setEvent(
                HomeContract.HomeEvent.CheckTablesAvailability(
                    time
                )
            )
        },
        chosenTableId = uiState.chosenTableId,
        onTableClick = { tableId ->
            viewModel.setEvent(HomeContract.HomeEvent.ChooseTable(tableId))
        },
        tableAvailabilityStatus = uiState.tableAvailabilityLoading,
        onLogout = onLogout,
        updateDateTime = { newDateTime -> dateTime.value = newDateTime },
        dateTime = dateTime.value,
        onReserveBtn = { viewModel.setEvent(HomeContract.HomeEvent.ReserveTable(dateTime.value)) },
        reservation = uiState.reservation,
        onReservationDelete = { viewModel.setEvent(HomeContract.HomeEvent.CancelReservation) }
    )
}

@Composable
fun HomeScreenContent(
    padding: PaddingValues,
    userData: UserData?,
    availableTables: List<Table>?,
    chosenTableId: Int?,
    tableAvailabilityStatus: Boolean,
    onTableCheckBtnClicked: (LocalDateTime) -> Unit = {},
    onTableClick: (Int) -> Unit,
    onLogout: () -> Unit,
    updateDateTime: (LocalDateTime) -> Unit,
    dateTime: LocalDateTime,
    onReserveBtn: () -> Unit,
    reservation: Reservation?,
    onReservationDelete: (Int) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier
            .verticalScroll(scrollState)
            .padding(padding)
    ) {
        userData?.let {
            UserInfoBar(userData = userData, onLogout = onLogout)
        }

        TableTimePicker(updateDateTime, dateTime = dateTime)

        Button(
            onClick = { onTableCheckBtnClicked(dateTime) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Sprawdź dostępne stoliki")
        }
        if (tableAvailabilityStatus) {
            CircularProgressIndicator(modifier = Modifier.size(40.dp))
        } else {
            availableTables?.let {
                TableAvailabilityView(
                    availableTables, chosenTableId = chosenTableId, onTableClick = onTableClick
                )
                if (chosenTableId in availableTables.map { it.tableId }) {
                    Button(
                        onClick = onReserveBtn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text("Zarezerwuj wybrany stolik")
                    }
                    reservation?.let {
                        ReservationRow(reservation, onReservationDelete = onReservationDelete)
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationRow(reservation: Reservation, onReservationDelete: (Int) -> Unit) {
    Column(Modifier.padding(vertical = 8.dp)) {
        Text(
            "Rezerwacja", modifier = Modifier.padding(horizontal = 16.dp), style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Card(
            elevation = CardDefaults.cardElevation(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ID", style = TextStyle(fontSize = 12.sp))
                    Text(reservation.id.toString(), style = TextStyle(fontSize = 16.sp))
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Time", style = TextStyle(fontSize = 12.sp))
                    Text(
                        reservation.reservationTime.format(DateTimeFormatter.ofPattern("dd-MM-yy HH:mm")),
                        style = TextStyle(fontSize = 16.sp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Table", style = TextStyle(fontSize = 12.sp))
                    Text(
                        reservation.tableId.toString(),
                        style = TextStyle(fontSize = 16.sp),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Box {
                    Image(
                        painter = painterResource(id = R.drawable.ic_baseline_close_24),
                        contentDescription = "Remove reservation",
                        modifier = Modifier
                            .clickable { onReservationDelete(reservation.id) }
                            .size(40.dp)
                    )
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(userData.profilePictureUrl)
                .crossfade(true).build(),
            placeholder = painterResource(id = pl.rczubak.stripetest.R.drawable.ic_launcher_background),
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

@Composable
fun TableTimePicker(
    updateDateTime: (LocalDateTime) -> Unit,
    dateTime: LocalDateTime,
) {
    val timepicker = TimePickerDialog(
        LocalContext.current, { _, hour: Int, minute: Int ->
            val newLocalDateTime = dateTime.with(LocalTime.of(hour, minute))
            updateDateTime(newLocalDateTime)
        }, 12, 0, false
    )
    val datepicker = DatePickerDialog(
        LocalContext.current,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDayOfMonth: Int ->
            val newLocalDateTime =
                dateTime.with(LocalDate.of(selectedYear, selectedMonth, selectedDayOfMonth))
            updateDateTime(newLocalDateTime)
        },
        dateTime.year,
        dateTime.monthValue,
        dateTime.dayOfMonth
    )
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = dateTime.format(DateTimeFormatter.ISO_DATE_TIME),
            style = TextStyle(fontSize = 16.sp, textAlign = TextAlign.Center),
            modifier = Modifier.fillMaxWidth()
        )
        Row() {
            Spacer(modifier = Modifier.width(20.dp))
            Button(onClick = { timepicker.show() }, modifier = Modifier.weight(1f)) {
                Text(text = "Czas")
            }
            Spacer(modifier = Modifier.width(20.dp))
            Button(onClick = { datepicker.show() }, modifier = Modifier.weight(1f)) {
                Text(text = "Data")
            }
            Spacer(modifier = Modifier.width(20.dp))
        }
    }

}

@Composable
fun TableAvailabilityView(
    availableTables: List<Table> = listOf(), chosenTableId: Int?, onTableClick: (Int) -> Unit
) {
    Text(
        text = "Dostępne stoliki", style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        ), modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
    Column {
        availableTables.forEach { table ->
            TableItem(
                tableId = table.tableId,
                nbOfSeats = table.numberOfSeats,
                isChosen = if (chosenTableId == null) false else chosenTableId == table.tableId,
                onItemClick = onTableClick
            )
        }
    }
}

@Composable
fun TableItem(
    tableId: Int, nbOfSeats: Int, isChosen: Boolean = false, onItemClick: (Int) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChosen) Color(0xffbdffbd) else Color.White
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onItemClick(tableId) }

    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxSize()
        ) {

            Image(
                painter = painterResource(id = R.drawable.restaurant),
                contentDescription = "Restaurant Table",
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(8.dp)
            )
            Text("Stół nr $tableId: $nbOfSeats osobowy")
        }

    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    CoffeeTheme {
        HomeScreenContent(
            padding = PaddingValues(),
            onTableCheckBtnClicked = {},
            availableTables = listOf(
                Table(123, numberOfSeats = 5),
                Table(124, numberOfSeats = 5),
                Table(125, numberOfSeats = 5)
            ),
            userData = UserData(
                userId = "abc",
                username = "Radek",
                profilePictureUrl = "https://picsum.photos/id/64/200/200"
            ),
            chosenTableId = 124,
            onTableClick = {},
            onLogout = {},
            tableAvailabilityStatus = false,
            updateDateTime = {},
            dateTime = LocalDateTime.now(),
            onReserveBtn = {},
            reservation = Reservation(
                tableId = 1,
                userId = "asadasd",
                reservationTime = LocalDateTime.now(),
                reservedAt = LocalDateTime.now(),
                id = 1
            ),
            onReservationDelete = {}
        )
    }

}