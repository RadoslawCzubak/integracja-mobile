package pl.rczubak.stripetest.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.rczubak.stripetest.R
import pl.rczubak.stripetest.ui.login.UserData

@Composable
fun UserInfoBar(
    userData: UserData, onLogout: () -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current).data(userData.profilePictureUrl)
                .crossfade(true).build(),
            placeholder = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = "user image",
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = userData.username ?: "Anonymous", style = TextStyle(
                fontSize = 16.sp
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onLogout() }, modifier = Modifier.height(40.dp)
        ) {
            Text(text = "Wyloguj")
        }
    }
}