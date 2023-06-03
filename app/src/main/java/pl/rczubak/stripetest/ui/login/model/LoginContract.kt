package pl.rczubak.stripetest.ui.login.model

import pl.rczubak.stripetest.ui.login.SignInResult

data class LoginState(
    val usernameError: Boolean = false,
    val passwordError: Boolean = false,
    val navigateToLogin: Boolean = false,
    val loginError: LoginError? = null
)

enum class LoginError {
    NO_USER_FOUND
}

sealed interface LoginEvent {
    data class Login(val username: String, val password: String) : LoginEvent
    object LoggedIn : LoginEvent
    data class OnSignInResult(val signInResult: SignInResult) : LoginEvent
}