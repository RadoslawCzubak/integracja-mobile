package pl.rczubak.stripetest.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.EmojiPeople
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.TableBar
import androidx.compose.ui.graphics.vector.ImageVector
import pl.rczubak.stripetest.R

sealed class Screen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector = Icons.Filled.Favorite
) {
    object Reservation : Screen("home", R.string.reservation, Icons.Filled.TableBar)
    object Order : Screen("order", R.string.order, Icons.Filled.Coffee)
    object Login : Screen("login", R.string.login)
    object Employee : Screen("employee", R.string.employee, Icons.Filled.EmojiPeople)
    object Splash : Screen("splash", R.string.splash, Icons.Filled.EmojiPeople)
}