package com.classicanime.app
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class HomeVm : ViewModel() {
    val decades = listOf(
        Triple("Golden 70s", 19700000, 19800000),
        Triple("Golden 80s", 19800000, 19900000),
        Triple("Cult 90s", 19900000, 20000000)
    )
    val rows = mutableStateMapOf<String, List<Media>>()
    val saved = mutableStateListOf<Media>()
    var error by mutableStateOf<String?>(null)

    init {
        decades.forEach { (name, from, to) ->
            viewModelScope.launch {
                try {
                    rows[name] = Api.svc.query(mapOf("query" to Api.Q, "variables" to mapOf("from" to from, "to" to to)))
                        .data?.Page?.media.orEmpty()
                } catch (e: Exception) { error = "Couldn't load titles. Check your connection." }
            }
        }
    }
    fun isSaved(m: Media) = saved.any { it.id == m.id }
    fun toggle(m: Media) { if (isSaved(m)) saved.removeAll { it.id == m.id } else saved.add(m) }
}
