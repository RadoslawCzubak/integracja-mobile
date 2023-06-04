package pl.rczubak.stripetest.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pl.rczubak.stripetest.ui.navigation.Screen

@Composable
fun SplashScreen(
    navController: NavController
) {
    SplashScreenContent(onNavigateMain = {
        navController.navigate(Screen.Reservation.route) {
            popUpTo(Screen.Reservation.route) {
                inclusive = true
            }
        }
    })
}

@Composable
fun SplashScreenContent(
    onNavigateMain: () -> Unit
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset("coffee.json"))
    val progress by animateLottieCompositionAsState(composition)

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        coroutineScope.launch {
            delay(4000)
            onNavigateMain()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFDCC6))
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
        )
    }
}

@Preview(showSystemUi = true)
@Composable
fun SplashPreview() {
    SplashScreenContent({})
}