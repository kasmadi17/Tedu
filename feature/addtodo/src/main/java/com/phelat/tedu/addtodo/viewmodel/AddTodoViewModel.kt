package com.phelat.tedu.addtodo.viewmodel

import android.os.Bundle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.phelat.tedu.addtodo.R
import com.phelat.tedu.androidresource.ResourceProvider
import com.phelat.tedu.androidresource.input.StringId
import com.phelat.tedu.androidresource.resource.StringResource
import com.phelat.tedu.coroutines.Dispatcher
import com.phelat.tedu.datasource.Updatable
import com.phelat.tedu.datasource.Writable
import com.phelat.tedu.lifecycle.SingleLiveData
import com.phelat.tedu.todo.constant.TodoConstant
import com.phelat.tedu.todo.entity.TodoEntity
import kotlinx.coroutines.launch
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.ZoneId
import java.util.Date

class AddTodoViewModel(
    private val dispatcher: Dispatcher,
    private val todoDataSourceWritable: Writable.Suspendable<TodoEntity>,
    private val todoDataSourceUpdatable: Updatable.Suspendable<TodoEntity>,
    private val stringResourceProvider: ResourceProvider<StringId, StringResource>
) : ViewModel() {

    private val _todoTextObservable = MutableLiveData<String>()
    val todoTextObservable: LiveData<String> = _todoTextObservable

    private val _todoDateObservable = MutableLiveData<String>()
    val todoDateObservable: LiveData<String> = _todoDateObservable

    private val _todoDateSheetObservable = SingleLiveData<Unit>()
    val todoDateSheetObservable: LiveData<Unit> = _todoDateSheetObservable

    private var todoForEdit: TodoEntity? = null

    private var selectedDate: LocalDate = LocalDate.now()

    fun onBundleReceive(bundle: Bundle?) {
        val todoForEdit = bundle?.getSerializable(TodoConstant.TODO_FOR_EDIT)
        if (todoForEdit is TodoEntity) {
            this.todoForEdit = todoForEdit
            _todoTextObservable.value = todoForEdit.todo
            val date = Instant.ofEpochMilli(todoForEdit.date.time).atZone(ZoneId.systemDefault())
                .toLocalDate()
            onDateSelect(date)
        }
    }

    fun onSaveTodoClicked(todo: String) {
        viewModelScope.launch(context = dispatcher.iO) {
            if (todoForEdit != null) {
                val updatedTodo = requireNotNull(todoForEdit).copy(todo, convertSelectedLocalDate())
                todoDataSourceUpdatable.update(updatedTodo)
            } else {
                todoDataSourceWritable.write(
                    TodoEntity(
                        todo = todo,
                        date = convertSelectedLocalDate()
                    )
                )
            }
        }
    }

    private fun convertSelectedLocalDate(): Date {
        // TODO: move date logic to a data source class
        return (selectedDate.atTime(LocalTime.now()))
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            .run { Date(this) }
    }

    fun onDateSelect(selectedDate: LocalDate) {
        this.selectedDate = selectedDate
        val today = LocalDate.now()
        _todoDateObservable.value = when {
            selectedDate == today -> {
                stringResourceProvider.getResource(StringId(R.string.addtodo_date_today_text)).resource
            }
            isSelectedDateTomorrow(today, selectedDate) -> {
                stringResourceProvider.getResource(StringId(R.string.addtodo_date_tomorrow_text)).resource
            }
            else -> {
                "${selectedDate.year}/${selectedDate.monthValue}/${selectedDate.dayOfMonth}"
            }
        }
    }

    private fun isSelectedDateTomorrow(today: LocalDate, selectedDate: LocalDate): Boolean {
        return (today.year == selectedDate.year)
            .and(today.monthValue == selectedDate.monthValue)
            .and(selectedDate.dayOfMonth - today.dayOfMonth == 1)
    }

    fun onSelectDateClick() {
        _todoDateSheetObservable.call()
    }

    fun getSelectedDate(): LocalDate {
        // TODO: refactor this
        return selectedDate
    }
}