import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Button
import androidx.fragment.app.DialogFragment
import com.example.parquiatenov10.AuthActivity
import com.example.parquiatenov10.R

class TerminosCondicionesDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_CONTENT = "content"
        private const val ARG_TITLE = "title"

        fun newInstance(content: String, title: String): TerminosCondicionesDialogFragment {
            val fragment = TerminosCondicionesDialogFragment()
            val args = Bundle()
            args.putString(ARG_CONTENT, content)
            args.putString(ARG_TITLE, title)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        return inflater.inflate(R.layout.dialog_terminos_condiciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = arguments?.getString(ARG_CONTENT) ?: ""
        val title = arguments?.getString(ARG_TITLE) ?: ""

        dialog?.setTitle(title)
        dialog?.setCanceledOnTouchOutside(false)

        val webView = view.findViewById<WebView>(R.id.webView)
        val closeButton = view.findViewById<Button>(R.id.btnCerrar)

        webView.loadDataWithBaseURL(
            null,
            content,
            "text/html",
            "UTF-8",
            null
        )

        closeButton.setOnClickListener {
            dismiss()
            (activity as? AuthActivity)?.showTermsDialogAfterWebViewClose()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }
}