import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.parquiatenov10.AuthActivity
import com.example.parquiatenov10.R

class TerminosCondicionesDialogFragment : DialogFragment() {

    companion object {
        private const val ARG_URL = "url"
        private const val ARG_TITLE = "title"

        fun newInstance(url: String, title: String): TerminosCondicionesDialogFragment {
            val fragment = TerminosCondicionesDialogFragment()
            val args = Bundle()
            args.putString(ARG_URL, url)
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
        isCancelable = false // Prevent accidental dismissal
        return inflater.inflate(R.layout.dialog_terminos_condiciones, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val url = arguments?.getString(ARG_URL) ?: ""
        val title = arguments?.getString(ARG_TITLE) ?: ""

        dialog?.setTitle(title)
        dialog?.setCanceledOnTouchOutside(false)

        val webView = view.findViewById<WebView>(R.id.webView)
        val closeButton = view.findViewById<Button>(R.id.btnCerrar)

        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.loadUrl(url)

        closeButton.setOnClickListener {
            dismiss()
            // Notify parent activity to show terms dialog again
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