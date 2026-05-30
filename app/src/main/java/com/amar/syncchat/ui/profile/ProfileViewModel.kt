package com.amar.syncchat.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amar.syncchat.data.local.TokenManager
import com.amar.syncchat.data.model.User
import com.amar.syncchat.data.remote.SocketManager
import com.amar.syncchat.data.repository.ChatRepository
import com.amar.syncchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val profileState: StateFlow<Resource<User>> = _profileState.asStateFlow()

    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            val token = tokenManager.token.firstOrNull() ?: return@launch
            _profileState.value = Resource.Loading()
            try {
                val response = repository.getProfile(token)
                Log.d("ProfileVM", "getProfile Response: $response")
                
                if (response.isSuccessful && response.body() != null) {
                    // Extracting the user from the ProfileResponse wrapper
                    val user = response.body()?.user
                    if (user != null) {
                        _profileState.value = Resource.Success(user)
                    } else {
                        _profileState.value = Resource.Error("User data not found in response")
                    }
                } else {
                    _profileState.value = Resource.Error(response.message().ifBlank { "Failed to fetch profile" })
                }
            } catch (e: Exception) {
                Log.e("ProfileVM", "Error fetching profile", e)
                _profileState.value = Resource.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            socketManager.disconnect()
            tokenManager.deleteToken()
        }
    }
}
