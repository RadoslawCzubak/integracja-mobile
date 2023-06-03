package pl.rczubak.stripetest.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
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
import com.stripe.android.paymentsheet.addresselement.AddressDetails
import com.stripe.android.paymentsheet.addresselement.AddressLauncher
import com.stripe.android.paymentsheet.addresselement.AddressLauncherResult
import kotlinx.coroutines.launch
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.koin.androidx.compose.koinViewModel
import pl.rczubak.stripetest.databinding.ActivityCheckoutBinding
import pl.rczubak.stripetest.ui.home.HomeScreen
import pl.rczubak.stripetest.ui.home.HomeViewModel
import pl.rczubak.stripetest.ui.login.GoogleAuthUiClient
import pl.rczubak.stripetest.ui.login.LoginScreen
import pl.rczubak.stripetest.ui.login.LoginViewModel
import pl.rczubak.stripetest.ui.login.model.LoginEvent
import pl.rczubak.stripetest.ui.navigation.Screen
import pl.rczubak.stripetest.ui.order.OrderScreen
import java.io.IOException

class CheckoutActivity : AppCompatActivity() {
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
    private lateinit var paymentIntentClientSecret: String
    private lateinit var paymentSheet: PaymentSheet

    private lateinit var payButton: Button

    private lateinit var addressLauncher: AddressLauncher

    private var shippingDetails: AddressDetails? = null

    private lateinit var addressButton: Button

    private val addressConfiguration = AddressLauncher.Configuration(
        additionalFields = AddressLauncher.AdditionalFieldsConfiguration(
            phone = AddressLauncher.AdditionalFieldsConfiguration.FieldConfiguration.REQUIRED
        ), allowedCountries = setOf("US", "CA", "GB"), title = "Shipping Address"
    )

    private lateinit var binding: ActivityCheckoutBinding

    private val navBarDestinations = listOf(
        Screen.Reservation,
        Screen.Order,
        Screen.Employee
    )

    @OptIn(ExperimentalMaterial3Api::class)
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
                                        navController.popBackStack()
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
                        ScaffoldWithBottomBar(navController = navController) { padding ->
                            OrderScreen(padding)
                        }
                    }
                }
            }
        }


//        // Hook up the pay button
//        payButton = binding.payButton
//        payButton.setOnClickListener(::onPayClicked)
//        payButton.isEnabled = false
//
//        paymentSheet = PaymentSheet(this, ::onPaymentSheetResult)
//
//        // Hook up the address button
//        addressButton = binding.addressButton
//        addressButton.setOnClickListener(::onAddressClicked)
//
//        addressLauncher = AddressLauncher(this, ::onAddressLauncherResult)
//
//        fetchPaymentIntent()
    }

    private fun fetchPaymentIntent() {
        val url = "$BACKEND_URL/create-payment-intent/27"

        val shoppingCartContent = """
            {
                "items": [
                    {"id":"xl-tshirt"}
                ]
            }
        """

        val mediaType = "application/json; charset=utf-8".toMediaType()

        val body = shoppingCartContent.toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(body).build()

        OkHttpClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                showAlert("Failed to load data", "Error: $e")
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    showAlert("Failed to load page", "Error: $response")
                } else {
                    val responseData = response.body?.string()
                    val responseJson = responseData?.let { JSONObject(it) } ?: JSONObject()
                    paymentIntentClientSecret = responseJson.getString("clientSecret")
                    runOnUiThread { payButton.isEnabled = true }
                    Log.i(TAG, "Retrieved PaymentIntent")
                }
            }
        })
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

    private fun onPayClicked(view: View) {
        val configuration = PaymentSheet.Configuration("Example, Inc.")

        // Present Payment Sheet
        paymentSheet.presentWithPaymentIntent(paymentIntentClientSecret, configuration)
    }

    private fun onAddressClicked(view: View) {
        addressLauncher.present(
            publishableKey = "pk_test_51N7DrUHoWwrUTQHV88Ip45mOZD44CCNHD0Wbc5maOW5zv5gQ74H03x8WsYgtg17Th4qa2wEB2AD6wwZ5WaWGBJcO00ZGYMJJHb",
            configuration = addressConfiguration
        )
    }

    private fun onPaymentSheetResult(paymentResult: PaymentSheetResult) {
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

    private fun onAddressLauncherResult(result: AddressLauncherResult) {
        // TODO: Handle result and update your UI
        when (result) {
            is AddressLauncherResult.Succeeded -> {
                shippingDetails = result.address
            }
            is AddressLauncherResult.Canceled -> {
                // TODO: Handle cancel
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