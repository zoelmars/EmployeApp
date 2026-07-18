package com.example.employeeapp.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.employeeapp.model.Employee
import com.example.employeeapp.ui.theme.theme.EmployeeAppTheme
import com.example.employeeapp.util.Result
import com.example.employeeapp.util.SecureStorage
import com.example.employeeapp.viewmodel.EmployeeViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeListScreen(
    employeeViewModel: EmployeeViewModel = viewModel(),
    onAddClick: () -> Unit,
    onDetailClick: (Int) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val username = SecureStorage.getUsername(context)

    val employeesState by employeeViewModel.employeesState.collectAsState()
    val operationState by employeeViewModel.operationState.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf("Semua") }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var employeeToDelete by remember { mutableStateOf<Employee?>(null) }

    val departments = listOf("Semua", "Engineering", "Design", "IT Security", "Management", "HR")

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(operationState) {
        when (val state = operationState) {
            is Result.Success -> {
                val message = when {
                    employeeViewModel.lastActionName.value.isNotEmpty() ->
                        "${employeeViewModel.lastActionName.value} berhasil diproses"
                    else -> "Operasi berhasil"
                }
                snackbarHostState.showSnackbar(message)
                employeeViewModel.resetOperationState()
            }
            is Result.Error -> {
                snackbarHostState.showSnackbar(state.message)
                employeeViewModel.resetOperationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Karyawan - $username") },
                actions = {
                    IconButton(onClick = { showLogoutDialog = true }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Logout")  // ganti ini
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Karyawan")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    employeeViewModel.searchWithDebounce(it)
                },
                label = { Text("Cari karyawan...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            searchQuery = ""
                            employeeViewModel.getAllEmployees()
                        }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                singleLine = true
            )

            FilterDepartmentDropdown(
                departments = departments,
                selectedDepartment = selectedDepartment,
                onDepartmentSelected = { department ->
                    selectedDepartment = department
                    val filterDept = if (department == "Semua") "" else department
                    employeeViewModel.getAllEmployees(searchQuery, filterDept)
                }
            )

            when (val state = employeesState) {
                is Result.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Result.Error -> {
                    ErrorView(
                        message = state.message,
                        onRetry = { employeeViewModel.getAllEmployees() }
                    )
                }
                is Result.Success -> {
                    val employees = state.data

                    Text(
                        text = "Menampilkan ${employees.size} karyawan",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (employees.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty() || selectedDepartment != "Semua") {
                                    "Tidak ada hasil untuk filter tersebut"
                                } else {
                                    "Belum ada data karyawan"
                                }
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            items(
                                items = employees,
                                key = { it.id }
                            ) { employee ->
                                EmployeeListItem(
                                    employee = employee,
                                    onClick = { onDetailClick(employee.id) },
                                    onDelete = { employeeToDelete = employee }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?") },
            text = { Text("Anda yakin ingin keluar dari aplikasi?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Keluar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }

    employeeToDelete?.let { employee ->
        AlertDialog(
            onDismissRequest = { employeeToDelete = null },
            title = { Text("Hapus ${employee.name}?") },
            text = { Text("Data karyawan ini akan dihapus secara permanen.") },
            confirmButton = {
                TextButton(onClick = {
                    employeeViewModel.deleteEmployee(employee.id)
                    employeeToDelete = null
                }) {
                    Text("Hapus", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { employeeToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDepartmentDropdown(
    departments: List<String>,
    selectedDepartment: String,
    onDepartmentSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = selectedDepartment,
            onValueChange = {},
            readOnly = true,
            label = { Text("Filter Departemen") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            departments.forEach { dept ->
                DropdownMenuItem(
                    text = { Text(dept) },
                    onClick = {
                        onDepartmentSelected(dept)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
fun EmployeeListItem(
    employee: Employee,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val rupiahFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = employee.position,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = employee.department,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = rupiahFormat.format(employee.salary),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = onRetry) {
                Text("Coba Lagi")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun EmployeeListScreenPreview() {
    EmployeeAppTheme() {
        EmployeeListScreen(
            onAddClick = {},
            onDetailClick = {},
            onLogout = {}
        )
    }
}