package com.luftborntest.ui

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.luftborntest.core.common.IMAGE_MANIPULATION_WORK_NAME
import com.luftborntest.core.common.KEY_IMAGE_URI
import com.luftborntest.core.common.TAG_OUTPUT
import com.luftborntest.core.common.TAG_OUTPUT_NAME
import com.luftborntest.utils.workers.BlurWorker
import com.luftborntest.utils.workers.CleanupWorker
import com.luftborntest.utils.workers.SaveImageToFileWorker

class BlurViewModel(application: Application) : ViewModel() {

    private var imageUri: Uri? = null
    private var taskName: String? = null
    internal var outputUri: Uri? = null
    private val workManager = WorkManager.getInstance(application)
    val outputWorkInfos: LiveData<List<WorkInfo>>

    init {
        imageUri = getImageUri(application.applicationContext)
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(TAG_OUTPUT)
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME)
    }

    /**
     * Creates the input data bundle which includes the Uri to operate on
     * @return Data which contains the Image Uri as a String
     */
    private fun createInputDataForUri(str: String): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(KEY_IMAGE_URI, imageUri.toString())
        }
        builder.putString(TAG_OUTPUT_NAME, str)
        return builder.build()
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     * @param blurLevel The amount to blur the image let it 3 to take long time
     */
    internal fun applyBlur(str: String) {
        // single work
//        workManager.enqueue(OneTimeWorkRequest.from(BlurWorker::class.java))

        //one work based on input and output
//        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
//            .setInputData(createInputDataForUri())
//            .build()
//
//        workManager.enqueue(blurRequest)

        // Add WorkRequest to Cleanup temporary images
        var continuation = workManager
            .beginWith(
                OneTimeWorkRequest
                    .from(CleanupWorker::class.java)
            )

        // one request
        // Add WorkRequest to blur the image
        val blurRequest = OneTimeWorkRequest.Builder(BlurWorker::class.java)
            .setInputData(createInputDataForUri(str))
            .build()

        continuation = continuation.then(blurRequest)


        // Add WorkRequest to save the image to the filesystem
        val save = OneTimeWorkRequest
            .Builder(SaveImageToFileWorker::class.java)
            .addTag(TAG_OUTPUT)
            .build()

        continuation = continuation.then(save)

        // Actually start the work
        continuation.enqueue()
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        return Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(com.luftborntest.R.drawable.android_cupcake))
            .appendPath(resources.getResourceTypeName(com.luftborntest.R.drawable.android_cupcake))
            .appendPath(resources.getResourceEntryName(com.luftborntest.R.drawable.android_cupcake))
            .build()
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }
}

class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
            BlurViewModel(application) as T
        } else {
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}