package com.example.chatapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.chatapp.Screens.ChatListScreen
import com.example.chatapp.Screens.LoginScreen
import com.example.chatapp.Screens.ProfileScreen
import com.example.chatapp.Screens.SignupScreen
import com.example.chatapp.Screens.SingleChatScreen
import com.example.chatapp.Screens.StatusScreen
import com.example.chatapp.ui.theme.ChatAppTheme
import dagger.hilt.android.AndroidEntryPoint


sealed class DestinationScreen(var route: String){
    object Signup: DestinationScreen("signup")
    object Login: DestinationScreen("login")
    object Profile: DestinationScreen("profile")
    object ChatList: DestinationScreen("chatList")
    object SingleChat: DestinationScreen("singleChat/{chatId}"){
        fun createRoute(id:String) = "singleChat/$id"
    }
    object StatusList: DestinationScreen("statusList")
    object SingleStatus: DestinationScreen("singleStatus/{userID}"){
        fun createRoute(userID:String) = "singleStatus/$userID"
    }


}
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContent {
            ChatAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ChatAppNavigation()
                }
            }
        }
    }
    @Composable
    fun ChatAppNavigation(){
        val navController = rememberNavController()
        var vm = hiltViewModel<LCViewModel>()
        NavHost(navController = navController, startDestination = DestinationScreen.Signup.route) {
            composable(DestinationScreen.Signup.route){
                SignupScreen(navController, vm)
            }
            composable(DestinationScreen.Login.route){
                LoginScreen(navController, vm)
            }
            composable(DestinationScreen.ChatList.route){
                ChatListScreen(navController, vm)
            }
            composable(DestinationScreen.StatusList.route){
                StatusScreen(navController, vm)
            }
            composable(DestinationScreen.Profile.route){
                ProfileScreen(navController, vm)
            }
            composable(DestinationScreen.SingleChat.route){
                val chatId = it.arguments?.getString("chatId")
                chatId?.let {
                    SingleChatScreen(navController = navController, vm = vm, chatId = chatId)
                }
            }
        }
    }
}

