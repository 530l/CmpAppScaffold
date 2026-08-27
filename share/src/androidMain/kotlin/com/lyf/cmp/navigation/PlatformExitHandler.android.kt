package com.lyf.cmp.navigation

import android.os.SystemClock
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.lyf.cmp.share.resources.Res
import com.lyf.cmp.share.resources.press_again_to_exit
import org.jetbrains.compose.resources.stringResource

private const val EXIT_CONFIRMATION_WINDOW_MS = 2_000L

/** 非首页 Tab 根节点直接回首页；首页根节点首次提示，再次返回才结束 Activity。 */
@Composable
internal actual fun PlatformExitHandler(
    isTabRoot: Boolean,
    isHomeRoot: Boolean,
    onNavigateHome: () -> Unit,
) {
    val activity = LocalActivity.current
    val exitMessage = stringResource(Res.string.press_again_to_exit)
    var lastBackPressedAt by remember { mutableLongStateOf(0L) }

    LaunchedEffect(isTabRoot, isHomeRoot) {
        if (!isTabRoot || !isHomeRoot) lastBackPressedAt = 0L
    }

    BackHandler(enabled = isTabRoot) {
        if (!isHomeRoot) {
            onNavigateHome()
            return@BackHandler
        }
        val now = SystemClock.elapsedRealtime()
        if (
            lastBackPressedAt != 0L &&
            now - lastBackPressedAt <= EXIT_CONFIRMATION_WINDOW_MS
        ) {
            activity?.finish()
        } else {
            val currentActivity = activity ?: return@BackHandler
            lastBackPressedAt = now
            Toast.makeText(currentActivity, exitMessage, Toast.LENGTH_SHORT).show()
        }
    }
}
