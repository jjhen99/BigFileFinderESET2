import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.bigfilefindereset2.FileInfo
import com.example.bigfilefindereset2.R

class LargestFileAdapter(private val context: Context, private val largestFiles: List<FileInfo>) : BaseAdapter() {

    override fun getCount(): Int {
        return largestFiles.size
    }

    override fun getItem(position: Int): Any {
        return largestFiles[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val viewHolder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_largest_file, parent, false)
            viewHolder = ViewHolder(view)
            view.tag = viewHolder
        } else {
            view = convertView
            viewHolder = view.tag as ViewHolder
        }

        val fileInfo = getItem(position) as FileInfo

        viewHolder.fileNameTextView.text = "${fileInfo.fileName}"
        viewHolder.fileSizeTextView.text = "Size: ${fileInfo.fileSize} bytes"
        viewHolder.filePathTextView.text = "Path: ${fileInfo.filePath}"

        return view
    }

    private class ViewHolder(view: View) {
        val fileNameTextView: TextView = view.findViewById(R.id.fileNameTextView)
        val fileSizeTextView: TextView = view.findViewById(R.id.fileSizeTextView)
        val filePathTextView: TextView = view.findViewById(R.id.filePathTextView)
    }
}
