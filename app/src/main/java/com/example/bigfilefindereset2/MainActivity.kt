package com.example.bigfilefindereset2

import LargestFileAdapter
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.documentfile.provider.DocumentFile


data class FileInfo(val fileName: String?, val fileSize: Long, val filePath:String?)

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_OPEN_DIRECTORY = 42
    private lateinit var folderListView: ListView
    private lateinit var numberOfFilesEditText: EditText
    private val selectedFolders = mutableListOf<Uri>()
    private val largestFilesMap = mutableMapOf<Uri, List<FileInfo>>()
    private val selectedFiles = mutableListOf<FileInfo>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val selectFolderButton = findViewById<Button>(R.id.button)
        folderListView = findViewById<ListView>(R.id.folderListView)
        numberOfFilesEditText = findViewById<EditText>(R.id.numberOfFilesEditText)



        selectFolderButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
            intent.addCategory(Intent.CATEGORY_DEFAULT)
            startActivityForResult(intent, REQUEST_CODE_OPEN_DIRECTORY)
        }

        folderListView.onItemLongClickListener = OnItemLongClickListener { adapterView, view, i, l ->
            removeItem(i)
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


        if (requestCode == REQUEST_CODE_OPEN_DIRECTORY && resultCode == Activity.RESULT_OK){
            val uri = data?.data
            if (uri != null){

                /*val largestFilesInFolder = findNLargestFilesInFolder(uri, numberOfFilesEditText.text.toString().toIntOrNull() ?: 0)
                largestFilesMap[uri] = largestFilesInFolder
                selectedFiles.addAll(largestFilesInFolder)
                updateFolderList() */

                //selectedFolders.clear() //keep an eye on this, could cause errors when removed, listview not updated
                selectedFolders.add(uri)
                Log.d("MainActivity", "Added folderUri: $uri")
                val largestFilesInFolder = findNLargestFilesInFolder(uri, numberOfFilesEditText.text.toString().toIntOrNull() ?: 0)
                selectedFiles.addAll(largestFilesInFolder)
                updateFolderList()

            }
        }
    }

    private fun removeItem(i:Int){
        //makeToast("Removed: " + selectedFiles.get(i))
        selectedFiles.removeAt(i)
        val adapter = folderListView.adapter as LargestFileAdapter
        adapter.notifyDataSetChanged()
    }

    private fun updateFolderList(){
        val largestFiles = selectedFiles.toMutableList()

        largestFiles.sortByDescending { it.fileSize } //could break

        //get value of N from edit text
        val n = numberOfFilesEditText.text.toString().toIntOrNull() ?: 0

        val limitedList = largestFiles.take(n)

        /*for (folderUri in selectedFolders){
            val largestFilesInFolder = largestFilesMap[folderUri] ?: emptyList()
            Log.d("MainActivity", "FolderUri: $folderUri, Largest Files Count: ${largestFilesInFolder.size}")
            largestFiles.addAll(largestFilesInFolder)
        }*/

        // Print the list of largestFiles to log for debugging
       /* for (fileInfo in largestFiles) {
            Log.d("FileInfo", "Name: ${fileInfo.fileName}, Size: ${fileInfo.fileSize}, Path: ${fileInfo.filePath}")
        }*/



        /*for (fileInfo in limitedList) {
            Log.d("LimitedList", "Name: ${fileInfo.fileName}, Size: ${fileInfo.fileSize}, Path: ${fileInfo.filePath}")
        }*/

        //create adapter to update UI displaying largest files for each selected folder
        val adapter = LargestFileAdapter(this, selectedFiles)
        folderListView.adapter = adapter
    }

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


}