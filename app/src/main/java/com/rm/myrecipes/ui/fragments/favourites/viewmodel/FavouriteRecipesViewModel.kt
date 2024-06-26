package com.rm.myrecipes.ui.fragments.favourites.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rm.myrecipes.domain.data.Recipe
import com.rm.myrecipes.domain.usecase.FavouriteRecipeUseCase
import com.rm.myrecipes.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavouriteRecipesViewModel @Inject constructor(
    private val favouriteRecipeUseCase: FavouriteRecipeUseCase
) : ViewModel() {

    private val _favouriteRecipesState = MutableStateFlow<UiState<List<Recipe>>>(UiState.Loading)
    val favouriteRecipesState: StateFlow<UiState<List<Recipe>>> = _favouriteRecipesState.asStateFlow()

    var recipeSaveState = RecipeSaveState()

    private var lastFetchJob: Job? = null

    fun fetchFavouriteRecipes() {
        lastFetchJob?.cancel()

        lastFetchJob = viewModelScope.launch {
            favouriteRecipeUseCase.invoke()
                .map {
                    UiState.Success(it) as UiState<List<Recipe>>
                }
                .onCompletion {
                    Timber.d("RecipeFavourites: flow has completed.")
                }
                .catch {
                    Timber.d("RecipeFavourites: caught: $it")
                    emit(UiState.Error("Something went wrong, please try again later."))
                }
                .collect {
                    _favouriteRecipesState.value = it
                }
        }
    }

    fun saveFavouriteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                favouriteRecipeUseCase.insertFavouriteRecipe(recipe)
            } catch (e: Exception) {
                Timber.d("RecipeFavourite: caught ${e.message}")
            }
        }
    }

    fun deleteFavouriteRecipe(recipe: Recipe) {
        viewModelScope.launch {
            try {
                favouriteRecipeUseCase.deleteRecipe(recipe)
            } catch (e: Exception) {
                Timber.d("RecipeFavourite: caught ${e.message}")
            }
        }
    }

    fun deleteAllFavouriteRecipe() {
        viewModelScope.launch {
            try {
                favouriteRecipeUseCase.deleteAlRecipes()
            } catch (e: Exception) {
                Timber.d("RecipeFavourite: caught ${e.message}")
            }
        }
    }

    fun invalidateRecipeSaveState() {
        recipeSaveState.savedId = null
        recipeSaveState.isSaved = false
    }
}

data class RecipeSaveState(
    var savedId: Int? = null,
    var isSaved: Boolean = false
)
