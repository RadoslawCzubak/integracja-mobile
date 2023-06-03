package pl.rczubak.stripetest.data

import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okhttp3.Interceptor
import okhttp3.Response
import java.util.concurrent.TimeUnit

class TokenInterceptor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val tokenTask = Firebase.auth.currentUser?.getIdToken(false)
        return if (tokenTask != null) {
            val token = Tasks.await(tokenTask, 10, TimeUnit.SECONDS).token
            val modifiedRequest = originalRequest.newBuilder()
                .header("x-api-key", token ?: "")
                .build()
            chain.proceed(modifiedRequest)
        } else {
            chain.proceed(originalRequest)
        }

    }
}