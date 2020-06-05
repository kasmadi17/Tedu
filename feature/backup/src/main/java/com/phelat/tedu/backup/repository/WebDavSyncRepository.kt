package com.phelat.tedu.backup.repository

import com.phelat.tedu.backup.BackupSyncRepository
import com.phelat.tedu.backup.entity.BackupTodoEntity
import com.phelat.tedu.backup.entity.WebDavCredentials
import com.phelat.tedu.backup.error.WebDavErrorContext
import com.phelat.tedu.coroutines.Dispatcher
import com.phelat.tedu.datasource.Readable
import com.phelat.tedu.dependencyinjection.feature.FeatureScope
import com.phelat.tedu.functional.Response
import com.phelat.tedu.functional.ifSuccessful
import com.phelat.tedu.functional.otherwise
import com.phelat.tedu.todo.entity.TodoEntity
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.Date
import javax.inject.Inject

@FeatureScope
internal class WebDavSyncRepository @Inject constructor(
    private val credentialsReadable: Readable<Response<WebDavCredentials, WebDavErrorContext>>,
    private val webDavReadable: Readable.IO<WebDavCredentials, Response<List<BackupTodoEntity>, WebDavErrorContext>>,
    private val dispatcher: Dispatcher
) : BackupSyncRepository {

    private val scope = MainScope()

    override fun sync() {
        scope.launch(context = dispatcher.iO) {
            credentialsReadable.read()
                .ifSuccessful { credentials -> readWebDav(credentials) }
        }
    }

    private suspend fun readWebDav(credentials: WebDavCredentials) {
        webDavReadable.read(credentials)
            .ifSuccessful { response -> handleSuccessfulCase(response) }
            .otherwise(::handleErrorCase)
    }

    private suspend fun handleSuccessfulCase(response: List<BackupTodoEntity>) {
        response.forEach { entity ->
            val convertedData = convertRawDataToTodoEntity(entity.data)
            when (entity.action) {
                "add" -> {
                    // todoWritable.write(convertedData)
                }
                "delete" -> {
                    // todoDeletable.delete(convertedData)
                }
                "update" -> {
                    // todoUpdatable.update(convertedData)
                }
            }
        }
    }

    private fun convertRawDataToTodoEntity(raw: JSONObject): TodoEntity {
        return TodoEntity(
            todo = raw.getString("todo"),
            todoId = raw.getInt("todoId"),
            isDone = raw.getBoolean("isDone"),
            date = Date(raw.getLong("date"))
        )
    }

    private fun handleErrorCase(error: WebDavErrorContext) {
        println(error)
    }
}