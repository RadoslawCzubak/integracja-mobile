package pl.rczubak.stripetest.ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.compose.CoffeeTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.gms.auth.api.identity.Identity
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import org.koin.android.ext.android.inject
import org.koin.androidx.compose.koinViewModel
import pl.rczubak.stripetest.data.service.CafeAPI
import pl.rczubak.stripetest.databinding.ActivityCheckoutBinding
import pl.rczubak.stripetest.ui.employee.EmployeeScreen
import pl.rczubak.stripetest.ui.home.HomeScreen
import pl.rczubak.stripetest.ui.home.HomeViewModel
import pl.rczubak.stripetest.ui.login.GoogleAuthUiClient
import pl.rczubak.stripetest.ui.login.LoginScreen
import pl.rczubak.stripetest.ui.login.LoginViewModel
import pl.rczubak.stripetest.ui.login.UserData
import pl.rczubak.stripetest.ui.login.model.LoginEvent
import pl.rczubak.stripetest.ui.navigation.Screen
import pl.rczubak.stripetest.ui.order.OrderScreen
import pl.rczubak.stripetest.ui.order.OrderViewModel
import pl.rczubak.stripetest.ui.order.model.OrderEvent
import pl.rczubak.stripetest.ui.splash.SplashScreen

class CheckoutActivity : AppCompatActivity() {
    private val cafeAPI: CafeAPI by inject()

    companion object {
        private const val TAG = "CheckoutActivity"
        private const val BACKEND_URL = "http://integracja.radoslav.pl"
    }

    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = applicationContext,
            oneTapClient = Identity.getSignInClient(applicationContext)
        )
    }

    private var onResult: () -> Unit = {}
    private lateinit var paymentIntentClientSecret: String
    private lateinit var paymentSheet: PaymentSheet

    private lateinit var binding: ActivityCheckoutBinding

    private val navBarDestinations = listOf(
        Screen.Reservation,
        Screen.Order,
        Screen.Employee
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        Firebase.messaging.subscribeToTopic("order")
        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setSystemBarsColor(
                    color = Color.Gray,
                    darkIcons = useDarkIcons
                )


                onDispose {}
            }
            val navController = rememberNavController()
            CoffeeTheme {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Login.route,
                ) {
                    composable(Screen.Employee.route){
                        ScaffoldWithBottomBar(navController = navController) {
                            EmployeeScreen(
                                paddingValues = it
                            )
                        }
                    }
                    composable(Screen.Reservation.route) {
                        ScaffoldWithBottomBar(navController = navController) { padding ->
                            val viewModel: HomeViewModel = koinViewModel()
                            HomeScreen(
                                padding = padding,
                                navController = navController,
                                viewModel = viewModel,
                                userData = googleAuthUiClient.getSignedInUser(),
                                onLogout = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.Login.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
                    composable(Screen.Login.route) {
                        val viewModel: LoginViewModel = koinViewModel()
                        val launcher =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult =
                                                googleAuthUiClient.signInWithIntent(
                                                    intent = result.data ?: return@launch
                                                )
                                            viewModel.setEvent(
                                                LoginEvent.OnSignInResult(
                                                    signInResult
                                                )
                                            )
                                        }
                                    }
                                })
                        LoginScreen(navController, viewModel, onGoogleSignIn = {
                            lifecycleScope.launch {
                                val signInIntentSender = googleAuthUiClient.signIn()
                                launcher.launch(
                                    IntentSenderRequest.Builder(
                                        signInIntentSender ?: return@launch
                                    ).build()
                                )
                            }
                        })
                    }

                    composable(Screen.Order.route) {
                        val viewModel: OrderViewModel = koinViewModel()
                        ScaffoldWithBottomBar(navController = navController) { padding ->
                            OrderScreen(
                                paddingValues = padding,
                                userData = googleAuthUiClient.getSignedInUser() ?: UserData(
                                    "",
                                    "Anonymous",
                                    ""
                                ),
                                onLogout = {
                                    lifecycleScope.launch {
                                        googleAuthUiClient.signOut()
                                        navController.navigate(Screen.Login.route) {
                                            popUpTo(Screen.Login.route) {
                                                inclusive = true
                                            }
                                        }
                                    }
                                }, onPayOrder = {
                                    lifecycleScope.launch {
                                        fetchPaymentIntent(it)
                                        val configuration =
                                            PaymentSheet.Configuration("Integracja Cafe, Inc.")
                                        with(Dispatchers.Main) {
                                            // Present Payment Sheet
                                            onResult = {
                                                viewModel.setEvent(OrderEvent.RefreshOrder)
                                            }
                                            paymentSheet.presentWithPaymentIntent(
                                                paymentIntentClientSecret,
                                                configuration
                                            )
                                        }
                                    }
                                })
                        }
                    }
                    composable(Screen.Splash.route) {
                        SplashScreen(navController = navController)
                    }
                }
            }
        }
        paymentSheet = PaymentSheet(this@CheckoutActivity) {
            onPaymentSheetResult(it, onResult)
        }
    }

    private suspend fun fetchPaymentIntent(orderId: Int) {
        paymentIntentClientSecret = cafeAPI.createPaymentIntent(orderId).clientSecret
    }

    private fun showAlert(title: String, message: String? = null) {
        runOnUiThread {
            val builder = AlertDialog.Builder(this).setTitle(title).setMessage(message)
            builder.setPositiveButton("Ok", null)
            builder.create().show()
        }
    }

    private fun showToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult, onResult: () -> Unit) {
        onResult.invoke()
        when (paymentResult) {
            is PaymentSheetResult.Completed -> {
                showToast("Payment complete!")
            }
            is PaymentSheetResult.Canceled -> {
                Log.i(TAG, "Payment canceled!")
            }
            is PaymentSheetResult.Failed -> {
                showAlert("Payment failed", paymentResult.error.localizedMessage)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun ScaffoldWithBottomBar(
        navController: NavController,
        content: @Composable (PaddingValues) -> Unit
    ) {
        Scaffold(
            bottomBar = {
                if (navController.currentDestination?.route != Screen.Login.route)
                    NavigationBar {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        navBarDestinations.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        screen.icon,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(screen.resourceId)) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        // Pop up to the start destination of the graph to
                                        // avoid building up a large stack of destinations
                                        // on the back stack as users select items
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        // Avoid multiple copies of the same destination when
                                        // reselecting the same item
                                        launchSingleTop = true
                                        // Restore state when reselecting a previously selected item
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
            }
        ) { padding ->
            content(padding)
        }
    }
}