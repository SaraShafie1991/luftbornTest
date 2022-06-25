package com.luftborntest.ui

import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.luftborntest.R
import com.luftborntest.core.common.KEY_IMAGE_URI
import com.luftborntest.core.common.KEY_TASK_NAME
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
                showWorkFinished(workInfo.outputData.getString(KEY_TASK_NAME), workInfo.state.name)
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
        val string2 = string ?: "Error in MediaStore"
        addTaskRow(string2, state)
    }

    private fun addTaskRow(s: String?, state: String) {
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
            changebgcolor(button1, button2, button3, button4)
            addTaskRow("Task 1", "STARTED")
            viewModel.applyBlur("Task 1")
        }
        button2.setOnClickListener {
            changebgcolor(button2, button1, button3, button4)

            addTaskRow("Task 2", "STARTED")
            viewModel.applyBlur("Task 2")
        }
        button3.setOnClickListener {
            changebgcolor(button3, button2, button1, button4)

            addTaskRow("Task 3", "STARTED")
            viewModel.applyBlur("Task 3")
        }
        button4.setOnClickListener {
            changebgcolor(button4, button2, button3, button1)

            addTaskRow("Task 4", "STARTED")
            viewModel.applyBlur("Task 4")
        }
    }

    private fun changebgcolor(
        button1: AppCompatButton?,
        button2: AppCompatButton?,
        button3: AppCompatButton?,
        button4: AppCompatButton?
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            button1?.setBackgroundColor(resources.getColor(R.color.black, null))
            button1?.setTextColor(resources.getColor(R.color.white, null))

            button2?.setBackgroundColor(resources.getColor(R.color.white, null))
            button2?.setTextColor(resources.getColor(R.color.black, null))

            button3?.setBackgroundColor(resources.getColor(R.color.white, null))
            button3?.setTextColor(resources.getColor(R.color.black, null))

            button4?.setBackgroundColor(resources.getColor(R.color.white, null))
            button4?.setTextColor(resources.getColor(R.color.black, null))
        }
    }

    private fun setupUI() {
        adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ArrayList())
        listView.adapter = adapter
    }
}