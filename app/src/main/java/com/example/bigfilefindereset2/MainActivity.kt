package com.example.bigfilefindereset2

import LargestFileAdapter
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.documentfile.provider.DocumentFile

//This data class is created so that it can store File Info. It contains the file name, the file size and the path of the file
data class FileInfo(val fileName: String?, val fileSize: Long, val filePath:String?)

class MainActivity : AppCompatActivity() {
    //These variables are useful for when we want to create notifications
    private val CHANNEL_ID = "channel_id"
    private val notificationId = 101


    private lateinit var folderListView: ListView
    private lateinit var numberOfFilesEditText: EditText
    private lateinit var introText: TextView
    private val selectedFiles = mutableListOf<FileInfo>()//list of FileInfo
    private lateinit var largestFilesInFolder:List<FileInfo> //made so that we can use text from largestFileInFolder in our notification
    private lateinit var notificationFileDescription:String

    //Create a documentPickerLauncher which is used to launch an activity
    //In this case, the activity will be to open a folder
    //We could use startActivityResult and onActivityResult, however these methods are deprecated

    private val documentPickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    )
    //When activityResult is reached
    //It checks to see if the data and uri exist
    //If they do, handleSelectedDirectory method is used
    {
        if (it.resultCode == Activity.RESULT_OK){
            val data:Intent? = it.data
            if (data != null){
                val uri = data.data
                if (uri != null){
                    handleSelectedDirectory(uri) //handleSelectedDirectory for this uri
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //For Notifications
        createNotificationChannel()

        val selectFolderButton = findViewById<Button>(R.id.button)
        folderListView = findViewById<ListView>(R.id.folderListView)
        numberOfFilesEditText = findViewById<EditText>(R.id.numberOfFilesEditText)
        introText = findViewById<TextView>(R.id.introText)

        introText.text = "Welcome to Big File Finder!\n\nTo begin, insert the number of biggest files you'd like to see in a folder.\n\nSelect your folder!\n\nYou can also add more files from different folders, just scroll down to see newly added files\n\nYou can remove files from the list by holding down on one of the files found\n\nA notification will show you the files found\n\nEnjoy your time with Big File Finder!"


        //When the selectFolderButton is clicked, an intent is created
        //This intent is an action opens the document tree
        //The default app is used to open the document tree. This is Androids built in file-picker
        //the documentPickLauncher is launched using this intent
        //The intro text is also removed so that it won't clash with the list view

        selectFolderButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            documentPickerLauncher.launch(intent)
            introText.text = ""
        }

        //When the user wants to remove an item from ListView, they can hold down the item
        folderListView.onItemLongClickListener = OnItemLongClickListener { adapterView, view, i, l ->
            removeItem(i)
            false
        }

    }

    //This function is used in our documentPickerLauncher
    //A value called largestFilesInFolder is created which contains the N largest files
    //a mutable list called selectedFiles has all of the files found in largestFilesInFolder added
    //an adapter is added so the selected files are displayed on ListView
    //The names of the largest files are also extracted, this is so the notifications will show the file names
    private fun handleSelectedDirectory(uri: Uri) {
        largestFilesInFolder = findNLargestFilesInFolder(uri, numberOfFilesEditText.text.toString().toIntOrNull() ?: 0)
        extractNameFromLargestFilesInFolder()
        selectedFiles.addAll(largestFilesInFolder)
        val adapter = LargestFileAdapter(this, selectedFiles)
        folderListView.adapter = adapter
        sendNotification()
    }

    //This function is used when an item is long-clicked
    //the selected file is removed at the position i where it is long-clicked
    //an adapter is made for the listview and is notified the Data Set has been changed
    private fun removeItem(i:Int){
        selectedFiles.removeAt(i)
        val adapter = folderListView.adapter as LargestFileAdapter
        adapter.notifyDataSetChanged()
    }

    //This function finds the N largest files in the folder
    //It takes the folder Uri and the number of files as inputs, and returns a list containing file info
    // The files in the folder are sorted in descending order and then n amount of files are taken
    //the largestFiles variable then has the file info from these n files added

    private fun findNLargestFilesInFolder(folderUri: Uri, n: Int): List<FileInfo>{
        val largestFiles = mutableListOf<FileInfo>()
        val folder = DocumentFile.fromTreeUri(this, folderUri)

        if (folder != null && folder.isDirectory){
            val files = folder.listFiles()
            val sortedFiles = files.filter{ it.isFile}
                .sortedByDescending{it.length()}
                .take(n)
            largestFiles.addAll(sortedFiles.map{FileInfo(it.name, it.length(), it.uri.toString()) })
        }
        return largestFiles
    }


    //This function is used for notifications and creating a notification channel
    private fun createNotificationChannel(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = "Notification Title"
            val descriptionText = "Notification description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name,importance).apply{
                description = descriptionText
            }
            val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

    }

    //This function extracts the fileName from the largest files found in the folder
    //It then creates a string which is used in the notification description
    private fun extractNameFromLargestFilesInFolder(): String {
        val fileNames = largestFilesInFolder.map{ it.fileName.toString() }
        val separator = ", "
        notificationFileDescription = fileNames.joinToString(separator)

        return notificationFileDescription
    }

    private fun sendNotification(){
        val intent = Intent(this, MainActivity::class.java)

        //These two lines have been added so that when clicking on the notification, it is return to the saved state of the app
        intent.action = Intent.ACTION_MAIN
        intent.addCategory(Intent.CATEGORY_LAUNCHER)

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0,intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Files have been found!")
            .setContentText("Click the notification to see your files")
            .setStyle(NotificationCompat.BigTextStyle().bigText("The following files have been found: $notificationFileDescription"))
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        //As far as I'm aware, there is no need to request permissions when it comes to
        //the notifications we are using
        //However, if there is a problem with a specific phone, there is a template
        //for checking permissions below this code
        //It is important to check which Manifest.permission is needed

        //Current error:
        //Call requires permission which may be rejected by user:
        // code should explicitly check to see if permission is available (with `checkPermission`)
        // or explicitly handle a potential `SecurityException`

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }


        /*if (hasNotificationPermission()) {
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        } else {

        }

    }
    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS //Line could be bad
        ) == PackageManager.PERMISSION_GRANTED*/
    }


}