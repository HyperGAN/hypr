package hypr.a255bits.com.hypr.ModelFragmnt

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.*
import hypr.a255bits.com.hypr.GeneratorLoader

import hypr.a255bits.com.hypr.R
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast


class ModelFragment : Fragment(), ModelFragmentMVP.view {

    var generatorLoader: GeneratorLoader? = null
    private var modelUrl: String? = null
    private var image: ByteArray? = null
    val interactor by lazy { ModelInteractor(context) }
    val presenter by lazy { ModelFragmentPresenter(this, interactor, context) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            modelUrl = arguments.getString(MODEL_URL_PARAM)
            image = arguments.getByteArray(IMAGE_PARAM)
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_model, container, false)
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val imageBitmap = image?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
        presenter.transformImage(imageBitmap)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.image_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.saveImage -> presenter.saveImageDisplayedToPhone()
            R.id.shareIamge -> presenter.shareImageToOtherApps()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        presenter.disconnectFaceDetector()
    }

    override fun showError(errorMesssage: String) {
        context.toast(errorMesssage)
    }

    override fun displayFocusedImage(imageFromGallery: Bitmap) {

        val scaled = Bitmap.createScaledBitmap(imageFromGallery, 128, 128, false)
        val encoded = generatorLoader!!.encode(scaled)
        focusedImage.setImageBitmap(generatorLoader!!.sample(encoded))

    }

    override fun shareImageToOtherApps(shareIntent: Intent) {
       startActivity(Intent.createChooser(shareIntent, getString(R.string.share_image)))
    }

    companion object {
        private val MODEL_URL_PARAM = "param1"
        private val IMAGE_PARAM = "param2"

        fun newInstance(modelUrl: String, image: ByteArray?, generatorLoader: GeneratorLoader): ModelFragment {
            val fragment = ModelFragment()
            val args = Bundle()
            args.putString(MODEL_URL_PARAM, modelUrl)
            args.putByteArray(IMAGE_PARAM, image)
            fragment.arguments = args
            fragment.generatorLoader = generatorLoader
            return fragment
        }
    }

}
