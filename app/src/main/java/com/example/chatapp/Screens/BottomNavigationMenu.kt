package com.example.chatapp.Screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.DestinationScreen
import com.example.chatapp.R
import com.example.chatapp.navigateTo


enum class BottomNavigationItem(val icon: Int, val navDestination: DestinationScreen){
    CHATLIST(R.drawable.chat, DestinationScreen.ChatList),
    //STATUSLIST(R.drawable.updates, DestinationScreen.StatusList),
    PROFILE(R.drawable.user, DestinationScreen.Profile)
}

@Composable
fun BottomNavigationMenu(selectedItem: BottomNavigationItem, navController: NavController){
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = 4.dp)
    ){
        for(item in BottomNavigationItem.entries){
            Image(painter = painterResource(id = item.icon), contentDescription = null,
                modifier = Modifier.size(40.dp)
                    .padding(4.dp)
                    .weight(1f)
                    .clickable {
                        navigateTo(navController, item.navDestination.route)
                    },
                colorFilter = if(item == selectedItem)
            ColorFilter.tint(Color.Yellow)
            else ColorFilter.tint(Color.Gray))
        }
    }

}