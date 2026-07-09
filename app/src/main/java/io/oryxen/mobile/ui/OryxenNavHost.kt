package io.oryxen.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.security.SessionEvent
import io.oryxen.mobile.security.SessionEventBus
import io.oryxen.mobile.ui.analytics.AnalyticsScreen
import io.oryxen.mobile.ui.auth.OnBoardScreen
import io.oryxen.mobile.ui.auth.SignInScreen
import io.oryxen.mobile.ui.auth.SignUpScreen
import io.oryxen.mobile.ui.billing.PlansScreen
import io.oryxen.mobile.ui.dashboard.DashboardScreen
import io.oryxen.mobile.ui.diagnosis.DiagnosisScreen
import io.oryxen.mobile.ui.notifications.NotificationsScreen
import io.oryxen.mobile.ui.plants.PlantDetailScreen
import io.oryxen.mobile.ui.plants.PlantFormScreen
import io.oryxen.mobile.ui.plants.PlantsListScreen
import io.oryxen.mobile.ui.chatbot.ChatbotScreen

object Routes {
    const val ONBOARD = "onboard"
    const val SIGN_UP = "sign_up"
    const val SIGN_IN = "sign_in"
    const val DASHBOARD = "dashboard"
    const val DIAGNOSIS = "diagnosis"
    const val PLANS = "plans"
    const val NOTIFICATIONS = "notifications"
    const val ANALYTICS = "analytics"
    const val CHATBOT = "chatbot"
    const val PLANTS = "plants"
    const val PLANT_DETAIL = "plants/{plantId}"
    const val PLANT_CREATE = "plants/new"
    const val PLANT_EDIT = "plants/{plantId}/edit"
    const val SCANNER = "scanner"
}

@Composable
fun OryxenNavHost() {
    val navController = rememberNavController()
    val start = if (ApiProvider.instance.session.isAuthenticated) Routes.DASHBOARD else Routes.ONBOARD

    LaunchedEffect(Unit) {
        SessionEventBus.events.collect { event ->
            when (event) {
                is SessionEvent.Expired -> {
                    navController.navigate(Routes.ONBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {

        // ── Auth Flow ───────────────────────────────────────────────

        composable(Routes.ONBOARD) {
            OnBoardScreen(
                onSignUp = { navController.navigate(Routes.SIGN_UP) },
                onLogIn = { navController.navigate(Routes.SIGN_IN) },
            )
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                onRegistered = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARD) { inclusive = true }
                    }
                },
                onNavigateSignIn = {
                    navController.navigate(Routes.SIGN_IN) {
                        popUpTo(Routes.ONBOARD)
                    }
                },
            )
        }

        composable(Routes.SIGN_IN) {
            SignInScreen(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.ONBOARD) { inclusive = true }
                    }
                },
                onNavigateSignUp = {
                    navController.navigate(Routes.SIGN_UP) {
                        popUpTo(Routes.ONBOARD)
                    }
                },
            )
        }

        // ── Main App ────────────────────────────────────────────────

        composable(Routes.DASHBOARD) {
            MainScaffold(
                onLogout = {
                    ApiProvider.instance.session.clear()
                    navController.navigate(Routes.ONBOARD) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateDiagnosis = { navController.navigate(Routes.DIAGNOSIS) },
                onNavigatePlans = { navController.navigate(Routes.PLANS) },
                onNavigateNotifications = { navController.navigate(Routes.NOTIFICATIONS) },
                onNavigateChatbot = { navController.navigate(Routes.CHATBOT) },
                onNavigatePlantDetail = { id -> navController.navigate("plants/$id") },
                onNavigatePlantCreate = { navController.navigate(Routes.PLANT_CREATE) },
                onNavigatePlantEdit = { id -> navController.navigate("plants/$id/edit") },
                onNavigateScanner = { navController.navigate(Routes.SCANNER) },
            )
        }

        composable(Routes.SCANNER) {
            io.oryxen.mobile.ui.scanner.ScannerScreen(
                onBack = { navController.popBackStack() },
                onDone = { navController.popBackStack() }
            )
        }

        composable(Routes.DIAGNOSIS) {
            DiagnosisScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.PLANS) {
            PlansScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ANALYTICS) {
            AnalyticsScreen()
        }

        composable(Routes.CHATBOT) {
            ChatbotScreen(onBack = { navController.popBackStack() })
        }



        // ── Plants ──────────────────────────────────────────────────

        composable(Routes.PLANTS) {
            PlantsListScreen(
                onBack = { navController.popBackStack() },
                onAddPlant = { navController.navigate(Routes.PLANT_CREATE) },
                onPlantClick = { id -> navController.navigate("plants/$id") },
                onEditPlant = { id -> navController.navigate("plants/$id/edit") },
            )
        }

        composable(
            route = Routes.PLANT_DETAIL,
            arguments = listOf(navArgument("plantId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            PlantDetailScreen(
                plantId = plantId,
                onBack = { navController.popBackStack() },
                onEdit = { id -> navController.navigate("plants/$id/edit") },
            )
        }

        composable(Routes.PLANT_CREATE) {
            PlantFormScreen(
                plantId = null,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.PLANT_EDIT,
            arguments = listOf(navArgument("plantId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val plantId = backStackEntry.arguments?.getString("plantId") ?: return@composable
            PlantFormScreen(
                plantId = plantId,
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }
    }
}
