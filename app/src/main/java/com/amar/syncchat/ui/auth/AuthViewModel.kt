package com.amar.syncchat.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amar.syncchat.data.local.TokenManager
import com.amar.syncchat.data.model.AuthResponse
import com.amar.syncchat.data.model.LoginRequest
import com.amar.syncchat.data.model.RegisterRequest
import com.amar.syncchat.data.repository.ChatRepository
import com.amar.syncchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _authState = MutableStateFlow<Resource<AuthResponse>?>(null)
    val authState: StateFlow<Resource<AuthResponse>?> = _authState

    private val _isLoggedIn = MutableStateFlow<Boolean?>(null)
    val isLoggedIn: StateFlow<Boolean?> = _isLoggedIn

    init {
        checkAuthStatus()
    }

    private fun checkAuthStatus() {
        viewModelScope.launch {
            val token = tokenManager.token.first()
            _isLoggedIn.value = !token.isNullOrEmpty()
        }
    }

    fun register(request: RegisterRequest) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            try {
                val response = repository.register(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    _authState.value = Resource.Success(authResponse)
                    _isLoggedIn.value = true
                } else {
                    _authState.value = Resource.Error(response.message() ?: "Registration failed")
                }
            } catch (e: Exception) {
                _authState.value = Resource.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    fun login(request: LoginRequest) {
        viewModelScope.launch {
            _authState.value = Resource.Loading()
            try {
                val response = repository.login(request)
                if (response.isSuccessful && response.body() != null) {
                    val authResponse = response.body()!!
                    tokenManager.saveToken(authResponse.token)
                    _authState.value = Resource.Success(authResponse)
                    _isLoggedIn.value = true
                } else {
                    _authState.value = Resource.Error(response.message() ?: "Login failed")
                }
            } catch (e: Exception) {
                _authState.value = Resource.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}
