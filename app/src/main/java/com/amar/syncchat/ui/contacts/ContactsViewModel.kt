package com.amar.syncchat.ui.contacts

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amar.syncchat.data.local.TokenManager
import com.amar.syncchat.data.model.User
import com.amar.syncchat.data.remote.SocketManager
import com.amar.syncchat.data.repository.ChatRepository
import com.amar.syncchat.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val repository: ChatRepository,
    private val tokenManager: TokenManager,
    private val socketManager: SocketManager
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _contacts = MutableStateFlow<Resource<List<User>>>(Resource.Loading())
    
    val contactsState: StateFlow<Resource<List<User>>> = combine(
        _contacts,
        _searchQuery
    ) { resource, query ->
        if (resource is Resource.Success) {
            val filtered = resource.data?.filter { 
                it.name?.contains(query, ignoreCase = true) == true || 
                it.mobile?.contains(query) == true
            } ?: emptyList()
            Resource.Success(filtered)
        } else {
            resource
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Resource.Loading())

    init {
        refreshContacts()
        observeSocketStatus()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun refreshContacts() {
        viewModelScope.launch {
            val token = tokenManager.token.firstOrNull() ?: return@launch
            _contacts.value = Resource.Loading()
            
            try {
                // Sirf remote API se contacts fetch karein
                val response = repository.getRemoteUsers(token)
                Log.d("ContactsVM", "getRemoteUsers: ${response.code()}")
                
                if (response.isSuccessful && response.body() != null) {
                    _contacts.value = Resource.Success(response.body()!!)
                } else {
                    _contacts.value = Resource.Error(response.message() ?: "Failed to fetch contacts")
                }
            } catch (e: Exception) {
                Log.e("ContactsVM", "Error", e)
                _contacts.value = Resource.Error(e.localizedMessage ?: "An error occurred")
            }
        }
    }

    private fun observeSocketStatus() {
        viewModelScope.launch {
            socketManager.connect()
            socketManager.userStatus.collect {
                refreshContacts()
            }
        }
    }
}
