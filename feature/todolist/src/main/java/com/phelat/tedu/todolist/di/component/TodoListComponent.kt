package com.phelat.tedu.todolist.di.component

import com.phelat.tedu.analytics.di.component.AnalyticsComponent
import com.phelat.tedu.androiddagger.DispatcherComponent
import com.phelat.tedu.androidresource.di.component.AndroidResourceComponent
import com.phelat.tedu.coroutines.di.component.ThreadComponent
import com.phelat.tedu.date.di.component.DateComponent
import com.phelat.tedu.dependencyinjection.StartupTasks
import com.phelat.tedu.dependencyinjection.feature.FeatureScope
import com.phelat.tedu.todo.di.component.TodoComponent
import com.phelat.tedu.todolist.di.module.TodoListBindingModule
import com.phelat.tedu.todolist.di.module.TodoListStartupModule
import com.phelat.tedu.todolist.di.qualifier.TodoListStartupTasks
import dagger.Component
import dagger.android.AndroidInjectionModule

@FeatureScope
@Component(
    modules = [
        AndroidInjectionModule::class,
        TodoListBindingModule::class,
        TodoListStartupModule::class
    ],
    dependencies = [
        TodoComponent::class,
        ThreadComponent::class,
        AnalyticsComponent::class,
        DateComponent::class,
        AndroidResourceComponent::class
    ]
)
interface TodoListComponent : DispatcherComponent {

    @TodoListStartupTasks
    fun todoListStartupTasks(): StartupTasks
}