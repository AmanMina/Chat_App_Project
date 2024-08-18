package com.example.chatapp

import android.icu.util.Calendar
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.core.text.isDigitsOnly
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.chatapp.data.CHATS
import com.example.chatapp.data.ChatData
import com.example.chatapp.data.ChatUser
import com.example.chatapp.data.Event
import com.example.chatapp.data.MESSAGE
import com.example.chatapp.data.Message
import com.example.chatapp.data.USER_NODE
import com.example.chatapp.data.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.toObject
import com.google.firebase.firestore.toObjects
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class LCViewModel @Inject constructor(
    val auth: FirebaseAuth,
    var db: FirebaseFirestore,
    val storage: FirebaseStorage
): ViewModel() {


    var inProcess = mutableStateOf(false)
    var inProcessChats = mutableStateOf(false)
    val eventMutableState = mutableStateOf<Event<String>?>(null)
    var signIn  = mutableStateOf(false)
    val userData = mutableStateOf<UserData?>(null)
    val chats = mutableStateOf<List<ChatData>>(listOf())
    val chatMessages = mutableStateOf<List<Message>>(listOf())
    val inProgressChatMessage = mutableStateOf(false)
    var currentChatMessageListener: ListenerRegistration?=null

    init {
        val currentUser = auth.currentUser
        signIn.value = currentUser!=null
        currentUser?.uid?.let {
            getUserData(it)

        }

    }

    fun populateMessages(chatId: String){
        inProgressChatMessage.value = true
        currentChatMessageListener = db.collection(CHATS).document(chatId).collection(MESSAGE)
            .addSnapshotListener { value, error ->
                if(error!=null){
                    handleException(error)
                }
                if(value!=null){
                    chatMessages.value = value.documents.mapNotNull {
                        it.toObject<Message>()
                    }.sortedBy { it.timeStamp }
                    inProgressChatMessage.value = false
                }
        }
    }

    fun dePopulateMessage(){
        chatMessages.value = listOf()
        currentChatMessageListener = null
    }

    fun populateChats(){
        inProcessChats.value = true
        db.collection(CHATS).where(
            Filter.or(
                Filter.equalTo("user1.userId", userData.value?.userId),
                Filter.equalTo("user2.userId", userData.value?.userId)
            )
        ).addSnapshotListener {
                              value, error ->
            if(error!=null){
                handleException(error)
            }
            if(value!=null){
                chats.value = value.documents.mapNotNull {
                    it.toObject<ChatData>()
                }
                inProcessChats.value = false
            }
        }
    }


    fun onSendReply(chatId: String, message: String){
        val time = Calendar.getInstance().time.toString()
        val msg = Message(
            userData.value?.userId,
            message,
            time
        )
        db.collection(CHATS).document(chatId).collection(MESSAGE).document().set(msg)

    }


    fun signUp(name:String, number:String, email:String, password: String){
        inProcess.value = true
        if(name.isEmpty() or number.isEmpty() or email.isEmpty() or password.isEmpty()){
            handleException(customMessage = "Please fill all the fields ")
            return
        }

        db.collection(USER_NODE).whereEqualTo("number", number).get().addOnSuccessListener {
            if(it.isEmpty){
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
                    if(it.isSuccessful){
                        signIn.value = true
                        createOrUpdateProfile(name, number)
                        Log.d("TAG23", "SignUp:User logged in")
                    }
                    else{
                        handleException(it.exception, customMessage = "Sign Up failed")
                    }
                }
            }
            else{
                handleException(customMessage = "Number Already exists")
                inProcess.value = false
            }
        }
    }

    fun login(email: String, password: String){
        if(email.isEmpty() or password.isEmpty()){
            handleException(customMessage = "Please fill all the fields")
            return
        }
        else{
            inProcess.value = true
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
                if(it.isSuccessful){
                    signIn.value = true
                    inProcess.value = false
                    auth.currentUser?.uid?.let {
                        getUserData(it)
                    }
                }else{
                    handleException(exception = it.exception, customMessage = "Log in failed")
                }
            }
        }
    }



    fun uploadProfileImage(uri: Uri) {
        uploadImage(uri){
            createOrUpdateProfile(imageUrl = it.toString())
        }
    }
    fun uploadImage(uri: Uri, onSuccess: (Uri)->Unit){
        inProcess.value = true
        val storageRef = storage.reference
        val uuid = UUID.randomUUID()
        val imageRef = storageRef.child("images/$uuid")
        val uploadTask = imageRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            val result = it.metadata?.reference?.downloadUrl
            result?.addOnSuccessListener(onSuccess)
            inProcess.value = false
        }
            .addOnFailureListener{
                handleException(it)
            }
    }



    private fun updateChatDocuments(uid: String, name: String?, number: String?, imageUrl: String?) {
        viewModelScope.launch {
            try {
                val userFilter1 = Filter.equalTo("user1.userId", uid)
                val userFilter2 = Filter.equalTo("user2.userId", uid)

                // Fetch chats where the user is either user1 or user2
                val chatsToUpdate = db.collection(CHATS)
                    .where(Filter.or(userFilter1, userFilter2))
                    .get()
                    .await()

                chatsToUpdate.documents.forEach { chatDocument ->
                    val chatData = chatDocument.toObject<ChatData>() ?: return@forEach
                    val updates = mutableMapOf<String, Any>()

                    // Update user1 or user2 fields based on the match
                    if (chatData.user1.userId == uid) {
                        if (name != null) updates["user1.name"] = name
                        if (number != null) updates["user1.number"] = number
                        if (imageUrl != null) updates["user1.imageUrl"] = imageUrl
                    } else if (chatData.user2.userId == uid) {
                        if (name != null) updates["user2.name"] = name
                        if (number != null) updates["user2.number"] = number
                        if (imageUrl != null) updates["user2.imageUrl"] = imageUrl
                    }

                    // Apply updates to the chat document
                    if (updates.isNotEmpty()) {
                        chatDocument.reference.update(updates)
                    }
                }
            } catch (e: Exception) {
                handleException(e, "Error updating chat documents")
            }
        }
    }


    fun createOrUpdateProfile(name: String?=null, number: String?=null, imageUrl: String?=null){
        var uid = auth.currentUser?.uid
        val userData = UserData(
            userId = uid,
            name = name?: userData.value?.name,
            number = number?: userData.value?.number,
            imageUrl = imageUrl?: userData.value?.imageUrl
        )

        uid?.let {
            inProcess.value = true
            db.collection(USER_NODE).document(uid).get().addOnSuccessListener {
                if(it.exists()){
//                    update user data

                    val updates = mutableMapOf<String, Any>()
                    if(name!=null) updates["name"] = name
                    if(number!=null) updates["number"] = number
                    if(imageUrl!=null) updates["imageUrl"] = imageUrl
                    it.reference.update(updates)
                        .addOnSuccessListener {
                            inProcess.value = false
                            getUserData(uid)
                            updateChatDocuments(uid, name, number, imageUrl)
                        }
                        .addOnFailureListener {
                            inProcess.value = false
                            handleException(it, "Can not update user data")
                        }
                }
                else{
                    db.collection(USER_NODE).document(uid).set(userData)
                    inProcess.value = false
                    getUserData(uid)
                }
            }
                .addOnFailureListener {
                    handleException(it, "Can not retrieve user")
                }
        }
    }

    private fun getUserData(uid: String) {
        inProcess.value = true
        db.collection(USER_NODE).document(uid).addSnapshotListener { value, error ->
            if(error!=null){
                handleException(error, "Can not retrieve user")
            }
            if(value!=null){
                var user = value.toObject<UserData>()
                userData.value = user
                inProcess.value = false
                populateChats()
            }
        }
    }

    fun handleException(exception: Exception?=null, customMessage: String=""){
        Log.e("LiveChatAppError", "Live chat exception: ", exception)
        exception?.printStackTrace()
        val errorMsg = exception?.localizedMessage?:""
        val message = if(customMessage.isNullOrEmpty()) errorMsg else customMessage

        eventMutableState.value = Event(message)
        inProcess.value = false

    }

    fun logout() {
        auth.signOut()
        signIn.value = false
        userData.value = null
        dePopulateMessage()
        currentChatMessageListener = null
        eventMutableState.value = Event("Logged Out")
    }

    fun onAddChat(number: String) {
        if(number.isEmpty() or !number.isDigitsOnly()){
            handleException(customMessage = "Field can have digits only")
        }else{
            db.collection(CHATS).where(Filter.or(
                Filter.and(
                    Filter.equalTo("user1.number", number),
                    Filter.equalTo("user2.number", userData.value?.number)
                ),
                Filter.and(
                    Filter.equalTo("user1.number", userData.value?.number),
                    Filter.equalTo("user2.number", number)
                )
            )).get().addOnSuccessListener {
                if(it.isEmpty){
                    db.collection(USER_NODE).whereEqualTo("number",number).get().addOnSuccessListener {
                        if(it.isEmpty){
                            handleException(customMessage = "number not found")
                        }
                        else{
                            val chatPartner = it.toObjects<UserData>()[0]
                            val id = db.collection(CHATS).document().id
                            val chat = ChatData(
                                chatId = id,
                                ChatUser(
                                    userId = userData.value?.userId,
                                    name = userData.value?.name,
                                    imageUrl = userData.value?.imageUrl,
                                    number = userData.value?.number
                                ),
                                ChatUser(
                                    userId = chatPartner.userId,
                                    name = chatPartner.name,
                                    imageUrl = chatPartner.imageUrl,
                                    number = chatPartner.number
                                )
                            )
                            db.collection(CHATS).document(id).set(chat)
                        }
                    }
                        .addOnFailureListener {
                            handleException(it)
                        }
                }
                else{
                    handleException(customMessage = "Chat already exists")
                }
            }
        }
    }


}

