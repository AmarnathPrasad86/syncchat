package com.amar.syncchat.ui.profile

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _profileState = MutableStateFlow<Resource<User>>(Resource.Loading())
    val profileState: StateFlow<Resource<User>> = _profileState

    init {
        getProfile()
    }

    fun getProfile() {
        viewModelScope.launch {
            _profileState.value = Resource.Loading()
            try {
                val token = tokenManager.token.first()
                if (token != null) {
                    val response = repository.getProfile(token)
                    if (response.isSuccessful && response.body() != null) {
                        _profileState.value = Resource.Success(response.body()!!)
                    } else {
                        _profileState.value = Resource.Error(response.message() ?: "Failed to fetch profile")
                    }
                } else {
                    _profileState.value = Resource.Error("Unauthorized")
                }
            } catch (e: Exception) {
                _profileState.value = Resource.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            // Logout ke waqt socket disconnect karna zaroori hai
            socketManager.disconnect()
            tokenManager.deleteToken()
        }
    }
}
