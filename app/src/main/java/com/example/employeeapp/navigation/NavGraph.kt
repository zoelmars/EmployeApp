package com.example.employeeapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.employeeapp.ui.*
import com.example.employeeapp.util.SecureStorage
import com.example.employeeapp.viewmodel.AuthViewModel
import com.example.employeeapp.viewmodel.EmployeeViewModel

object Routes {
    const val LOGIN = "login"
    const val EMPLOYEE_LIST = "employee_list"
    const val ADD_EMPLOYEE = "add_employee"
    const val DETAIL_EMPLOYEE = "employee_detail/{empId}"
    const val EDIT_EMPLOYEE = "employee_edit/{empId}"

    fun detailRoute(id: Int) = "employee_detail/$id"
    fun editRoute(id: Int) = "employee_edit/$id"
}

@Composable
fun AppNavGraph() {
    val context = LocalContext.current
    val navController = rememberNavController()

    val authViewModel: AuthViewModel = viewModel()
    val employeeViewModel: EmployeeViewModel = viewModel()

    val isLoggedIn = SecureStorage.isLoggedIn(context) && !SecureStorage.isTokenExpired(context)
    val startDestination = if (isLoggedIn) Routes.EMPLOYEE_LIST else Routes.LOGIN

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authViewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Routes.EMPLOYEE_LIST) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.EMPLOYEE_LIST) {
            EmployeeListScreen(
                employeeViewModel = employeeViewModel,
                onAddClick = {
                    navController.navigate(Routes.ADD_EMPLOYEE)
                },
                onDetailClick = { empId ->
                    navController.navigate(Routes.detailRoute(empId))
                },
                onLogout = {
                    authViewModel.logout {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(Routes.ADD_EMPLOYEE) {
            AddEditScreen(
                employeeViewModel = employeeViewModel,
                employeeId = null,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.DETAIL_EMPLOYEE,
            arguments = listOf(
                navArgument("empId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val empId = backStackEntry.arguments?.getInt("empId") ?: 0
            DetailScreen(
                employeeViewModel = employeeViewModel,
                employeeId = empId,
                onBack = {
                    navController.popBackStack()
                },
                onEdit = {
                    navController.navigate(Routes.editRoute(empId))
                },
                onDeleted = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_EMPLOYEE,
            arguments = listOf(
                navArgument("empId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val empId = backStackEntry.arguments?.getInt("empId") ?: 0
            AddEditScreen(
                employeeViewModel = employeeViewModel,
                employeeId = empId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}