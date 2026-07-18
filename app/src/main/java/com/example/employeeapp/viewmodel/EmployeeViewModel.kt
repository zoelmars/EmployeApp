package com.example.employeeapp.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.employeeapp.model.Employee
import com.example.employeeapp.repository.EmployeeRepository
import com.example.employeeapp.util.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmployeeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = EmployeeRepository(getApplication())

    private val _employeesState = MutableStateFlow<Result<List<Employee>>>(Result.Loading)
    val employeesState: StateFlow<Result<List<Employee>>> = _employeesState.asStateFlow()

    private val _detailState = MutableStateFlow<Result<Employee>?>(null)
    val detailState: StateFlow<Result<Employee>?> = _detailState.asStateFlow()

    private val _operationState = MutableStateFlow<Result<Unit>?>(null)
    val operationState: StateFlow<Result<Unit>?> = _operationState.asStateFlow()

    private var searchJob: Job? = null

    private val _lastActionName = MutableStateFlow("")
    val lastActionName: StateFlow<String> = _lastActionName.asStateFlow()

    init {
        getAllEmployees()
    }

    fun getAllEmployees(search: String = "", department: String = "") {
        viewModelScope.launch {
            _employeesState.value = Result.Loading
            val result = repository.getAllEmployees(search, department)
            _employeesState.value = result
        }
    }

    fun searchWithDebounce(query: String) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500L)
            getAllEmployees(query)
        }
    }

    fun getEmployeeById(id: Int) {
        viewModelScope.launch {
            _detailState.value = Result.Loading
            val result = repository.getEmployeeById(id)
            _detailState.value = result
        }
    }

    fun createEmployee(employee: Employee, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _lastActionName.value = employee.name
            _operationState.value = Result.Loading
            val result = repository.createEmployee(employee)

            val transformedResult: Result<Unit> = when (result) {
                is Result.Success -> Result.Success(Unit)
                is Result.Error -> Result.Error(result.message)
                is Result.Loading -> Result.Loading
            }

            if (transformedResult is Result.Success) {
                getAllEmployees()
                onSuccess()
            }
            _operationState.value = transformedResult
        }
    }

    fun updateEmployee(id: Int, employee: Employee, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _lastActionName.value = employee.name
            _operationState.value = Result.Loading
            val result = repository.updateEmployee(id, employee)

            if (result is Result.Success) {
                getAllEmployees()
                onSuccess()
            }
            _operationState.value = result
        }
    }

    fun deleteEmployee(id: Int) {
        viewModelScope.launch {
            _operationState.value = Result.Loading
            val result = repository.deleteEmployee(id)

            if (result is Result.Success) {
                getAllEmployees()
            }
            _operationState.value = result
        }
    }

    fun resetOperationState() {
        _operationState.value = null
    }

    fun resetDetailState() {
        _detailState.value = null
    }
}