package com.martinezmencias.eventscheduler.ui.list

import android.Manifest
import android.content.Context
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.martinezmencias.eventscheduler.R
import com.martinezmencias.eventscheduler.ui.common.PermissionRequester
import com.martinezmencias.eventscheduler.domain.Error
import com.martinezmencias.eventscheduler.domain.Event
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Fragment.createListState(
    context: Context = requireContext(),
    scope: CoroutineScope = viewLifecycleOwner.lifecycleScope,
    navController: NavController = findNavController(),
    permissionRequester: PermissionRequester = PermissionRequester(this)
) = ListState(context, scope, navController, permissionRequester)

class ListState(
    private val context: Context,
    private val scope: CoroutineScope,
    private val navController: NavController,
    private val permissionRequester: PermissionRequester
) {
    fun onEventClicked(event: Event) {
        val action = ListFragmentDirections.actionListToDetail(event.id)
        navController.navigate(action)
    }

    fun onFavoritesClicked() {
        val action = ListFragmentDirections.actionListToFavorites()
        navController.navigate(action)
    }

    fun requestLocationPermission(afterRequest: (Boolean) -> Unit) {
        scope.launch {
            val result = permissionRequester.request(Manifest.permission.ACCESS_COARSE_LOCATION)
            afterRequest(result)
        }
    }

    fun errorToString(error: Error) = when (error) {
        Error.Connectivity -> context.getString(R.string.connectivity_error)
        is Error.Server -> String.format(context.getString(R.string.server_error), error.code)
        is Error.Unknown -> String.format(context.getString(R.string.server_error), error.message)
    }
}