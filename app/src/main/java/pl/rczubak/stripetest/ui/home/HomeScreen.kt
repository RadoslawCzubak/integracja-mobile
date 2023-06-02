package pl.rczubak.stripetest.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen() {
    HomeScreenContent()
}

@Composable
fun HomeScreenContent() {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween) {
        Button(onClick = {}) {
            Text("Sprawdź dostępne stoliki")
        }

        Button(onClick = {}) {
            Text("Sprawdź dostępne stoliki")
        }
        Button(onClick = {}) {
            Text("Sprawdź dostępne stoliki")
        }
        Button(onClick = {}) {
            Text("Sprawdź dostępne stoliki")
        }
        Button(onClick = {}) {
            Text("Sprawdź dostępne stoliki")
        }

    }
}

@Preview(showSystemUi = true)
@Composable
fun HomeScreenPreview() {
    HomeScreenContent()
}