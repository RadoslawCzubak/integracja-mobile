package pl.rczubak.stripetest.ui.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.rczubak.stripetest.R
import pl.rczubak.stripetest.ui.login.model.LoginEvent
import pl.rczubak.stripetest.ui.navigation.Screen

@Composable
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel,
    onGoogleSignIn: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val username = rememberSaveable {
        mutableStateOf("")
    }
    val password = rememberSaveable {
        mutableStateOf("")
    }

    LaunchedEffect(key1 = uiState.navigateToLogin) {
        if (uiState.navigateToLogin) {
            viewModel.setEvent(LoginEvent.LoggedIn)
            navController.navigate(Screen.Splash.route){
                popUpTo(Screen.Splash.route){
                    inclusive = true
                }
            }
        }
    }
    LoginScreenContent(
        usernameText = username.value,
        passwordText = password.value,
        onUsernameChange = { username.value = it },
        onPasswordChange = { password.value = it },
        onLoginClick = { viewModel.setEvent(LoginEvent.Login(username.value, password.value)) },
        onGoogleLoginClick = onGoogleSignIn,
        fakeLogin = { viewModel.setEvent(LoginEvent.Login("radek.cz1@o2.pl", "Haslo123!")) }
    )
}

@Composable
fun LoginScreenContent(
    usernameText: String,
    passwordText: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onGoogleLoginClick: () -> Unit,
    fakeLogin: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        LoginInputs(
            usernameText, passwordText, onUsernameChange, onPasswordChange
        )
        Spacer(modifier = Modifier.height(40.dp))
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            shape = RoundedCornerShape(8.dp),
        ) {
            Text(text = "Zaloguj")
        }
        Spacer(modifier = Modifier.height(10.dp))
        GoogleSignInButton(onClick = onGoogleLoginClick)
        Button(onClick = { fakeLogin() }) {
            Text(text = "Fake Login")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginInputs(
    usernameText: String,
    passwordText: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
) {
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = usernameText,
            onValueChange = onUsernameChange,
            label = { Text(text = "E-mail") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
        )
        Spacer(modifier = Modifier.height(20.dp))
        OutlinedTextField(
            value = passwordText,
            onValueChange = onPasswordChange,
            label = { Text(text = "Hasło") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrect = false,
                keyboardType = KeyboardType.Password,
            ),
            trailingIcon = {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else Icons.Filled.VisibilityOff

                // Please provide localized description for accessibility services
                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, description)
                }
            }
        )
    }
}

@Composable
fun GoogleSignInButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            contentColor = Color.White,
            containerColor = Color.Black
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_google_sign_in), contentDescription = "",
            modifier = Modifier.height(24.dp)
        )
        Text(text = "Sign in with Google", modifier = Modifier.padding(6.dp))
    }
}

@Preview(showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    LoginScreenContent(usernameText = "Siema",
        passwordText = "Siema",
        onPasswordChange = {},
        onUsernameChange = {},
        onLoginClick = {},
        onGoogleLoginClick = {},
        fakeLogin = {})
}