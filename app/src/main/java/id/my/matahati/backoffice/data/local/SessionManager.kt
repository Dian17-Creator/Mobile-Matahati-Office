package id.my.matahati.backoffice.data.local

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import id.my.matahati.backoffice.model.User

class SessionManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val PREF_NAME = "matahati_session"
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_USER = "user_data"
        private const val KEY_SELECTED_OUTLET_ID = "selected_outlet_id"
        private const val KEY_SELECTED_OUTLET_NAME = "selected_outlet_name"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun saveSession(token: String, user: User) {
        val editor = prefs.edit()
        editor.putString(KEY_TOKEN, token)
        editor.putString(KEY_USER, gson.toJson(user))
        
        val defaultOutletId = user.defaultOutletId ?: user.outlets.firstOrNull()?.id ?: 0
        val defaultOutletName = user.defaultOutletName ?: user.outlets.firstOrNull()?.name ?: "Semua Outlet"
        
        editor.putInt(KEY_SELECTED_OUTLET_ID, defaultOutletId)
        editor.putString(KEY_SELECTED_OUTLET_NAME, defaultOutletName)
        editor.apply()
    }

    fun getToken(): String? {
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getSelectedOutletId(): Int {
        return prefs.getInt(KEY_SELECTED_OUTLET_ID, 0)
    }

    fun setSelectedOutletId(outletId: Int, outletName: String) {
        prefs.edit()
            .putInt(KEY_SELECTED_OUTLET_ID, outletId)
            .putString(KEY_SELECTED_OUTLET_NAME, outletName)
            .apply()
    }

    fun getSelectedOutletName(): String {
        return prefs.getString(KEY_SELECTED_OUTLET_NAME, "Semua Outlet") ?: "Semua Outlet"
    }

    fun isLoggedIn(): Boolean {
        return !getToken().isNull_or_empty_or_blank()
    }

    private fun String?.isNull_or_empty_or_blank(): Boolean {
        return this.isNullOrEmpty() || this.isBlank()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
