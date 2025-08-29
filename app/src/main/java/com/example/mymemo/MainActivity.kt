package com.example.mymemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.TopAppBar as M2TopAppBar

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

import com.example.mymemo.ui.theme.Theme

import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import java.util.UUID


data class Memo(
    val id: String = UUID.randomUUID().toString(),
    val title: String, 
    val content: String=""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { Theme { MyApp() } }
    }
}

class MemoViewModel: ViewModel() {
    private val _memos = MutableStateFlow<List<Memo>>(emptyList())
    val memos: StateFlow<List<Memo>> = _memos.asStateFlow()

    fun add(title: String, content: String) {
        _memos.value = listOf(Memo(title = title, content = content)) + memos.value
    }

    fun update(id: String, title: String, content: String) {
        _memos.value = memos.value.map {
            if (it.id == id) it.copy(title = title, content = content) else it
        }
    }

    fun delete(id: String) {
        _memos.value = memos.value.filterNot { it.id == id }
    }

    fun find(id: String): Memo? = _memos.value.firstOrNull { it.id == id }
}

@Composable
fun HomeScreen(navController: NavController, viewModel: MemoViewModel) {
    val memos by viewModel.memos.collectAsStateWithLifecycle()
 
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        M2TopAppBar(
            title = {
                Text(
                    "메모장앱",
                    fontSize = 22.sp,                
                    fontWeight = FontWeight.SemiBold 
                )
            },
            actions = {
                TextButton(onClick = { navController.navigate("add") }) {
                    Text("추가")
                }
            },
            backgroundColor = Color.White,
            contentColor = Color(0xFF111111),
            elevation = 0.dp
        )

        Spacer(Modifier.height(12.dp))

        if (memos.isEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    "메모가 없습니다.",
                    modifier = Modifier.padding(8.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(items = memos, key = { it.id } ) { memo ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate("detail/${memo.id}") }
                            .padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = memo.title,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (memo.content.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(memo.content, fontSize = 14.sp, color = Color(0xFF555555))
                        }
                        HorizontalDivider(color = Color.LightGray, thickness = 1.dp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddMemoScreen(navController: NavController, viewModel: MemoViewModel) {
    var title by rememberSaveable { mutableStateOf("") }
    var content by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        M2TopAppBar(
            title = { Text("메모 추가", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
            actions = {
                TextButton(
                    onClick = {
                        val t = title.trim()
                        if (t.isNotEmpty()) {
                            viewModel.add(t, content.trim())
                            navController.popBackStack()
                        }
                    }
                ) { Text("저장") }
            },
            backgroundColor = Color.White,
            contentColor = Color(0xFF111111),
            elevation = 0.dp
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            placeholder = { Text("제목") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            placeholder = { Text("내용") },
            singleLine = false,
            minLines = 5,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
        )
    }

}


@Composable
fun DetailScreen(navController: NavController, viewModel: MemoViewModel, id: String) {
    val memo = viewModel.find(id)
    if (memo == null) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            Text("메모를 찾을 수 없습니다.")
            Spacer(Modifier.height(12.dp))
            TextButton(onClick = { navController.popBackStack() }) { Text("뒤로") }
        }
        return
    }

    var title by rememberSaveable(memo.id) { mutableStateOf(memo.title) }
    var content by rememberSaveable(memo.id) { mutableStateOf(memo.content) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        M2TopAppBar(
            title = { Text("메모 상세", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
            actions = {
                TextButton(onClick = {
                    viewModel.delete(memo.id)
                    navController.popBackStack()
                }) { Text("삭제") }

                TextButton(onClick = {
                    val t = title.trim()
                    if (t.isNotEmpty()) {
                        viewModel.update(memo.id, t, content.trim())
                        navController.popBackStack()
                    }
                }) { Text("저장") }
            },
            backgroundColor = Color.White,
            contentColor = Color(0xFF111111),
            elevation = 0.dp
        )

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = content,
            onValueChange = { content = it },
            singleLine = false,
            minLines = 6,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true)
        )
    }
}

@Composable
fun MyApp() {
    val navController = rememberNavController()
    val viewModel: MemoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("home") { HomeScreen(navController, viewModel) }
        composable("add") { AddMemoScreen(navController, viewModel) }
        composable("detail/{id}") { backStack ->
            val id = backStack.arguments?.getString("id").orEmpty()
            DetailScreen(navController, viewModel, id)
        }
    }
}