package pl.rczubak.stripetest.ui.login

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import pl.rczubak.stripetest.ui.login.model.LoginError
import pl.rczubak.stripetest.ui.login.model.LoginEvent
import pl.rczubak.stripetest.ui.login.model.LoginState

class LoginViewModel(
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginState())
    val uiState = _uiState.asStateFlow()

    fun setEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.Login -> handleLogin(event.username, event.password)
            LoginEvent.LoggedIn -> setState { state -> state.copy(navigateToLogin = false) }
            is LoginEvent.OnSignInResult -> handleGoogleLogin(event.signInResult)
        }
    }

    private fun handleGoogleLogin(signInResult: SignInResult) {
        setState { state -> state.copy(navigateToLogin = signInResult.data != null) }
    }

    private fun handleLogin(username: String, password: String) {
        if (validateInput(username, password))
            firebaseAuth.signInWithEmailAndPassword(username, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        setState { state -> state.copy(navigateToLogin = true) }
                    } else {
                        setState { loginState ->
                            loginState.copy(loginError = LoginError.NO_USER_FOUND)
                        }
                    }
                }
    }

    private fun validateInput(username: String, password: String): Boolean {
        setState { state ->
            state.copy(usernameError = username.isNotBlank(), passwordError = password.isNotBlank())
        }
        return username.isNotBlank() && password.isNotBlank()
    }

    private fun setState(changeState: (old: LoginState) -> LoginState) {
        _uiState.value = changeState(_uiState.value)
    }
}
