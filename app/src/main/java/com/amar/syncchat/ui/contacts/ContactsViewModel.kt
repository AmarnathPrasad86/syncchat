package com.amar.syncchat.ui.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amar.syncchat.data.local.TokenManager
import com.amar.syncchat.data.model.User
import com.amar.syncchat.data.repository.ChatRepository
import com.amar.syncchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _contactsState = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    val contactsState: StateFlow<Resource<List<User>>> = _contactsState

    init {
        getContacts()
    }

    fun getContacts() {
        viewModelScope.launch {
            _contactsState.value = Resource.Loading()
            try {
                val token = tokenManager.token.first()
                if (token != null) {
                    val response = repository.getContacts(token)
                    if (response.isSuccessful && response.body() != null) {
                        _contactsState.value = Resource.Success(response.body()!!)
                    } else {
                        _contactsState.value = Resource.Error(response.message() ?: "Failed to fetch contacts")
                    }
                } else {
                    _contactsState.value = Resource.Error("Unauthorized")
                }
            } catch (e: Exception) {
                _contactsState.value = Resource.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }
}
