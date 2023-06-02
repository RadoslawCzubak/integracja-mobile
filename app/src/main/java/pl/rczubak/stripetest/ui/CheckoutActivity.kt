package pl.rczubak.stripetest.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.addresselement.AddressDetails
import com.stripe.android.paymentsheet.addresselement.AddressLauncher
import com.stripe.android.paymentsheet.addresselement.AddressLauncherResult
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import pl.rczubak.stripetest.databinding.ActivityCheckoutBinding
import pl.rczubak.stripetest.ui.home.HomeScreen
import java.io.IOException

class CheckoutActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "CheckoutActivity"
        private const val BACKEND_URL = "http://integracja.radoslav.pl"
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
        ),
        allowedCountries = setOf("US", "CA", "GB"),
        title = "Shipping Address"
    )

    private lateinit var binding: ActivityCheckoutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        Firebase.messaging.subscribeToTopic("order")
            .addOnCompleteListener { task ->
                var msg = "Subscribed"
                if (!task.isSuccessful) {
                    msg = "Subscribe failed"
                }
                Log.d("Push_init", msg)
                Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
            }
        setContent {
            val navController = rememberNavController()
            MaterialTheme {
                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen()
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
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        OkHttpClient()
            .newCall(request)
            .enqueue(object : Callback {
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
            val builder = AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
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