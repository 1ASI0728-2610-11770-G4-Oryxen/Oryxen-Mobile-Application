package io.oryxen.mobile.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.oryxen.mobile.data.remote.ApiProvider
import io.oryxen.mobile.security.SessionEvent
import io.oryxen.mobile.security.SessionEventBus
import io.oryxen.mobile.ui.analytics.AnalyticsScreen
import io.oryxen.mobile.ui.auth.LoginScreen
import io.oryxen.mobile.ui.billing.PlansScreen
import io.oryxen.mobile.ui.community.CommunityScreen
import io.oryxen.mobile.ui.dashboard.DashboardScreen
import io.oryxen.mobile.ui.diagnosis.DiagnosisScreen
import io.oryxen.mobile.ui.notifications.NotificationsScreen

object Routes {
    const val LOGIN = "login"
    const val DASHBOARD = "dashboard"
    const val DIAGNOSIS = "diagnosis"
    const val PLANS = "plans"
    const val NOTIFICATIONS = "notifications"
    const val ANALYTICS = "analytics"
    const val COMMUNITY = "community"
}

@Composable
fun OryxenNavHost() {
    val navController = rememberNavController()
    val start = if (ApiProvider.instance.session.isAuthenticated) Routes.DASHBOARD else Routes.LOGIN

    LaunchedEffect(Unit) {
        SessionEventBus.events.collect { event ->
            when (event) {
                is SessionEvent.Expired -> {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onLogout = {
                    ApiProvider.instance.session.clear()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onNavigateDiagnosis = {
                    navController.navigate(Routes.DIAGNOSIS)
                },
                onNavigatePlans = {
                    navController.navigate(Routes.PLANS)
                },
                onNavigateNotifications = {
                    navController.navigate(Routes.NOTIFICATIONS)
                },
                onNavigateAnalytics = {
                    navController.navigate(Routes.ANALYTICS)
                },
                onNavigateCommunity = {
                    navController.navigate(Routes.COMMUNITY)
                },
            )
        }
        composable(Routes.DIAGNOSIS) {
            DiagnosisScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.PLANS) {
            PlansScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.NOTIFICATIONS) {
            NotificationsScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.ANALYTICS) {
            AnalyticsScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
        composable(Routes.COMMUNITY) {
            CommunityScreen(
                onBack = {
                    navController.popBackStack()
                },
            )
        }
    }
}
