package pl.rczubak.stripetest.ui

import android.content.res.Resources.Theme
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        Firebase.messaging.subscribeToTopic("order")
        setContent {
            val systemUiController = rememberSystemUiController()
            val useDarkIcons = !isSystemInDarkTheme()

            DisposableEffect(systemUiController, useDarkIcons) {
                systemUiController.setSystemBarsColor(
                    color =  Color.Gray,
                    darkIcons = useDarkIcons
                )


                onDispose {}
            }
            val navController = rememberNavController()
            CoffeeTheme {
                NavHost(navController = navController, startDestination = "login") {

                    composable("home") {
                        val viewModel: HomeViewModel = koinViewModel()
                        HomeScreen(
                            navController,
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
                    composable("login") {
                        val viewModel: LoginViewModel = koinViewModel()
                        val uiState by viewModel.uiState.collectAsState()
                        val launcher =
                            rememberLauncherForActivityResult(contract = ActivityResultContracts.StartIntentSenderForResult(),
                                onResult = { result ->
                                    if (result.resultCode == RESULT_OK) {
                                        lifecycleScope.launch {
                                            val signInResult = googleAuthUiClient.signInWithIntent(
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
}