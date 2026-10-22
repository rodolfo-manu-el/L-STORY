package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.StoryBuilderApp
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val auth = remember { FirebaseAuth.getInstance() }
    var currentUser by remember { mutableStateOf(auth.currentUser) }
    // Abre directamente en el estudio (Modo Estudio Local) sin obligar al usuario a iniciar sesión primero
    var useLocalStudioWithoutLogin by rememberSaveable { mutableStateOf(true) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            currentUser = firebaseAuth.currentUser
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user != null) {
        StoryBuilderApp(
            currentUserId = user.uid,
            userDisplayName = user.displayName ?: user.email?.substringBefore("@") ?: "Autor",
            isGuestMode = false,
            onAuthSuccess = { currentUser = auth.currentUser },
            onSignedOut = {
                currentUser = null
                useLocalStudioWithoutLogin = false
            }
        )
    } else if (useLocalStudioWithoutLogin) {
        StoryBuilderApp(
            currentUserId = "local_studio_author",
            userDisplayName = "Autor en Modo Estudio Local",
            isGuestMode = true,
            onAuthSuccess = { currentUser = auth.currentUser },
            onSignedOut = { useLocalStudioWithoutLogin = false }
        )
    } else {
        AuthScreen(
            onAuthSuccess = { currentUser = auth.currentUser },
            onContinueWithoutLogin = { useLocalStudioWithoutLogin = true }
        )
    }
}
