package com.luftborntest.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.luftborntest.R
import com.luftborntest.core.common.KEY_IMAGE_URI
import com.luftborntest.core.common.TAG_OUTPUT_NAME
import kotlinx.android.synthetic.main.activity_main.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    lateinit var adapter: ArrayAdapter<String>
    private val viewModel: BlurViewModel by viewModels { BlurViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupUI()
        setupClickListeners()
        setupObservers()
    }

    private fun setupObservers() {
        viewModel.outputWorkInfos.observe(this, workInfosObserver())
    }

    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->
            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkFinished(workInfo.outputData.getString(TAG_OUTPUT_NAME), workInfo.state.name)
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)
                if (!outputImageUri.isNullOrEmpty()) {
                    viewModel.setOutputUri(outputImageUri)
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    private fun showWorkInProgress() {
        progressBar.visibility = View.VISIBLE
    }

    private fun showWorkFinished(string: String?, state: String) {
        progressBar.visibility = View.GONE
        addTaskRow(string, state)
    }

    private fun addTaskRow(s: String?, state: String){
        val current = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            LocalDateTime.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)
        adapter.add("$formatted $s $state")
    }

    private fun setupClickListeners() {
        button1.setOnClickListener {
            addTaskRow("Task 1", "STARTED")
            viewModel.applyBlur("Task 1")
        }
        button2.setOnClickListener {
            addTaskRow("Task 2", "STARTED")
            viewModel.applyBlur("Task 2")
        }
        button3.setOnClickListener {
            addTaskRow("Task 3", "STARTED")
            viewModel.applyBlur("Task 3")
        }
        button4.setOnClickListener {
            addTaskRow("Task 4", "STARTED")
            viewModel.applyBlur("Task 4")
        }
    }

    private fun setupUI() {
        adapter= ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList())
        listView.adapter = adapter
    }
}