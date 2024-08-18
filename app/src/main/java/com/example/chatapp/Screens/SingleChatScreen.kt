package com.example.chatapp.Screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.chatapp.CommonDivider
import com.example.chatapp.CommonImage
import com.example.chatapp.LCViewModel
import com.example.chatapp.data.Message

@Composable
fun SingleChatScreen(navController: NavController, vm: LCViewModel, chatId: String) {
    var reply by rememberSaveable { mutableStateOf("") }
    val onSendReply = {
        vm.onSendReply(chatId, reply)
        reply = ""
    }

    val myUser = vm.userData.value
    val chatMessages = vm.chatMessages
    val currentChat = vm.chats.value.firstOrNull { it.chatId == chatId }
    val chatUser = if(myUser?.userId == currentChat?.user1?.userId)  currentChat?.user2 else currentChat?.user1
    val listState = rememberLazyListState()
    LaunchedEffect(key1 = chatMessages.value) {
        if(chatMessages.value.isNotEmpty()){
            listState.scrollToItem(chatMessages.value.size-1)
        }
        vm.populateMessages(chatId)
    }

    BackHandler {
        vm.dePopulateMessage()
        navController.popBackStack()
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            ChatHeader(
                name = chatUser?.name?: "---",
                imageUrl = chatUser?.imageUrl ?: ""
            ) {
                vm.dePopulateMessage()
                navController.popBackStack()
            }

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState
            ) {
                items(chatMessages.value) { message ->
                    ChatMessageItem(
                        message = message,
                        isMyMessage = message.sendBy == myUser?.userId
                    )
                }
            }

            ReplyBox(
                reply = reply,
                onReplyChange = { reply = it },
                onSendReply = onSendReply
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: Message, isMyMessage: Boolean) {
    val alignment = if (isMyMessage) Alignment.End else Alignment.Start
    val color = if (isMyMessage) Color.Green else Color.Yellow

    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
        horizontalAlignment = alignment
    ) {
        Text(
            text = message.message ?: "",
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color)
                .padding(12.dp),
            color = Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun ChatHeader(name: String, imageUrl: String, onBlackClicked: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back", // Added content description
            modifier = Modifier
                .padding(8.dp)
                .clickable { onBlackClicked.invoke() }
        )
        CommonImage(
            data = imageUrl,
            modifier = Modifier
                .padding(8.dp)
                .size(50.dp)
                .clip(CircleShape)
        )
        Text(
            text = name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
fun ReplyBox(
    reply: String,
    onReplyChange: (String) -> Unit,
    onSendReply: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        CommonDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextField(
                value = reply,
                onValueChange = onReplyChange,
                modifier = Modifier.weight(1f),
                maxLines = 3,
                placeholder = { Text("Enter your message") } // Added placeholder
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = onSendReply,
                modifier = Modifier
                    .height(60.dp)
                    .wrapContentWidth()
            ) {
                Text(text = "Send")
            }
        }
    }
}