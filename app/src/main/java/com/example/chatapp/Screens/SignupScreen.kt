package com.example.chatapp.Screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.chatapp.CheckSignedIn
import com.example.chatapp.CommonProgressBar
import com.example.chatapp.DestinationScreen
import com.example.chatapp.LCViewModel
import com.example.chatapp.navigateTo

@Composable
fun SignupScreen(navController: NavController, vm: LCViewModel) {

    CheckSignedIn(vm = vm, navController = navController)

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize()
    ) {
        val nameState = remember {
            mutableStateOf(TextFieldValue())
        }
        val numberState = remember {
            mutableStateOf(TextFieldValue())
        }
        val emailState = remember {
            mutableStateOf(TextFieldValue())
        }
        val passwordState = remember {
            mutableStateOf(TextFieldValue())
        }


        val focus = LocalFocusManager.current

        Text(
            text = "Sign up",
            modifier = Modifier.padding(8.dp),
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            fontSize = 30.sp
        )
        
        OutlinedTextField(
            value = nameState.value,
            onValueChange = {
                nameState.value = it
            },
            label = { Text(text = "Name") },
            modifier = Modifier.padding(8.dp)
        )
        OutlinedTextField(
            value = numberState.value,
            onValueChange = {
                numberState.value = it
            },
            label = { Text(text = "Phone Number") },
            modifier = Modifier.padding(8.dp)
        )
        OutlinedTextField(
            value = emailState.value,
            onValueChange = {
                emailState.value = it
            },
            label = { Text(text = "Email") },
            modifier = Modifier.padding(8.dp)
        )
        OutlinedTextField(
            value = passwordState.value,
            onValueChange = {
                passwordState.value = it
            },
            label = { Text(text = "Password") },
            modifier = Modifier.padding(8.dp)
        )

        Button(onClick = {
            vm.signUp(
                nameState.value.text, numberState.value.text,emailState.value.text,passwordState.value.text
            )
        },
            modifier = Modifier.padding(8.dp)
        ) {
            Text(text = "SIGN UP")
        }

        Row {
            Text(text = "Already a user? ")
            Text(
                text = "Sign in",
                color = Color.LightGray,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.clickable {
                    navigateTo(navController, DestinationScreen.Login.route)
                }
            )
        }
    }
    if(vm.inProcess.value){
        CommonProgressBar()
    }
}