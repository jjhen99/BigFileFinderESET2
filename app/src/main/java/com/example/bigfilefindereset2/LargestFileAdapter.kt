import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.bigfilefindereset2.FileInfo
import com.example.bigfilefindereset2.R

//This is a custom class used to make an adapter
//This adapter returns the list of File Info

class LargestFileAdapter(private val context: Context, private val largestFiles: List<FileInfo>) : BaseAdapter() {

    override fun getCount(): Int { //returns the file size
        return largestFiles.size
    }

    override fun getItem(position: Int): Any { //returns the file info at the specified position
                                                //This is used so ListView can put each file info in each row
        return largestFiles[position]
    }

    override fun getItemId(position: Int): Long { //returns the item position as a long
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_largest_file, parent, false) //newly created view represents one item on the list
            viewHolder = ViewHolder(view) //caches references to view
            view.tag = viewHolder //viewHolder stored as a tag in view
        } else {
            view = convertView //view can be reused for new item
            viewHolder = view.tag as ViewHolder // viewHolder retrieved from recycled views tag. cached references to views can be used
        }

        val fileInfo = getItem(position) as FileInfo


        //the viewHolder uses the file info and changes the textview to their respective file info parts.
        viewHolder.fileNameTextView.text = "${fileInfo.fileName}"
        viewHolder.fileSizeTextView.text = "Size: ${fileInfo.fileSize} bytes"
        viewHolder.filePathTextView.text = "Path: ${fileInfo.filePath}"

        return view
    }

    //ViewHolder utilizes three textviews in a separate activity: fileName, fileSize and filePath
    private class ViewHolder(view: View) {
        val fileNameTextView: TextView = view.findViewById(R.id.fileNameTextView)
        val fileSizeTextView: TextView = view.findViewById(R.id.fileSizeTextView)
        val filePathTextView: TextView = view.findViewById(R.id.filePathTextView)
    }
}
