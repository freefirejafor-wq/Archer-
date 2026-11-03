package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ArcherMainHudScreen
import com.example.ui.AuthScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ObsidianDeep
import com.example.util.configureAppCheck
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check configuration with launch intent before any UI or Firebase calls
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = ObsidianDeep
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth.currentUser)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val auth: FirebaseAuth? = remember(context) {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) Firebase.auth else null
        } catch (_: Throwable) {
            null
        }
    }
    val hasWebClientId = remember(context) {
        try {
            val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            resId != 0 && context.getString(resId).isNotBlank()
        } catch (_: Throwable) {
            false
        }
    }

    var localSessionUnlocked by remember { mutableStateOf(auth == null || !hasWebClientId) }
    val currentUserFlow = remember(auth) {
        auth?.authStateFlow() ?: flowOf(null)
    }
    val currentUser by currentUserFlow.collectAsStateWithLifecycle(initialValue = auth?.currentUser)
    val user = currentUser

    if (user == null && !localSessionUnlocked) {
        AuthScreen(
            onAuthSuccess = {
                localSessionUnlocked = true
            }
        )
    } else {
        ArcherMainHudScreen(currentUserId = user?.uid ?: "local_archer_user")
    }
}
