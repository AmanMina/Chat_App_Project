package com.example.chatapp.Screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.CommonDivider
import com.example.chatapp.CommonImage
import com.example.chatapp.CommonProgressBar
import com.example.chatapp.DestinationScreen
import com.example.chatapp.LCViewModel
import com.example.chatapp.navigateTo

@Composable
fun ProfileScreen(navController: NavController, vm: LCViewModel) {
    val inProgress =vm.inProcess.value
    if (inProgress) {
        CommonProgressBar()
    } else {
        val userData = vm.userData.value
        var name by rememberSaveable { mutableStateOf(userData?.name ?: "") }
        var number by rememberSaveable { mutableStateOf(userData?.number ?: "") }


        Scaffold(
            bottomBar = {
                BottomNavigationMenu(
                    selectedItem = BottomNavigationItem.PROFILE,
                    navController = navController
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                ProfileContent(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    vm = vm,
                    name = name,
                    number = number,
                    onNameChange = { name = it },
                    onNumberChange = { number = it },
                    onBack = {
                        navigateTo(navController = navController, route = DestinationScreen.ChatList.route)
                    },
                    onSave = {
                        vm.createOrUpdateProfile(name = name, number = number)
                    },
                    onLogOut = {
                        vm.logout()
                        navigateTo(navController = navController, DestinationScreen.Login.route)
                    }
                )
            }
        }
    }
}
@Composable
fun ProfileContent(
    modifier: Modifier = Modifier,
    vm: LCViewModel,
    name: String,number: String,
    onNameChange: (String) -> Unit,
    onNumberChange: (String) -> Unit,
    onBack: () -> Unit,
    onSave: () -> Unit,
    onLogOut: () -> Unit
) {
    val imageUrl = vm.userData.value?.imageUrl

    Column(modifier = modifier.padding(16.dp)) { // Added padding to the main column
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = onBack) { Text("Back") }
            TextButton(onClick = onSave) { Text("Save") }
        }
        CommonDivider()

        // Profile Image
        ProfileImage(imageUrl = imageUrl, vm = vm)
        CommonDivider()

        // Input Fields
        TextFieldRow(label = "Name", value = name, onValueChange = onNameChange)
        TextFieldRow(label = "Number", value = number, onValueChange = onNumberChange)
        CommonDivider()

        // Logout Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(onClick = onLogOut) { Text("Log out") }
        }
    }
}


@Composable
fun TextFieldRow(
    label: String,
    value: String,
    onValueChange: (String) ->Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,modifier = Modifier.width(100.dp),
            style = MaterialTheme.typography.bodyMedium // Added styling for label
        )
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f), // TextField takes remaining space
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent
            ),
            singleLine = true, // Ensures single-line input
            textStyle = MaterialTheme.typography.bodyLarge // Added styling for input text
        )
    }
}


@Composable
fun ProfileImage(imageUrl: String?, vm: LCViewModel) {

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {
        uri ->
        uri?.let{
            vm.uploadProfileImage(uri)
        }
    }
    Box(modifier = Modifier.height(intrinsicSize = IntrinsicSize.Min)){
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxSize()
                .clickable {
                    launcher.launch("image/*")
                },
            horizontalAlignment = Alignment.CenterHorizontally) {
            CommonImage(
                data = imageUrl,
                modifier = Modifier
                    .padding(8.dp)
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
            )
            Text(text = "Change Profile Picture")
        }
        if(vm.inProcess.value){
            CommonProgressBar()
        }

    }
}
